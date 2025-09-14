package com.connection.processing.buffer.objects.json.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import com.connection.processing.buffer.objects.json.exception.BufferJsonObjectAlreadyExistsException;
import com.connection.processing.buffer.objects.json.exception.BufferJsonObjectNotFoundException;
import com.connection.processing.buffer.objects.json.model.BufferJsonObjectDALM;

@Repository
public class BufferJsonObjectRepositorySQLImpl implements BufferJsonObjectRepository {

    private static final String SELECT_OBJECT = "SELECT uid, buffer_uid, data";
    private static final String FROM_OBJECT = " FROM processing.buffer_json_datas";

    private static final String SELECT_OBJECT_BY_UID = SELECT_OBJECT + FROM_OBJECT + " WHERE uid = :uid";
    private static final String SELECT_OBJECTS_BY_BUFFER = SELECT_OBJECT + FROM_OBJECT + " WHERE buffer_uid = :buffer_uid";

    private static final String INSERT_OBJECT = "INSERT INTO processing.buffer_json_datas (uid, buffer_uid, data) " +
            "VALUES (:uid, :buffer_uid, :data)";

    private static final String DELETE_OBJECT = "DELETE FROM processing.buffer_json_datas WHERE uid = :uid";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<BufferJsonObjectDALM> objectRowMapper = (rs, rowNum) -> {
        BufferJsonObjectDALM object = new BufferJsonObjectDALM();
        object.setUid(UUID.fromString(rs.getString("uid")));
        object.setBufferUid(UUID.fromString(rs.getString("buffer_uid")));
        object.setData(rs.getString("data"));
        return object;
    };

    public BufferJsonObjectRepositorySQLImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void add(BufferJsonObjectDALM object) throws BufferJsonObjectAlreadyExistsException {
        if (exists(object.getUid())) {
            throw new BufferJsonObjectAlreadyExistsException("Object with UID " + object.getUid() + " already exists");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", object.getUid());
        params.addValue("buffer_uid", object.getBufferUid());
        params.addValue("data", object.getData());
        
        jdbcTemplate.update(INSERT_OBJECT, params);
    }

    @Override
    @Transactional
    public void delete(UUID uid) throws BufferJsonObjectNotFoundException {
        if (!exists(uid)) {
            throw new BufferJsonObjectNotFoundException("Object with UID " + uid + " not found");
        }
        
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        
        jdbcTemplate.update(DELETE_OBJECT, params);
    }

    @Override
    @Transactional(readOnly = true)
    public BufferJsonObjectDALM findByUid(UUID uid) throws BufferJsonObjectNotFoundException {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("uid", uid);
            
            return jdbcTemplate.queryForObject(SELECT_OBJECT_BY_UID, params, objectRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new BufferJsonObjectNotFoundException("Object with UID " + uid + " not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BufferJsonObjectDALM> findByBufferUid(UUID bufferUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        
        return jdbcTemplate.query(SELECT_OBJECTS_BY_BUFFER, params, objectRowMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID uid) {
        try {
            findByUid(uid);
            return true;
        } catch (BufferJsonObjectNotFoundException e) {
            return false;
        }
    }
}