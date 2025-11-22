
package com.connection.device.converter;

import com.connection.device.model.DeviceBlm;
import com.connection.device.model.DeviceDalm;
import com.connection.device.model.DeviceDto;
import java.util.UUID;

/** . */
public class DeviceConverter {
    /** . */
    public DeviceBlm toBlm(DeviceDalm dalm) {
        return new DeviceBlm(dalm.getUid(), dalm.getClientUuid(),
                dalm.getDeviceName(), dalm.getDeviceDescription());
    }

    /** . */
    public DeviceBlm toBlm(DeviceDto dto) {
        return new DeviceBlm(UUID.fromString(dto.getUid()),
                UUID.fromString(dto.getClientUuid()), dto.getDeviceName(),
                dto.getDeviceDescription());
    }

    /** . */
    public DeviceDto toDto(DeviceBlm blm) {
        return new DeviceDto(blm.getUid().toString(),
                blm.getClientUuid().toString(), blm.getDeviceName(),
                blm.getDeviceDescription());
    }

    /** . */
    public DeviceDalm toDalm(DeviceBlm blm) {
        return new DeviceDalm(blm.getUid(), blm.getClientUuid(),
                blm.getDeviceName(), blm.getDeviceDescription());
    }
}
