package com.connection.client.repository;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.BiFunction;

import com.connection.client.converter.ClientConverter;
import com.connection.client.exception.ClientAlreadyExisistsException;
import com.connection.client.exception.ClientNotFoundException;
import com.connection.client.model.ClientBLM;
import com.connection.client.model.ClientDALM;
import com.connection.client.validator.ClientValidator;

public class ClientReactiveRepositoryImpl implements ClientRepository {

    private static final String SELECT_CLIENT = "SELECT uid, email, birth_date, username, password";
    private static final String FROM_CORE_CLIENT = " FROM core.client";

    private static final String SELECT_CLIENT_BY_UID = SELECT_CLIENT + FROM_CORE_CLIENT + " WHERE uid = $1";
    private static final String SELECT_CLIENT_BY_EMAIL = SELECT_CLIENT + FROM_CORE_CLIENT + " WHERE email = $1";
    private static final String SELECT_CLIENT_BY_USERNAME = SELECT_CLIENT + FROM_CORE_CLIENT + " WHERE username = $1";
    private static final String SELECT_CLIENT_BY_EMAIL_PASSWORD = SELECT_CLIENT + FROM_CORE_CLIENT
            + " WHERE email = $1 AND password = $2";
    private static final String SELECT_CLIENT_BY_USERNAME_PASSWORD = SELECT_CLIENT + FROM_CORE_CLIENT
            + " WHERE username = $1 AND password = $2";

    private static final String INSERT_CLIENT = "INSERT INTO core.client (uid, email, birth_date, username, password) "
            +
            "VALUES ($1, $2, $3, $4, $5)";

    private static final String DELETE_CLIENT_BY_UID = "DELETE FROM core.client WHERE uid = $1";
    private static final String COUNT_CLIENT_BY_EMAIL = "SELECT COUNT(*) FROM core.client WHERE email = $1";
    private static final String COUNT_CLIENT_BY_USERNAME = "SELECT COUNT(*) FROM core.client WHERE username = $1";

    private final ClientConverter converter = new ClientConverter();
    private final ClientValidator validator = new ClientValidator();
    private final ConnectionFactory connectionFactory;

    private final BiFunction<Row, RowMetadata, ClientDALM> clientRowMapper = (row, metadata) -> {
        ClientDALM client = new ClientDALM();
        client.setUid(row.get("uid", UUID.class));
        client.setEmail(row.get("email", String.class));

        LocalDate birthDate = row.get("birth_date", LocalDate.class);
        if (birthDate != null) {
            client.setBirthDate(java.sql.Date.valueOf(birthDate));
        }

        client.setUsername(row.get("username", String.class));
        client.setPassword(row.get("password", String.class));
        return client;
    };

    public ClientReactiveRepositoryImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Mono<Void> add(ClientBLM clientBLM) throws ClientAlreadyExisistsException {
        return Mono.defer(() -> {
            // Валидация BLM модели
            validator.validate(clientBLM);

            ClientDALM clientDALM = converter.toDALM(clientBLM);
            UUID uid = clientDALM.getUid() != null ? clientDALM.getUid() : UUID.randomUUID();

            return Mono.usingWhen(
                    connectionFactory.create(),
                    connection -> checkEmailExists(connection, clientDALM.getEmail())
                            .flatMap(emailExists -> {
                                if (emailExists) {
                                    return Mono.error(new ClientAlreadyExisistsException(
                                            "Client with email " + clientDALM.getEmail() + " already exists"));
                                }
                                return checkUsernameExists(connection, clientDALM.getUsername());
                            })
                            .flatMap(usernameExists -> {
                                if (usernameExists) {
                                    return Mono.error(new ClientAlreadyExisistsException(
                                            "Client with username " + clientDALM.getUsername() + " already exists"));
                                }
                                return insertClient(connection, clientDALM, uid);
                            }),
                    Connection::close);
        });
    }

