package com.connection.token.repository;

import java.util.UUID;

import com.connection.token.exception.RefreshTokenAlreadyExisistsException;
import com.connection.token.exception.RefreshTokenNotFoundException;
import com.connection.token.model.RefreshTokenDALM;

import reactor.core.publisher.Mono;

public interface RefreshTokenRepository {
    /**
     * Добавить новый refreshToken
     */
    Mono<Void> add(RefreshTokenDALM refreshTokenDALM) throws RefreshTokenAlreadyExisistsException;

    /**
     * Обновить токен
     */
    Mono<Void> updateToken(RefreshTokenDALM refreshTokenDALM, RefreshTokenDALM newRefreshTokenDALM)
            throws RefreshTokenNotFoundException;

    /**
     * Отозвать токен
     */
    Mono<Void> revoke(RefreshTokenDALM refreshTokenDALM) throws RefreshTokenNotFoundException;

    /**
     * Отозвать все токены клиента
     */
    Mono<Void> revokeAll(UUID clientUUID);

    /**
     * Отозвать все закончившиеся токены
     */
    Mono<Void> cleanUpExpired();
}