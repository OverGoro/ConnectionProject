package com.connection.client.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Date;
import java.util.UUID;

import com.connection.client.converter.ClientConverter;
import com.connection.client.exception.ClientAlreadyExisistsException;
import com.connection.client.exception.ClientNotFoundException;
import com.connection.client.model.ClientBLM;
import com.connection.client.model.ClientDALM;
import com.connection.client.validator.ClientValidator;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class ClientRepositorySQLImpl implements ClientRepository {

    private static final String SELECT_CLIENT = "SELECT uid, email, birth_date, username, password";

    private static final String SELECT_CLIENT_BY_UID = SELECT_CLIENT +
            " FROM core.client WHERE uid = :uid";

    private static final String SELECT_CLIENT_BY_EMAIL = SELECT_CLIENT +
            " FROM core.client WHERE email = :email";

    private static final String SELECT_CLIENT_BY_USERNAME = SELECT_CLIENT +
            " FROM core.client WHERE username = :username";

    private static final String SELECT_CLIENT_BY_EMAIL_PASSWORD = SELECT_CLIENT +
            " FROM core.client WHERE email = :email AND password = :password";

    private static final String SELECT_CLIENT_BY_USERNAME_PASSWORD = SELECT_CLIENT +
            " FROM core.client WHERE username = :username AND password = :password";

    private static final String INSERT_CLIENT = "INSERT INTO core.client (uid, email, birth_date, username, password) "
            +
            "VALUES (:uid, :email, :birth_date, :username, :password)";

    private static final String DELETE_CLIENT_BY_UID = "DELETE FROM core.client WHERE uid = :uid";

    private final ClientConverter converter = new ClientConverter();
    private final ClientValidator validator = new ClientValidator();
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<ClientDALM> clientRowMapper = (rs, rowNum) -> {
        ClientDALM client = new ClientDALM();
        client.setUid(UUID.fromString(rs.getString("uid")));
        client.setEmail(rs.getString("email"));
        client.setBirthDate(rs.getDate("birth_date"));
        client.setUsername(rs.getString("username"));
        client.setPassword(rs.getString("password"));
        return client;
    };

    public ClientRepositorySQLImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Mono<Void> add(ClientBLM clientBLM) throws ClientAlreadyExisistsException {
        return Mono.fromCallable(() -> {
            // Валидация BLM модели
            validator.validate(clientBLM);
            
            try {
                // Проверяем существование по email
                findByEmailSync(clientBLM.getEmail());
                throw new ClientAlreadyExisistsException("Client with email " + clientBLM.getEmail() + " already exists");
            } catch (ClientNotFoundException e) {
                // Клиент не найден по email - продолжаем
            }

            try {
                // Проверяем существование по username
                findByUsernameSync(clientBLM.getUsername());
                throw new ClientAlreadyExisistsException(
                        "Client with username " + clientBLM.getUsername() + " already exists");
            } catch (ClientNotFoundException e) {
                // Клиент не найден по username - продолжаем
            }

            // Конвертация BLM в DALM
            ClientDALM clientDALM = converter.toDALM(clientBLM);

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("uid", clientDALM.getUid() != null ? clientDALM.getUid() : UUID.randomUUID());
            params.addValue("email", clientDALM.getEmail());
            params.addValue("birth_date",
                    clientDALM.getBirthDate() != null ? new Date(clientDALM.getBirthDate().getTime()) : null);
            params.addValue("username", clientDALM.getUsername());
            params.addValue("password", clientDALM.getPassword());

            jdbcTemplate.update(INSERT_CLIENT, params);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<ClientBLM> findByUid(UUID uuid) throws ClientNotFoundException {
        return Mono.fromCallable(() -> {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("uid", uuid);
            try {
                ClientDALM dalClient = jdbcTemplate.queryForObject(SELECT_CLIENT_BY_UID, params, clientRowMapper);
                // Конвертация DALM в BLM
                return converter.toBLM(dalClient);
            } catch (EmptyResultDataAccessException e) {
                throw new ClientNotFoundException("Client with UID " + uuid + " not found");
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<ClientBLM> findByEmail(String emailString) throws ClientNotFoundException {
        return Mono.fromCallable(() -> {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("email", emailString);
            try {
                ClientDALM dalClient = jdbcTemplate.queryForObject(SELECT_CLIENT_BY_EMAIL, params, clientRowMapper);
                // Конвертация DALM в BLM
                return converter.toBLM(dalClient);
            } catch (EmptyResultDataAccessException e) {
                throw new ClientNotFoundException("Client with email " + emailString + " not found");
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<ClientBLM> findByUsername(String usernameString) throws ClientNotFoundException {
        return Mono.fromCallable(() -> {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("username", usernameString);
            try {
                ClientDALM dalClient = jdbcTemplate.queryForObject(SELECT_CLIENT_BY_USERNAME, params, clientRowMapper);
                // Конвертация DALM в BLM
                return converter.toBLM(dalClient);
            } catch (EmptyResultDataAccessException e) {
                throw new ClientNotFoundException("Client with username " + usernameString + " not found");
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<ClientBLM> findByEmailPassword(String emailString, String passwordString) throws ClientNotFoundException {
        return Mono.fromCallable(() -> {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("email", emailString);
            params.addValue("password", passwordString);
            try {
                ClientDALM dalClient = jdbcTemplate.queryForObject(SELECT_CLIENT_BY_EMAIL_PASSWORD, params, clientRowMapper);
                // Конвертация DALM в BLM
                return converter.toBLM(dalClient);
            } catch (EmptyResultDataAccessException e) {
                throw new ClientNotFoundException("Client with email " + emailString + " and provided password not found");
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<ClientBLM> findByUsernamePassword(String usernameString, String passwordString)
            throws ClientNotFoundException {
        return Mono.fromCallable(() -> {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("username", usernameString);
            params.addValue("password", passwordString);
            try {
                ClientDALM dalClient = jdbcTemplate.queryForObject(SELECT_CLIENT_BY_USERNAME_PASSWORD, params, clientRowMapper);
                // Конвертация DALM в BLM
                return converter.toBLM(dalClient);
            } catch (EmptyResultDataAccessException e) {
                throw new ClientNotFoundException(
                        "Client with username " + usernameString + " and provided password not found");
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> deleteByUid(UUID uuid) throws ClientNotFoundException {
        return Mono.fromCallable(() -> {
            // Проверяем существование клиента перед удалением
            findByUidSync(uuid);

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("uid", uuid);

            jdbcTemplate.update(DELETE_CLIENT_BY_UID, params);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    // Синхронные методы для внутреннего использования
    private ClientBLM findByUidSync(UUID uuid) throws ClientNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uuid);
        try {
            ClientDALM dalClient = jdbcTemplate.queryForObject(SELECT_CLIENT_BY_UID, params, clientRowMapper);
            return converter.toBLM(dalClient);
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException("Client with UID " + uuid + " not found");
        }
    }

    private ClientBLM findByEmailSync(String emailString) throws ClientNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("email", emailString);
        try {
            ClientDALM dalClient = jdbcTemplate.queryForObject(SELECT_CLIENT_BY_EMAIL, params, clientRowMapper);
            return converter.toBLM(dalClient);
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException("Client with email " + emailString + " not found");
        }
    }

    private ClientBLM findByUsernameSync(String usernameString) throws ClientNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("username", usernameString);
        try {
            ClientDALM dalClient = jdbcTemplate.queryForObject(SELECT_CLIENT_BY_USERNAME, params, clientRowMapper);
            return converter.toBLM(dalClient);
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException("Client with username " + usernameString + " not found");
        }
    }
}