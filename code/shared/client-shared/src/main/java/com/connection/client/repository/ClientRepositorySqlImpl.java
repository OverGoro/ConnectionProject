package com.connection.client.repository;

import com.connection.client.converter.ClientConverter;
import com.connection.client.exception.ClientAlreadyExisistsException;
import com.connection.client.exception.ClientNotFoundException;
import com.connection.client.model.ClientBlm;
import com.connection.client.model.ClientDalm;
import com.connection.client.validator.ClientValidator;
import java.sql.Date;
import java.util.UUID;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** . */
public class ClientRepositorySqlImpl implements ClientRepository {

    private static final String SELECT_CLIENT =
            "SELECT uid, email, birth_date, username, password";

    private static final String SELECT_CLIENT_BY_UID =
            SELECT_CLIENT + " FROM core.client WHERE uid = :uid";

    private static final String SELECT_CLIENT_BY_EMAIL =
            SELECT_CLIENT + " FROM core.client WHERE email = :email";

    private static final String SELECT_CLIENT_BY_USERNAME =
            SELECT_CLIENT + " FROM core.client WHERE username = :username";

    private static final String SELECT_CLIENT_BY_EMAIL_PASSWORD = SELECT_CLIENT
            + " FROM core.client WHERE email = :email AND password = :password";

    private static final String SELECT_CLIENT_BY_USERNAME_PASSWORD =
            SELECT_CLIENT
                    + " FROM core.client WHERE username = :username AND password = :password";

    private static final String INSERT_CLIENT =
            "INSERT INTO core.client (uid, email, birth_date, username, password) "
                    + "VALUES (:uid, :email, :birth_date, :username, :password)";

    private static final String DELETE_CLIENT_BY_UID =
            "DELETE FROM core.client WHERE uid = :uid";

    private final ClientConverter converter = new ClientConverter();
    private final ClientValidator validator = new ClientValidator();
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<ClientDalm> clientRowMapper = (rs, rowNum) -> {
        ClientDalm client = new ClientDalm();
        client.setUid(UUID.fromString(rs.getString("uid")));
        client.setEmail(rs.getString("email"));
        client.setBirthDate(rs.getDate("birth_date"));
        client.setUsername(rs.getString("username"));
        client.setPassword(rs.getString("password"));
        return client;
    };

    /** . */
    public ClientRepositorySqlImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** . */
    @Override
    @Transactional
    public void add(ClientBlm clientBlm) throws ClientAlreadyExisistsException {
        // Валидация Blm модели
        validator.validate(clientBlm);

        try {
            // Проверяем существование по email
            findByEmail(clientBlm.getEmail());
            throw new ClientAlreadyExisistsException("Client with email "
                    + clientBlm.getEmail() + " already exists");
        } catch (ClientNotFoundException e) {
            // Клиент не найден по email - продолжаем
        }

        try {
            // Проверяем существование по username
            findByUsername(clientBlm.getUsername());
            throw new ClientAlreadyExisistsException("Client with username "
                    + clientBlm.getUsername() + " already exists");
        } catch (ClientNotFoundException e) {
            // Клиент не найден по username - продолжаем
        }

        // Конвертация Blm в Dalm
        ClientDalm clientDalm = converter.toDalm(clientBlm);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", clientDalm.getUid() != null ? clientDalm.getUid()
                : UUID.randomUUID());
        params.addValue("email", clientDalm.getEmail());
        params.addValue("birth_date",
                clientDalm.getBirthDate() != null
                        ? new Date(clientDalm.getBirthDate().getTime())
                        : null);
        params.addValue("username", clientDalm.getUsername());
        params.addValue("password", clientDalm.getPassword());

        jdbcTemplate.update(INSERT_CLIENT, params);
    }

    /** . */
    @Override
    @Transactional
    public ClientBlm findByUid(UUID uuid) throws ClientNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uuid);
        try {
            ClientDalm dalClient = jdbcTemplate.queryForObject(
                    SELECT_CLIENT_BY_UID, params, clientRowMapper);
            // Конвертация Dalm в Blm
            return converter.toBlm(dalClient);
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException(
                    "Client with UID " + uuid + " not found");
        }
    }

    /** . */
    @Override
    @Transactional
    public ClientBlm findByEmail(String emailString)
            throws ClientNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("email", emailString);
        try {
            ClientDalm dalClient = jdbcTemplate.queryForObject(
                    SELECT_CLIENT_BY_EMAIL, params, clientRowMapper);
            // Конвертация Dalm в Blm
            return converter.toBlm(dalClient);
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException(
                    "Client with email " + emailString + " not found");
        }
    }

    /** . */
    @Override
    @Transactional
    public ClientBlm findByUsername(String usernameString)
            throws ClientNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("username", usernameString);
        try {
            ClientDalm dalClient = jdbcTemplate.queryForObject(
                    SELECT_CLIENT_BY_USERNAME, params, clientRowMapper);
            // Конвертация Dalm в Blm
            return converter.toBlm(dalClient);
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException(
                    "Client with username " + usernameString + " not found");
        }
    }

    /** . */
    @Override
    @Transactional
    public ClientBlm findByEmailPassword(String emailString,
            String passwordString) throws ClientNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("email", emailString);
        params.addValue("password", passwordString);
        try {
            ClientDalm dalClient = jdbcTemplate.queryForObject(
                    SELECT_CLIENT_BY_EMAIL_PASSWORD, params, clientRowMapper);
            // Конвертация Dalm в Blm
            return converter.toBlm(dalClient);
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException("Client with email " + emailString
                    + " and provided password not found");
        }
    }

    @Override
    @Transactional
    public ClientBlm findByUsernamePassword(String usernameString,
            String passwordString) throws ClientNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("username", usernameString);
        params.addValue("password", passwordString);
        try {
            ClientDalm dalClient = jdbcTemplate.queryForObject(
                    SELECT_CLIENT_BY_USERNAME_PASSWORD, params,
                    clientRowMapper);
            // Конвертация Dalm в Blm
            return converter.toBlm(dalClient);
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException("Client with username "
                    + usernameString + " and provided password not found");
        }
    }

    @Override
    @Transactional
    public void deleteByUid(UUID uuid) throws ClientNotFoundException {
        // Проверяем существование клиента перед удалением
        findByUid(uuid);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uuid);

        jdbcTemplate.update(DELETE_CLIENT_BY_UID, params);
    }
}
