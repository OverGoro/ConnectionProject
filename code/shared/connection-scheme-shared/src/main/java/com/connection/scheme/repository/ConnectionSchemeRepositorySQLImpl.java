package com.connection.scheme.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.connection.scheme.converter.ConnectionSchemeConverter;
import com.connection.scheme.exception.ConnectionSchemeAlreadyExistsException;
import com.connection.scheme.exception.ConnectionSchemeNotFoundException;
import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDALM;
import com.connection.scheme.validator.ConnectionSchemeValidator;

import java.util.List;
import java.util.UUID;

@Repository
public class ConnectionSchemeRepositorySQLImpl implements ConnectionSchemeRepository {

    // Основные запросы для схем
    private static final String SELECT_SCHEME = "SELECT cs.uid, cs.client_uid, cs.scheme_json";
    private static final String FROM_SCHEME = " FROM processing.connection_scheme cs";

    private static final String SELECT_SCHEME_BY_UID = SELECT_SCHEME + FROM_SCHEME + " WHERE cs.uid = :uid";
    private static final String SELECT_SCHEMES_BY_CLIENT = SELECT_SCHEME + FROM_SCHEME
            + " WHERE cs.client_uid = :client_uid";
    private static final String SELECT_SCHEMES_BY_BUFFER = SELECT_SCHEME +
            FROM_SCHEME +
            " INNER JOIN processing.connection_scheme_buffer csb ON cs.uid = csb.scheme_uid" +
            " WHERE csb.buffer_uid = :buffer_uid";

    // Запросы для получения usedBuffers из связующей таблицы
    private static final String SELECT_USED_BUFFERS = "SELECT csb.buffer_uid FROM processing.connection_scheme_buffer csb "
            +
            "WHERE csb.scheme_uid = :scheme_uid";

    // Операции со схемами - ИСПРАВЛЕНО: добавлено ::jsonb для преобразования типа
    private static final String INSERT_SCHEME = "INSERT INTO processing.connection_scheme (uid, client_uid, scheme_json) "
            +
            "VALUES (:uid, :client_uid, :scheme_json::jsonb)";

    private static final String UPDATE_SCHEME = "UPDATE processing.connection_scheme SET scheme_json = :scheme_json::jsonb "
            +
            "WHERE uid = :uid";

    private static final String DELETE_SCHEME = "DELETE FROM processing.connection_scheme WHERE uid = :uid";

    // Операции со связующей таблицей буферов
    private static final String INSERT_SCHEME_BUFFER = "INSERT INTO processing.connection_scheme_buffer (uid, scheme_uid, buffer_uid) "
            +
            "VALUES (:uid, :scheme_uid, :buffer_uid)";

    private static final String DELETE_SCHEME_BUFFERS = "DELETE FROM processing.connection_scheme_buffer WHERE scheme_uid = :scheme_uid";

    private final ConnectionSchemeConverter converter = new ConnectionSchemeConverter();
    private final ConnectionSchemeValidator validator = new ConnectionSchemeValidator();
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<ConnectionSchemeDALM> schemeRowMapper = (rs, rowNum) -> {
        ConnectionSchemeDALM scheme = new ConnectionSchemeDALM();
        UUID schemeUid = UUID.fromString(rs.getString("uid"));

        scheme.setUid(schemeUid);
        scheme.setClientUid(UUID.fromString(rs.getString("client_uid")));
        scheme.setSchemeJson(rs.getString("scheme_json"));
        // UsedBuffers будут заполняться отдельным запросом
        scheme.setUsedBuffers(getUsedBuffersForScheme(schemeUid));

        return scheme;
    };

    public ConnectionSchemeRepositorySQLImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void add(ConnectionSchemeBLM scheme) throws ConnectionSchemeAlreadyExistsException {
        // Валидация BLM модели
        validator.validate(scheme);
        
        if (exists(scheme.getUid())) {
            throw new ConnectionSchemeAlreadyExistsException(scheme.getUid().toString());
        }

        try {
            // Конвертация BLM в DALM
            ConnectionSchemeDALM dalScheme = converter.toDALM(scheme);
            
            // Сохраняем основную схему
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("uid", dalScheme.getUid());
            params.addValue("client_uid", dalScheme.getClientUid());
            params.addValue("scheme_json", dalScheme.getSchemeJson());

            int rowsAffected = jdbcTemplate.update(INSERT_SCHEME, params);

            // Сохраняем связи с буферами
            if (dalScheme.getUsedBuffers() != null && !dalScheme.getUsedBuffers().isEmpty()) {
                saveSchemeBuffers(dalScheme.getUid(), dalScheme.getUsedBuffers());
            }

        } catch (Exception e) {
            throw new ConnectionSchemeAlreadyExistsException("Failed to create scheme: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void update(ConnectionSchemeBLM scheme) throws ConnectionSchemeNotFoundException {
        // Валидация BLM модели
        validator.validate(scheme);
        
        if (!exists(scheme.getUid())) {
            throw new ConnectionSchemeNotFoundException(scheme.getUid().toString());
        }

        try {
            // Конвертация BLM в DALM
            ConnectionSchemeDALM dalScheme = converter.toDALM(scheme);
            
            // Обновляем основную схему
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("uid", dalScheme.getUid());
            params.addValue("scheme_json", dalScheme.getSchemeJson());

            int rowsAffected = jdbcTemplate.update(UPDATE_SCHEME, params);

            // Обновляем связи с буферами
            updateSchemeBuffers(dalScheme.getUid(), dalScheme.getUsedBuffers());

        } catch (Exception e) {
            throw new ConnectionSchemeNotFoundException("Failed to update scheme: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void delete(UUID uid) throws ConnectionSchemeNotFoundException {
        if (!exists(uid)) {
            throw new ConnectionSchemeNotFoundException("Scheme with UID " + uid + " not found");
        }

        try {
            // Удаляем схему (связи с буферами удалятся каскадом благодаря FOREIGN KEY constraint)
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("uid", uid);

            int rowsAffected = jdbcTemplate.update(DELETE_SCHEME, params);

        } catch (Exception e) {
            throw new ConnectionSchemeNotFoundException("Failed to delete scheme: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ConnectionSchemeBLM findByUid(UUID uid) throws ConnectionSchemeNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            ConnectionSchemeDALM dalScheme = jdbcTemplate.queryForObject(SELECT_SCHEME_BY_UID, params, schemeRowMapper);
            // Конвертация DALM в BLM
            return converter.toBLM(dalScheme);
        } catch (EmptyResultDataAccessException e) {
            throw new ConnectionSchemeNotFoundException("Scheme with UID " + uid + " not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConnectionSchemeBLM> findByClientUid(UUID clientUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_uid", clientUid);

        List<ConnectionSchemeDALM> dalSchemes = jdbcTemplate.query(SELECT_SCHEMES_BY_CLIENT, params, schemeRowMapper);
        
        // Конвертация списка DALM в BLM
        return dalSchemes.stream()
                .map(converter::toBLM)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConnectionSchemeBLM> findByBufferUid(UUID bufferUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);

        List<ConnectionSchemeDALM> dalSchemes = jdbcTemplate.query(SELECT_SCHEMES_BY_BUFFER, params, schemeRowMapper);
        
        // Конвертация списка DALM в BLM
        return dalSchemes.stream()
                .map(converter::toBLM)
                .toList();
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

    /**
     * Получает список UID буферов, привязанных к схеме
     */
    private List<UUID> getUsedBuffersForScheme(UUID schemeUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("scheme_uid", schemeUid);

        List<UUID> buffers = jdbcTemplate.query(SELECT_USED_BUFFERS, params,
                (rs, rowNum) -> UUID.fromString(rs.getString("buffer_uid")));

        return buffers;
    }

    /**
     * Сохраняет связи схемы с буферами
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
                throw new RuntimeException("Failed to save buffer connection", e);
            }
        }
    }

    /**
     * Обновляет связи схемы с буферами
     */
    private void updateSchemeBuffers(UUID schemeUid, List<UUID> usedBuffers) {
        // Удаляем старые связи
        MapSqlParameterSource deleteParams = new MapSqlParameterSource();
        deleteParams.addValue("scheme_uid", schemeUid);

        int deletedRows = jdbcTemplate.update(DELETE_SCHEME_BUFFERS, deleteParams);

        // Сохраняем новые связи
        saveSchemeBuffers(schemeUid, usedBuffers);
    }
}