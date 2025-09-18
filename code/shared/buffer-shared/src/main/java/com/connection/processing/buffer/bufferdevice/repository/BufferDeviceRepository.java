// BufferDeviceRepository.java
package com.connection.processing.buffer.bufferdevice.repository;

import java.util.List;
import java.util.UUID;

import com.connection.processing.buffer.bufferdevice.exception.BufferDeviceAlreadyExistsException;
import com.connection.processing.buffer.bufferdevice.exception.BufferDeviceNotFoundException;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDALM;

public interface BufferDeviceRepository {
    /**
     * Добавить связь буфер-устройство
     */
    void add(BufferDeviceDALM bufferDevice) throws BufferDeviceAlreadyExistsException;

    /**
     * Удалить связь буфер-устройство
     */
    void delete(BufferDeviceDALM bufferDevice) throws BufferDeviceNotFoundException;

    /**
     * Проверить существование связи буфер-устройство
     */
    boolean exists(UUID bufferUid, UUID deviceUid);

    /**
     * Найти все устройства связанные с буфером
     */
    List<UUID> findDeviceUidsByBufferUid(UUID bufferUid);

    /**
     * Найти все буферы связанные с устройством
     */
    List<UUID> findBufferUidsByDeviceUid(UUID deviceUid);

    /**
     * Удалить все связи для буфера
     */
    void deleteAllByBufferUid(UUID bufferUid);

    /**
     * Удалить все связи для устройства
     */
    void deleteAllByDeviceUid(UUID deviceUid);

    /**
     * Добавить несколько связей для буфера
     */
    void addDevicesToBuffer(UUID bufferUid, List<UUID> deviceUids) throws BufferDeviceAlreadyExistsException;

    /**
     * Добавить несколько связей для устройства
     */
    void addBuffersToDevice(UUID deviceUid, List<UUID> bufferUids) throws BufferDeviceAlreadyExistsException;
}