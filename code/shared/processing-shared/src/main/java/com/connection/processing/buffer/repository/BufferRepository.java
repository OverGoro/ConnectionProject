// BufferRepository.java
package com.connection.processing.buffer.repository;

import java.util.List;
import java.util.UUID;

import com.connection.processing.buffer.exception.BufferAlreadyExistsException;
import com.connection.processing.buffer.exception.BufferNotFoundException;
import com.connection.processing.buffer.model.BufferDALM;

public interface BufferRepository {
    void add(BufferDALM buffer) throws BufferAlreadyExistsException;
    void delete(UUID uid) throws BufferNotFoundException;
    BufferDALM findByUid(UUID uid) throws BufferNotFoundException;
    List<BufferDALM> findByConnectionSchemeUid(UUID connectionSchemeUid);
    boolean exists(UUID uid);
}