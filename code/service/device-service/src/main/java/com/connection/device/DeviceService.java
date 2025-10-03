package com.connection.device;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.connection.device.model.DeviceBLM;

public interface DeviceService {
    DeviceBLM createDevice(DeviceBLM deviceDTO);
    DeviceBLM getDevice(UUID deviceUid);
    List<DeviceBLM> getDevicesByClient();
    DeviceBLM updateDevice(DeviceBLM deviceDTO);
    void deleteDevice(UUID deviceUid);
    boolean deviceExists(UUID deviceUid);
    Map<String, Object> getHealthStatus();
}