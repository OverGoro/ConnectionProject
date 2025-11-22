package com.connection.processing.buffer.repository;

import com.connection.processing.buffer.converter.BufferConverter;
import com.connection.processing.buffer.exception.BufferAlreadyExistsException;
import com.connection.processing.buffer.exception.BufferNotFoundException;
import com.connection.processing.buffer.model.BufferBlm;
import com.connection.processing.buffer.model.BufferDalm;
import com.connection.processing.buffer.validator.BufferValidator;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** . */
public class BufferRepositorySqlImpl implements BufferRepository {

    // Fixed SQL strings with consistent spacing
    private static final String SELECT_BUFFER =
            "SELECT b.uid, b.device_uid,"
            + " b.max_messages_number, b.max_message_size, b.message_prototype";
    private static final String FROM_BUFFER = " FROM processing.buffer b";

    private static final String SELECT_BUFFER_BY_UID =
            SELECT_BUFFER + FROM_BUFFER + " WHERE b.uid = :uid";
    private static final String SELECT_BUFFERS_BY_DEVICE =
            SELECT_BUFFER + FROM_BUFFER + " WHERE b.device_uid = :device_uid";
    private static final String SELECT_BUFFERS_BY_SCHEME = SELECT_BUFFER
            + FROM_BUFFER
            + " INNER JOIN processing.connection_scheme_buffer csb ON b.uid = csb.buffer_uid"
            + " WHERE csb.scheme_uid = :scheme_uid";

    private static final String INSERT_BUFFER = "INSERT INTO processing.buffer "
            + "(uid, device_uid, max_messages_number, max_message_size, message_prototype) "
            + "VALUES (:uid, :device_uid,"
            + " :max_messages_number, :max_message_size, :message_prototype)";

    private static final String UPDATE_BUFFER =
            "UPDATE processing.buffer SET device_uid = :device_uid, "
                    + "max_messages_number = :max_messages_number,"
                    + " max_message_size = :max_message_size, "
                    + "message_prototype = :message_prototype WHERE uid = :uid";

    private static final String DELETE_BUFFER =
            "DELETE FROM processing.buffer WHERE uid = :uid";
    private static final String DELETE_BUFFERS_BY_DEVICE =
            "DELETE FROM processing.buffer WHERE device_uid = :device_uid";

    // Sql для работы со связями connection_scheme_buffer
    private static final String INSERT_SCHEME_BUFFER_LINK =
            "INSERT INTO processing.connection_scheme_buffer (uid, scheme_uid, buffer_uid) "
                    + "VALUES (:link_uid, :scheme_uid, :buffer_uid)";

    private static final String DELETE_SCHEME_BUFFER_LINKS =
            "DELETE FROM processing.connection_scheme_buffer WHERE buffer_uid = :buffer_uid";

    private static final String SELECT_BUFFER_UIDS_BY_SCHEME =
            "SELECT buffer_uid FROM processing.connection_scheme_buffer"
                    + " WHERE scheme_uid = :scheme_uid";
    private static final String EXISTS_SCHEME_BUFFER_LINK =
            "SELECT COUNT(*) FROM processing.connection_scheme_buffer"
                    + " WHERE scheme_uid = :scheme_uid AND buffer_uid = :buffer_uid";

    private final BufferConverter converter = new BufferConverter();
    private final BufferValidator validator = new BufferValidator();
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<BufferDalm> bufferRowMapper = (rs, rowNum) -> {
        BufferDalm buffer = new BufferDalm();
        buffer.setUid(UUID.fromString(rs.getString("uid")));
        buffer.setDeviceUid(UUID.fromString(rs.getString("device_uid")));
        buffer.setMaxMessagesNumber(rs.getInt("max_messages_number"));
        buffer.setMaxMessageSize(rs.getInt("max_message_size"));
        buffer.setMessagePrototype(rs.getString("message_prototype"));
        return buffer;
    };

    /** . */
    public BufferRepositorySqlImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void add(BufferBlm buffer) throws BufferAlreadyExistsException {
        validator.validate(buffer);

        if (exists(buffer.getUid())) {
            throw new BufferAlreadyExistsException(
                    "Buffer with UID " + buffer.getUid() + " already exists");
        }

        BufferDalm dalBuffer = converter.toDalm(buffer);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", dalBuffer.getUid());
        params.addValue("device_uid", dalBuffer.getDeviceUid());
        params.addValue("max_messages_number",
                dalBuffer.getMaxMessagesNumber());
        params.addValue("max_message_size", dalBuffer.getMaxMessageSize());
        params.addValue("message_prototype", dalBuffer.getMessagePrototype());

        jdbcTemplate.update(INSERT_BUFFER, params);
    }

    @Override
    @Transactional
    public void update(BufferBlm buffer) throws BufferNotFoundException {
        validator.validate(buffer);

        if (!exists(buffer.getUid())) {
            throw new BufferNotFoundException(
                    "Buffer with UID " + buffer.getUid() + " not found");
        }

        BufferDalm dalBuffer = converter.toDalm(buffer);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", dalBuffer.getUid());
        params.addValue("device_uid", dalBuffer.getDeviceUid());
        params.addValue("max_messages_number",
                dalBuffer.getMaxMessagesNumber());
        params.addValue("max_message_size", dalBuffer.getMaxMessageSize());
        params.addValue("message_prototype", dalBuffer.getMessagePrototype());

        jdbcTemplate.update(UPDATE_BUFFER, params);
    }

