// BufferJsonDataService.java
package com.connection.bufferjsondata;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.connection.processing.buffer.objects.json.model.BufferJsonDataBLM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDTO;

public interface BufferJsonDataService {
    BufferJsonDataBLM addJsonData(String accessToken, BufferJsonDataDTO jsonDataDTO);
    BufferJsonDataBLM getJsonData(String accessToken, UUID dataUid);
    List<BufferJsonDataBLM> getJsonDataByBuffer(String accessToken, UUID bufferUid);
    List<BufferJsonDataBLM> getJsonDataByBufferAndCreatedAfter(String accessToken, UUID bufferUid, Instant createdAfter);
    List<BufferJsonDataBLM> getJsonDataByBufferAndCreatedBefore(String accessToken, UUID bufferUid, Instant createdBefore);
    List<BufferJsonDataBLM> getJsonDataByBufferAndCreatedBetween(String accessToken, UUID bufferUid, Instant startDate, Instant endDate);
    BufferJsonDataBLM getNewestJsonDataByBuffer(String accessToken, UUID bufferUid);
    BufferJsonDataBLM getOldestJsonDataByBuffer(String accessToken, UUID bufferUid);
    List<BufferJsonDataBLM> getNewestJsonDataByBuffer(String accessToken, UUID bufferUid, int limit);
    List<BufferJsonDataBLM> getOldestJsonDataByBuffer(String accessToken, UUID bufferUid, int limit);
    void deleteJsonData(String accessToken, UUID dataUid);
    void deleteJsonDataByBuffer(String accessToken, UUID bufferUid);
    void deleteOldJsonData(String accessToken, Instant olderThan);
    int countJsonDataByBuffer(String accessToken, UUID bufferUid);
    boolean jsonDataExists(String accessToken, UUID dataUid);
    Map<String, Object> getHealthStatus();
}