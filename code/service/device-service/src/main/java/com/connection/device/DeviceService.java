package com.connection.device;

import com.connection.device.model.DeviceBlm;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** . */
public interface DeviceService {
    /** . */
    DeviceBlm createDevice(DeviceBlm deviceBlm);

    /** . */
    DeviceBlm getDevice(UUID deviceUid);

    /** . */
    List<DeviceBlm> getDevicesByClient(UUID clientUid);

    /** . */
    DeviceBlm updateDevice(DeviceBlm deviceBlm);

    /** . */
    void deleteDevice(UUID deviceUid);

    /** . */
    boolean deviceExists(UUID deviceUid);

    /** . */
    Map<String, Object> getHealthStatus();
}
