// BufferDeviceService.java
package com.service.bufferdevice;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.connection.processing.buffer.bufferdevice.model.BufferDeviceBLM;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDTO;

public interface BufferDeviceService {
    BufferDeviceBLM createBufferDevice(String accessToken, BufferDeviceDTO bufferDeviceDTO);
    void deleteBufferDevice(String accessToken, UUID bufferUid, UUID deviceUid);
    void deleteAllBufferDevicesForBuffer(String accessToken, UUID bufferUid);
    void deleteAllBufferDevicesForDevice(String accessToken, UUID deviceUid);
    List<BufferDeviceBLM> getBufferDevicesByBuffer(String accessToken, UUID bufferUid);
    List<BufferDeviceBLM> getBufferDevicesByDevice(String accessToken, UUID deviceUid);
    boolean bufferDeviceExists(String accessToken, UUID bufferUid, UUID deviceUid);
    void addDevicesToBuffer(String accessToken, UUID bufferUid, List<UUID> deviceUids);
    void addBuffersToDevice(String accessToken, UUID deviceUid, List<UUID> bufferUids);
    Map<String, Object> getHealthStatus();
}