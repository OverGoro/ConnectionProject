package com.service.device.auth.mother;

import java.util.Date;
import java.util.UUID;

import com.connection.device.model.DeviceDALM;
import com.connection.device.token.model.DeviceAccessTokenBLM;
import com.connection.device.token.model.DeviceAccessTokenDALM;
import com.connection.device.token.model.DeviceTokenBLM;
import com.connection.device.token.model.DeviceTokenDALM;

public class DeviceTokenObjectMother {

    public static final UUID DEVICE_UID = UUID.fromString("333e4567-e89b-12d3-a456-426614174003");
    public static final UUID DEVICE_TOKEN_UID = UUID.fromString("443e4567-e89b-12d3-a456-426614174004");
    public static final UUID DEVICE_ACCESS_TOKEN_UID = UUID.fromString("553e4567-e89b-12d3-a456-426614174005");
    public static final String VALID_DEVICE_TOKEN_STRING = "valid.device.token.string";
    public static final String VALID_DEVICE_ACCESS_TOKEN_STRING = "valid.device.access.token.string";

    public static DeviceDALM createValidDeviceDALM() {
        return DeviceDALM.builder()
            .uid(DEVICE_UID)
            .clientUuid(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
            .deviceName("Test Device")
            .deviceDescription("Test Description")
            .build();
    }

    public static DeviceTokenBLM createValidDeviceTokenBLM() {
        Date createdAt = new Date();
        Date expiresAt = new Date(createdAt.getTime() + 86400000); // 24 hours
        
        return DeviceTokenBLM.builder()
            .token(VALID_DEVICE_TOKEN_STRING)
            .uid(DEVICE_TOKEN_UID)
            .deviceUid(DEVICE_UID)
            .createdAt(createdAt)
            .expiresAt(expiresAt)
            .build();
    }

    public static DeviceTokenDALM createValidDeviceTokenDALM() {
        Date createdAt = new Date();
        Date expiresAt = new Date(createdAt.getTime() + 86400000); // 24 hours
        
        return DeviceTokenDALM.builder()
            .uid(DEVICE_TOKEN_UID)
            .deviceUid(DEVICE_UID)
            .token(VALID_DEVICE_TOKEN_STRING)
            .createdAt(createdAt)
            .expiresAt(expiresAt)
            .build();
    }

    public static DeviceAccessTokenBLM createValidDeviceAccessTokenBLM() {
        Date createdAt = new Date();
        Date expiresAt = new Date(createdAt.getTime() + 3600000); // 1 hour
        
        return DeviceAccessTokenBLM.builder()
            .token(VALID_DEVICE_ACCESS_TOKEN_STRING)
            .uid(DEVICE_ACCESS_TOKEN_UID)
            .deviceTokenUid(DEVICE_TOKEN_UID)
            .createdAt(createdAt)
            .expiresAt(expiresAt)
            .build();
    }

    public static DeviceAccessTokenDALM createValidDeviceAccessTokenDALM() {
        Date createdAt = new Date();
        Date expiresAt = new Date(createdAt.getTime() + 3600000); // 1 hour
        
        return DeviceAccessTokenDALM.builder()
            .uid(DEVICE_ACCESS_TOKEN_UID)
            .deviceTokenUid(DEVICE_TOKEN_UID)
            .token(VALID_DEVICE_ACCESS_TOKEN_STRING)
            .createdAt(createdAt)
            .expiresAt(expiresAt)
            .build();
    }

    public static DeviceAccessTokenBLM createExpiredDeviceAccessTokenBLM() {
        Date createdAt = new Date(System.currentTimeMillis() - 7200000); // 2 hours ago
        Date expiresAt = new Date(createdAt.getTime() + 3600000); // 1 hour, already expired
        
        return DeviceAccessTokenBLM.builder()
            .token("expired.device.access.token")
            .uid(DEVICE_ACCESS_TOKEN_UID)
            .deviceTokenUid(DEVICE_TOKEN_UID)
            .createdAt(createdAt)
            .expiresAt(expiresAt)
            .build();
    }
}