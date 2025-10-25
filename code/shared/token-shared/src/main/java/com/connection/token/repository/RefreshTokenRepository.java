package com.connection.token.repository;

import java.util.UUID;

import com.connection.token.exception.RefreshTokenAlreadyExisistsException;
import com.connection.token.exception.RefreshTokenNotFoundException;
import com.connection.token.model.RefreshTokenDALM;

public interface RefreshTokenRepository {
    /**
     * Лобавить новый refreshToken
     * 
     * @param refreshTokenDALM Новый токен
     */
    public void add(RefreshTokenDALM refreshTokenDALM) throws RefreshTokenAlreadyExisistsException;

    /**
     * Обновить токен
     * Создается новый токен взамен старого, старый отзывается
     * 
     * @param refreshTokenDALM Старый токен
     * @return Новый сохраненный токен
     * @throws RefreshTokenNotFoundException Если токен с таким uid не существует
     */
    public void updateToken(RefreshTokenDALM refreshTokenDALM, RefreshTokenDALM newRefreshTokenDALM)
            throws RefreshTokenNotFoundException;

    /**
     * Отозвать токен
     * 
     * @param refreshTokenDALM Токен для отзыва
     * @throws RefreshTokenNotFoundException Если токен с таким uid не существует
     */
    public void revoke(RefreshTokenDALM refreshTokenDALM) throws RefreshTokenNotFoundException;

    /**
     * Отозвать все токены клиента
     * 
     * @param clientDALM Клиент
     */
    public void revokeAll(UUID clientUUID);

    /**
     * Отозвать все закончившиеся токены
     */
    public void cleanUpExpired();
}
