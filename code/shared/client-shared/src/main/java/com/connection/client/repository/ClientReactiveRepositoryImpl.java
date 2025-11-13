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
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

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
    public Mono<Void> add(ClientBLM clientBLM) throws ClientAlreadyExisistsException {
        // Валидация BLM модели
        validator.validate(clientBLM);
        
        ClientDALM clientDALM = converter.toDALM(clientBLM);
        
        return Mono.usingWhen(
            clientConnectionFactory.create(),
            connection -> checkClientExistsByEmail(connection, clientDALM.getEmail())
                .flatMap(emailExists -> {
                    if (emailExists) {
                        return Mono.error(new ClientAlreadyExisistsException(
                            "Client with email " + clientDALM.getEmail() + " already exists"));
                    }
                    return checkClientExistsByUsername(connection, clientDALM.getUsername());
                })
                .flatMap(usernameExists -> {
                    if (usernameExists) {
                        return Mono.error(new ClientAlreadyExisistsException(
                            "Client with username " + clientDALM.getUsername() + " already exists"));
                    }
                    return insertClient(connection, clientDALM);
                }),
            Connection::close
        );
    }

    @Override
    public Mono<ClientBLM> findByUid(UUID uuid) throws ClientNotFoundException {
        return Mono.usingWhen(
            clientConnectionFactory.create(),
            connection -> findClientByQuery(connection, SELECT_CLIENT_BY_UID, uuid),
            Connection::close
        );
    }

    @Override
    public Mono<ClientBLM> findByEmail(String emailString) throws ClientNotFoundException {
        return Mono.usingWhen(
            clientConnectionFactory.create(),
            connection -> findClientByQuery(connection, SELECT_CLIENT_BY_EMAIL, emailString),
            Connection::close
        );
    }

    @Override
    public Mono<ClientBLM> findByUsername(String usernameString) throws ClientNotFoundException {
        return Mono.usingWhen(
            clientConnectionFactory.create(),
            connection -> findClientByQuery(connection, SELECT_CLIENT_BY_USERNAME, usernameString),
            Connection::close
        );
    }

    @Override
    public Mono<ClientBLM> findByEmailPassword(String emailString, String passwordString) throws ClientNotFoundException {
        return Mono.usingWhen(
            clientConnectionFactory.create(),
            connection -> {
                return Mono.from(connection.createStatement(SELECT_CLIENT_BY_EMAIL_PASSWORD)
                    .bind("$1", emailString)
                    .bind("$2", passwordString)
                    .execute())
                    .flatMap(result -> 
                        Mono.from(result.map((row, metadata) -> mapRowToClientDALM(row)))
                    )
                    .map(converter::toBLM)
                    .switchIfEmpty(Mono.error(new ClientNotFoundException(
                        "Client with email " + emailString + " and provided password not found")));
            },
            Connection::close
        );
    }

    @Override
    public Mono<ClientBLM> findByUsernamePassword(String usernameString, String passwordString) throws ClientNotFoundException {
        return Mono.usingWhen(
            clientConnectionFactory.create(),
            connection -> {
                return Mono.from(connection.createStatement(SELECT_CLIENT_BY_USERNAME_PASSWORD)
                    .bind("$1", usernameString)
                    .bind("$2", passwordString)
                    .execute())
                    .flatMap(result -> 
                        Mono.from(result.map((row, metadata) -> mapRowToClientDALM(row)))
                    )
                    .map(converter::toBLM)
                    .switchIfEmpty(Mono.error(new ClientNotFoundException(
                        "Client with username " + usernameString + " and provided password not found")));
            },
            Connection::close
        );
    }

    @Override
    public Mono<Void> deleteByUid(UUID uuid) throws ClientNotFoundException {
        return findByUid(uuid)
            .flatMap(client -> Mono.usingWhen(
                clientConnectionFactory.create(),
                connection -> {
                    return Mono.from(connection.createStatement(DELETE_CLIENT_BY_UID)
                        .bind("$1", uuid)
                        .execute())
                        .then();
                },
                Connection::close
            ));
    }

    private Mono<Boolean> checkClientExistsByEmail(Connection connection, String email) {
        String sql = "SELECT COUNT(*) as count FROM core.client WHERE email = $1";
        
        return Mono.from(connection.createStatement(sql)
            .bind("$1", email)
            .execute())
            .flatMap(result -> 
                Mono.from(result.map((row, metadata) -> row.get("count", Long.class) > 0))
            )
            .defaultIfEmpty(false);
    }

    private Mono<Boolean> checkClientExistsByUsername(Connection connection, String username) {
        String sql = "SELECT COUNT(*) as count FROM core.client WHERE username = $1";
        
        return Mono.from(connection.createStatement(sql)
            .bind("$1", username)
            .execute())
            .flatMap(result -> 
                Mono.from(result.map((row, metadata) -> row.get("count", Long.class) > 0))
            )
            .defaultIfEmpty(false);
    }

    private Mono<Void> insertClient(Connection connection, ClientDALM clientDALM) {
        UUID uid = clientDALM.getUid() != null ? clientDALM.getUid() : UUID.randomUUID();
        
        Statement stmt = connection.createStatement(INSERT_CLIENT)
            .bind("$1", uid)
            .bind("$2", clientDALM.getEmail())
            .bind("$3", clientDALM.getBirthDate() != null ? 
                clientDALM.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null)
            .bind("$4", clientDALM.getUsername())
            .bind("$5", clientDALM.getPassword());

        return Mono.from(stmt.execute()).then();
    }

    private Mono<ClientBLM> findClientByQuery(Connection connection, String sql, Object value) {
        return Mono.from(connection.createStatement(sql)
            .bind("$1", value)
            .execute())
            .flatMap(result -> 
                Mono.from(result.map((row, metadata) -> mapRowToClientDALM(row)))
            )
            .map(converter::toBLM)
            .switchIfEmpty(Mono.error(new ClientNotFoundException(
                "Client not found for query: " + sql + " with value: " + value)));
    }

    private ClientDALM mapRowToClientDALM(Row row) {
        LocalDate birthDateLocal = row.get("birth_date", LocalDate.class);
        Date birthDate = birthDateLocal != null ? 
            Date.from(birthDateLocal.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;

        return ClientDALM.builder()
            .uid(row.get("uid", UUID.class))
            .email(row.get("email", String.class))
            .birthDate(birthDate)
            .username(row.get("username", String.class))
            .password(row.get("password", String.class))
            .build();
    }
}