    @Override
    @Transactional
    public void delete(UUID uid) throws BufferNotFoundException {
        if (!exists(uid)) {
            throw new BufferNotFoundException(
                    "Buffer with UID " + uid + " not found");
        }

        MapSqlParameterSource deleteLinksParams = new MapSqlParameterSource();
        deleteLinksParams.addValue("buffer_uid", uid);
        jdbcTemplate.update(DELETE_SCHEME_BUFFER_LINKS, deleteLinksParams);

        MapSqlParameterSource deleteParams = new MapSqlParameterSource();
        deleteParams.addValue("uid", uid);
        jdbcTemplate.update(DELETE_BUFFER, deleteParams);
    }

    @Override
    @Transactional
    public void deleteByDeviceUid(UUID deviceUid) {
        List<BufferBlm> deviceBuffers = findByDeviceUid(deviceUid);

        for (BufferBlm buffer : deviceBuffers) {
            MapSqlParameterSource deleteLinksParams =
                    new MapSqlParameterSource();
            deleteLinksParams.addValue("buffer_uid", buffer.getUid());
            jdbcTemplate.update(DELETE_SCHEME_BUFFER_LINKS, deleteLinksParams);
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("device_uid", deviceUid);
        jdbcTemplate.update(DELETE_BUFFERS_BY_DEVICE, params);
    }

    @Override
    @Transactional(readOnly = true)
    public BufferBlm findByUid(UUID uid) throws BufferNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            BufferDalm dalBuffer = jdbcTemplate.queryForObject(
                    SELECT_BUFFER_BY_UID, params, bufferRowMapper);
            return converter.toBlm(dalBuffer);
        } catch (EmptyResultDataAccessException e) {
            throw new BufferNotFoundException(
                    "Buffer with UID " + uid + " not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BufferBlm> findByDeviceUid(UUID deviceUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("device_uid", deviceUid);
        List<BufferDalm> dalBuffers = jdbcTemplate
                .query(SELECT_BUFFERS_BY_DEVICE, params, bufferRowMapper);

        return dalBuffers.stream().map(converter::toBlm).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BufferBlm> findByConnectionSchemeUid(UUID connectionSchemeUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("scheme_uid", connectionSchemeUid);
        List<BufferDalm> dalBuffers = jdbcTemplate
                .query(SELECT_BUFFERS_BY_SCHEME, params, bufferRowMapper);

        return dalBuffers.stream().map(converter::toBlm).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID uid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            jdbcTemplate.queryForObject(SELECT_BUFFER_BY_UID, params,
                    bufferRowMapper);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    @Transactional
    public void addBufferToConnectionScheme(UUID bufferUid,
            UUID connectionSchemeUid) {
        if (!exists(bufferUid)) {
            throw new BufferNotFoundException(
                    "Buffer with UID " + bufferUid + " not found");
        }

        if (isBufferLinkedToScheme(bufferUid, connectionSchemeUid)) {
            throw new IllegalArgumentException("Buffer " + bufferUid
                    + " is already linked to scheme " + connectionSchemeUid);
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("link_uid", UUID.randomUUID());
        params.addValue("scheme_uid", connectionSchemeUid);
        params.addValue("buffer_uid", bufferUid);

        jdbcTemplate.update(INSERT_SCHEME_BUFFER_LINK, params);
    }

    @Override
    @Transactional
    public void removeBufferFromConnectionScheme(UUID bufferUid,
            UUID connectionSchemeUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("scheme_uid", connectionSchemeUid);
        params.addValue("buffer_uid", bufferUid);

        String deleteSpecificLink =
                "DELETE FROM processing.connection_scheme_buffer "
                        + "WHERE scheme_uid = :scheme_uid AND buffer_uid = :buffer_uid";
        jdbcTemplate.update(deleteSpecificLink, params);
    }

    /** . */

    @Transactional(readOnly = true)
    public List<UUID> findBufferUidsByConnectionScheme(
            UUID connectionSchemeUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("scheme_uid", connectionSchemeUid);

        return jdbcTemplate.query(SELECT_BUFFER_UIDS_BY_SCHEME, params,
                (rs, rowNum) -> UUID.fromString(rs.getString("buffer_uid")));
    }

    /** . */

    @Transactional(readOnly = true)
    public boolean isBufferLinkedToScheme(UUID bufferUid,
            UUID connectionSchemeUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("scheme_uid", connectionSchemeUid);
        params.addValue("buffer_uid", bufferUid);

        Integer count = jdbcTemplate.queryForObject(EXISTS_SCHEME_BUFFER_LINK,
                params, Integer.class);
        return count != null && count > 0;
    }

    /** . */
    @Transactional(readOnly = true)
    public int countByConnectionScheme(UUID connectionSchemeUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("scheme_uid", connectionSchemeUid);

        String countSql =
                "SELECT COUNT(*) FROM processing.connection_scheme_buffer"
                        + " WHERE scheme_uid = :scheme_uid";
        Integer count =
                jdbcTemplate.queryForObject(countSql, params, Integer.class);
        return count != null ? count : 0;
    }

    /** . */

    @Transactional
    public void clearBufferSchemeLinks(UUID bufferUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        jdbcTemplate.update(DELETE_SCHEME_BUFFER_LINKS, params);
    }
}
