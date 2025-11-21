package com.connection.message.repository;

import com.connection.message.converter.MessageConverter;
import com.connection.message.exception.MessageAddException;
import com.connection.message.exception.MessageNotFoundException;
import com.connection.message.model.MessageBlm;
import com.connection.message.model.MessageDalm;
import com.connection.message.validator.MessageValidator;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** . */
public class MessageRepositorySqlImpl implements MessageRepository {

    private static final String SELECT_MESSAGE =
            "SELECT uid, buffer_uid, content, content_type, created_at";

    private static final String SELECT_MESSAGE_BY_UID =
            SELECT_MESSAGE + " FROM processing.message WHERE uid = :uid";

    private static final String SELECT_MESSAGES_BY_BUFFER_UID = SELECT_MESSAGE
            + " FROM processing.message WHERE buffer_uid = :buffer_uid ORDER BY created_at DESC";

    private static final String SELECT_MESSAGES_BY_BUFFER_UID_AND_TIME_RANGE =
            SELECT_MESSAGE
                    + " FROM processing.message WHERE buffer_uid = :buffer_uid "
                    + "AND created_at BETWEEN :start_time AND :end_time ORDER BY created_at DESC";

    private static final String INSERT_MESSAGE =
            "INSERT INTO processing.message (uid, buffer_uid, content, content_type, created_at) "
                    + "VALUES (:uid, :buffer_uid, :content::jsonb, :content_type, :created_at)";

    private static final String DELETE_MESSAGE_BY_UID =
            "DELETE FROM processing.message WHERE uid = :uid";

    private static final String DELETE_MESSAGES_BY_BUFFER_UID =
            "DELETE FROM processing.message WHERE buffer_uid = :buffer_uid";

    private final MessageConverter converter = new MessageConverter();
    private final MessageValidator validator = new MessageValidator();
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<MessageDalm> messageRowMapper = (rs, rowNum) -> {
        MessageDalm message = new MessageDalm();
        message.setUid(UUID.fromString(rs.getString("uid")));
        message.setBufferUid(UUID.fromString(rs.getString("buffer_uid")));

        // Для чтения JSON из базы
        String contentJson = rs.getString("content");
        message.setContent(contentJson);

        message.setContentType(rs.getString("content_type"));
        message.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
        return message;
    };

    /** . */
    public MessageRepositorySqlImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void add(MessageBlm messageBlm) throws MessageAddException {
        // Валидация Blm модели
        validator.validate(messageBlm);

        // Конвертация Blm в Dalm
        MessageDalm messageDalm = converter.toDalm(messageBlm);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid",
                messageDalm.getUid() != null ? messageDalm.getUid()
                        : UUID.randomUUID());
        params.addValue("buffer_uid", messageDalm.getBufferUid());

        // Важно: content должен быть валидным JSON
        String content = messageDalm.getContent();
        if (content == null) {
            content = "{}"; // пустой JSON объект по умолчанию
        }
        params.addValue("content", content);

        params.addValue("content_type", messageDalm.getContentType());
        params.addValue("created_at",
                messageDalm.getCreatedAt() != null
                        ? new Timestamp(messageDalm.getCreatedAt().getTime())
                        : new Timestamp(System.currentTimeMillis()));

        jdbcTemplate.update(INSERT_MESSAGE, params);
    }

    @Override
    @Transactional
    public MessageBlm findByUid(UUID uuid) throws MessageNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uuid);
        try {
            MessageDalm dalMessage = jdbcTemplate.queryForObject(
                    SELECT_MESSAGE_BY_UID, params, messageRowMapper);
            // Конвертация Dalm в Blm
            return converter.toBlm(dalMessage);
        } catch (EmptyResultDataAccessException e) {
            throw new MessageNotFoundException(
                    "Message with UID " + uuid + " not found");
        }
    }

    @Override
    @Transactional
    public List<MessageBlm> findByBufferUid(UUID bufferUid)
            throws MessageNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        try {
            List<MessageDalm> dalMessages = jdbcTemplate.query(
                    SELECT_MESSAGES_BY_BUFFER_UID, params, messageRowMapper);
            if (dalMessages.isEmpty()) {
                throw new MessageNotFoundException(
                        "No messages found for buffer UID " + bufferUid);
            }
            // Конвертация списка Dalm в Blm
            return dalMessages.stream().map(converter::toBlm).toList();
        } catch (EmptyResultDataAccessException e) {
            throw new MessageNotFoundException(
                    "No messages found for buffer UID " + bufferUid);
        }
    }

    @Override
    @Transactional
    public List<MessageBlm> findByBufferUidAndTimeRange(UUID bufferUid,
            Date startTime, Date endTime) throws MessageNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        params.addValue("start_time", new Timestamp(startTime.getTime()));
        params.addValue("end_time", new Timestamp(endTime.getTime()));

        try {
            List<MessageDalm> dalMessages = jdbcTemplate.query(
                    SELECT_MESSAGES_BY_BUFFER_UID_AND_TIME_RANGE, params,
                    messageRowMapper);
            if (dalMessages.isEmpty()) {
                throw new MessageNotFoundException(
                        "No messages found for buffer UID " + bufferUid
                                + " in specified time range");
            }
            // Конвертация списка Dalm в Blm
            return dalMessages.stream().map(converter::toBlm).toList();
        } catch (EmptyResultDataAccessException e) {
            throw new MessageNotFoundException(
                    "No messages found for buffer UID " + bufferUid
                            + " in specified time range");
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
    public void deleteByBufferUid(UUID bufferUid)
            throws MessageNotFoundException {
        // Проверяем существование сообщений перед удалением
        findByBufferUid(bufferUid);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);

        jdbcTemplate.update(DELETE_MESSAGES_BY_BUFFER_UID, params);
    }
}
