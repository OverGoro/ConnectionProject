// BufferDeviceRepositorySQLImpl.java
package com.connection.processing.buffer.bufferdevice.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.connection.processing.buffer.bufferdevice.exception.BufferDeviceAlreadyExistsException;
import com.connection.processing.buffer.bufferdevice.exception.BufferDeviceNotFoundException;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDALM;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class BufferDeviceRepositorySQLImpl implements BufferDeviceRepository {

    private static final String SELECT_DEVICE_UIDS = "SELECT device_uid";
    private static final String SELECT_BUFFER_UIDS = "SELECT buffer_uid";
    private static final String FROM_BUFFER_DEVICES = " FROM processing.buffer_devices";

    private static final String SELECT_DEVICES_BY_BUFFER = SELECT_DEVICE_UIDS + FROM_BUFFER_DEVICES + " WHERE buffer_uid = :buffer_uid";
    private static final String SELECT_BUFFERS_BY_DEVICE = SELECT_BUFFER_UIDS + FROM_BUFFER_DEVICES + " WHERE device_uid = :device_uid";
    private static final String SELECT_RELATIONSHIP = "SELECT buffer_uid, device_uid" + FROM_BUFFER_DEVICES + " WHERE buffer_uid = :buffer_uid AND device_uid = :device_uid";

    private static final String INSERT_RELATIONSHIP = "INSERT INTO processing.buffer_devices (buffer_uid, device_uid) " +
            "VALUES (:buffer_uid, :device_uid)";

    private static final String DELETE_RELATIONSHIP = "DELETE FROM processing.buffer_devices WHERE buffer_uid = :buffer_uid AND device_uid = :device_uid";
    private static final String DELETE_ALL_BY_BUFFER = "DELETE FROM processing.buffer_devices WHERE buffer_uid = :buffer_uid";
    private static final String DELETE_ALL_BY_DEVICE = "DELETE FROM processing.buffer_devices WHERE device_uid = :device_uid";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<UUID> uuidRowMapper = (rs, rowNum) -> UUID.fromString(rs.getString(1));

    private final RowMapper<BufferDeviceDALM> bufferDeviceRowMapper = (rs, rowNum) -> {
        BufferDeviceDALM bufferDevice = new BufferDeviceDALM();
        bufferDevice.setBufferUid(UUID.fromString(rs.getString("buffer_uid")));
        bufferDevice.setDeviceUid(UUID.fromString(rs.getString("device_uid")));
        return bufferDevice;
    };

    public BufferDeviceRepositorySQLImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void add(BufferDeviceDALM bufferDevice) throws BufferDeviceAlreadyExistsException {
        if (exists(bufferDevice.getBufferUid(), bufferDevice.getDeviceUid())) {
            throw new BufferDeviceAlreadyExistsException(
                bufferDevice.getBufferUid().toString(),
                bufferDevice.getDeviceUid().toString()
            );
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferDevice.getBufferUid());
        params.addValue("device_uid", bufferDevice.getDeviceUid());

        jdbcTemplate.update(INSERT_RELATIONSHIP, params);
    }

    @Override
    @Transactional
    public void delete(BufferDeviceDALM bufferDevice) throws BufferDeviceNotFoundException {
        if (!exists(bufferDevice.getBufferUid(), bufferDevice.getDeviceUid())) {
            throw new BufferDeviceNotFoundException(
                bufferDevice.getBufferUid().toString(),
                bufferDevice.getDeviceUid().toString()
            );
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferDevice.getBufferUid());
        params.addValue("device_uid", bufferDevice.getDeviceUid());

        jdbcTemplate.update(DELETE_RELATIONSHIP, params);
    }

    @Override
    @Transactional
    public void deleteAllByBufferUid(UUID bufferUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);

        jdbcTemplate.update(DELETE_ALL_BY_BUFFER, params);
    }

    @Override
    @Transactional
    public void deleteAllByDeviceUid(UUID deviceUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("device_uid", deviceUid);

        jdbcTemplate.update(DELETE_ALL_BY_DEVICE, params);
    }

    @Override
    @Transactional
    public void addDevicesToBuffer(UUID bufferUid, List<UUID> deviceUids) throws BufferDeviceAlreadyExistsException {
        for (UUID deviceUid : deviceUids) {
            if (exists(bufferUid, deviceUid)) {
                throw new BufferDeviceAlreadyExistsException(bufferUid.toString(), deviceUid.toString());
            }
        }

        // Пакетная вставка
        List<MapSqlParameterSource> paramsList = deviceUids.stream()
            .map(deviceUid -> {
                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("buffer_uid", bufferUid);
                params.addValue("device_uid", deviceUid);
                return params;
            })
            .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(INSERT_RELATIONSHIP, paramsList.toArray(new MapSqlParameterSource[0]));
    }

    @Override
    @Transactional
    public void addBuffersToDevice(UUID deviceUid, List<UUID> bufferUids) throws BufferDeviceAlreadyExistsException {
        for (UUID bufferUid : bufferUids) {
            if (exists(bufferUid, deviceUid)) {
                throw new BufferDeviceAlreadyExistsException(bufferUid.toString(), deviceUid.toString());
            }
        }

        // Пакетная вставка
        List<MapSqlParameterSource> paramsList = bufferUids.stream()
            .map(bufferUid -> {
                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("buffer_uid", bufferUid);
                params.addValue("device_uid", deviceUid);
                return params;
            })
            .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(INSERT_RELATIONSHIP, paramsList.toArray(new MapSqlParameterSource[0]));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID bufferUid, UUID deviceUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        params.addValue("device_uid", deviceUid);
        try {
            jdbcTemplate.queryForObject(SELECT_RELATIONSHIP, params, bufferDeviceRowMapper);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findDeviceUidsByBufferUid(UUID bufferUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buffer_uid", bufferUid);
        return jdbcTemplate.query(SELECT_DEVICES_BY_BUFFER, params, uuidRowMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findBufferUidsByDeviceUid(UUID deviceUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("device_uid", deviceUid);
        return jdbcTemplate.query(SELECT_BUFFERS_BY_DEVICE, params, uuidRowMapper);
    }
}