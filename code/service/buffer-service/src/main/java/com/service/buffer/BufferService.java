// BufferService.java
package com.service.buffer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferDTO;

public interface BufferService {
    BufferBLM createBuffer(UUID clientUid, BufferDTO bufferDTO);
    BufferBLM getBufferByUid(UUID clientUid, UUID bufferUid);
    List<BufferBLM> getBuffersByClient(UUID clientUid);
    List<BufferBLM> getBuffersByConnectionScheme(UUID clientUid, UUID connectionSchemeUid);
    BufferBLM updateBuffer(UUID clientUid, UUID bufferUid, BufferDTO bufferDTO);
    void deleteBuffer(UUID clientUid, UUID bufferUid);
    void deleteBuffersByConnectionScheme(UUID clientUid, UUID connectionSchemeUid);
    boolean bufferExists(UUID clientUid, UUID bufferUid);
    Map<String, Object> getHealthStatus();
}
