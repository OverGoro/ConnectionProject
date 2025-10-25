// DeviceConverter.java
package com.connection.device.converter;

import java.util.UUID;

import com.connection.device.model.DeviceBLM;
import com.connection.device.model.DeviceDALM;
import com.connection.device.model.DeviceDTO;

public class DeviceConverter {
    public DeviceBLM toBLM(DeviceDALM dalm) {
        return new DeviceBLM(
            dalm.getUid(),
            dalm.getClientUuid(),
            dalm.getDeviceName(),
            dalm.getDeviceDescription()
        );
    }

    public DeviceBLM toBLM(DeviceDTO dto) {
        return new DeviceBLM(
            UUID.fromString(dto.getUid()),
            UUID.fromString(dto.getClientUuid()),
            dto.getDeviceName(),
            dto.getDeviceDescription()
        );
    }

    public DeviceDTO toDTO(DeviceBLM blm) {
        return new DeviceDTO(
            blm.getUid().toString(),
            blm.getClientUuid().toString(),
            blm.getDeviceName(),
            blm.getDeviceDescription()
        );
    }

    public DeviceDALM toDALM(DeviceBLM blm) {
        return new DeviceDALM(
            blm.getUid(),
            blm.getClientUuid(),
            blm.getDeviceName(),
            blm.getDeviceDescription()
        );
    }
}