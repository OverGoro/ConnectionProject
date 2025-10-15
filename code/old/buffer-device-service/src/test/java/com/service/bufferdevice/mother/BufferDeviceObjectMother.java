package com.service.bufferdevice.mother;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.connection.device.model.DeviceBLM;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceBLM;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDTO;
import com.connection.processing.buffer.model.BufferBLM;

public class BufferDeviceObjectMother {

    public static final UUID CLIENT_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    public static final UUID BUFFER_UUID = UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
    public static final UUID DEVICE_UUID = UUID.fromString("423e4567-e89b-12d3-a456-426614174003");
    public static final UUID OTHER_BUFFER_UUID = UUID.fromString("523e4567-e89b-12d3-a456-426614174004");
    public static final UUID OTHER_DEVICE_UUID = UUID.fromString("623e4567-e89b-12d3-a456-426614174005");
    public static final String VALID_TOKEN = "valid-token-123";
    public static final String INVALID_TOKEN = "invalid-token-456";

    public static BufferDeviceDTO createValidBufferDeviceDTO() {
        return new BufferDeviceDTO(
            BUFFER_UUID.toString(),
            DEVICE_UUID.toString()
        );
    }

    public static BufferDeviceBLM createValidBufferDeviceBLM() {
        return new BufferDeviceBLM(BUFFER_UUID, DEVICE_UUID);
    }

    public static BufferBLM createValidBufferBLM() {
        return new BufferBLM(
            BUFFER_UUID,
            CLIENT_UUID, // connectionSchemeUid = clientUid
            100, // maxMessagesNumber
            1024, // maxMessageSize
            "message prototype"
        );
    }

    public static BufferBLM createBufferWithDifferentClient() {
        return new BufferBLM(
            BUFFER_UUID,
            UUID.randomUUID(), // different client
            100,
            1024,
            "message prototype"
        );
    }

    public static DeviceBLM createValidDeviceBLM() {
        return new DeviceBLM(
            DEVICE_UUID,
            CLIENT_UUID,
            "Test Device",
            "Test Description"
        );
    }

    public static DeviceBLM createDeviceWithDifferentClient() {
        return new DeviceBLM(
            DEVICE_UUID,
            UUID.randomUUID(), // different client
            "Test Device",
            "Test Description"
        );
    }

    public static List<UUID> createDeviceUidList() {
        return Arrays.asList(DEVICE_UUID, OTHER_DEVICE_UUID);
    }

    public static List<UUID> createBufferUidList() {
        return Arrays.asList(BUFFER_UUID, OTHER_BUFFER_UUID);
    }

    public static Map<String, Object> createHealthResponse() {
        return Map.of("status", "OK", "service", "test-service");
    }
}