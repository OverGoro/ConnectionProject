package com.connection.token.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import com.connection.token.exception.RefreshTokenAlreadyExisistsException;
import com.connection.token.exception.RefreshTokenNotFoundException;
import com.connection.token.model.RefreshTokenDALM;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class RefreshTokenRepositorySQLImpl implements RefreshTokenRepository {

    private static final String SELECT_REFRESH_TOKEN = "SELECT uid, client_id, token, created_at, expires_at";
    private static final String FROM_REFRESH_TOKEN = " FROM \"access\".refresh_token";

    private static final String SELECT_TOKEN_BY_UID = SELECT_REFRESH_TOKEN + FROM_REFRESH_TOKEN + " WHERE uid = :uid";
    private static final String SELECT_TOKEN_BY_TOKEN = SELECT_REFRESH_TOKEN + FROM_REFRESH_TOKEN + " WHERE token = :token";

    private static final String INSERT_REFRESH_TOKEN = "INSERT INTO \"access\".refresh_token (uid, client_id, token, created_at, expires_at) " +
            "VALUES (:uid, :client_id, :token, :created_at, :expires_at)";

    private static final String UPDATE_TOKEN = "UPDATE \"access\".refresh_token SET token = :new_token, expires_at = :new_expires_at, created_at = :new_created_at " +
            "WHERE token = :old_token";

    private static final String REVOKE_TOKEN = "DELETE FROM \"access\".refresh_token WHERE uid = :uid";
    private static final String REVOKE_ALL_CLIENT_TOKENS = "DELETE FROM \"access\".refresh_token WHERE client_id = :client_id";
    private static final String CLEANUP_EXPIRED_TOKENS = "DELETE FROM \"access\".refresh_token WHERE expires_at < NOW()";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<RefreshTokenDALM> refreshTokenRowMapper = (rs, rowNum) -> {
        RefreshTokenDALM token = new RefreshTokenDALM();
        token.setUid(UUID.fromString(rs.getString("uid")));
        token.setClientUID(UUID.fromString(rs.getString("client_id")));
        token.setToken(rs.getString("token"));
        token.setCreatedAt(rs.getTimestamp("created_at"));
        token.setExpiresAt(rs.getTimestamp("expires_at"));
        return token;
    };

    public RefreshTokenRepositorySQLImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Mono<Void> add(RefreshTokenDALM refreshTokenDALM) throws RefreshTokenAlreadyExisistsException {
        return Mono.fromCallable(() -> {
            // Проверяем существование по token
            if (tokenExistsSync(refreshTokenDALM.getToken())) {
                throw new RefreshTokenAlreadyExisistsException("Refresh token already exists");
            }

            // Проверяем существование по uid
            if (uidExistsSync(refreshTokenDALM.getUid())) {
                throw new RefreshTokenAlreadyExisistsException("Refresh token with UID " + refreshTokenDALM.getUid() + " already exists");
            }

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("uid", refreshTokenDALM.getUid());
            params.addValue("client_id", refreshTokenDALM.getClientUID());
            params.addValue("token", refreshTokenDALM.getToken());
            params.addValue("created_at", new Timestamp(refreshTokenDALM.getCreatedAt().getTime()));
            params.addValue("expires_at", new Timestamp(refreshTokenDALM.getExpiresAt().getTime()));

            jdbcTemplate.update(INSERT_REFRESH_TOKEN, params);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> updateToken(RefreshTokenDALM refreshTokenDALM, RefreshTokenDALM newRefreshTokenDALM) 
            throws RefreshTokenNotFoundException {
        return Mono.fromCallable(() -> {
            // Проверяем существование старого токена
            if (!tokenExistsSync(refreshTokenDALM.getToken())) {
                throw new RefreshTokenNotFoundException("Refresh token not found");
            }

            // Проверяем что новый токен не существует
            if (tokenExistsSync(newRefreshTokenDALM.getToken())) {
                throw new RefreshTokenAlreadyExisistsException("Refresh token already exists");
            }

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("old_token", refreshTokenDALM.getToken());
            params.addValue("new_token", newRefreshTokenDALM.getToken());
            params.addValue("new_created_at", new Timestamp(newRefreshTokenDALM.getCreatedAt().getTime()));
            params.addValue("new_expires_at", new Timestamp(newRefreshTokenDALM.getExpiresAt().getTime()));

            jdbcTemplate.update(UPDATE_TOKEN, params);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> revoke(RefreshTokenDALM refreshTokenDALM) throws RefreshTokenNotFoundException {
        return Mono.fromCallable(() -> {
            // Проверяем существование токена
            if (!uidExistsSync(refreshTokenDALM.getUid())) {
                throw new RefreshTokenNotFoundException("Refresh token with UID " + refreshTokenDALM.getUid() + " not found");
            }

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("uid", refreshTokenDALM.getUid());

            jdbcTemplate.update(REVOKE_TOKEN, params);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> revokeAll(UUID clientUUID) {
        return Mono.fromCallable(() -> {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("client_id", clientUUID);

            jdbcTemplate.update(REVOKE_ALL_CLIENT_TOKENS, params);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> cleanUpExpired() {
        return Mono.fromCallable(() -> {
            jdbcTemplate.update(CLEANUP_EXPIRED_TOKENS, new MapSqlParameterSource());
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    // Синхронные вспомогательные методы для внутреннего использования
    private boolean uidExistsSync(UUID uid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            jdbcTemplate.queryForObject(SELECT_TOKEN_BY_UID, params, refreshTokenRowMapper);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private boolean tokenExistsSync(String token) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("token", token);
        try {
            jdbcTemplate.queryForObject(SELECT_TOKEN_BY_TOKEN, params, refreshTokenRowMapper);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    // Дополнительные методы для внутреннего использования (синхронные)
    private RefreshTokenDALM findByUidSync(UUID uid) throws RefreshTokenNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            return jdbcTemplate.queryForObject(SELECT_TOKEN_BY_UID, params, refreshTokenRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new RefreshTokenNotFoundException("Refresh token with UID " + uid + " not found");
        }
    }

    private RefreshTokenDALM findByTokenSync(String token) throws RefreshTokenNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("token", token);
        try {
            return jdbcTemplate.queryForObject(SELECT_TOKEN_BY_TOKEN, params, refreshTokenRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new RefreshTokenNotFoundException("Refresh token not found");
        }
    }

    private boolean isTokenValidSync(String token) {
        try {
            RefreshTokenDALM refreshToken = findByTokenSync(token);
            return refreshToken.getExpiresAt().after(new Date());
        } catch (RefreshTokenNotFoundException e) {
            return false;
        }
    }

    // Реактивные версии дополнительных методов (если нужны)
    public Mono<RefreshTokenDALM> findByUid(UUID uid) throws RefreshTokenNotFoundException {
        return Mono.fromCallable(() -> findByUidSync(uid))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<RefreshTokenDALM> findByToken(String token) throws RefreshTokenNotFoundException {
        return Mono.fromCallable(() -> findByTokenSync(token))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Boolean> isTokenValid(String token) {
        return Mono.fromCallable(() -> isTokenValidSync(token))
                .subscribeOn(Schedulers.boundedElastic());
    }
}