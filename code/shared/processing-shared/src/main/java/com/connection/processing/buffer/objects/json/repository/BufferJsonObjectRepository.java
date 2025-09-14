// BufferJsonObjectRepository.java
package com.connection.processing.buffer.objects.json.repository;

import java.util.List;
import java.util.UUID;

import com.connection.processing.buffer.objects.json.exception.BufferJsonObjectAlreadyExistsException;
import com.connection.processing.buffer.objects.json.exception.BufferJsonObjectNotFoundException;
import com.connection.processing.buffer.objects.json.model.BufferJsonObjectDALM;

public interface BufferJsonObjectRepository {
    void add(BufferJsonObjectDALM object) throws BufferJsonObjectAlreadyExistsException;
    void delete(UUID uid) throws BufferJsonObjectNotFoundException;
    BufferJsonObjectDALM findByUid(UUID uid) throws BufferJsonObjectNotFoundException;
    List<BufferJsonObjectDALM> findByBufferUid(UUID bufferUid);
    boolean exists(UUID uid);
}