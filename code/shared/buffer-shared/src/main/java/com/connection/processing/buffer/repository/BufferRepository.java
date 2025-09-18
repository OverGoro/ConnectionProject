// BufferRepository.java
package com.connection.processing.buffer.repository;

import java.util.List;
import java.util.UUID;

import com.connection.processing.buffer.exception.BufferAlreadyExistsException;
import com.connection.processing.buffer.exception.BufferNotFoundException;
import com.connection.processing.buffer.model.BufferDALM;

public interface BufferRepository {
    /**
     * Добавить новый буфер
     */
    void add(BufferDALM buffer) throws BufferAlreadyExistsException;

    /**
     * Обновить буфер
     */
    void update(BufferDALM buffer) throws BufferNotFoundException;

    /**
     * Удалить буфер
     */
    void delete(UUID uid) throws BufferNotFoundException;

    /**
     * Найти буфер по UID
     */
    BufferDALM findByUid(UUID uid) throws BufferNotFoundException;

    /**
     * Найти все буферы схемы соединения
     */
    List<BufferDALM> findByConnectionSchemeUid(UUID connectionSchemeUid);

    /**
     * Проверить существование буфера
     */
    boolean exists(UUID uid);

    /**
     * Удалить все буферы схемы соединения
     */
    void deleteByConnectionSchemeUid(UUID connectionSchemeUid);
}