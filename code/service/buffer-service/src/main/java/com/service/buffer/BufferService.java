package com.service.buffer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.connection.processing.buffer.model.BufferBLM;

public interface BufferService {
    BufferBLM createBuffer(BufferBLM bufferBLM);
    BufferBLM getBufferByUid(UUID bufferUid);
    List<BufferBLM> getBuffersByClient(UUID clientUid);
    List<BufferBLM> getBuffersByDevice(UUID deviceUid);
    List<BufferBLM> getBuffersByConnectionScheme(UUID connectionSchemeUid);
    BufferBLM updateBuffer(UUID bufferUid, BufferBLM bufferBLM);
    void deleteBuffer(UUID bufferUid);
    void deleteBufferFromConnectionScheme(UUID connectionSchemeUid, UUID bufferUid);
    void deleteAllBuffersFromConnectionScheme(UUID connectionSchemeUid);
    boolean bufferExists(UUID bufferUid);
    Map<String, Object> getHealthStatus();
}