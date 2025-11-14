package com.connection.client.repository;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.Statement;

import com.connection.client.converter.ClientConverter;
import com.connection.client.exception.ClientAlreadyExisistsException;
import com.connection.client.exception.ClientNotFoundException;
import com.connection.client.model.ClientBLM;
import com.connection.client.model.ClientDALM;
import com.connection.client.validator.ClientValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class ClientReactiveRepositoryImpl implements ClientRepository {

    private final ConnectionFactory clientConnectionFactory;
    private final ClientConverter converter;
    private final ClientValidator validator = new ClientValidator();

    private static final String SELECT_CLIENT = "SELECT uid, email, birth_date, username, password";
    private static final String SELECT_CLIENT_BY_UID = SELECT_CLIENT +
            " FROM core.client WHERE uid = $1";
    private static final String SELECT_CLIENT_BY_EMAIL = SELECT_CLIENT +
            " FROM core.client WHERE email = $1";
    private static final String SELECT_CLIENT_BY_USERNAME = SELECT_CLIENT +
            " FROM core.client WHERE username = $1";
    private static final String SELECT_CLIENT_BY_EMAIL_PASSWORD = SELECT_CLIENT +
            " FROM core.client WHERE email = $1 AND password = $2";
    private static final String SELECT_CLIENT_BY_USERNAME_PASSWORD = SELECT_CLIENT +
            " FROM core.client WHERE username = $1 AND password = $2";
    private static final String INSERT_CLIENT = "INSERT INTO core.client (uid, email, birth_date, username, password) " +
            "VALUES ($1, $2, $3, $4, $5)";
    private static final String DELETE_CLIENT_BY_UID = "DELETE FROM core.client WHERE uid = $1";

    @Override
    public Mono<Void> add(ClientBLM clientBLM) {
        log.info("Attempting to add new client with email: {} and username: {}", 
                clientBLM.getEmail(), clientBLM.getUsername());
        
        validator.validate(clientBLM);
        ClientDALM clientDALM = converter.toDALM(clientBLM);
        
        return Mono.usingWhen(
                clientConnectionFactory.create(),
                connection -> {
                    log.debug("Starting transaction for client addition");
                    return Mono.from(connection.beginTransaction())
                            .then(Mono.defer(() -> {
                                log.trace("Checking client existence by email: {}", clientDALM.getEmail());
                                return checkClientExistsByEmail(connection, clientDALM.getEmail());
                            }))
                            .filter(exists -> !exists)
                            .switchIfEmpty(Mono.defer(() -> {
                                log.warn("Client with email {} already exists", clientDALM.getEmail());
                                return Mono.error(new ClientAlreadyExisistsException("Email already exists"));
                            }))
                            .then(Mono.defer(() -> {
                                log.trace("Checking client existence by username: {}", clientDALM.getUsername());
                                return checkClientExistsByUsername(connection, clientDALM.getUsername());
                            }))
                            .filter(exists -> !exists)
                            .switchIfEmpty(Mono.defer(() -> {
                                log.warn("Client with username {} already exists", clientDALM.getUsername());
                                return Mono.error(new ClientAlreadyExisistsException("Username already exists"));
                            }))
                            .then(Mono.defer(() -> {
                                log.debug("Proceeding with client insertion");
                                return insertClient(connection, clientDALM);
                            }))
                            .then(Mono.defer(() -> {
                                log.debug("Committing transaction");
                                return Mono.from(connection.commitTransaction())
                                        .doOnSuccess(v -> log.info("Transaction committed successfully for client: {}", clientDALM.getEmail()));
                            }))
                            .doOnSuccess(v -> log.info("Successfully added client with UID: {}, email: {}", 
                                    clientDALM.getUid(), clientDALM.getEmail()))
                            .doOnError(error -> {
                                log.error("Error during client addition with email: {} - {}. Rolling back transaction.", 
                                        clientDALM.getEmail(), error.getMessage());
                                Mono.from(connection.rollbackTransaction())
                                        .doOnSuccess(v -> log.debug("Rollback completed"))
                                        .doOnError(e -> log.error("Error during rollback: {}", e.getMessage()))
                                        .subscribe();
                            });
                },
                connection -> {
                    log.trace("Closing connection");
                    return Mono.from(connection.close());
                },
                (connection, error) -> {
                    log.error("Async rollback triggered for error: {}", error.getMessage());
                    return Mono.from(connection.rollbackTransaction())
                            .then(Mono.from(connection.close()));
                },
                connection -> {
                    log.trace("Async close connection");
                    return Mono.from(connection.close());
                }
        );
    }

    @Override
    public Mono<ClientBLM> findByUid(UUID uuid) throws ClientNotFoundException {
        log.debug("Searching for client by UID: {}", uuid);
        
        return Mono.usingWhen(
                clientConnectionFactory.create(),
                connection -> findClientByQuery(connection, SELECT_CLIENT_BY_UID, uuid)
                        .doOnSuccess(client -> log.debug("Found client by UID: {}", uuid))
                        .doOnError(error -> log.warn("Client not found by UID: {} - {}", uuid, error.getMessage())),
                Connection::close);
    }

    @Override
    public Mono<ClientBLM> findByEmail(String emailString) throws ClientNotFoundException {
        log.debug("Searching for client by email: {}", emailString);
        
        return Mono.usingWhen(
                clientConnectionFactory.create(),
                connection -> findClientByQuery(connection, SELECT_CLIENT_BY_EMAIL, emailString)
                        .doOnSuccess(client -> log.debug("Found client by email: {}", emailString))
                        .doOnError(error -> log.warn("Client not found by email: {} - {}", emailString, error.getMessage())),
                Connection::close);
    }

    @Override
    public Mono<ClientBLM> findByUsername(String usernameString) throws ClientNotFoundException {
        log.debug("Searching for client by username: {}", usernameString);
        
        return Mono.usingWhen(
                clientConnectionFactory.create(),
                connection -> findClientByQuery(connection, SELECT_CLIENT_BY_USERNAME, usernameString)
                        .doOnSuccess(client -> log.debug("Found client by username: {}", usernameString))
                        .doOnError(error -> log.warn("Client not found by username: {} - {}", usernameString, error.getMessage())),
                Connection::close);
    }

    @Override
    public Mono<ClientBLM> findByEmailPassword(String emailString, String passwordString)
            throws ClientNotFoundException {
        log.debug("Attempting authentication by email: {}", emailString);
        
        return Mono.usingWhen(
                clientConnectionFactory.create(),
                connection -> {
                    return Mono.from(connection.createStatement(SELECT_CLIENT_BY_EMAIL_PASSWORD)
                            .bind("$1", emailString)
                            .bind("$2", passwordString)
                            .execute())
                            .flatMap(result -> Mono.from(result.map((row, metadata) -> mapRowToClientDALM(row))))
                            .map(converter::toBLM)
                            .doOnSuccess(client -> log.info("Successful authentication by email: {}", emailString))
                            .doOnError(error -> log.warn("Failed authentication by email: {} - {}", emailString, error.getMessage()))
                            .switchIfEmpty(Mono.error(new ClientNotFoundException(
                                    "Client with email " + emailString + " and provided password not found")));
                },
                Connection::close);
    }

    @Override
    public Mono<ClientBLM> findByUsernamePassword(String usernameString, String passwordString)
            throws ClientNotFoundException {
        log.debug("Attempting authentication by username: {}", usernameString);
        
        return Mono.usingWhen(
                clientConnectionFactory.create(),
                connection -> {
                    return Mono.from(connection.createStatement(SELECT_CLIENT_BY_USERNAME_PASSWORD)
                            .bind("$1", usernameString)
                            .bind("$2", passwordString)
                            .execute())
                            .flatMap(result -> Mono.from(result.map((row, metadata) -> mapRowToClientDALM(row))))
                            .map(converter::toBLM)
                            .doOnSuccess(client -> log.info("Successful authentication by username: {}", usernameString))
                            .doOnError(error -> log.warn("Failed authentication by username: {} - {}", usernameString, error.getMessage()))
                            .switchIfEmpty(Mono.error(new ClientNotFoundException(
                                    "Client with username " + usernameString + " and provided password not found")));
                },
                Connection::close);
    }

    @Override
    public Mono<Void> deleteByUid(UUID uuid) throws ClientNotFoundException {
        log.info("Attempting to delete client by UID: {}", uuid);
        
        return findByUid(uuid)
                .flatMap(client -> Mono.usingWhen(
                        clientConnectionFactory.create(),
                        connection -> {
                            return Mono.from(connection.beginTransaction())
                                    .then(Mono.from(connection.createStatement(DELETE_CLIENT_BY_UID)
                                            .bind("$1", uuid)
                                            .execute()))
                                    .flatMap(result -> Mono.from(result.getRowsUpdated()))
                                    .doOnNext(rows -> log.debug("Deleted {} rows for UID: {}", rows, uuid))
                                    .then(Mono.from(connection.commitTransaction()))
                                    .doOnSuccess(v -> log.info("Successfully deleted client with UID: {}", uuid))
                                    .doOnError(error -> {
                                        log.error("Failed to delete client with UID: {} - {}", uuid, error.getMessage());
                                        Mono.from(connection.rollbackTransaction()).subscribe();
                                    });
                        },
                        Connection::close,
                        (conn, err) -> Mono.from(conn.rollbackTransaction()),
                        Connection::close
                ))
                .doOnError(error -> log.warn("Cannot delete client - client not found by UID: {}", uuid));
    }

    private Mono<Boolean> checkClientExistsByEmail(Connection connection, String email) {
        log.trace("Checking if client exists by email: {}", email);
        
        String sql = "SELECT COUNT(*) as count FROM core.client WHERE email = $1";

        return Mono.from(connection.createStatement(sql)
                .bind("$1", email)
                .execute())
                .flatMap(result -> Mono.from(result.map((row, metadata) -> row.get("count", Long.class))))
                .map(count -> count > 0)
                .defaultIfEmpty(false)
                .doOnNext(exists -> log.trace("Client existence check by email {}: {}", email, exists));
    }

    private Mono<Boolean> checkClientExistsByUsername(Connection connection, String username) {
        log.trace("Checking if client exists by username: {}", username);
        
        String sql = "SELECT COUNT(*) as count FROM core.client WHERE username = $1";

        return Mono.from(connection.createStatement(sql)
                .bind("$1", username)
                .execute())
                .flatMap(result -> Mono.from(result.map((row, metadata) -> row.get("count", Long.class))))
                .map(count -> count > 0)
                .defaultIfEmpty(false)
                .doOnNext(exists -> log.trace("Client existence check by username {}: {}", username, exists));
    }

    private Mono<Void> insertClient(Connection connection, ClientDALM clientDALM) {
        UUID uid = clientDALM.getUid() != null ? clientDALM.getUid() : UUID.randomUUID();
        clientDALM.setUid(uid); // Ensure UID is set for logging
        
        log.debug("Inserting new client with UID: {}, email: {}", uid, clientDALM.getEmail());

        Statement stmt = connection.createStatement(INSERT_CLIENT)
                .bind("$1", uid)
                .bind("$2", clientDALM.getEmail())
                .bind("$3",
                        clientDALM.getBirthDate() != null
                                ? clientDALM.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                                : null)
                .bind("$4", clientDALM.getUsername())
                .bind("$5", clientDALM.getPassword());

        return Mono.from(stmt.execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                .doOnNext(rowsUpdated -> log.debug("Insert executed, rows affected: {}", rowsUpdated))
                .then()
                .doOnSuccess(v -> log.info("Client inserted successfully with UID: {}", uid))
                .doOnError(error -> log.error("Failed to insert client with UID: {} - {}", uid, error.getMessage()));
    }

    private Mono<ClientBLM> findClientByQuery(Connection connection, String sql, Object value) {
        log.trace("Executing query: {} with value: {}", sql, value);
        
        return Mono.from(connection.createStatement(sql)
                .bind("$1", value)
                .execute())
                .flatMap(result -> Mono.from(result.map((row, metadata) -> mapRowToClientDALM(row))))
                .map(converter::toBLM)
                .doOnNext(client -> log.trace("Query executed successfully: {}", sql))
                .doOnError(error -> log.trace("Query failed: {} - {}", sql, error.getMessage()))
                .switchIfEmpty(Mono.error(new ClientNotFoundException(
                        "Client not found for query: " + sql + " with value: " + value)));
    }

    private ClientDALM mapRowToClientDALM(Row row) {
        log.trace("Mapping row to ClientDALM");
        
        LocalDate birthDateLocal = row.get("birth_date", LocalDate.class);
        Date birthDate = birthDateLocal != null
                ? Date.from(birthDateLocal.atStartOfDay(ZoneId.systemDefault()).toInstant())
                : null;

        return ClientDALM.builder()
                .uid(row.get("uid", UUID.class))
                .email(row.get("email", String.class))
                .birthDate(birthDate)
                .username(row.get("username", String.class))
                .password(row.get("password", String.class))
                .build();
    }

    // Метод для проверки соединения с БД
    public Mono<Boolean> testConnection() {
        return Mono.usingWhen(
                clientConnectionFactory.create(),
                connection -> {
                    log.info("Testing database connection");
                    return Mono.from(connection.createStatement("SELECT 1").execute())
                            .flatMap(result -> Mono.from(result.map((row, metadata) -> true)))
                            .doOnSuccess(v -> log.info("Database connection test successful"))
                            .doOnError(error -> log.error("Database connection test failed: {}", error.getMessage()));
                },
                Connection::close
        );
    }
}