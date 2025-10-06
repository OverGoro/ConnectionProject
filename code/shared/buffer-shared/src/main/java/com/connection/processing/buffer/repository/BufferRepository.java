package com.connection.processing.buffer.repository;

import java.util.List;
import java.util.UUID;
import com.connection.processing.buffer.exception.BufferAlreadyExistsException;
import com.connection.processing.buffer.exception.BufferNotFoundException;
import com.connection.processing.buffer.model.BufferDALM;

public interface BufferRepository {
    // Основные CRUD операции
    void add(BufferDALM buffer) throws BufferAlreadyExistsException;
    void update(BufferDALM buffer) throws BufferNotFoundException;
    void delete(UUID uid) throws BufferNotFoundException;
    BufferDALM findByUid(UUID uid) throws BufferNotFoundException;
    
    // Методы для работы с device
    List<BufferDALM> findByDeviceUid(UUID deviceUid);
    boolean exists(UUID uid);
    void deleteByDeviceUid(UUID deviceUid);
    
    // Методы для связи с connection scheme
    void addBufferToConnectionScheme(UUID bufferUid, UUID connectionSchemeUid);
    void removeBufferFromConnectionScheme(UUID bufferUid, UUID connectionSchemeUid);
    List<BufferDALM> findByConnectionSchemeUid(UUID connectionSchemeUid);
}