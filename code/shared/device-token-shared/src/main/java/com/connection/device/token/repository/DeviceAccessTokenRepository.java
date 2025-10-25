package com.connection.device.token.repository;

import java.util.UUID;

import com.connection.device.token.exception.DeviceAccessTokenExistsException;
import com.connection.device.token.exception.DeviceAccessTokenNotFoundException;
import com.connection.device.token.model.DeviceAccessTokenBLM;

public interface DeviceAccessTokenRepository {
    /**
     * Добавить новый device access token
     * 
     * @param deviceAccessTokenBLM Новый токен
     * @throws DeviceAccessTokenExistsException Если активный токен уже существует
     */
    void add(DeviceAccessTokenBLM deviceAccessTokenBLM) throws DeviceAccessTokenExistsException;

    /**
     * Найти токен по UID
     * 
     * @param uid UID токена
     * @return Найденный токен
     * @throws DeviceAccessTokenNotFoundException Если токен не найден
     */
    DeviceAccessTokenBLM findByUid(UUID uid) throws DeviceAccessTokenNotFoundException;

    /**
     * Найти токен по значению токена
     * 
     * @param token Значение токена
     * @return Найденный токен
     * @throws DeviceAccessTokenNotFoundException Если токен не найден
     */
    DeviceAccessTokenBLM findByToken(String token) throws DeviceAccessTokenNotFoundException;

    /**
     * Найти токен по UID device token
     * 
     * @param deviceTokenUid UID device token
     * @return Найденный токен
     * @throws DeviceAccessTokenNotFoundException Если токен не найден
     */
    DeviceAccessTokenBLM findByDeviceTokenUid(UUID deviceTokenUid) throws DeviceAccessTokenNotFoundException;

    /**
     * Отозвать токен по UID
     * 
     * @param uid UID токена
     * @throws DeviceAccessTokenNotFoundException Если токен не найден
     */
    void revoke(UUID uid) throws DeviceAccessTokenNotFoundException;

    /**
     * Отозвать все токены по UID device token
     * 
     * @param deviceTokenUid UID device token
     */
    void revokeByDeviceTokenUid(UUID deviceTokenUid);

    /**
     * Отозвать все истекшие токены
     */
    void revokeAllExpired();

    /**
     * Проверить наличие активного токена для device token
     * 
     * @param deviceTokenUid UID device token
     * @return true если активный токен существует
     */
    boolean hasDeviceAccessToken(UUID deviceTokenUid);
}