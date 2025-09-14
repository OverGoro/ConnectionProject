// BufferRepositorySQLImpl.java
package com.connection.processing.buffer.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import com.connection.processing.buffer.exception.BufferAlreadyExistsException;
import com.connection.processing.buffer.exception.BufferNotFoundException;
import com.connection.processing.buffer.model.BufferDALM;

@Repository
public class BufferRepositorySQLImpl implements BufferRepository {

    private static final String SELECT_BUFFER = "SELECT uid, connection_scheme_uid";
    private static final String FROM_BUFFER = " FROM processing.buffer";

    private static final String SELECT_BUFFER_BY_UID = SELECT_BUFFER + FROM_BUFFER + " WHERE uid = :uid";
    private static final String SELECT_BUFFERS_BY_SCHEME = SELECT_BUFFER + FROM_BUFFER + " WHERE connection_scheme_uid = :connection_scheme_uid";

    private static final String INSERT_BUFFER = "INSERT INTO processing.buffer (uid, connection_scheme_uid) " +
            "VALUES (:uid, :connection_scheme_uid)";

    private static final String DELETE_BUFFER = "DELETE FROM processing.buffer WHERE uid = :uid";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<BufferDALM> bufferRowMapper = (rs, rowNum) -> {
        BufferDALM buffer = new BufferDALM();
        buffer.setUid(UUID.fromString(rs.getString("uid")));
        buffer.setConnectionSchemeUid(UUID.fromString(rs.getString("connection_scheme_uid")));
        return buffer;
    };

    public BufferRepositorySQLImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void add(BufferDALM buffer) throws BufferAlreadyExistsException {
        if (exists(buffer.getUid())) {
            throw new BufferAlreadyExistsException("Buffer with UID " + buffer.getUid() + " already exists");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", buffer.getUid());
        params.addValue("connection_scheme_uid", buffer.getConnectionSchemeUid());

        jdbcTemplate.update(INSERT_BUFFER, params);
    }

    @Override
    @Transactional
    public void delete(UUID uid) throws BufferNotFoundException {
        if (!exists(uid)) {
            throw new BufferNotFoundException("Buffer with UID " + uid + " not found");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);

        jdbcTemplate.update(DELETE_BUFFER, params);
    }

    @Override
    @Transactional(readOnly = true)
    public BufferDALM findByUid(UUID uid) throws BufferNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            return jdbcTemplate.queryForObject(SELECT_BUFFER_BY_UID, params, bufferRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new BufferNotFoundException("Buffer with UID " + uid + " not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BufferDALM> findByConnectionSchemeUid(UUID connectionSchemeUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("connection_scheme_uid", connectionSchemeUid);
        return jdbcTemplate.query(SELECT_BUFFERS_BY_SCHEME, params, bufferRowMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID uid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            jdbcTemplate.queryForObject(SELECT_BUFFER_BY_UID, params, bufferRowMapper);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}