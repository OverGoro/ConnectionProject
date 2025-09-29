// ConnectionSchemeRepositorySQLImpl.java
package com.connection.scheme.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.connection.scheme.exception.ConnectionSchemeAlreadyExistsException;
import com.connection.scheme.exception.ConnectionSchemeNotFoundException;
import com.connection.scheme.model.ConnectionSchemeDALM;

import java.util.List;
import java.util.UUID;

@Repository
public class ConnectionSchemeRepositorySQLImpl implements ConnectionSchemeRepository {

    private static final String SELECT_SCHEME = "SELECT cs.uid, cs.client_uid, cs.scheme_json";
    private static final String FROM_SCHEME = " FROM processing.connection_scheme cs";

    private static final String SELECT_SCHEME_BY_UID = SELECT_SCHEME + FROM_SCHEME + " WHERE cs.uid = :uid";
    private static final String SELECT_SCHEMES_BY_CLIENT = SELECT_SCHEME + FROM_SCHEME
            + " WHERE cs.client_uid = :client_uid";

    private static final String SELECT_USED_BUFFERS = "SELECT buffer_uid FROM processing.connection_scheme_buffer WHERE scheme_uid = :scheme_uid";

    private static final String SELECT_SCHEMES_BY_BUFFER = SELECT_SCHEME +
            " FROM processing.connection_scheme cs" +
            " JOIN processing.connection_scheme_buffer csb ON cs.uid = csb.scheme_uid" +
            " WHERE csb.buffer_uid = :buffer_uid";

    private static final String INSERT_SCHEME = "INSERT INTO processing.connection_scheme (uid, client_uid, scheme_json) "
            +
            "VALUES (:uid, :client_uid, :scheme_json)";

    private static final String INSERT_SCHEME_BUFFER = "INSERT INTO processing.connection_scheme_buffer (uid, scheme_uid, buffer_uid) "
            +
            "VALUES (:buffer_relation_uid, :scheme_uid, :buffer_uid)";

    private static final String UPDATE_SCHEME = "UPDATE processing.connection_scheme SET scheme_json = :scheme_json " +
            "WHERE uid = :uid";

    private static final String DELETE_SCHEME_BUFFERS = "DELETE FROM processing.connection_scheme_buffer WHERE scheme_uid = :scheme_uid";

    private static final String DELETE_SCHEME = "DELETE FROM processing.connection_scheme WHERE uid = :uid";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<ConnectionSchemeDALM> schemeRowMapper = (rs, rowNum) -> {
        ConnectionSchemeDALM scheme = new ConnectionSchemeDALM();
        UUID schemeUid = UUID.fromString(rs.getString("uid"));

        scheme.setUid(schemeUid);
        scheme.setClientUid(UUID.fromString(rs.getString("client_uid")));
        scheme.setSchemeJson(rs.getString("scheme_json"));
        scheme.setUsedBuffers(getUsedBuffersForScheme(schemeUid));

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

        // Сохраняем основную схему
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", scheme.getUid());
        params.addValue("client_uid", scheme.getClientUid());
        params.addValue("scheme_json", scheme.getSchemeJson());

        jdbcTemplate.update(INSERT_SCHEME, params);

        // Сохраняем связи с буферами
        saveUsedBuffers(scheme.getUid(), scheme.getUsedBuffers());
    }

    @Override
    @Transactional
    public void update(ConnectionSchemeDALM scheme) throws ConnectionSchemeNotFoundException {
        if (!exists(scheme.getUid())) {
            throw new ConnectionSchemeNotFoundException("Scheme with UID " + scheme.getUid() + " not found");
        }

        // Обновляем основную схему
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", scheme.getUid());
        params.addValue("scheme_json", scheme.getSchemeJson());

        jdbcTemplate.update(UPDATE_SCHEME, params);

        // Обновляем связи с буферами
        updateUsedBuffers(scheme.getUid(), scheme.getUsedBuffers());
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
    public List<ConnectionSchemeDALM> findByBufferUid(UUID bufferUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        return jdbcTemplate.query(SELECT_SCHEMES_BY_BUFFER, params, schemeRowMapper);
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

    private List<UUID> getUsedBuffersForScheme(UUID schemeUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("scheme_uid", schemeUid);

        return jdbcTemplate.query(SELECT_USED_BUFFERS, params,
                (rs, rowNum) -> UUID.fromString(rs.getString("buffer_uid")));
    }

    private void saveUsedBuffers(UUID schemeUid, List<UUID> usedBuffers) {
        if (usedBuffers == null || usedBuffers.isEmpty()) {
            return;
        }

        for (UUID bufferUid : usedBuffers) {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("buffer_relation_uid", UUID.randomUUID());
            params.addValue("scheme_uid", schemeUid);
            params.addValue("buffer_uid", bufferUid);

            jdbcTemplate.update(INSERT_SCHEME_BUFFER, params);
        }
    }

    private void updateUsedBuffers(UUID schemeUid, List<UUID> usedBuffers) {
        // Удаляем старые связи
        MapSqlParameterSource deleteParams = new MapSqlParameterSource();
        deleteParams.addValue("scheme_uid", schemeUid);
        jdbcTemplate.update(DELETE_SCHEME_BUFFERS, deleteParams);

        // Сохраняем новые связи
        saveUsedBuffers(schemeUid, usedBuffers);
    }
}