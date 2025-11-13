package com.connection.token.repository;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.Statement;

import com.connection.token.exception.RefreshTokenAlreadyExisistsException;
import com.connection.token.exception.RefreshTokenNotFoundException;
import com.connection.token.model.RefreshTokenDALM;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
public class RefreshTokenReactiveRepositoryImpl implements RefreshTokenRepository {

    private final ConnectionFactory refreshTokenConnectionFactory;

    @Override
    public Mono<Void> add(RefreshTokenDALM refreshTokenDALM) throws RefreshTokenAlreadyExisistsException {
        return Mono.usingWhen(
            refreshTokenConnectionFactory.create(),
            connection -> checkTokenExists(connection, refreshTokenDALM.getToken(), refreshTokenDALM.getUid())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RefreshTokenAlreadyExisistsException("Refresh token already exists"));
                    }
                    return insertToken(connection, refreshTokenDALM);
                }),
            Connection::close
        );
    }

    @Override
    public Mono<Void> updateToken(RefreshTokenDALM oldToken, RefreshTokenDALM newToken) 
            throws RefreshTokenNotFoundException {
        return Mono.usingWhen(
            refreshTokenConnectionFactory.create(),
            connection -> checkTokenExistsByToken(connection, oldToken.getToken())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new RefreshTokenNotFoundException("Refresh token not found"));
                    }
                    return checkTokenExistsByToken(connection, newToken.getToken())
                        .flatMap(newExists -> {
                            if (newExists) {
                                return Mono.error(new RefreshTokenAlreadyExisistsException("Refresh token already exists"));
                            }
                            return updateTokenInDb(connection, oldToken.getToken(), newToken);
                        });
                }),
            Connection::close
        );
    }

    @Override
    public Mono<Void> revoke(RefreshTokenDALM refreshTokenDALM) throws RefreshTokenNotFoundException {
        return Mono.usingWhen(
            refreshTokenConnectionFactory.create(),
            connection -> checkUidExists(connection, refreshTokenDALM.getUid())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new RefreshTokenNotFoundException(
                            "Refresh token with UID " + refreshTokenDALM.getUid() + " not found"));
                    }
                    return revokeToken(connection, refreshTokenDALM.getUid());
                }),
            Connection::close
        );
    }

    @Override
    public Mono<Void> revokeAll(UUID clientUUID) {
        return Mono.usingWhen(
            refreshTokenConnectionFactory.create(),
            connection -> {
                String sql = "DELETE FROM \"access\".refresh_token WHERE client_id = $1";
                return Mono.from(connection.createStatement(sql)
                    .bind("$1", clientUUID)
                    .execute())
                    .then();
            },
            Connection::close
        );
    }

    @Override
    public Mono<Void> cleanUpExpired() {
        return Mono.usingWhen(
            refreshTokenConnectionFactory.create(),
            connection -> {
                String sql = "DELETE FROM \"access\".refresh_token WHERE expires_at < NOW()";
                return Mono.from(connection.createStatement(sql).execute()).then();
            },
            Connection::close
        );
    }

    // Вспомогательные методы
    private Mono<Boolean> checkTokenExists(Connection connection, String token, UUID uid) {
        String sql = "SELECT COUNT(*) as count FROM \"access\".refresh_token WHERE token = $1 OR uid = $2";
        
        return Mono.from(connection.createStatement(sql)
            .bind("$1", token)
            .bind("$2", uid)
            .execute())
            .flatMap(result -> 
                Mono.from(result.map((row, metadata) -> row.get("count", Long.class) > 0))
            )
            .defaultIfEmpty(false);
    }

    private Mono<Boolean> checkTokenExistsByToken(Connection connection, String token) {
        String sql = "SELECT COUNT(*) as count FROM \"access\".refresh_token WHERE token = $1";
        
        return Mono.from(connection.createStatement(sql)
            .bind("$1", token)
            .execute())
            .flatMap(result -> 
                Mono.from(result.map((row, metadata) -> row.get("count", Long.class) > 0))
            )
            .defaultIfEmpty(false);
    }

    private Mono<Boolean> checkUidExists(Connection connection, UUID uid) {
        String sql = "SELECT COUNT(*) as count FROM \"access\".refresh_token WHERE uid = $1";
        
        return Mono.from(connection.createStatement(sql)
            .bind("$1", uid)
            .execute())
            .flatMap(result -> 
                Mono.from(result.map((row, metadata) -> row.get("count", Long.class) > 0))
            )
            .defaultIfEmpty(false);
    }

    private Mono<Void> insertToken(Connection connection, RefreshTokenDALM token) {
        String sql = "INSERT INTO \"access\".refresh_token (uid, client_id, token, created_at, expires_at) " +
                    "VALUES ($1, $2, $3, $4, $5)";
        
        Statement stmt = connection.createStatement(sql)
            .bind("$1", token.getUid())
            .bind("$2", token.getClientUID())
            .bind("$3", token.getToken())
            .bind("$4", toLocalDateTime(token.getCreatedAt()))
            .bind("$5", toLocalDateTime(token.getExpiresAt()));

        return Mono.from(stmt.execute()).then();
    }

    private Mono<Void> updateTokenInDb(Connection connection, String oldToken, RefreshTokenDALM newToken) {
        String sql = "UPDATE \"access\".refresh_token SET token = $1, expires_at = $2, created_at = $3 " +
                    "WHERE token = $4";
        
        Statement stmt = connection.createStatement(sql)
            .bind("$1", newToken.getToken())
            .bind("$2", toLocalDateTime(newToken.getExpiresAt()))
            .bind("$3", toLocalDateTime(newToken.getCreatedAt()))
            .bind("$4", oldToken);

        return Mono.from(stmt.execute()).then();
    }

    private Mono<Void> revokeToken(Connection connection, UUID uid) {
        String sql = "DELETE FROM \"access\".refresh_token WHERE uid = $1";
        return Mono.from(connection.createStatement(sql)
            .bind("$1", uid)
            .execute())
            .then();
    }

    // Дополнительные реактивные методы
    public Mono<RefreshTokenDALM> findByUid(UUID uid) throws RefreshTokenNotFoundException {
        return Mono.usingWhen(
            refreshTokenConnectionFactory.create(),
            connection -> {
                String sql = "SELECT uid, client_id, token, created_at, expires_at " +
                           "FROM \"access\".refresh_token WHERE uid = $1";
                
                return Mono.from(connection.createStatement(sql)
                    .bind("$1", uid)
                    .execute())
                    .flatMap(result -> 
                        Mono.from(result.map((row, metadata) -> mapRowToRefreshTokenDALM(row)))
                    )
                    .switchIfEmpty(Mono.error(new RefreshTokenNotFoundException(
                        "Refresh token with UID " + uid + " not found")));
            },
            Connection::close
        );
    }

    public Mono<RefreshTokenDALM> findByToken(String token) throws RefreshTokenNotFoundException {
        return Mono.usingWhen(
            refreshTokenConnectionFactory.create(),
            connection -> {
                String sql = "SELECT uid, client_id, token, created_at, expires_at " +
                           "FROM \"access\".refresh_token WHERE token = $1";
                
                return Mono.from(connection.createStatement(sql)
                    .bind("$1", token)
                    .execute())
                    .flatMap(result -> 
                        Mono.from(result.map((row, metadata) -> mapRowToRefreshTokenDALM(row)))
                    )
                    .switchIfEmpty(Mono.error(new RefreshTokenNotFoundException("Refresh token not found")));
            },
            Connection::close
        );
    }

    public Mono<Boolean> isTokenValid(String token) {
        return findByToken(token)
            .map(refreshToken -> refreshToken.getExpiresAt().after(new Date()))
            .onErrorReturn(RefreshTokenNotFoundException.class, false);
    }

    // Маппинг Row -> RefreshTokenDALM
    private RefreshTokenDALM mapRowToRefreshTokenDALM(Row row) {
        LocalDateTime createdAtLocal = row.get("created_at", LocalDateTime.class);
        LocalDateTime expiresAtLocal = row.get("expires_at", LocalDateTime.class);

        return RefreshTokenDALM.builder()
            .uid(row.get("uid", UUID.class))
            .clientUID(row.get("client_id", UUID.class))
            .token(row.get("token", String.class))
            .createdAt(toDate(createdAtLocal))
            .expiresAt(toDate(expiresAtLocal))
            .build();
    }

    // Утилиты для конвертации Date <-> LocalDateTime
    private LocalDateTime toLocalDateTime(Date date) {
        return date != null ? 
            date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }

    private Date toDate(LocalDateTime localDateTime) {
        return localDateTime != null ? 
            Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }
}