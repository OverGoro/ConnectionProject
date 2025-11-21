package com.connection.device.token.repository;

import com.connection.device.token.converter.DeviceTokenConverter;
import com.connection.device.token.exception.DeviceTokenAlreadyExistsException;
import com.connection.device.token.exception.DeviceTokenNotFoundException;
import com.connection.device.token.generator.DeviceTokenGenerator;
import com.connection.device.token.model.DeviceTokenBlm;
import com.connection.device.token.model.DeviceTokenDalm;
import com.connection.device.token.validator.DeviceTokenValidator;
import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** . */
public class DeviceTokenRepositorySqlImpl implements DeviceTokenRepository {

    private static final String SELECT_DEVICE_TOKEN =
            "SELECT uid, device_uid, token, created_at, expires_at";
    private static final String FROM_DEVICE_TOKEN = " FROM access.device_token";

    private static final String SELECT_TOKEN_BY_UID =
            SELECT_DEVICE_TOKEN + FROM_DEVICE_TOKEN + " WHERE uid = :uid";
    private static final String SELECT_TOKEN_BY_TOKEN =
            SELECT_DEVICE_TOKEN + FROM_DEVICE_TOKEN + " WHERE token = :token";
    private static final String SELECT_TOKEN_BY_DEVICE_UID = SELECT_DEVICE_TOKEN
            + FROM_DEVICE_TOKEN + " WHERE device_uid = :device_uid";

    private static final String INSERT_DEVICE_TOKEN =
            "INSERT INTO access.device_token (uid, device_uid, token, created_at, expires_at) "
                    + "VALUES (:uid, :device_uid, :token, :created_at, :expires_at)";

    // private static final String UPDATE_TOKEN =
    //         "UPDATE access.device_token SET token = :token, expires_at = :expires_at "
    //                 + "WHERE uid = :uid";

    private static final String REVOKE_TOKEN =
            "DELETE FROM access.device_token WHERE uid = :uid";
    private static final String REVOKE_BY_DEVICE_UID =
            "DELETE FROM access.device_token WHERE device_uid = :device_uid";
    private static final String CLEANUP_EXPIRED_TOKENS =
            "DELETE FROM access.device_token WHERE expires_at < NOW()";
    private static final String EXISTS_BY_DEVICE_UID =
            "SELECT COUNT(*) FROM access.device_token "
                    + "WHERE device_uid = :device_uid AND expires_at > NOW()";
    private static final String DEVICE_EXISTS =
            "SELECT COUNT(*) FROM core.device WHERE uid = :device_uid";

    private final DeviceTokenConverter converter;
    private final DeviceTokenValidator validator = new DeviceTokenValidator();
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<DeviceTokenDalm> deviceTokenRowMapper =
            (rs, rowNum) -> {
                DeviceTokenDalm token = new DeviceTokenDalm();
                token.setUid(UUID.fromString(rs.getString("uid")));
                token.setDeviceUid(UUID.fromString(rs.getString("device_uid")));
                token.setToken(rs.getString("token"));
                token.setCreatedAt(rs.getTimestamp("created_at"));
                token.setExpiresAt(rs.getTimestamp("expires_at"));
                return token;
            };

    /** . */
    public DeviceTokenRepositorySqlImpl(NamedParameterJdbcTemplate jdbcTemplate,
            DeviceTokenGenerator generator) {
        this.jdbcTemplate = jdbcTemplate;
        this.converter = new DeviceTokenConverter(generator);
    }

