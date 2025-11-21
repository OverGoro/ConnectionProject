package com.connection.device.mother;

import com.connection.device.model.DeviceBlm;
import com.connection.device.model.DeviceDalm;
import com.connection.device.model.DeviceDto;
import java.util.UUID;

/** . */
public class DeviceObjectMother {

    public static final UUID CLIENT_UUID =
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    public static final UUID DEVICE_UUID =
            UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
    public static final String VALID_TOKEN = "valid-token-123";
    public static final String INVALID_TOKEN = "invalid-token-456";

    /** . */
    public static DeviceDto createValidDeviceDto() {
        return new DeviceDto(DEVICE_UUID.toString(), CLIENT_UUID.toString(),
                "Test Device", "Test Description");
    }

    /** . */
    public static DeviceDto createInvalidDeviceDto() {
        return new DeviceDto("invalid-uuid", "invalid-client-uuid", "", // empty name
                null // null description
        );
    }

    /** . */
    public static DeviceBlm createValidDeviceBlm() {
        return new DeviceBlm(DEVICE_UUID, CLIENT_UUID, "Test Device",
                "Test Description");
    }

    /** . */
    public static DeviceDalm createValidDeviceDalm() {
        return new DeviceDalm(DEVICE_UUID, CLIENT_UUID, "Test Device",
                "Test Description");
    }

    /** . */
    public static DeviceDto createDeviceDtoWithDifferentClient() {
        return new DeviceDto(DEVICE_UUID.toString(),
                UUID.randomUUID().toString(), // different client
                "Test Device", "Test Description");
    }
}
