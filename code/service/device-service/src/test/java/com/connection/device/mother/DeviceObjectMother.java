package com.connection.device.mother;

import java.util.UUID;

import com.connection.device.model.DeviceBLM;
import com.connection.device.model.DeviceDALM;
import com.connection.device.model.DeviceDTO;

public class DeviceObjectMother {

    public static final UUID CLIENT_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    public static final UUID DEVICE_UUID = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
    public static final String VALID_TOKEN = "valid-token-123";
    public static final String INVALID_TOKEN = "invalid-token-456";

    public static DeviceDTO createValidDeviceDTO() {
        return new DeviceDTO(
            DEVICE_UUID.toString(),
            CLIENT_UUID.toString(),
            "Test Device",
            "Test Description"
        );
    }

    public static DeviceDTO createInvalidDeviceDTO() {
        return new DeviceDTO(
            "invalid-uuid",
            "invalid-client-uuid",
            "", // empty name
            null // null description
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

    public static DeviceDALM createValidDeviceDALM() {
        return new DeviceDALM(
            DEVICE_UUID,
            CLIENT_UUID,
            "Test Device",
            "Test Description"
        );
    }

    public static DeviceDTO createDeviceDTOWithDifferentClient() {
        return new DeviceDTO(
            DEVICE_UUID.toString(),
            UUID.randomUUID().toString(), // different client
            "Test Device",
            "Test Description"
        );
    }
}