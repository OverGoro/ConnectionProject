package com.connection.device.token.repository;

import java.util.UUID;

import com.connection.device.token.exception.DeviceTokenAlreadyExistsException;
import com.connection.device.token.exception.DeviceTokenNotFoundException;
import com.connection.device.token.model.DeviceTokenDALM;

public interface DeviceTokenRepository {
    /**
     * Добавить новый device token
     * 
     * @param deviceTokenDALM Новый токен
     * @throws DeviceTokenAlreadyExistsException Если токен уже существует
     */
    void add(DeviceTokenDALM deviceTokenDALM) throws DeviceTokenAlreadyExistsException;

    /**
     * Найти токен по UID
     * 
     * @param uid UID токена
     * @return Найденный токен
     * @throws DeviceTokenNotFoundException Если токен не найден
     */
    DeviceTokenDALM findByUid(UUID uid) throws DeviceTokenNotFoundException;

    /**
     * Найти токен по значению токена
     * 
     * @param token Значение токена
     * @return Найденный токен
     * @throws DeviceTokenNotFoundException Если токен не найден
     */
    DeviceTokenDALM findByToken(String token) throws DeviceTokenNotFoundException;

    /**
     * Найти токен по UID устройства
     * 
     * @param deviceUid UID устройства
     * @return Найденный токен
     * @throws DeviceTokenNotFoundException Если токен не найден
     */
    DeviceTokenDALM findByDeviceUid(UUID deviceUid) throws DeviceTokenNotFoundException;

    /**
     * Отозвать токен по UID
     * 
     * @param uid UID токена
     * @throws DeviceTokenNotFoundException Если токен не найден
     */
    void revoke(UUID uid) throws DeviceTokenNotFoundException;

    /**
     * Отозвать все токены устройства
     * 
     * @param deviceUid UID устройства
     */
    void revokeByDeviceUid(UUID deviceUid);

    /**
     * Очистить истекшие токены
     */
    void cleanUpExpired();

    /**
     * Проверить существование активного токена для устройства
     * 
     * @param deviceUid UID устройства
     * @return true если активный токен существует
     */
    boolean existsByDeviceUid(UUID deviceUid);
}