// BufferDeviceConverter.java
package com.connection.processing.buffer.bufferdevice.converter;

import java.util.UUID;

import com.connection.processing.buffer.bufferdevice.model.BufferDeviceBLM;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDALM;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDTO;

public class BufferDeviceConverter {
    public BufferDeviceBLM toBLM(BufferDeviceDALM dalm) {
        return new BufferDeviceBLM(
            dalm.getBufferUid(),
            dalm.getDeviceUid()
        );
    }

    public BufferDeviceBLM toBLM(BufferDeviceDTO dto) {
        return new BufferDeviceBLM(
            UUID.fromString(dto.getBufferUid()),
            UUID.fromString(dto.getDeviceUid())
        );
    }

    public BufferDeviceDTO toDTO(BufferDeviceBLM blm) {
        return new BufferDeviceDTO(
            blm.getBufferUid().toString(),
            blm.getDeviceUid().toString()
        );
    }

    public BufferDeviceDALM toDALM(BufferDeviceBLM blm) {
        return new BufferDeviceDALM(
            blm.getBufferUid(),
            blm.getDeviceUid()
        );
    }
}