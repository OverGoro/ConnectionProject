package com.connection.token.repository;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.Statement;

import com.connection.token.exception.RefreshTokenAlreadyExisistsException;
import com.connection.token.exception.RefreshTokenNotFoundException;
import com.connection.token.model.RefreshTokenDALM;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class RefreshTokenReactiveRepositoryImpl implements RefreshTokenRepository {

    private final ConnectionFactory refreshTokenConnectionFactory;

    @Override
    public Mono<Void> add(RefreshTokenDALM refreshTokenDALM) throws RefreshTokenAlreadyExisistsException {
        log.info("Attempting to add new refresh token for client UID: {}", refreshTokenDALM.getClientUID());
        
        return Mono.usingWhen(
            refreshTokenConnectionFactory.create(),
            connection -> {
                log.debug("Starting transaction for token addition");
                return Mono.from(connection.beginTransaction())
                        .then(Mono.defer(() -> {
                            log.trace("Checking if token already exists: {}", refreshTokenDALM.getToken());
                            return checkTokenExists(connection, refreshTokenDALM.getToken(), refreshTokenDALM.getUid());
                        }))
                        .filter(exists -> !exists)
                        .switchIfEmpty(Mono.defer(() -> {
                            log.warn("Refresh token already exists for token: {} or UID: {}", 
                                    refreshTokenDALM.getToken(), refreshTokenDALM.getUid());
                            return Mono.error(new RefreshTokenAlreadyExisistsException("Refresh token already exists"));
                        }))
                        .then(Mono.defer(() -> {
                            log.debug("Proceeding with token insertion");
                            return insertToken(connection, refreshTokenDALM);
                        }))
                        .then(Mono.defer(() -> {
                            log.debug("Committing transaction for token addition");
                            return Mono.from(connection.commitTransaction())
                                    .doOnSuccess(v -> log.info("Transaction committed successfully for token: {}", 
                                            refreshTokenDALM.getToken()));
                        }))
                        .doOnSuccess(v -> log.info("Successfully added refresh token with UID: {} for client: {}", 
                                refreshTokenDALM.getUid(), refreshTokenDALM.getClientUID()))
                        .doOnError(error -> {
                            log.error("Error during token addition for client: {} - {}. Rolling back transaction.", 
                                    refreshTokenDALM.getClientUID(), error.getMessage());
                            Mono.from(connection.rollbackTransaction())
                                    .doOnSuccess(v -> log.debug("Rollback completed for token addition"))
                                    .doOnError(e -> log.error("Error during rollback: {}", e.getMessage()))
                                    .subscribe();
                        });
            },
            connection -> {
                log.trace("Closing connection for token addition");
                return Mono.from(connection.close());
            },
            (connection, error) -> {
                log.error("Async rollback triggered for token addition error: {}", error.getMessage());
                return Mono.from(connection.rollbackTransaction())
                        .then(Mono.from(connection.close()));
            },
            connection -> {
                log.trace("Async close connection for token addition");
                return Mono.from(connection.close());
            }
        );
    }

    @Override
    public Mono<Void> updateToken(RefreshTokenDALM oldToken, RefreshTokenDALM newToken) 
            throws RefreshTokenNotFoundException {
        log.info("Attempting to update token from: {} to: {}", oldToken.getToken(), newToken.getToken());
        
        return Mono.usingWhen(
            refreshTokenConnectionFactory.create(),
            connection -> {
                log.debug("Starting transaction for token update");
                return Mono.from(connection.beginTransaction())
                        .then(Mono.defer(() -> {
                            log.trace("Checking if old token exists: {}", oldToken.getToken());
                            return checkTokenExistsByToken(connection, oldToken.getToken());
                        }))
                        .filter(exists -> exists)
                        .switchIfEmpty(Mono.defer(() -> {
                            log.warn("Old refresh token not found: {}", oldToken.getToken());
                            return Mono.error(new RefreshTokenNotFoundException("Refresh token not found"));
                        }))
                        .then(Mono.defer(() -> {
                            log.trace("Checking if new token already exists: {}", newToken.getToken());
                            return checkTokenExistsByToken(connection, newToken.getToken());
                        }))
                        .filter(newExists -> !newExists)
                        .switchIfEmpty(Mono.defer(() -> {
                            log.warn("New refresh token already exists: {}", newToken.getToken());
                            return Mono.error(new RefreshTokenAlreadyExisistsException("Refresh token already exists"));
                        }))
                        .then(Mono.defer(() -> {
                            log.debug("Proceeding with token update");
                            return updateTokenInDb(connection, oldToken.getToken(), newToken);
                        }))
                        .then(Mono.defer(() -> {
                            log.debug("Committing transaction for token update");
                            return Mono.from(connection.commitTransaction())
                                    .doOnSuccess(v -> log.info("Transaction committed successfully for token update"));
                        }))
                        .doOnSuccess(v -> log.info("Successfully updated token from {} to {}", 
                                oldToken.getToken(), newToken.getToken()))
                        .doOnError(error -> {
                            log.error("Error during token update from {} to {} - {}. Rolling back transaction.", 
                                    oldToken.getToken(), newToken.getToken(), error.getMessage());
                            Mono.from(connection.rollbackTransaction())
                                    .doOnSuccess(v -> log.debug("Rollback completed for token update"))
                                    .doOnError(e -> log.error("Error during rollback: {}", e.getMessage()))
                                    .subscribe();
                        });
            },
            connection -> {
                log.trace("Closing connection for token update");
                return Mono.from(connection.close());
            },
            (connection, error) -> {
                log.error("Async rollback triggered for token update error: {}", error.getMessage());
                return Mono.from(connection.rollbackTransaction())
                        .then(Mono.from(connection.close()));
            },
            connection -> {
                log.trace("Async close connection for token update");
                return Mono.from(connection.close());
            }
        );
    }

    @Override
    public Mono<Void> revoke(RefreshTokenDALM refreshTokenDALM) throws RefreshTokenNotFoundException {
        log.info("Attempting to revoke token with UID: {}", refreshTokenDALM.getUid());
        
        return Mono.usingWhen(
            refreshTokenConnectionFactory.create(),
            connection -> {
                log.debug("Starting transaction for token revocation");
                return Mono.from(connection.beginTransaction())
                        .then(Mono.defer(() -> {
                            log.trace("Checking if UID exists: {}", refreshTokenDALM.getUid());
                            return checkUidExists(connection, refreshTokenDALM.getUid());
                        }))
                        .filter(exists -> exists)
                        .switchIfEmpty(Mono.defer(() -> {
                            log.warn("Refresh token with UID {} not found", refreshTokenDALM.getUid());
                            return Mono.error(new RefreshTokenNotFoundException(
                                "Refresh token with UID " + refreshTokenDALM.getUid() + " not found"));
                        }))
                        .then(Mono.defer(() -> {
                            log.debug("Proceeding with token revocation");
                            return revokeToken(connection, refreshTokenDALM.getUid());
                        }))
                        .then(Mono.defer(() -> {
                            log.debug("Committing transaction for token revocation");
                            return Mono.from(connection.commitTransaction())
                                    .doOnSuccess(v -> log.info("Transaction committed successfully for token revocation"));
                        }))
                        .doOnSuccess(v -> log.info("Successfully revoked token with UID: {}", refreshTokenDALM.getUid()))
                        .doOnError(error -> {
                            log.error("Error during token revocation for UID: {} - {}. Rolling back transaction.", 
                                    refreshTokenDALM.getUid(), error.getMessage());
                            Mono.from(connection.rollbackTransaction())
                                    .doOnSuccess(v -> log.debug("Rollback completed for token revocation"))
                                    .doOnError(e -> log.error("Error during rollback: {}", e.getMessage()))
                                    .subscribe();
                        });
            },
            connection -> {
                log.trace("Closing connection for token revocation");
                return Mono.from(connection.close());
            },
            (connection, error) -> {
                log.error("Async rollback triggered for token revocation error: {}", error.getMessage());
                return Mono.from(connection.rollbackTransaction())
                        .then(Mono.from(connection.close()));
            },
            connection -> {
                log.trace("Async close connection for token revocation");
                return Mono.from(connection.close());
            }
        );
    }

    @Override
    public Mono<Void> revokeAll(UUID clientUUID) {
        log.info("Attempting to revoke all tokens for client: {}", clientUUID);
        
        return Mono.usingWhen(
            refreshTokenConnectionFactory.create(),
            connection -> {
                log.debug("Starting transaction for revoke all tokens");
                return Mono.from(connection.beginTransaction())
                        .then(Mono.defer(() -> {
                            String sql = "DELETE FROM \"access\".refresh_token WHERE client_id = $1";
                            log.trace("Executing revoke all query for client: {}", clientUUID);
                            return Mono.from(connection.createStatement(sql)
                                .bind("$1", clientUUID)
                                .execute());
                        }))
                        .flatMap(result -> Mono.from(result.getRowsUpdated()))
                        .doOnNext(rowsDeleted -> log.debug("Revoked {} tokens for client: {}", rowsDeleted, clientUUID))
                        .then(Mono.defer(() -> {
                            log.debug("Committing transaction for revoke all");
                            return Mono.from(connection.commitTransaction())
                                    .doOnSuccess(v -> log.info("Transaction committed successfully for revoke all"));
                        }))
                        .doOnSuccess(v -> log.info("Successfully revoked all tokens for client: {}", clientUUID))
                        .doOnError(error -> {
                            log.error("Error during revoke all for client: {} - {}. Rolling back transaction.", 
                                    clientUUID, error.getMessage());
                            Mono.from(connection.rollbackTransaction())
                                    .doOnSuccess(v -> log.debug("Rollback completed for revoke all"))
                                    .doOnError(e -> log.error("Error during rollback: {}", e.getMessage()))
                                    .subscribe();
                        });
            },
            connection -> {
                log.trace("Closing connection for revoke all");
                return Mono.from(connection.close());
            },
            (connection, error) -> {
                log.error("Async rollback triggered for revoke all error: {}", error.getMessage());
                return Mono.from(connection.rollbackTransaction())
                        .then(Mono.from(connection.close()));
            },
            connection -> {
                log.trace("Async close connection for revoke all");
                return Mono.from(connection.close());
            }
        );
    }

    @Override
    public Mono<Void> cleanUpExpired() {
        log.info("Attempting to clean up expired tokens");
        
        return Mono.usingWhen(
            refreshTokenConnectionFactory.create(),
            connection -> {
                log.debug("Starting transaction for cleanup expired tokens");
                return Mono.from(connection.beginTransaction())
                        .then(Mono.defer(() -> {
                            String sql = "DELETE FROM \"access\".refresh_token WHERE expires_at < NOW()";
                            log.trace("Executing cleanup expired tokens query");
                            return Mono.from(connection.createStatement(sql).execute());
                        }))
                        .flatMap(result -> Mono.from(result.getRowsUpdated()))
                        .doOnNext(rowsDeleted -> log.info("Cleaned up {} expired tokens", rowsDeleted))
                        .then(Mono.defer(() -> {
                            log.debug("Committing transaction for cleanup");
                            return Mono.from(connection.commitTransaction())
                                    .doOnSuccess(v -> log.info("Transaction committed successfully for cleanup"));
                        }))
                        .doOnSuccess(v -> log.info("Successfully cleaned up expired tokens"))
                        .doOnError(error -> {
                            log.error("Error during cleanup expired tokens - {}. Rolling back transaction.", 
                                    error.getMessage());
                            Mono.from(connection.rollbackTransaction())
                                    .doOnSuccess(v -> log.debug("Rollback completed for cleanup"))
                                    .doOnError(e -> log.error("Error during rollback: {}", e.getMessage()))
                                    .subscribe();
                        });
            },
            connection -> {
                log.trace("Closing connection for cleanup");
                return Mono.from(connection.close());
            },
            (connection, error) -> {
                log.error("Async rollback triggered for cleanup error: {}", error.getMessage());
                return Mono.from(connection.rollbackTransaction())
                        .then(Mono.from(connection.close()));
            },
            connection -> {
                log.trace("Async close connection for cleanup");
                return Mono.from(connection.close());
            }
        );
    }

    // Вспомогательные методы
    private Mono<Boolean> checkTokenExists(Connection connection, String token, UUID uid) {
        log.trace("Checking token existence for token: {} and UID: {}", token, uid);
        
        String sql = "SELECT COUNT(*) as count FROM \"access\".refresh_token WHERE token = $1 OR uid = $2";
        
        return Mono.from(connection.createStatement(sql)
            .bind("$1", token)
            .bind("$2", uid)
            .execute())
            .flatMap(result -> 
                Mono.from(result.map((row, metadata) -> row.get("count", Long.class)))
            )
            .map(count -> count > 0)
            .defaultIfEmpty(false)
            .doOnNext(exists -> log.trace("Token existence check result: {}", exists));
    }

    private Mono<Boolean> checkTokenExistsByToken(Connection connection, String token) {
        log.trace("Checking token existence by token: {}", token);
        
        String sql = "SELECT COUNT(*) as count FROM \"access\".refresh_token WHERE token = $1";
        
        return Mono.from(connection.createStatement(sql)
            .bind("$1", token)
            .execute())
            .flatMap(result -> 
                Mono.from(result.map((row, metadata) -> row.get("count", Long.class)))
            )
            .map(count -> count > 0)
            .defaultIfEmpty(false)
            .doOnNext(exists -> log.trace("Token existence by token check result: {}", exists));
    }

    private Mono<Boolean> checkUidExists(Connection connection, UUID uid) {
        log.trace("Checking token existence by UID: {}", uid);
        
        String sql = "SELECT COUNT(*) as count FROM \"access\".refresh_token WHERE uid = $1";
        
        return Mono.from(connection.createStatement(sql)
            .bind("$1", uid)
            .execute())
            .flatMap(result -> 
                Mono.from(result.map((row, metadata) -> row.get("count", Long.class)))
            )
            .map(count -> count > 0)
            .defaultIfEmpty(false)
            .doOnNext(exists -> log.trace("Token existence by UID check result: {}", exists));
    }

    private Mono<Void> insertToken(Connection connection, RefreshTokenDALM token) {
        log.debug("Inserting new token with UID: {} for client: {}", token.getUid(), token.getClientUID());
        
        String sql = "INSERT INTO \"access\".refresh_token (uid, client_id, token, created_at, expires_at) " +
                    "VALUES ($1, $2, $3, $4, $5)";
        
        Statement stmt = connection.createStatement(sql)
            .bind("$1", token.getUid())
            .bind("$2", token.getClientUID())
            .bind("$3", token.getToken())
            .bind("$4", toLocalDateTime(token.getCreatedAt()))
            .bind("$5", toLocalDateTime(token.getExpiresAt()));

        return Mono.from(stmt.execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                .doOnNext(rowsUpdated -> log.debug("Token insert executed, rows affected: {}", rowsUpdated))
                .then()
                .doOnSuccess(v -> log.info("Token inserted successfully with UID: {}", token.getUid()))
                .doOnError(error -> log.error("Failed to insert token with UID: {} - {}", token.getUid(), error.getMessage()));
    }

    private Mono<Void> updateTokenInDb(Connection connection, String oldToken, RefreshTokenDALM newToken) {
        log.debug("Updating token from: {} to: {}", oldToken, newToken.getToken());
        
        String sql = "UPDATE \"access\".refresh_token SET token = $1, expires_at = $2, created_at = $3 " +
                    "WHERE token = $4";
        
        Statement stmt = connection.createStatement(sql)
            .bind("$1", newToken.getToken())
            .bind("$2", toLocalDateTime(newToken.getExpiresAt()))
            .bind("$3", toLocalDateTime(newToken.getCreatedAt()))
            .bind("$4", oldToken);

        return Mono.from(stmt.execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                .doOnNext(rowsUpdated -> log.debug("Token update executed, rows affected: {}", rowsUpdated))
                .then()
                .doOnSuccess(v -> log.info("Token updated successfully from {} to {}", oldToken, newToken.getToken()))
                .doOnError(error -> log.error("Failed to update token from {} - {}", oldToken, error.getMessage()));
    }

    private Mono<Void> revokeToken(Connection connection, UUID uid) {
        log.debug("Revoking token with UID: {}", uid);
        
        String sql = "DELETE FROM \"access\".refresh_token WHERE uid = $1";
        return Mono.from(connection.createStatement(sql)
            .bind("$1", uid)
            .execute())
            .flatMap(result -> Mono.from(result.getRowsUpdated()))
            .doOnNext(rowsDeleted -> log.debug("Token revocation executed, rows affected: {}", rowsDeleted))
            .then()
            .doOnSuccess(v -> log.info("Token revoked successfully with UID: {}", uid))
            .doOnError(error -> log.error("Failed to revoke token with UID: {} - {}", uid, error.getMessage()));
    }

    // Дополнительные реактивные методы
    public Mono<RefreshTokenDALM> findByUid(UUID uid) throws RefreshTokenNotFoundException {
        log.debug("Searching for token by UID: {}", uid);
        
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
                    .doOnSuccess(token -> log.debug("Found token by UID: {}", uid))
                    .doOnError(error -> log.warn("Token not found by UID: {} - {}", uid, error.getMessage()))
                    .switchIfEmpty(Mono.error(new RefreshTokenNotFoundException(
                        "Refresh token with UID " + uid + " not found")));
            },
            Connection::close
        );
    }

    public Mono<RefreshTokenDALM> findByToken(String token) throws RefreshTokenNotFoundException {
        log.debug("Searching for token by token string: {}", token);
        
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
                    .doOnSuccess(foundToken -> log.debug("Found token by token string: {}", token))
                    .doOnError(error -> log.warn("Token not found by token string: {} - {}", token, error.getMessage()))
                    .switchIfEmpty(Mono.error(new RefreshTokenNotFoundException("Refresh token not found")));
            },
            Connection::close
        );
    }

    public Mono<Boolean> isTokenValid(String token) {
        log.trace("Checking token validity: {}", token);
        
        return findByToken(token)
            .map(refreshToken -> {
                boolean isValid = refreshToken.getExpiresAt().after(new Date());
                log.trace("Token validity check for {}: {}", token, isValid);
                return isValid;
            })
            .onErrorReturn(RefreshTokenNotFoundException.class, false)
            .doOnError(error -> log.warn("Error during token validity check for {}: {}", token, error.getMessage()));
    }

    // Маппинг Row -> RefreshTokenDALM
    private RefreshTokenDALM mapRowToRefreshTokenDALM(Row row) {
        log.trace("Mapping row to RefreshTokenDALM");
        
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

    // Метод для проверки соединения с БД
    public Mono<Boolean> testConnection() {
        return Mono.usingWhen(
                refreshTokenConnectionFactory.create(),
                connection -> {
                    log.info("Testing database connection for tokens");
                    return Mono.from(connection.createStatement("SELECT 1").execute())
                            .flatMap(result -> Mono.from(result.map((row, metadata) -> true)))
                            .doOnSuccess(v -> log.info("Database connection test successful for tokens"))
                            .doOnError(error -> log.error("Database connection test failed for tokens: {}", error.getMessage()));
                },
                Connection::close
        );
    }
}