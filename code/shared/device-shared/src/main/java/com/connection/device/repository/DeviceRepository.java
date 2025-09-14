// DeviceRepository.java
package com.connection.device.repository;

import java.util.List;
import java.util.UUID;

import com.connection.device.exception.DeviceAlreadyExistsException;
import com.connection.device.exception.DeviceNotFoundException;
import com.connection.device.model.DeviceDALM;

public interface DeviceRepository {
    /**
     * Добавить новое устройство
     */
    void add(DeviceDALM device) throws DeviceAlreadyExistsException;

    /**
     * Обновить устройство
     */
    void update(DeviceDALM device) throws DeviceNotFoundException;

    /**
     * Удалить устройство
     */
    void delete(UUID uid) throws DeviceNotFoundException;

    /**
     * Найти устройство по UID
     */
    DeviceDALM findByUid(UUID uid) throws DeviceNotFoundException;

    /**
     * Найти все устройства клиента
     */
    List<DeviceDALM> findByClientUuid(UUID clientUuid);

    /**
     * Проверить существование устройства
     */
    boolean exists(UUID uid);

    /**
     * Проверить существование устройства по имени для клиента
     */
    boolean existsByClientAndName(UUID clientUuid, String deviceName);
}