    /** . */
    @Override
    @Transactional
    public void add(DeviceTokenBlm deviceTokenBlm)
            throws DeviceTokenAlreadyExistsException {
        // Валидация Blm модели
        validator.validate(deviceTokenBlm);

        // Проверяем существование device
        if (!deviceExists(deviceTokenBlm.getDeviceUid())) {
            throw new IllegalArgumentException("Device with UID "
                    + deviceTokenBlm.getDeviceUid() + " not found");
        }

        // Проверяем существование по uid
        if (exists(deviceTokenBlm.getUid())) {
            throw new DeviceTokenAlreadyExistsException("Device token with UID "
                    + deviceTokenBlm.getUid() + " already exists");
        }

        // Конвертация Blm в Dalm
        DeviceTokenDalm deviceTokenDalm = converter.toDalm(deviceTokenBlm);

        // Проверяем существование по token
        if (tokenExists(deviceTokenDalm.getToken())) {
            throw new DeviceTokenAlreadyExistsException(
                    "Device token already exists");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", deviceTokenDalm.getUid());
        params.addValue("device_uid", deviceTokenDalm.getDeviceUid());
        params.addValue("token", deviceTokenDalm.getToken());
        params.addValue("created_at",
                new Timestamp(deviceTokenDalm.getCreatedAt().getTime()));
        params.addValue("expires_at",
                new Timestamp(deviceTokenDalm.getExpiresAt().getTime()));

        jdbcTemplate.update(INSERT_DEVICE_TOKEN, params);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceTokenBlm findByUid(UUID uid)
            throws DeviceTokenNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            DeviceTokenDalm dalToken = jdbcTemplate.queryForObject(
                    SELECT_TOKEN_BY_UID, params, deviceTokenRowMapper);
            // Конвертация Dalm в Blm
            return converter.toBlm(dalToken);
        } catch (EmptyResultDataAccessException e) {
            throw new DeviceTokenNotFoundException(
                    "Device token with UID " + uid + " not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceTokenBlm findByToken(String token)
            throws DeviceTokenNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("token", token);
        try {
            DeviceTokenDalm dalToken = jdbcTemplate.queryForObject(
                    SELECT_TOKEN_BY_TOKEN, params, deviceTokenRowMapper);
            // Конвертация Dalm в Blm
            return converter.toBlm(dalToken);
        } catch (EmptyResultDataAccessException e) {
            throw new DeviceTokenNotFoundException("Device token not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceTokenBlm findByDeviceUid(UUID deviceUid)
            throws DeviceTokenNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("device_uid", deviceUid);
        try {
            DeviceTokenDalm dalToken = jdbcTemplate.queryForObject(
                    SELECT_TOKEN_BY_DEVICE_UID, params, deviceTokenRowMapper);
            // Конвертация Dalm в Blm
            return converter.toBlm(dalToken);
        } catch (EmptyResultDataAccessException e) {
            throw new DeviceTokenNotFoundException(
                    "Device token for device UID " + deviceUid + " not found");
        }
    }

    @Override
    @Transactional
    public void revoke(UUID uid) throws DeviceTokenNotFoundException {
        // Проверяем существование токена
        if (!exists(uid)) {
            throw new DeviceTokenNotFoundException(
                    "Device token with UID " + uid + " not found");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);

        jdbcTemplate.update(REVOKE_TOKEN, params);
    }

    @Override
    @Transactional
    public void revokeByDeviceUid(UUID deviceUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("device_uid", deviceUid);

        jdbcTemplate.update(REVOKE_BY_DEVICE_UID, params);
    }

    @Override
    @Transactional
    public void cleanUpExpired() {
        jdbcTemplate.update(CLEANUP_EXPIRED_TOKENS,
                new MapSqlParameterSource());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDeviceUid(UUID deviceUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("device_uid", deviceUid);
        Integer count = jdbcTemplate.queryForObject(EXISTS_BY_DEVICE_UID,
                params, Integer.class);
        return count != null && count > 0;
    }

    // Вспомогательные методы
    @Transactional(readOnly = true)
    boolean deviceExists(UUID deviceUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("device_uid", deviceUid);
        Integer count = jdbcTemplate.queryForObject(DEVICE_EXISTS, params,
                Integer.class);
        return count != null && count > 0;
    }

    @Transactional(readOnly = true)
    boolean exists(UUID uid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            jdbcTemplate.queryForObject(SELECT_TOKEN_BY_UID, params,
                    deviceTokenRowMapper);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Transactional(readOnly = true)
    boolean tokenExists(String token) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("token", token);
        try {
            jdbcTemplate.queryForObject(SELECT_TOKEN_BY_TOKEN, params,
                    deviceTokenRowMapper);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}
