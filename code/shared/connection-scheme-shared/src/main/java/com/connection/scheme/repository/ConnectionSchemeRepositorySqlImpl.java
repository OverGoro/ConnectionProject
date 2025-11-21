package com.connection.scheme.repository;

import com.connection.scheme.converter.ConnectionSchemeConverter;
import com.connection.scheme.exception.ConnectionSchemeAlreadyExistsException;
import com.connection.scheme.exception.ConnectionSchemeNotFoundException;
import com.connection.scheme.model.ConnectionSchemeBlm;
import com.connection.scheme.model.ConnectionSchemeDalm;
import com.connection.scheme.validator.ConnectionSchemeValidator;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** . */
public class ConnectionSchemeRepositorySqlImpl
        implements ConnectionSchemeRepository {

    // Основные запросы для схем
    private static final String SELECT_SCHEME =
            "SELECT cs.uid, cs.client_uid, cs.scheme_json";
    private static final String FROM_SCHEME =
            " FROM processing.connection_scheme cs";

    private static final String SELECT_SCHEME_BY_UID =
            SELECT_SCHEME + FROM_SCHEME + " WHERE cs.uid = :uid";
    private static final String SELECT_SCHEMES_BY_CLIENT =
            SELECT_SCHEME + FROM_SCHEME + " WHERE cs.client_uid = :client_uid";
    private static final String SELECT_SCHEMES_BY_BUFFER = SELECT_SCHEME
            + FROM_SCHEME
            + " INNER JOIN processing.connection_scheme_buffer csb ON cs.uid = csb.scheme_uid"
            + " WHERE csb.buffer_uid = :buffer_uid";

    // Запросы для получения usedBuffers из связующей таблицы
    private static final String SELECT_USED_BUFFERS =
            "SELECT csb.buffer_uid FROM processing.connection_scheme_buffer csb "
                    + "WHERE csb.scheme_uid = :scheme_uid";

    // Операции со схемами - РАСКОММЕНТИРОВАНО
    private static final String INSERT_SCHEME =
            "INSERT INTO processing.connection_scheme (uid, client_uid, scheme_json) "
                    + "VALUES (:uid, :client_uid, :scheme_json::jsonb)";

    private static final String UPDATE_SCHEME =
            "UPDATE processing.connection_scheme SET scheme_json = :scheme_json::jsonb "
                    + "WHERE uid = :uid";

    private static final String DELETE_SCHEME =
            "DELETE FROM processing.connection_scheme WHERE uid = :uid";

    // Операции со связующей таблицей буферов
    private static final String INSERT_SCHEME_BUFFER =
            "INSERT INTO processing.connection_scheme_buffer (uid, scheme_uid, buffer_uid) "
                    + "VALUES (:uid, :scheme_uid, :buffer_uid)";

    private static final String DELETE_SCHEME_BUFFERS =
            "DELETE FROM processing.connection_scheme_buffer WHERE scheme_uid = :scheme_uid";

    private final ConnectionSchemeConverter converter =
            new ConnectionSchemeConverter();
    private final ConnectionSchemeValidator validator =
            new ConnectionSchemeValidator();
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<ConnectionSchemeDalm> schemeRowMapper =
            (rs, rowNum) -> {
                ConnectionSchemeDalm scheme = new ConnectionSchemeDalm();
                UUID schemeUid = UUID.fromString(rs.getString("uid"));

                scheme.setUid(schemeUid);
                scheme.setClientUid(
                        UUID.fromString(rs.getString("client_uid")));
                scheme.setSchemeJson(rs.getString("scheme_json"));
                // UsedBuffers будут заполняться отдельным запросом
                scheme.setUsedBuffers(getUsedBuffersForScheme(schemeUid));

                return scheme;
            };

    /** . */
    public ConnectionSchemeRepositorySqlImpl(
            NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void add(ConnectionSchemeBlm scheme)
            throws ConnectionSchemeAlreadyExistsException {
        // Валидация Blm модели
        validator.validate(scheme);

        if (exists(scheme.getUid())) {
            throw new ConnectionSchemeAlreadyExistsException(
                    scheme.getUid().toString());
        }

        try {
            // Конвертация Blm в Dalm
            ConnectionSchemeDalm dalScheme = converter.toDalm(scheme);

            // Сохраняем основную схему - РАСКОММЕНТИРОВАНО
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("uid", dalScheme.getUid());
            params.addValue("client_uid", dalScheme.getClientUid());
            params.addValue("scheme_json", dalScheme.getSchemeJson());

            jdbcTemplate.update(INSERT_SCHEME, params);

            // Сохраняем связи с буферами
            if (dalScheme.getUsedBuffers() != null
                    && !dalScheme.getUsedBuffers().isEmpty()) {
                saveSchemeBuffers(dalScheme.getUid(),
                        dalScheme.getUsedBuffers());
            }

        } catch (Exception e) {
            throw new ConnectionSchemeAlreadyExistsException(
                    "Failed to create scheme: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void update(ConnectionSchemeBlm scheme)
            throws ConnectionSchemeNotFoundException {
        // Валидация Blm модели
        validator.validate(scheme);

        if (!exists(scheme.getUid())) {
            throw new ConnectionSchemeNotFoundException(
                    scheme.getUid().toString());
        }

        try {
            // Конвертация Blm в Dalm
            ConnectionSchemeDalm dalScheme = converter.toDalm(scheme);

            // Обновляем основную схему - РАСКОММЕНТИРОВАНО
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("uid", dalScheme.getUid());
            params.addValue("scheme_json", dalScheme.getSchemeJson());

            jdbcTemplate.update(UPDATE_SCHEME, params);

            // Обновляем связи с буферами
            updateSchemeBuffers(dalScheme.getUid(), dalScheme.getUsedBuffers());

        } catch (Exception e) {
            throw new ConnectionSchemeNotFoundException(
                    "Failed to update scheme: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void delete(UUID uid) throws ConnectionSchemeNotFoundException {
        if (!exists(uid)) {
            throw new ConnectionSchemeNotFoundException(
                    "Scheme with UID " + uid + " not found");
        }

        try {
            // Удаляем схему - РАСКОММЕНТИРОВАНО
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("uid", uid);

            jdbcTemplate.update(DELETE_SCHEME, params);

        } catch (Exception e) {
            throw new ConnectionSchemeNotFoundException(
                    "Failed to delete scheme: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ConnectionSchemeBlm findByUid(UUID uid)
            throws ConnectionSchemeNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            ConnectionSchemeDalm dalScheme = jdbcTemplate.queryForObject(
                    SELECT_SCHEME_BY_UID, params, schemeRowMapper);
            // Конвертация Dalm в Blm
            return converter.toBlm(dalScheme);
        } catch (EmptyResultDataAccessException e) {
            throw new ConnectionSchemeNotFoundException(
                    "Scheme with UID " + uid + " not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConnectionSchemeBlm> findByClientUid(UUID clientUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_uid", clientUid);

        List<ConnectionSchemeDalm> dalSchemes = jdbcTemplate
                .query(SELECT_SCHEMES_BY_CLIENT, params, schemeRowMapper);

        // Конвертация списка Dalm в Blm
        return dalSchemes.stream().map(converter::toBlm).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConnectionSchemeBlm> findByBufferUid(UUID bufferUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);

        List<ConnectionSchemeDalm> dalSchemes = jdbcTemplate
                .query(SELECT_SCHEMES_BY_BUFFER, params, schemeRowMapper);

        // Конвертация списка Dalm в Blm
        return dalSchemes.stream().map(converter::toBlm).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID uid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            jdbcTemplate.queryForObject(SELECT_SCHEME_BY_UID, params,
                    schemeRowMapper);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    /**
     * Получает список UID буферов, привязанных к схеме.
     */
    private List<UUID> getUsedBuffersForScheme(UUID schemeUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("scheme_uid", schemeUid);

        List<UUID> buffers = jdbcTemplate.query(SELECT_USED_BUFFERS, params,
                (rs, rowNum) -> UUID.fromString(rs.getString("buffer_uid")));

        return buffers;
    }

    /**
     * Сохраняет связи схемы с буферами.
     */
    private void saveSchemeBuffers(UUID schemeUid, List<UUID> usedBuffers) {
        if (usedBuffers == null || usedBuffers.isEmpty()) {
            return;
        }

        for (UUID bufferUid : usedBuffers) {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("uid", UUID.randomUUID()); // Генерируем новый UUID для связи
            params.addValue("scheme_uid", schemeUid);
            params.addValue("buffer_uid", bufferUid);

            try {
                jdbcTemplate.update(INSERT_SCHEME_BUFFER, params);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save buffer connection",
                        e);
            }
        }
    }

    /**
     * Обновляет связи схемы с буферами.
     */
    private void updateSchemeBuffers(UUID schemeUid, List<UUID> usedBuffers) {
        // Удаляем старые связи - РАСКОММЕНТИРОВАНО
        MapSqlParameterSource deleteParams = new MapSqlParameterSource();
        deleteParams.addValue("scheme_uid", schemeUid);

        jdbcTemplate.update(DELETE_SCHEME_BUFFERS, deleteParams);

        // Сохраняем новые связи
        saveSchemeBuffers(schemeUid, usedBuffers);
    }
}