package com.connection.processing.buffer.repository;

import java.util.List;
import java.util.UUID;
import com.connection.processing.buffer.exception.BufferAlreadyExistsException;
import com.connection.processing.buffer.exception.BufferNotFoundException;
import com.connection.processing.buffer.model.BufferBLM;

public interface BufferRepository {
    // Основные CRUD операции
    void add(BufferBLM buffer) throws BufferAlreadyExistsException;
    void update(BufferBLM buffer) throws BufferNotFoundException;
    void delete(UUID uid) throws BufferNotFoundException;
    BufferBLM findByUid(UUID uid) throws BufferNotFoundException;
    
    // Методы для работы с device
    List<BufferBLM> findByDeviceUid(UUID deviceUid);
    boolean exists(UUID uid);
    void deleteByDeviceUid(UUID deviceUid);
    
    // Методы для связи с connection scheme
    void addBufferToConnectionScheme(UUID bufferUid, UUID connectionSchemeUid);
    void removeBufferFromConnectionScheme(UUID bufferUid, UUID connectionSchemeUid);
    List<BufferBLM> findByConnectionSchemeUid(UUID connectionSchemeUid);
}