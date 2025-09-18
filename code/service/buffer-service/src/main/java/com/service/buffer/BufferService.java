// BufferService.java
package com.service.buffer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferDTO;

public interface BufferService {
    BufferBLM createBuffer(String accessToken, BufferDTO bufferDTO);
    BufferBLM getBuffer(String accessToken, UUID bufferUid);
    List<BufferBLM> getBuffersByClient(String accessToken);
    List<BufferBLM> getBuffersByConnectionScheme(String accessToken, UUID connectionSchemeUid);
    BufferBLM updateBuffer(String accessToken, UUID bufferUid, BufferDTO bufferDTO);
    void deleteBuffer(String accessToken, UUID bufferUid);
    void deleteBuffersByConnectionScheme(String accessToken, UUID connectionSchemeUid);
    boolean bufferExists(String accessToken, UUID bufferUid);
    Map<String, Object> getHealthStatus();
}