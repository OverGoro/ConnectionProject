// Реализация репозитория SQL
package com.connection.message.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.connection.message.exception.MessageAddException;
import com.connection.message.exception.MessageNotFoundException;
import com.connection.message.model.MessageDALM;

import org.springframework.transaction.annotation.Transactional;

public class MessageRepositorySQLImpl implements MessageRepository {

    private static final String SELECT_MESSAGE = "SELECT uid, buffer_uid, content, content_type, created_at";

    private static final String SELECT_MESSAGE_BY_UID = SELECT_MESSAGE +
            " FROM processing.message WHERE uid = :uid";

    private static final String SELECT_MESSAGES_BY_BUFFER_UID = SELECT_MESSAGE +
            " FROM processing.message WHERE buffer_uid = :buffer_uid ORDER BY created_at DESC";

    private static final String SELECT_MESSAGES_BY_BUFFER_UID_AND_TIME_RANGE = SELECT_MESSAGE +
            " FROM processing.message WHERE buffer_uid = :buffer_uid AND created_at BETWEEN :start_time AND :end_time ORDER BY created_at DESC";

    private static final String INSERT_MESSAGE = "INSERT INTO processing.message (uid, buffer_uid, content, content_type, created_at) " +
            "VALUES (:uid, :buffer_uid, :content, :content_type, :created_at)";

    private static final String DELETE_MESSAGE_BY_UID = "DELETE FROM processing.message WHERE uid = :uid";

    private static final String DELETE_MESSAGES_BY_BUFFER_UID = "DELETE FROM processing.message WHERE buffer_uid = :buffer_uid";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<MessageDALM> messageRowMapper = (rs, rowNum) -> {
        MessageDALM message = new MessageDALM();
        message.setUid(UUID.fromString(rs.getString("uid")));
        message.setBufferUid(UUID.fromString(rs.getString("buffer_uid")));
        message.setContent(rs.getString("content"));
        message.setContentType(rs.getString("content_type"));
        message.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
        return message;
    };

    public MessageRepositorySQLImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void add(MessageDALM messageDALM) throws MessageAddException {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("uid", messageDALM.getUid() != null ? messageDALM.getUid() : UUID.randomUUID());
            params.addValue("buffer_uid", messageDALM.getBufferUid());
            params.addValue("content", messageDALM.getContent());
            params.addValue("content_type", messageDALM.getContentType());
            params.addValue("created_at", 
                messageDALM.getCreatedAt() != null ? new Timestamp(messageDALM.getCreatedAt().getTime()) : new Timestamp(System.currentTimeMillis()));

            jdbcTemplate.update(INSERT_MESSAGE, params);
        } catch (Exception e) {
            throw new MessageAddException("Failed to add message: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public MessageDALM findByUid(UUID uuid) throws MessageNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uuid);
        try {
            return jdbcTemplate.queryForObject(SELECT_MESSAGE_BY_UID, params, messageRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new MessageNotFoundException("Message with UID " + uuid + " not found");
        }
    }

    @Override
    @Transactional
    public List<MessageDALM> findByBufferUid(UUID bufferUid) throws MessageNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        try {
            List<MessageDALM> messages = jdbcTemplate.query(SELECT_MESSAGES_BY_BUFFER_UID, params, messageRowMapper);
            if (messages.isEmpty()) {
                throw new MessageNotFoundException("No messages found for buffer UID " + bufferUid);
            }
            return messages;
        } catch (EmptyResultDataAccessException e) {
            throw new MessageNotFoundException("No messages found for buffer UID " + bufferUid);
        }
    }

    @Override
    @Transactional
    public List<MessageDALM> findByBufferUidAndTimeRange(UUID bufferUid, Date startTime, Date endTime) throws MessageNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        params.addValue("start_time", new Timestamp(startTime.getTime()));
        params.addValue("end_time", new Timestamp(endTime.getTime()));
        
        try {
            List<MessageDALM> messages = jdbcTemplate.query(SELECT_MESSAGES_BY_BUFFER_UID_AND_TIME_RANGE, params, messageRowMapper);
            if (messages.isEmpty()) {
                throw new MessageNotFoundException("No messages found for buffer UID " + bufferUid + " in specified time range");
            }
            return messages;
        } catch (EmptyResultDataAccessException e) {
            throw new MessageNotFoundException("No messages found for buffer UID " + bufferUid + " in specified time range");
        }
    }

    @Override
    @Transactional
    public void deleteByUid(UUID uuid) throws MessageNotFoundException {
        // Проверяем существование сообщения перед удалением
        findByUid(uuid);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uuid);

        jdbcTemplate.update(DELETE_MESSAGE_BY_UID, params);
    }

    @Override
    @Transactional
    public void deleteByBufferUid(UUID bufferUid) throws MessageNotFoundException {
        // Проверяем существование сообщений перед удалением
        findByBufferUid(bufferUid);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);

        jdbcTemplate.update(DELETE_MESSAGES_BY_BUFFER_UID, params);
    }
}