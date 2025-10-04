package com.connection.device;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.connection.device.model.DeviceBLM;

public interface DeviceService {
    DeviceBLM createDevice(UUID clientUid, DeviceBLM deviceBLM);
    DeviceBLM getDevice(UUID clientUid, UUID deviceUid);
    List<DeviceBLM> getDevicesByClient(UUID clientUid);
    DeviceBLM updateDevice(UUID clientUid, DeviceBLM deviceBLM);
    void deleteDevice(UUID clientUid, UUID deviceUid);
    boolean deviceExists(UUID deviceUid);
    Map<String, Object> getHealthStatus();
}