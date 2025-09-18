// BufferJsonDataRepositorySQLImpl.java
package com.connection.processing.buffer.objects.json.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.connection.processing.buffer.objects.json.exception.BufferJsonDataAlreadyExistsException;
import com.connection.processing.buffer.objects.json.exception.BufferJsonDataNotFoundException;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDALM;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class BufferJsonDataRepositorySQLImpl implements BufferJsonDataRepository {

    private static final String SELECT_DATA = "SELECT uid, buffer_uid, data, created_at";
    private static final String FROM_DATA = " FROM processing.buffer_json_datas";

    private static final String SELECT_DATA_BY_UID = SELECT_DATA + FROM_DATA + " WHERE uid = :uid";
    private static final String SELECT_DATA_BY_BUFFER = SELECT_DATA + FROM_DATA + " WHERE buffer_uid = :buffer_uid";
    private static final String SELECT_DATA_BY_BUFFER_AFTER = SELECT_DATA + FROM_DATA + " WHERE buffer_uid = :buffer_uid AND created_at > :created_after";
    private static final String SELECT_DATA_BY_BUFFER_BEFORE = SELECT_DATA + FROM_DATA + " WHERE buffer_uid = :buffer_uid AND created_at < :created_before";
    private static final String SELECT_DATA_BY_BUFFER_BETWEEN = SELECT_DATA + FROM_DATA + " WHERE buffer_uid = :buffer_uid AND created_at BETWEEN :start_date AND :end_date";

    private static final String SELECT_NEWEST_BY_BUFFER = SELECT_DATA + FROM_DATA + " WHERE buffer_uid = :buffer_uid ORDER BY created_at DESC LIMIT 1";
    private static final String SELECT_OLDEST_BY_BUFFER = SELECT_DATA + FROM_DATA + " WHERE buffer_uid = :buffer_uid ORDER BY created_at ASC LIMIT 1";
    private static final String SELECT_NEWEST_N_BY_BUFFER = SELECT_DATA + FROM_DATA + " WHERE buffer_uid = :buffer_uid ORDER BY created_at DESC LIMIT :limit";
    private static final String SELECT_OLDEST_N_BY_BUFFER = SELECT_DATA + FROM_DATA + " WHERE buffer_uid = :buffer_uid ORDER BY created_at ASC LIMIT :limit";

    private static final String INSERT_DATA = "INSERT INTO processing.buffer_json_datas (uid, buffer_uid, data, created_at) " +
            "VALUES (:uid, :buffer_uid, :data, :created_at)";

    private static final String DELETE_DATA = "DELETE FROM processing.buffer_json_datas WHERE uid = :uid";
    private static final String DELETE_DATA_BY_BUFFER = "DELETE FROM processing.buffer_json_datas WHERE buffer_uid = :buffer_uid";
    private static final String DELETE_OLD_DATA = "DELETE FROM processing.buffer_json_datas WHERE created_at < :older_than";

    private static final String COUNT_DATA_BY_BUFFER = "SELECT COUNT(*) FROM processing.buffer_json_datas WHERE buffer_uid = :buffer_uid";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<BufferJsonDataDALM> dataRowMapper = (rs, rowNum) -> {
        BufferJsonDataDALM data = new BufferJsonDataDALM();
        data.setUid(UUID.fromString(rs.getString("uid")));
        data.setBufferUid(UUID.fromString(rs.getString("buffer_uid")));
        data.setData(rs.getString("data"));
        data.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        return data;
    };

    public BufferJsonDataRepositorySQLImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void add(BufferJsonDataDALM data) throws BufferJsonDataAlreadyExistsException {
        if (exists(data.getUid())) {
            throw new BufferJsonDataAlreadyExistsException("Buffer JSON data with UID " + data.getUid() + " already exists");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", data.getUid());
        params.addValue("buffer_uid", data.getBufferUid());
        params.addValue("data", data.getData());
        params.addValue("created_at", data.getCreatedAt());

        jdbcTemplate.update(INSERT_DATA, params);
    }

    @Override
    @Transactional
    public void delete(UUID uid) throws BufferJsonDataNotFoundException {
        if (!exists(uid)) {
            throw new BufferJsonDataNotFoundException("Buffer JSON data with UID " + uid + " not found");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);

        jdbcTemplate.update(DELETE_DATA, params);
    }

    @Override
    @Transactional
    public void deleteByBufferUid(UUID bufferUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);

        jdbcTemplate.update(DELETE_DATA_BY_BUFFER, params);
    }

    @Override
    @Transactional
    public void deleteOldData(Instant olderThan) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("older_than", olderThan);

        jdbcTemplate.update(DELETE_OLD_DATA, params);
    }

    @Override
    @Transactional(readOnly = true)
    public BufferJsonDataDALM findByUid(UUID uid) throws BufferJsonDataNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            return jdbcTemplate.queryForObject(SELECT_DATA_BY_UID, params, dataRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new BufferJsonDataNotFoundException("Buffer JSON data with UID " + uid + " not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BufferJsonDataDALM> findByBufferUid(UUID bufferUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        return jdbcTemplate.query(SELECT_DATA_BY_BUFFER, params, dataRowMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BufferJsonDataDALM> findByBufferUidAndCreatedAfter(UUID bufferUid, Instant createdAfter) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        params.addValue("created_after", createdAfter);
        return jdbcTemplate.query(SELECT_DATA_BY_BUFFER_AFTER, params, dataRowMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BufferJsonDataDALM> findByBufferUidAndCreatedBefore(UUID bufferUid, Instant createdBefore) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        params.addValue("created_before", createdBefore);
        return jdbcTemplate.query(SELECT_DATA_BY_BUFFER_BEFORE, params, dataRowMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BufferJsonDataDALM> findByBufferUidAndCreatedBetween(UUID bufferUid, Instant startDate, Instant endDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        params.addValue("start_date", startDate);
        params.addValue("end_date", endDate);
        return jdbcTemplate.query(SELECT_DATA_BY_BUFFER_BETWEEN, params, dataRowMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public BufferJsonDataDALM findNewestByBufferUid(UUID bufferUid) throws BufferJsonDataNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        try {
            return jdbcTemplate.queryForObject(SELECT_NEWEST_BY_BUFFER, params, dataRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new BufferJsonDataNotFoundException("No JSON data found for buffer UID " + bufferUid);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BufferJsonDataDALM findOldestByBufferUid(UUID bufferUid) throws BufferJsonDataNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        try {
            return jdbcTemplate.queryForObject(SELECT_OLDEST_BY_BUFFER, params, dataRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new BufferJsonDataNotFoundException("No JSON data found for buffer UID " + bufferUid);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BufferJsonDataDALM> findNewestByBufferUid(UUID bufferUid, int limit) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        params.addValue("limit", limit);
        return jdbcTemplate.query(SELECT_NEWEST_N_BY_BUFFER, params, dataRowMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BufferJsonDataDALM> findOldestByBufferUid(UUID bufferUid, int limit) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        params.addValue("limit", limit);
        return jdbcTemplate.query(SELECT_OLDEST_N_BY_BUFFER, params, dataRowMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID uid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            jdbcTemplate.queryForObject(SELECT_DATA_BY_UID, params, dataRowMapper);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int countByBufferUid(UUID bufferUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        return jdbcTemplate.queryForObject(COUNT_DATA_BY_BUFFER, params, Integer.class);
    }
}