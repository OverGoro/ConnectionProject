// BufferRepositorySQLImpl.java
package com.connection.processing.buffer.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.connection.processing.buffer.exception.BufferAlreadyExistsException;
import com.connection.processing.buffer.exception.BufferNotFoundException;
import com.connection.processing.buffer.model.BufferDALM;

import java.util.List;
import java.util.UUID;

@Repository
public class BufferRepositorySQLImpl implements BufferRepository {

    private static final String SELECT_BUFFER = "SELECT uid, connection_scheme_uid, max_messages_number, max_message_size, message_prototype";
    private static final String FROM_BUFFER = " FROM processing.buffer";

    private static final String SELECT_BUFFER_BY_UID = SELECT_BUFFER + FROM_BUFFER + " WHERE uid = :uid";
    private static final String SELECT_BUFFERS_BY_SCHEME = SELECT_BUFFER + FROM_BUFFER + " WHERE connection_scheme_uid = :connection_scheme_uid";

    private static final String INSERT_BUFFER = "INSERT INTO processing.buffer (uid, connection_scheme_uid, max_messages_number, max_message_size, message_prototype) " +
            "VALUES (:uid, :connection_scheme_uid, :max_messages_number, :max_message_size, :message_prototype)";

    private static final String UPDATE_BUFFER = "UPDATE processing.buffer SET max_messages_number = :max_messages_number, " +
            "max_message_size = :max_message_size, message_prototype = :message_prototype " +
            "WHERE uid = :uid";

    private static final String DELETE_BUFFER = "DELETE FROM processing.buffer WHERE uid = :uid";
    private static final String DELETE_BUFFERS_BY_SCHEME = "DELETE FROM processing.buffer WHERE connection_scheme_uid = :connection_scheme_uid";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<BufferDALM> bufferRowMapper = (rs, rowNum) -> {
        BufferDALM buffer = new BufferDALM();
        buffer.setUid(UUID.fromString(rs.getString("uid")));
        buffer.setConnectionSchemeUid(UUID.fromString(rs.getString("connection_scheme_uid")));
        buffer.setMaxMessagesNumber(rs.getInt("max_messages_number"));
        buffer.setMaxMessageSize(rs.getInt("max_message_size"));
        buffer.setMessagePrototype(rs.getString("message_prototype"));
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
        params.addValue("max_messages_number", buffer.getMaxMessagesNumber());
        params.addValue("max_message_size", buffer.getMaxMessageSize());
        params.addValue("message_prototype", buffer.getMessagePrototype());

        jdbcTemplate.update(INSERT_BUFFER, params);
    }

    @Override
    @Transactional
    public void update(BufferDALM buffer) throws BufferNotFoundException {
        if (!exists(buffer.getUid())) {
            throw new BufferNotFoundException("Buffer with UID " + buffer.getUid() + " not found");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", buffer.getUid());
        params.addValue("max_messages_number", buffer.getMaxMessagesNumber());
        params.addValue("max_message_size", buffer.getMaxMessageSize());
        params.addValue("message_prototype", buffer.getMessagePrototype());

        jdbcTemplate.update(UPDATE_BUFFER, params);
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
    @Transactional
    public void deleteByConnectionSchemeUid(UUID connectionSchemeUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("connection_scheme_uid", connectionSchemeUid);

        jdbcTemplate.update(DELETE_BUFFERS_BY_SCHEME, params);
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