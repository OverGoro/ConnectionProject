package com.connection.client.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Date;
import java.util.UUID;

import com.connection.client.exception.ClientAlreadyExisistsException;
import com.connection.client.exception.ClientNotFoundException;
import com.connection.client.model.ClientDALM;

import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void add(ClientDALM clientDALM) throws ClientAlreadyExisistsException {
        try {
            // Проверяем существование по email
            findByEmail(clientDALM.getEmail());
            throw new ClientAlreadyExisistsException("Client with email " + clientDALM.getEmail() + " already exists");
        } catch (ClientNotFoundException e) {
            // Клиент не найден по email - продолжаем
        }

        try {
            // Проверяем существование по username
            findByUsername(clientDALM.getUsername());
            throw new ClientAlreadyExisistsException(
                    "Client with username " + clientDALM.getUsername() + " already exists");
        } catch (ClientNotFoundException e) {
            // Клиент не найден по username - продолжаем
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", clientDALM.getUid() != null ? clientDALM.getUid() : UUID.randomUUID());
        params.addValue("email", clientDALM.getEmail());
        params.addValue("birth_date",
                clientDALM.getBirthDate() != null ? new Date(clientDALM.getBirthDate().getTime()) : null);
        params.addValue("username", clientDALM.getUsername());
        params.addValue("password", clientDALM.getPassword());

        jdbcTemplate.update(INSERT_CLIENT, params);
    }

    @Override
    @Transactional
    public ClientDALM findByUid(UUID uuid) throws ClientNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uuid);
        try {
            return jdbcTemplate.queryForObject(SELECT_CLIENT_BY_UID, params, clientRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException("Client with UID " + uuid + " not found");
        }
    }

    @Override
    @Transactional
    public ClientDALM findByEmail(String emailString) throws ClientNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("email", emailString);
        try {
            return jdbcTemplate.queryForObject(SELECT_CLIENT_BY_EMAIL, params, clientRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException("Client with email " + emailString + " not found");
        }
    }

    @Override
    @Transactional
    public ClientDALM findByUsername(String usernameString) throws ClientNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("username", usernameString);
        try {
            return jdbcTemplate.queryForObject(SELECT_CLIENT_BY_USERNAME, params, clientRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException("Client with username " + usernameString + " not found");
        }
    }

    @Override
    @Transactional
    public ClientDALM findByEmailPassword(String emailString, String passwordString) throws ClientNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("email", emailString);
        params.addValue("password", passwordString);
        try {
            return jdbcTemplate.queryForObject(SELECT_CLIENT_BY_EMAIL_PASSWORD, params, clientRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException("Client with email " + emailString + " and provided password not found");
        }
    }

    @Override
    @Transactional
    public ClientDALM findByUsernamePassword(String usernameString, String passwordString)
            throws ClientNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("username", usernameString);
        params.addValue("password", passwordString);
        try {
            return jdbcTemplate.queryForObject(SELECT_CLIENT_BY_USERNAME_PASSWORD, params, clientRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException(
                    "Client with username " + usernameString + " and provided password not found");
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