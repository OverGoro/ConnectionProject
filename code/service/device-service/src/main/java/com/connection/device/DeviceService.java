package com.connection.device;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.connection.device.model.DeviceBLM;
import com.connection.device.model.DeviceDTO;

public interface DeviceService {
    DeviceBLM createDevice(String accessToken, DeviceDTO deviceDTO);
    DeviceBLM getDevice(String accessToken, UUID deviceUid);
    List<DeviceBLM> getDevicesByClient(String accessToken);
    DeviceBLM updateDevice(String accessToken, UUID deviceUid, DeviceDTO deviceDTO);
    void deleteDevice(String accessToken, UUID deviceUid);
    boolean deviceExists(String accessToken, UUID deviceUid);
    Map<String, Object> getHealthStatus();
}