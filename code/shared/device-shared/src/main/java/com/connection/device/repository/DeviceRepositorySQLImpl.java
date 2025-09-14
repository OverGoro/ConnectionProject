// DeviceRepositorySQLImpl.java
package com.connection.device.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import com.connection.device.exception.DeviceAlreadyExistsException;
import com.connection.device.exception.DeviceNotFoundException;
import com.connection.device.model.DeviceDALM;

@Repository
public class DeviceRepositorySQLImpl implements DeviceRepository {

    private static final String SELECT_DEVICE = "SELECT uid, client_uuid, device_name, device_description";
    private static final String FROM_DEVICE = " FROM core.device";

    private static final String SELECT_DEVICE_BY_UID = SELECT_DEVICE + FROM_DEVICE + " WHERE uid = :uid";
    private static final String SELECT_DEVICES_BY_CLIENT = SELECT_DEVICE + FROM_DEVICE + " WHERE client_uuid = :client_uuid";
    private static final String SELECT_DEVICE_BY_CLIENT_AND_NAME = SELECT_DEVICE + FROM_DEVICE + " WHERE client_uuid = :client_uuid AND device_name = :device_name";

    private static final String INSERT_DEVICE = "INSERT INTO core.device (uid, client_uuid, device_name, device_description) " +
            "VALUES (:uid, :client_uuid, :device_name, :device_description)";

    private static final String UPDATE_DEVICE = "UPDATE core.device SET device_name = :device_name, device_description = :device_description " +
            "WHERE uid = :uid";

    private static final String DELETE_DEVICE = "DELETE FROM core.device WHERE uid = :uid";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<DeviceDALM> deviceRowMapper = (rs, rowNum) -> {
        DeviceDALM device = new DeviceDALM();
        device.setUid(UUID.fromString(rs.getString("uid")));
        device.setClientUuid(UUID.fromString(rs.getString("client_uuid")));
        device.setDeviceName(rs.getString("device_name"));
        device.setDeviceDescription(rs.getString("device_description"));
        return device;
    };

    public DeviceRepositorySQLImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void add(DeviceDALM device) throws DeviceAlreadyExistsException {
        if (exists(device.getUid())) {
            throw new DeviceAlreadyExistsException("Device with UID " + device.getUid() + " already exists");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", device.getUid());
        params.addValue("client_uuid", device.getClientUuid());
        params.addValue("device_name", device.getDeviceName());
        params.addValue("device_description", device.getDeviceDescription());

        jdbcTemplate.update(INSERT_DEVICE, params);
    }

    @Override
    @Transactional
    public void update(DeviceDALM device) throws DeviceNotFoundException {
        if (!exists(device.getUid())) {
            throw new DeviceNotFoundException("Device with UID " + device.getUid() + " not found");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", device.getUid());
        params.addValue("device_name", device.getDeviceName());
        params.addValue("device_description", device.getDeviceDescription());

        jdbcTemplate.update(UPDATE_DEVICE, params);
    }

    @Override
    @Transactional
    public void delete(UUID uid) throws DeviceNotFoundException {
        if (!exists(uid)) {
            throw new DeviceNotFoundException("Device with UID " + uid + " not found");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);

        jdbcTemplate.update(DELETE_DEVICE, params);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceDALM findByUid(UUID uid) throws DeviceNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            return jdbcTemplate.queryForObject(SELECT_DEVICE_BY_UID, params, deviceRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new DeviceNotFoundException("Device with UID " + uid + " not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceDALM> findByClientUuid(UUID clientUuid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_uuid", clientUuid);
        return jdbcTemplate.query(SELECT_DEVICES_BY_CLIENT, params, deviceRowMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID uid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            jdbcTemplate.queryForObject(SELECT_DEVICE_BY_UID, params, deviceRowMapper);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByClientAndName(UUID clientUuid, String deviceName) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_uuid", clientUuid);
        params.addValue("device_name", deviceName);
        try {
            jdbcTemplate.queryForObject(SELECT_DEVICE_BY_CLIENT_AND_NAME, params, deviceRowMapper);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}