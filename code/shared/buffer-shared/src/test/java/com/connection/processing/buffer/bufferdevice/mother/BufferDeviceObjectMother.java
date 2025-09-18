package com.connection.processing.buffer.bufferdevice.mother;

import java.util.UUID;

import com.connection.processing.buffer.bufferdevice.model.BufferDeviceBLM;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDALM;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDTO;

public class BufferDeviceObjectMother {

    private static final UUID DEFAULT_BUFFER_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID DEFAULT_DEVICE_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

    public static BufferDeviceDTO createValidBufferDeviceDTO() {
        return BufferDeviceDTO.builder()
                .bufferUid(DEFAULT_BUFFER_UID.toString())
                .deviceUid(DEFAULT_DEVICE_UID.toString())
                .build();
    }

    public static BufferDeviceBLM createValidBufferDeviceBLM() {
        return BufferDeviceBLM.builder()
                .bufferUid(DEFAULT_BUFFER_UID)
                .deviceUid(DEFAULT_DEVICE_UID)
                .build();
    }

    public static BufferDeviceDALM createValidBufferDeviceDALM() {
        return BufferDeviceDALM.builder()
                .bufferUid(DEFAULT_BUFFER_UID)
                .deviceUid(DEFAULT_DEVICE_UID)
                .build();
    }

    public static BufferDeviceDTO createBufferDeviceDTOWithNullBufferUid() {
        return BufferDeviceDTO.builder()
                .bufferUid(null)
                .deviceUid(DEFAULT_DEVICE_UID.toString())
                .build();
    }

    public static BufferDeviceDTO createBufferDeviceDTOWithNullDeviceUid() {
        return BufferDeviceDTO.builder()
                .bufferUid(DEFAULT_BUFFER_UID.toString())
                .deviceUid(null)
                .build();
    }

    public static BufferDeviceDTO createBufferDeviceDTOWithInvalidBufferUid() {
        return BufferDeviceDTO.builder()
                .bufferUid("invalid-uuid")
                .deviceUid(DEFAULT_DEVICE_UID.toString())
                .build();
    }

    public static BufferDeviceDTO createBufferDeviceDTOWithInvalidDeviceUid() {
        return BufferDeviceDTO.builder()
                .bufferUid(DEFAULT_BUFFER_UID.toString())
                .deviceUid("invalid-uuid")
                .build();
    }

    public static BufferDeviceBLM createBufferDeviceBLMWithNullFields() {
        return BufferDeviceBLM.builder()
                .bufferUid(null)
                .deviceUid(null)
                .build();
    }

    public static BufferDeviceDALM createBufferDeviceForBuffer(UUID bufferUid) {
        return BufferDeviceDALM.builder()
                .bufferUid(bufferUid)
                .deviceUid(UUID.randomUUID())
                .build();
    }

    public static BufferDeviceDALM createBufferDeviceForDevice(UUID deviceUid) {
        return BufferDeviceDALM.builder()
                .bufferUid(UUID.randomUUID())
                .deviceUid(deviceUid)
                .build();
    }
}