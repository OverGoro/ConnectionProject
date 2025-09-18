// BufferJsonDataRepository.java
package com.connection.processing.buffer.objects.json.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.connection.processing.buffer.objects.json.exception.BufferJsonDataAlreadyExistsException;
import com.connection.processing.buffer.objects.json.exception.BufferJsonDataNotFoundException;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDALM;

public interface BufferJsonDataRepository {
    /**
     * Добавить новое JSON сообщение в буфер
     */
    void add(BufferJsonDataDALM data) throws BufferJsonDataAlreadyExistsException;

    /**
     * Удалить JSON сообщение
     */
    void delete(UUID uid) throws BufferJsonDataNotFoundException;

    /**
     * Найти JSON сообщение по UID
     */
    BufferJsonDataDALM findByUid(UUID uid) throws BufferJsonDataNotFoundException;

    /**
     * Найти все JSON сообщения буфера
     */
    List<BufferJsonDataDALM> findByBufferUid(UUID bufferUid);

    /**
     * Найти JSON сообщения буфера созданные после указанной даты
     */
    List<BufferJsonDataDALM> findByBufferUidAndCreatedAfter(UUID bufferUid, Instant createdAfter);

    /**
     * Найти JSON сообщения буфера созданные до указанной даты
     */
    List<BufferJsonDataDALM> findByBufferUidAndCreatedBefore(UUID bufferUid, Instant createdBefore);

    /**
     * Найти JSON сообщения буфера в указанном временном диапазоне
     */
    List<BufferJsonDataDALM> findByBufferUidAndCreatedBetween(UUID bufferUid, Instant startDate, Instant endDate);

    /**
     * Найти самое новое JSON сообщение буфера
     */
    BufferJsonDataDALM findNewestByBufferUid(UUID bufferUid) throws BufferJsonDataNotFoundException;

    /**
     * Найти самое старое JSON сообщение буфера
     */
    BufferJsonDataDALM findOldestByBufferUid(UUID bufferUid) throws BufferJsonDataNotFoundException;

    /**
     * Найти N самых новых JSON сообщений буфера
     */
    List<BufferJsonDataDALM> findNewestByBufferUid(UUID bufferUid, int limit);

    /**
     * Найти N самых старых JSON сообщений буфера
     */
    List<BufferJsonDataDALM> findOldestByBufferUid(UUID bufferUid, int limit);

    /**
     * Проверить существование JSON сообщения
     */
    boolean exists(UUID uid);

    /**
     * Удалить все JSON сообщения буфера
     */
    void deleteByBufferUid(UUID bufferUid);

    /**
     * Удалить старые JSON сообщения (до указанной даты)
     */
    void deleteOldData(Instant olderThan);

    /**
     * Получить количество сообщений в буфере
     */
    int countByBufferUid(UUID bufferUid);
}