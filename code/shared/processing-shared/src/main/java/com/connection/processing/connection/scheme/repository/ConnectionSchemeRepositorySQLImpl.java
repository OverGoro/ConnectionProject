// ConnectionSchemeRepositorySQLImpl.java
package com.connection.processing.connection.scheme.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import com.connection.processing.connection.scheme.exception.ConnectionSchemeAlreadyExistsException;
import com.connection.processing.connection.scheme.exception.ConnectionSchemeNotFoundException;
import com.connection.processing.connection.scheme.model.ConnectionSchemeDALM;

@Repository
public class ConnectionSchemeRepositorySQLImpl implements ConnectionSchemeRepository {

    private static final String SELECT_SCHEME = "SELECT uid, client_uid, scheme_json";
    private static final String FROM_SCHEME = " FROM processing.connection_scheme";

    private static final String SELECT_SCHEME_BY_UID = SELECT_SCHEME + FROM_SCHEME + " WHERE uid = :uid";
    private static final String SELECT_SCHEMES_BY_CLIENT = SELECT_SCHEME + FROM_SCHEME + " WHERE client_uid = :client_uid";

    private static final String INSERT_SCHEME = "INSERT INTO processing.connection_scheme (uid, client_uid, scheme_json) " +
            "VALUES (:uid, :client_uid, :scheme_json)";

    private static final String UPDATE_SCHEME = "UPDATE processing.connection_scheme SET scheme_json = :scheme_json " +
            "WHERE uid = :uid";

    private static final String DELETE_SCHEME = "DELETE FROM processing.connection_scheme WHERE uid = :uid";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<ConnectionSchemeDALM> schemeRowMapper = (rs, rowNum) -> {
        ConnectionSchemeDALM scheme = new ConnectionSchemeDALM();
        scheme.setUid(UUID.fromString(rs.getString("uid")));
        scheme.setClientUid(UUID.fromString(rs.getString("client_uid")));
        scheme.setSchemeJson(rs.getString("scheme_json"));
        return scheme;
    };

    public ConnectionSchemeRepositorySQLImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void add(ConnectionSchemeDALM scheme) throws ConnectionSchemeAlreadyExistsException {
        if (exists(scheme.getUid())) {
            throw new ConnectionSchemeAlreadyExistsException("Scheme with UID " + scheme.getUid() + " already exists");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", scheme.getUid());
        params.addValue("client_uid", scheme.getClientUid());
        params.addValue("scheme_json", scheme.getSchemeJson());

        jdbcTemplate.update(INSERT_SCHEME, params);
    }

    @Override
    @Transactional
    public void update(ConnectionSchemeDALM scheme) throws ConnectionSchemeNotFoundException {
        if (!exists(scheme.getUid())) {
            throw new ConnectionSchemeNotFoundException("Scheme with UID " + scheme.getUid() + " not found");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", scheme.getUid());
        params.addValue("scheme_json", scheme.getSchemeJson());

        jdbcTemplate.update(UPDATE_SCHEME, params);
    }

    @Override
    @Transactional
    public void delete(UUID uid) throws ConnectionSchemeNotFoundException {
        if (!exists(uid)) {
            throw new ConnectionSchemeNotFoundException("Scheme with UID " + uid + " not found");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);

        jdbcTemplate.update(DELETE_SCHEME, params);
    }

    @Override
    @Transactional(readOnly = true)
    public ConnectionSchemeDALM findByUid(UUID uid) throws ConnectionSchemeNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            return jdbcTemplate.queryForObject(SELECT_SCHEME_BY_UID, params, schemeRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new ConnectionSchemeNotFoundException("Scheme with UID " + uid + " not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConnectionSchemeDALM> findByClientUid(UUID clientUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_uid", clientUid);
        return jdbcTemplate.query(SELECT_SCHEMES_BY_CLIENT, params, schemeRowMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID uid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            jdbcTemplate.queryForObject(SELECT_SCHEME_BY_UID, params, schemeRowMapper);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}