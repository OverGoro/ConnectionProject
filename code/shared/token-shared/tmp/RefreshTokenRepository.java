package com.connection.token.repository;

import java.util.UUID;

import com.connection.token.exception.RefreshTokenAlreadyExisistsException;
import com.connection.token.exception.RefreshTokenNotFoundException;
import com.connection.token.model.RefreshTokenDalm;

public interface RefreshTokenRepository {
    /**
     * Лобавить новый refreshToken
     * 
     * @param refreshTokenDalm Новый токен
     */
    public void add(RefreshTokenDalm refreshTokenDalm) throws RefreshTokenAlreadyExisistsException;

    /**
     * Обновить токен
     * Создается новый токен взамен старого, старый отзывается
     * 
     * @param refreshTokenDalm Старый токен
     * @return Новый сохраненный токен
     * @throws RefreshTokenNotFoundException Если токен с таким uid не существует
     */
    public void updateToken(RefreshTokenDalm refreshTokenDalm, RefreshTokenDalm newRefreshTokenDalm)
            throws RefreshTokenNotFoundException;

    /**
     * Отозвать токен
     * 
     * @param refreshTokenDalm Токен для отзыва
     * @throws RefreshTokenNotFoundException Если токен с таким uid не существует
     */
    public void revoke(RefreshTokenDalm refreshTokenDalm) throws RefreshTokenNotFoundException;

    /**
     * Отозвать все токены клиента
     * 
     * @param clientDalm Клиент
     */
    public void revokeAll(UUID clientUUID);

    /**
     * Отозвать все закончившиеся токены
     */
    public void cleanUpExpired();
}
