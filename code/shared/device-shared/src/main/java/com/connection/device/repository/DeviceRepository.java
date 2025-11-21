package com.connection.device.repository;

import com.connection.device.exception.DeviceAlreadyExistsException;
import com.connection.device.exception.DeviceNotFoundException;
import com.connection.device.model.DeviceBlm;
import java.util.List;
import java.util.UUID;

/** . */
public interface DeviceRepository {
    /**
     * Добавить новое устройство.
     */
    void add(DeviceBlm device) throws DeviceAlreadyExistsException;

    /**
     * Обновить устройство.
     */
    void update(DeviceBlm device) throws DeviceNotFoundException;

    /**
     * Удалить устройство.
     */
    void delete(UUID uid) throws DeviceNotFoundException;

    /**
     * Найти устройство по UID.
     */
    DeviceBlm findByUid(UUID uid) throws DeviceNotFoundException;

    /**
     * Найти все устройства клиента.
     */
    List<DeviceBlm> findByClientUuid(UUID clientUuid);

    /**
     * Проверить существование устройства.
     */
    boolean exists(UUID uid);

    /**
     * Проверить существование устройства по имени для клиента.
     */
    boolean existsByClientAndName(UUID clientUuid, String deviceName);
}