    @Override
    public Mono<ClientBLM> findByUid(UUID uuid) throws ClientNotFoundException {
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> executeQueryForSingleResult(
                        connection,
                        SELECT_CLIENT_BY_UID,
                        stmt -> stmt.bind(0, uuid)),
                Connection::close);
    }

    @Override
    public Mono<ClientBLM> findByEmail(String emailString) throws ClientNotFoundException {
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> executeQueryForSingleResult(
                        connection,
                        SELECT_CLIENT_BY_EMAIL,
                        stmt -> stmt.bind(0, emailString)),
                Connection::close);
    }

    @Override
    public Mono<ClientBLM> findByUsername(String usernameString) throws ClientNotFoundException {
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> executeQueryForSingleResult(
                        connection,
                        SELECT_CLIENT_BY_USERNAME,
                        stmt -> stmt.bind(0, usernameString)),
                Connection::close);
    }

    @Override
    public Mono<ClientBLM> findByEmailPassword(String emailString, String passwordString)
            throws ClientNotFoundException {
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> executeQueryForSingleResult(
                        connection,
                        SELECT_CLIENT_BY_EMAIL_PASSWORD,
                        stmt -> stmt.bind(0, emailString).bind(1, passwordString)),
                Connection::close);
    }

    @Override
    public Mono<ClientBLM> findByUsernamePassword(String usernameString, String passwordString)
            throws ClientNotFoundException {
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> executeQueryForSingleResult(
                        connection,
                        SELECT_CLIENT_BY_USERNAME_PASSWORD,
                        stmt -> stmt.bind(0, usernameString).bind(1, passwordString)),
                Connection::close);
    }

    @Override
    public Mono<Void> deleteByUid(UUID uuid) throws ClientNotFoundException {
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> checkClientExistsByUid(connection, uuid)
                        .flatMap(exists -> {
                            if (!exists) {
                                return Mono
                                        .error(new ClientNotFoundException("Client with UID " + uuid + " not found"));
                            }
                            return executeUpdate(connection, DELETE_CLIENT_BY_UID, stmt -> stmt.bind(0, uuid));
                        })
                        .then(),
                Connection::close);
    }

    // Вспомогательные методы
    private Mono<Boolean> checkEmailExists(Connection connection, String email) {
        return executeQueryForCount(connection, COUNT_CLIENT_BY_EMAIL, stmt -> stmt.bind(0, email))
                .map(count -> count > 0);
    }

    private Mono<Boolean> checkUsernameExists(Connection connection, String username) {
        return executeQueryForCount(connection, COUNT_CLIENT_BY_USERNAME, stmt -> stmt.bind(0, username))
                .map(count -> count > 0);
    }

    private Mono<Boolean> checkClientExistsByUid(Connection connection, UUID uuid) {
        return executeQueryForSingleResult(connection, SELECT_CLIENT_BY_UID, stmt -> stmt.bind(0, uuid))
                .map(client -> true)
                .onErrorResume(ClientNotFoundException.class, e -> Mono.just(false));
    }

    private Mono<ClientBLM> executeQueryForSingleResult(
            Connection connection,
            String sql,
            java.util.function.Consumer<Statement> binder) {

        Statement statement = connection.createStatement(sql);
        binder.accept(statement);

        return Flux.from(statement.execute())
                .flatMap(result -> result.map(clientRowMapper))
                .next()
                .map(converter::toBLM)
                .switchIfEmpty(Mono.error(new ClientNotFoundException("Client not found")));
    }

    private Mono<Long> executeQueryForCount(
            Connection connection,
            String sql,
            java.util.function.Consumer<Statement> binder) {

        Statement statement = connection.createStatement(sql);
        binder.accept(statement);

        return Flux.from(statement.execute())
                .flatMap(result -> result.map((row, metadata) -> row.get(0, Long.class)))
                .next()
                .defaultIfEmpty(0L);
    }

    private Mono<Long> executeUpdate(
            Connection connection,
            String sql,
            java.util.function.Consumer<Statement> binder) {

        Statement statement = connection.createStatement(sql);
        binder.accept(statement);

        return Mono.from(statement.execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()));
    }

    private Mono<Void> insertClient(Connection connection, ClientDALM clientDALM, UUID uid) {
        return executeUpdate(connection, INSERT_CLIENT, stmt -> stmt.bind(0, uid)
                .bind(1, clientDALM.getEmail())
                .bind(2, clientDALM.getBirthDate() != null ? clientDALM.getBirthDate() : null)
                .bind(3, clientDALM.getUsername())
                .bind(4, clientDALM.getPassword())).then();
    }
}