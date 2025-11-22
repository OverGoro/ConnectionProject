package com.connection.device.token.repository;

import com.connection.device.token.converter.DeviceAccessTokenConverter;
import com.connection.device.token.exception.DeviceAccessTokenExistsException;
import com.connection.device.token.exception.DeviceAccessTokenNotFoundException;
import com.connection.device.token.exception.DeviceTokenNotFoundException;
import com.connection.device.token.generator.DeviceAccessTokenGenerator;
import com.connection.device.token.model.DeviceAccessTokenBlm;
import com.connection.device.token.model.DeviceAccessTokenDalm;
import com.connection.device.token.validator.DeviceAccessTokenValidator;
import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** . */
public class DeviceAccessTokenRepositorySqlImpl
        implements DeviceAccessTokenRepository {

    private static final String SELECT_DEVICE_ACCESS_TOKEN =
            "SELECT uid, device_token_uid, token, created_at, expires_at";
    private static final String FROM_DEVICE_ACCESS_TOKEN =
            " FROM access.device_access_token";

    private static final String SELECT_TOKEN_BY_UID = SELECT_DEVICE_ACCESS_TOKEN
            + FROM_DEVICE_ACCESS_TOKEN + " WHERE uid = :uid";
    private static final String SELECT_TOKEN_BY_TOKEN =
            SELECT_DEVICE_ACCESS_TOKEN + FROM_DEVICE_ACCESS_TOKEN
                    + " WHERE token = :token";
    private static final String SELECT_TOKEN_BY_DEVICE_TOKEN_UID =
            SELECT_DEVICE_ACCESS_TOKEN + FROM_DEVICE_ACCESS_TOKEN
                    + " WHERE device_token_uid = :device_token_uid";

    private static final String INSERT_DEVICE_ACCESS_TOKEN =
            "INSERT INTO access.device_access_token "
                    + "(uid, device_token_uid, token, created_at, expires_at) "
                    + "VALUES (:uid, :device_token_uid, :token, :created_at, :expires_at)";

    private static final String REVOKE_TOKEN =
            "DELETE FROM access.device_access_token WHERE uid = :uid";
    private static final String REVOKE_BY_DEVICE_TOKEN_UID =
            "DELETE FROM access.device_access_token WHERE device_token_uid = :device_token_uid";
    private static final String CLEANUP_EXPIRED_TOKENS =
            "DELETE FROM access.device_access_token WHERE expires_at < NOW()";
    private static final String HAS_ACTIVE_TOKEN =
            "SELECT COUNT(*) FROM access.device_access_token "
                    + "WHERE device_token_uid = :device_token_uid AND expires_at > NOW()";
    private static final String DEVICE_TOKEN_EXISTS =
            "SELECT COUNT(*) FROM access.device_token WHERE uid = :device_token_uid";

    private final DeviceAccessTokenConverter converter;
    private final DeviceAccessTokenValidator validator =
            new DeviceAccessTokenValidator();
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<DeviceAccessTokenDalm> deviceAccessTokenRowMapper =
            (rs, rowNum) -> {
                DeviceAccessTokenDalm token = new DeviceAccessTokenDalm();
                token.setUid(UUID.fromString(rs.getString("uid")));
                token.setDeviceTokenUid(
                        UUID.fromString(rs.getString("device_token_uid")));
                token.setToken(rs.getString("token"));
                token.setCreatedAt(rs.getTimestamp("created_at"));
                token.setExpiresAt(rs.getTimestamp("expires_at"));
                return token;
            };

    /** . */
    public DeviceAccessTokenRepositorySqlImpl(
            NamedParameterJdbcTemplate jdbcTemplate,
            DeviceAccessTokenGenerator generator) {
        this.jdbcTemplate = jdbcTemplate;
        this.converter = new DeviceAccessTokenConverter(generator);
    }

    @Override
    @Transactional
    public void add(DeviceAccessTokenBlm deviceAccessTokenBlm)
            throws DeviceAccessTokenExistsException {
        // Валидация Blm модели
        validator.validate(deviceAccessTokenBlm);

        // Проверяем существование device_token
        if (!deviceTokenExists(deviceAccessTokenBlm.getDeviceTokenUid())) {
            throw new DeviceTokenNotFoundException("Device token with UID "
                    + deviceAccessTokenBlm.getDeviceTokenUid() + " not found");
        }

        // Проверяем, что нет активного токена для этого device_token_uid
        if (hasDeviceAccessToken(deviceAccessTokenBlm.getDeviceTokenUid())) {
            throw new DeviceAccessTokenExistsException(
                    "Device access token already exists for device token UID "
                            + deviceAccessTokenBlm.getDeviceTokenUid());
        }

        // Конвертация Blm в Dalm
        DeviceAccessTokenDalm deviceAccessTokenDalm =
                converter.toDalm(deviceAccessTokenBlm);

        // Проверяем существование по uid
        if (uidExists(deviceAccessTokenDalm.getUid())) {
            throw new DeviceAccessTokenExistsException(
                    "Device access token with UID "
                            + deviceAccessTokenDalm.getUid()
                            + " already exists");
        }

        // Проверяем существование по token
        if (tokenExists(deviceAccessTokenDalm.getToken())) {
            throw new DeviceAccessTokenExistsException(
                    "Device access token already exists");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", deviceAccessTokenDalm.getUid());
        params.addValue("device_token_uid",
                deviceAccessTokenDalm.getDeviceTokenUid());
        params.addValue("token", deviceAccessTokenDalm.getToken());
        params.addValue("created_at",
                new Timestamp(deviceAccessTokenDalm.getCreatedAt().getTime()));
        params.addValue("expires_at",
                new Timestamp(deviceAccessTokenDalm.getExpiresAt().getTime()));

        jdbcTemplate.update(INSERT_DEVICE_ACCESS_TOKEN, params);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceAccessTokenBlm findByUid(UUID uid)
            throws DeviceAccessTokenNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            DeviceAccessTokenDalm dalToken = jdbcTemplate.queryForObject(
                    SELECT_TOKEN_BY_UID, params, deviceAccessTokenRowMapper);
            // Конвертация Dalm в Blm
            return converter.toBlm(dalToken);
        } catch (EmptyResultDataAccessException e) {
            throw new DeviceAccessTokenNotFoundException(
                    "Device access token with UID " + uid + " not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceAccessTokenBlm findByToken(String token)
            throws DeviceAccessTokenNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("token", token);
        try {
            DeviceAccessTokenDalm dalToken = jdbcTemplate.queryForObject(
                    SELECT_TOKEN_BY_TOKEN, params, deviceAccessTokenRowMapper);
            // Конвертация Dalm в Blm
            return converter.toBlm(dalToken);
        } catch (EmptyResultDataAccessException e) {
            throw new DeviceAccessTokenNotFoundException(
                    "Device access token not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceAccessTokenBlm findByDeviceTokenUid(UUID deviceTokenUid)
            throws DeviceAccessTokenNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("device_token_uid", deviceTokenUid);
        try {
            DeviceAccessTokenDalm dalToken = jdbcTemplate.queryForObject(
                    SELECT_TOKEN_BY_DEVICE_TOKEN_UID, params,
                    deviceAccessTokenRowMapper);
            // Конвертация Dalm в Blm
            return converter.toBlm(dalToken);
        } catch (EmptyResultDataAccessException e) {
            throw new DeviceAccessTokenNotFoundException(
                    "Device access token for device token UID " + deviceTokenUid
                            + " not found");
        }
    }

    @Override
    @Transactional
    public void revoke(UUID uid) throws DeviceAccessTokenNotFoundException {
        // Проверяем существование токена
        if (!uidExists(uid)) {
            throw new DeviceAccessTokenNotFoundException(
                    "Device access token with UID " + uid + " not found");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);

        jdbcTemplate.update(REVOKE_TOKEN, params);
    }

    @Override
    @Transactional
    public void revokeByDeviceTokenUid(UUID deviceTokenUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("device_token_uid", deviceTokenUid);

        jdbcTemplate.update(REVOKE_BY_DEVICE_TOKEN_UID, params);
    }

    @Override
    @Transactional
    public void revokeAllExpired() {
        jdbcTemplate.update(CLEANUP_EXPIRED_TOKENS,
                new MapSqlParameterSource());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasDeviceAccessToken(UUID deviceTokenUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("device_token_uid", deviceTokenUid);
        Integer count = jdbcTemplate.queryForObject(HAS_ACTIVE_TOKEN, params,
                Integer.class);
        return count != null && count > 0;
    }

    // Вспомогательные методы
    @Transactional(readOnly = true)
    boolean deviceTokenExists(UUID deviceTokenUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("device_token_uid", deviceTokenUid);
        Integer count = jdbcTemplate.queryForObject(DEVICE_TOKEN_EXISTS, params,
                Integer.class);
        return count != null && count > 0;
    }

    @Transactional(readOnly = true)
    boolean uidExists(UUID uid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            jdbcTemplate.queryForObject(SELECT_TOKEN_BY_UID, params,
                    deviceAccessTokenRowMapper);
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
                    deviceAccessTokenRowMapper);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}
