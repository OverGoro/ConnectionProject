package com.service.auth.repository.token.refresh;

import com.service.auth.exception.token.refresh.RefreshTokenNotFoundException;
import com.service.auth.model.ClientDALM;
import com.service.auth.model.RefreshTokenDALM;

public interface RefreshTokenRepository {
    /**
     * Лобавить новый refreshToken
     * 
     * @param clientDALM Клиент
     * @return Новый сохраненный токен
     */
    public RefreshTokenDALM add(ClientDALM clientDALM);

    /**
     * Обновить токен
     * Создается новый токен взамен старого, старый отзывается
     * 
     * @param refreshTokenDALM Старый токен
     * @return Новый сохраненный токен
     * @throws RefreshTokenNotFoundException Если токен с таким uid не существует
     */
    public RefreshTokenDALM refreshToken(RefreshTokenDALM refreshTokenDALM, RefreshTokenDALM newRefreshTokenDALM)
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
    public void revokeAll(ClientDALM clientDALM);

    /**
     * Отозвать все закончившиеся токены
     */
    public void cleanUpExpired();
}
