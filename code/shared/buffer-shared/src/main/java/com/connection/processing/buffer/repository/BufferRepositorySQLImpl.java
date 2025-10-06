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

    private static final String SELECT_BUFFER = "SELECT b.uid, b.device_uid, b.max_messages_number, b.max_message_size, b.message_prototype";
    private static final String FROM_BUFFER = " FROM processing.buffer b";

    private static final String SELECT_BUFFER_BY_UID = SELECT_BUFFER + FROM_BUFFER + " WHERE b.uid = :uid";
    private static final String SELECT_BUFFERS_BY_DEVICE = SELECT_BUFFER + FROM_BUFFER
            + " WHERE b.device_uid = :device_uid";
    private static final String SELECT_BUFFERS_BY_SCHEME = SELECT_BUFFER +
            FROM_BUFFER +
            " INNER JOIN processing.connection_scheme_buffer csb ON b.uid = csb.buffer_uid" +
            " WHERE csb.scheme_uid = :scheme_uid";

    private static final String INSERT_BUFFER = "INSERT INTO processing.buffer (uid, device_uid, max_messages_number, max_message_size, message_prototype) "
            +
            "VALUES (:uid, :device_uid, :max_messages_number, :max_message_size, :message_prototype)";

    private static final String UPDATE_BUFFER = "UPDATE processing.buffer SET device_uid = :device_uid, " +
            "max_messages_number = :max_messages_number, max_message_size = :max_message_size, " +
            "message_prototype = :message_prototype WHERE uid = :uid";

    private static final String DELETE_BUFFER = "DELETE FROM processing.buffer WHERE uid = :uid";
    private static final String DELETE_BUFFERS_BY_DEVICE = "DELETE FROM processing.buffer WHERE device_uid = :device_uid";

    // SQL для работы со связями connection_scheme_buffer
    private static final String INSERT_SCHEME_BUFFER_LINK = "INSERT INTO processing.connection_scheme_buffer (uid, scheme_uid, buffer_uid) "
            +
            "VALUES (:link_uid, :scheme_uid, :buffer_uid)";

    private static final String DELETE_SCHEME_BUFFER_LINKS = "DELETE FROM processing.connection_scheme_buffer WHERE buffer_uid = :buffer_uid";
    private static final String DELETE_SCHEME_BUFFER_LINKS_BY_SCHEME = "DELETE FROM processing.connection_scheme_buffer WHERE scheme_uid = :scheme_uid";
    private static final String DELETE_SPECIFIC_SCHEME_BUFFER_LINK = "DELETE FROM processing.connection_scheme_buffer WHERE scheme_uid = :scheme_uid AND buffer_uid = :buffer_uid";

    private static final String SELECT_BUFFER_UIDS_BY_SCHEME = "SELECT buffer_uid FROM processing.connection_scheme_buffer WHERE scheme_uid = :scheme_uid";
    private static final String EXISTS_SCHEME_BUFFER_LINK = "SELECT COUNT(*) FROM processing.connection_scheme_buffer WHERE scheme_uid = :scheme_uid AND buffer_uid = :buffer_uid";
    private static final String EXISTS_BY_DEVICE_AND_NAME = "SELECT COUNT(*) FROM processing.buffer WHERE device_uid = :device_uid AND message_prototype = :message_prototype";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<BufferDALM> bufferRowMapper = (rs, rowNum) -> {
        BufferDALM buffer = new BufferDALM();
        buffer.setUid(UUID.fromString(rs.getString("uid")));
        buffer.setDeviceUid(UUID.fromString(rs.getString("device_uid")));
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
        params.addValue("device_uid", buffer.getDeviceUid());
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
        params.addValue("device_uid", buffer.getDeviceUid());
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

        // Сначала удаляем связи со схемами соединений
        MapSqlParameterSource deleteLinksParams = new MapSqlParameterSource();
        deleteLinksParams.addValue("buffer_uid", uid);
        jdbcTemplate.update(DELETE_SCHEME_BUFFER_LINKS, deleteLinksParams);

        // Затем удаляем сам буфер
        MapSqlParameterSource deleteParams = new MapSqlParameterSource();
        deleteParams.addValue("uid", uid);
        jdbcTemplate.update(DELETE_BUFFER, deleteParams);
    }

    @Override
    @Transactional
    public void deleteByDeviceUid(UUID deviceUid) {
        // Сначала находим все буферы устройства
        List<BufferDALM> deviceBuffers = findByDeviceUid(deviceUid);

        // Удаляем связи этих буферов со схемами
        for (BufferDALM buffer : deviceBuffers) {
            MapSqlParameterSource deleteLinksParams = new MapSqlParameterSource();
            deleteLinksParams.addValue("buffer_uid", buffer.getUid());
            jdbcTemplate.update(DELETE_SCHEME_BUFFER_LINKS, deleteLinksParams);
        }

        // Затем удаляем сами буферы
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("device_uid", deviceUid);
        jdbcTemplate.update(DELETE_BUFFERS_BY_DEVICE, params);
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
    public List<BufferDALM> findByDeviceUid(UUID deviceUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("device_uid", deviceUid);
        return jdbcTemplate.query(SELECT_BUFFERS_BY_DEVICE, params, bufferRowMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BufferDALM> findByConnectionSchemeUid(UUID connectionSchemeUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("scheme_uid", connectionSchemeUid);
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

    
    /**
     * Привязать конкретный буфер к схеме соединений
     */
    @Override
    @Transactional
    public void addBufferToConnectionScheme(UUID bufferUid, UUID connectionSchemeUid) {
        // Проверяем существование буфера
        if (!exists(bufferUid)) {
            throw new BufferNotFoundException("Buffer with UID " + bufferUid + " not found");
        }

        // Проверяем, не привязан ли уже этот буфер к схеме
        if (isBufferLinkedToScheme(bufferUid, connectionSchemeUid)) {
            throw new IllegalArgumentException(
                    "Buffer " + bufferUid + " is already linked to scheme " + connectionSchemeUid);
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("link_uid", UUID.randomUUID());
        params.addValue("scheme_uid", connectionSchemeUid);
        params.addValue("buffer_uid", bufferUid);

        jdbcTemplate.update(INSERT_SCHEME_BUFFER_LINK, params);
    }

    /**
     * Отвязать конкретный буфер от схемы соединений
     */
    @Override
    @Transactional
    public void removeBufferFromConnectionScheme(UUID bufferUid, UUID connectionSchemeUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("scheme_uid", connectionSchemeUid);
        params.addValue("buffer_uid", bufferUid);

        String deleteSpecificLink = "DELETE FROM processing.connection_scheme_buffer WHERE scheme_uid = :scheme_uid AND buffer_uid = :buffer_uid";
        jdbcTemplate.update(deleteSpecificLink, params);
    }

    /**
     * Получить все UID буферов, привязанных к схеме соединений
     */
    @Transactional(readOnly = true)
    public List<UUID> findBufferUidsByConnectionScheme(UUID connectionSchemeUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("scheme_uid", connectionSchemeUid);

        return jdbcTemplate.query(SELECT_BUFFER_UIDS_BY_SCHEME, params,
                (rs, rowNum) -> UUID.fromString(rs.getString("buffer_uid")));
    }

    /**
     * Проверить, привязан ли буфер к схеме соединений
     */
    @Transactional(readOnly = true)
    public boolean isBufferLinkedToScheme(UUID bufferUid, UUID connectionSchemeUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("scheme_uid", connectionSchemeUid);
        params.addValue("buffer_uid", bufferUid);

        Integer count = jdbcTemplate.queryForObject(EXISTS_SCHEME_BUFFER_LINK, params, Integer.class);
        return count != null && count > 0;
    }

    /**
     * Получить количество буферов, привязанных к схеме соединений
     */
    @Transactional(readOnly = true)
    public int countByConnectionScheme(UUID connectionSchemeUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("scheme_uid", connectionSchemeUid);

        String countSql = "SELECT COUNT(*) FROM processing.connection_scheme_buffer WHERE scheme_uid = :scheme_uid";
        Integer count = jdbcTemplate.queryForObject(countSql, params, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Очистить все связи буфера со схемами соединений
     */
    @Transactional
    public void clearBufferSchemeLinks(UUID bufferUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        jdbcTemplate.update(DELETE_SCHEME_BUFFER_LINKS, params);
    }
}