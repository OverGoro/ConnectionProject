package com.connection.device.mother;

import java.util.UUID;

import com.connection.device.model.DeviceBLM;
import com.connection.device.model.DeviceDALM;
import com.connection.device.model.DeviceDTO;

public class DeviceObjectMother {

    private static final UUID DEFAULT_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID DEFAULT_CLIENT_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final String DEFAULT_NAME = "Test Device";
    private static final String DEFAULT_DESCRIPTION = "Test Device Description";

    // Valid devices
    public static DeviceDTO createValidDeviceDTO() {
        return DeviceDTO.builder()
                .uid(DEFAULT_UID.toString())
                .clientUuid(DEFAULT_CLIENT_UUID.toString())
                .deviceName(DEFAULT_NAME)
                .deviceDescription(DEFAULT_DESCRIPTION)
                .build();
    }

    public static DeviceBLM createValidDeviceBLM() {
        return DeviceBLM.builder()
                .uid(DEFAULT_UID)
                .clientUuid(DEFAULT_CLIENT_UUID)
                .deviceName(DEFAULT_NAME)
                .deviceDescription(DEFAULT_DESCRIPTION)
                .build();
    }

    public static DeviceDALM createValidDeviceDALM() {
        return DeviceDALM.builder()
                .uid(DEFAULT_UID)
                .clientUuid(DEFAULT_CLIENT_UUID)
                .deviceName(DEFAULT_NAME)
                .deviceDescription(DEFAULT_DESCRIPTION)
                .build();
    }

    // Invalid devices
    public static DeviceDTO createDeviceDTOWithNullUid() {
        return DeviceDTO.builder()
                .uid(null)
                .clientUuid(DEFAULT_CLIENT_UUID.toString())
                .deviceName(DEFAULT_NAME)
                .deviceDescription(DEFAULT_DESCRIPTION)
                .build();
    }

    public static DeviceDTO createDeviceDTOWithInvalidUid() {
        return DeviceDTO.builder()
                .uid("invalid-uuid")
                .clientUuid(DEFAULT_CLIENT_UUID.toString())
                .deviceName(DEFAULT_NAME)
                .deviceDescription(DEFAULT_DESCRIPTION)
                .build();
    }

    public static DeviceDTO createDeviceDTOWithLongName() {
        return DeviceDTO.builder()
                .uid(DEFAULT_UID.toString())
                .clientUuid(DEFAULT_CLIENT_UUID.toString())
                .deviceName("A".repeat(101)) // Exceeds 100 characters
                .deviceDescription(DEFAULT_DESCRIPTION)
                .build();
    }

    public static DeviceDTO createDeviceDTOWithLongDescription() {
        return DeviceDTO.builder()
                .uid(DEFAULT_UID.toString())
                .clientUuid(DEFAULT_CLIENT_UUID.toString())
                .deviceName(DEFAULT_NAME)
                .deviceDescription("A".repeat(501)) // Exceeds 500 characters
                .build();
    }

    public static DeviceBLM createDeviceBLMWithNullFields() {
        return DeviceBLM.builder()
                .uid(null)
                .clientUuid(null)
                .deviceName(null)
                .deviceDescription(null)
                .build();
    }

    // Devices for specific scenarios
    public static DeviceDALM createDeviceForClient(UUID clientUuid) {
        return DeviceDALM.builder()
                .uid(UUID.randomUUID())
                .clientUuid(clientUuid)
                .deviceName("Client Specific Device")
                .deviceDescription("Device for client " + clientUuid)
                .build();
    }

    public static DeviceDALM createDeviceWithName(UUID clientUuid, String name) {
        return DeviceDALM.builder()
                .uid(UUID.randomUUID())
                .clientUuid(clientUuid)
                .deviceName(name)
                .deviceDescription("Device with name " + name)
                .build();
    }
}