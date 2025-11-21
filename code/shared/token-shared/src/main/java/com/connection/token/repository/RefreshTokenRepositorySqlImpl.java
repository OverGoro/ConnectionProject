package com.connection.token.repository;

import com.connection.token.exception.RefreshTokenAlreadyExisistsException;
import com.connection.token.exception.RefreshTokenNotFoundException;
import com.connection.token.model.RefreshTokenDalm;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** . */
public class RefreshTokenRepositorySqlImpl implements RefreshTokenRepository {

    private static final String SELECT_REFRESH_TOKEN =
            "SELECT uid, client_id, token, created_at, expires_at";
    private static final String FROM_REFRESH_TOKEN =
            " FROM \"access\".refresh_token";

    private static final String SELECT_TOKEN_BY_UID =
            SELECT_REFRESH_TOKEN + FROM_REFRESH_TOKEN + " WHERE uid = :uid";
    private static final String SELECT_TOKEN_BY_TOKEN =
            SELECT_REFRESH_TOKEN + FROM_REFRESH_TOKEN + " WHERE token = :token";

    private static final String INSERT_REFRESH_TOKEN =
            "INSERT INTO \"access\".refresh_token (uid, client_id, token, created_at, expires_at) "
                    + "VALUES (:uid, :client_id, :token, :created_at, :expires_at)";

    private static final String UPDATE_TOKEN =
            "UPDATE \"access\".refresh_token SET token ="
                    + " :new_token, expires_at = :new_expires_at, created_at = :new_created_at "
                    + "WHERE token = :old_token";

    private static final String REVOKE_TOKEN =
            "DELETE FROM \"access\".refresh_token WHERE uid = :uid";
    private static final String REVOKE_ALL_CLIENT_TOKENS =
            "DELETE FROM \"access\".refresh_token WHERE client_id = :client_id";
    private static final String CLEANUP_EXPIRED_TOKENS =
            "DELETE FROM \"access\".refresh_token WHERE expires_at < NOW()";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<RefreshTokenDalm> refreshTokenRowMapper =
            (rs, rowNum) -> {
                RefreshTokenDalm token = new RefreshTokenDalm();
                token.setUid(UUID.fromString(rs.getString("uid")));
                token.setClientUid(UUID.fromString(rs.getString("client_id")));
                token.setToken(rs.getString("token"));
                token.setCreatedAt(rs.getTimestamp("created_at"));
                token.setExpiresAt(rs.getTimestamp("expires_at"));
                return token;
            };

    /** . */
    public RefreshTokenRepositorySqlImpl(
            NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void add(RefreshTokenDalm refreshTokenDalm)
            throws RefreshTokenAlreadyExisistsException {
        // Проверяем существование по token
        if (tokenExists(refreshTokenDalm.getToken())) {
            throw new RefreshTokenAlreadyExisistsException(
                    "Refresh token already exists");
        }

        // Проверяем существование по uid
        if (uidExists(refreshTokenDalm.getUid())) {
            throw new RefreshTokenAlreadyExisistsException(
                    "Refresh token with UID " + refreshTokenDalm.getUid()
                            + " already exists");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", refreshTokenDalm.getUid());
        params.addValue("client_id", refreshTokenDalm.getClientUid());
        params.addValue("token", refreshTokenDalm.getToken());
        params.addValue("created_at",
                new Timestamp(refreshTokenDalm.getCreatedAt().getTime()));
        params.addValue("expires_at",
                new Timestamp(refreshTokenDalm.getExpiresAt().getTime()));

        jdbcTemplate.update(INSERT_REFRESH_TOKEN, params);
    }

    @Override
    @Transactional
    public void updateToken(RefreshTokenDalm refreshTokenDalm,
            RefreshTokenDalm newRefreshTokenDalm)
            throws RefreshTokenNotFoundException {
        // Проверяем существование старого токена
        if (!tokenExists(refreshTokenDalm.getToken())) {
            throw new RefreshTokenNotFoundException("Refresh token not found");
        }

        // Проверяем что новый токен не существует
        if (tokenExists(newRefreshTokenDalm.getToken())) {
            throw new RefreshTokenAlreadyExisistsException(
                    "Refresh token already exists");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("old_token", refreshTokenDalm.getToken());
        params.addValue("new_token", newRefreshTokenDalm.getToken());
        params.addValue("new_created_at",
                new Timestamp(newRefreshTokenDalm.getCreatedAt().getTime()));
        params.addValue("new_expires_at",
                new Timestamp(newRefreshTokenDalm.getExpiresAt().getTime()));

        jdbcTemplate.update(UPDATE_TOKEN, params);
    }

    @Override
    @Transactional
    public void revoke(RefreshTokenDalm refreshTokenDalm)
            throws RefreshTokenNotFoundException {
        // Проверяем существование токена
        if (!uidExists(refreshTokenDalm.getUid())) {
            throw new RefreshTokenNotFoundException("Refresh token with UID "
                    + refreshTokenDalm.getUid() + " not found");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", refreshTokenDalm.getUid());

        jdbcTemplate.update(REVOKE_TOKEN, params);
    }

    @Override
    @Transactional
    public void revokeAll(UUID clientUuid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_id", clientUuid);

        jdbcTemplate.update(REVOKE_ALL_CLIENT_TOKENS, params);
    }

    @Override
    @Transactional
    public void cleanUpExpired() {
        jdbcTemplate.update(CLEANUP_EXPIRED_TOKENS,
                new MapSqlParameterSource());
    }

    // Вспомогательные методы
    @Transactional(readOnly = true)
    boolean uidExists(UUID uid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            jdbcTemplate.queryForObject(SELECT_TOKEN_BY_UID, params,
                    refreshTokenRowMapper);
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
                    refreshTokenRowMapper);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    // Дополнительные методы для внутреннего использования
    @Transactional(readOnly = true)
    RefreshTokenDalm findByUid(UUID uid) throws RefreshTokenNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            return jdbcTemplate.queryForObject(SELECT_TOKEN_BY_UID, params,
                    refreshTokenRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new RefreshTokenNotFoundException(
                    "Refresh token with UID " + uid + " not found");
        }
    }

    @Transactional(readOnly = true)
    RefreshTokenDalm findByToken(String token)
            throws RefreshTokenNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("token", token);
        try {
            return jdbcTemplate.queryForObject(SELECT_TOKEN_BY_TOKEN, params,
                    refreshTokenRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new RefreshTokenNotFoundException("Refresh token not found");
        }
    }

    @Transactional(readOnly = true)
    boolean isTokenValid(String token) {
        try {
            RefreshTokenDalm refreshToken = findByToken(token);
            return refreshToken.getExpiresAt().after(new Date());
        } catch (RefreshTokenNotFoundException e) {
            return false;
        }
    }
}
