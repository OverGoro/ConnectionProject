package com.connection.device.mother;

import com.connection.device.model.DeviceBlm;
import com.connection.device.model.DeviceDalm;
import com.connection.device.model.DeviceDto;
import java.util.UUID;

/** . */
public class DeviceObjectMother {

    private static final UUID DEFAULT_UID =
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID DEFAULT_CLIENT_UUID =
            UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final String DEFAULT_NAME = "Test Device";
    private static final String DEFAULT_DESCRIPTION = "Test Device Description";

    // Valid devices
    /** . */
    public static DeviceDto createValidDeviceDto() {
        return DeviceDto.builder().uid(DEFAULT_UID.toString())
                .clientUuid(DEFAULT_CLIENT_UUID.toString())
                .deviceName(DEFAULT_NAME).deviceDescription(DEFAULT_DESCRIPTION)
                .build();
    }

    /** . */
    public static DeviceBlm createValidDeviceBlm() {
        return DeviceBlm.builder().uid(DEFAULT_UID)
                .clientUuid(DEFAULT_CLIENT_UUID).deviceName(DEFAULT_NAME)
                .deviceDescription(DEFAULT_DESCRIPTION).build();
    }

    /** . */
    public static DeviceDalm createValidDeviceDalm() {
        return DeviceDalm.builder().uid(DEFAULT_UID)
                .clientUuid(DEFAULT_CLIENT_UUID).deviceName(DEFAULT_NAME)
                .deviceDescription(DEFAULT_DESCRIPTION).build();
    }

    // Invalid devices
    /** . */
    public static DeviceDto createDeviceDtoWithNullUid() {
        return DeviceDto.builder().uid(null)
                .clientUuid(DEFAULT_CLIENT_UUID.toString())
                .deviceName(DEFAULT_NAME).deviceDescription(DEFAULT_DESCRIPTION)
                .build();
    }

    /** . */
    public static DeviceDto createDeviceDtoWithInvalidUid() {
        return DeviceDto.builder().uid("invalid-uuid")
                .clientUuid(DEFAULT_CLIENT_UUID.toString())
                .deviceName(DEFAULT_NAME).deviceDescription(DEFAULT_DESCRIPTION)
                .build();
    }

    /** . */
    public static DeviceDto createDeviceDtoWithLongName() {
        return DeviceDto.builder().uid(DEFAULT_UID.toString())
                .clientUuid(DEFAULT_CLIENT_UUID.toString())
                .deviceName("A".repeat(101)) // Exceeds 100 characters
                .deviceDescription(DEFAULT_DESCRIPTION).build();
    }

    /** . */
    public static DeviceDto createDeviceDtoWithLongDescription() {
        return DeviceDto.builder().uid(DEFAULT_UID.toString())
                .clientUuid(DEFAULT_CLIENT_UUID.toString())
                .deviceName(DEFAULT_NAME).deviceDescription("A".repeat(501))
                .build();
    }

    /** . */
    public static DeviceBlm createDeviceBlmWithNullFields() {
        return DeviceBlm.builder().uid(null).clientUuid(null).deviceName(null)
                .deviceDescription(null).build();
    }

    // Devices for specific scenarios
    /** . */
    public static DeviceDalm createDeviceForClient(UUID clientUuid) {
        return DeviceDalm.builder().uid(UUID.randomUUID())
                .clientUuid(clientUuid).deviceName("Client Specific Device")
                .deviceDescription("Device for client " + clientUuid).build();
    }

    /** . */
    public static DeviceDalm createDeviceWithName(UUID clientUuid,
            String name) {
        return DeviceDalm.builder().uid(UUID.randomUUID())
                .clientUuid(clientUuid).deviceName(name)
                .deviceDescription("Device with name " + name).build();
    }
}
