package com.connection.device.token.mother;

import java.util.Date;
import java.util.UUID;

import com.connection.device.token.model.DeviceAccessTokenBLM;
import com.connection.device.token.model.DeviceAccessTokenDALM;
import com.connection.device.token.model.DeviceAccessTokenDTO;
import com.connection.device.token.model.DeviceTokenBLM;
import com.connection.device.token.model.DeviceTokenDALM;
import com.connection.device.token.model.DeviceTokenDTO;

public class DeviceTokenObjectMother {

    private static final UUID DEFAULT_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID DEFAULT_DEVICE_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final UUID DEFAULT_DEVICE_TOKEN_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
    private static final String DEFAULT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
    private static final Date DEFAULT_CREATED_AT = new Date(System.currentTimeMillis() - 3600000);
    private static final Date DEFAULT_EXPIRES_AT = new Date(System.currentTimeMillis() + 3600000);

    // DeviceToken objects
    public static DeviceTokenDTO createValidDeviceTokenDTO() {
        return DeviceTokenDTO.builder()
                .token(DEFAULT_TOKEN)
                .build();
    }

    public static DeviceTokenBLM createValidDeviceTokenBLM() {
        return DeviceTokenBLM.builder()
                .token(DEFAULT_TOKEN)
                .uid(DEFAULT_UID)
                .deviceUid(DEFAULT_DEVICE_UID)
                .createdAt(DEFAULT_CREATED_AT)
                .expiresAt(DEFAULT_EXPIRES_AT)
                .build();
    }

    public static DeviceTokenDALM createValidDeviceTokenDALM() {
        return DeviceTokenDALM.builder()
                .uid(DEFAULT_UID)
                .deviceUid(DEFAULT_DEVICE_UID)
                .token(DEFAULT_TOKEN)
                .createdAt(DEFAULT_CREATED_AT)
                .expiresAt(DEFAULT_EXPIRES_AT)
                .build();
    }

    public static DeviceTokenDTO createDeviceTokenDTOWithEmptyToken() {
        return DeviceTokenDTO.builder()
                .token("")
                .build();
    }

    public static DeviceTokenBLM createDeviceTokenBLMWithExpiredToken() {
        return DeviceTokenBLM.builder()
                .token(DEFAULT_TOKEN)
                .uid(DEFAULT_UID)
                .deviceUid(DEFAULT_DEVICE_UID)
                .createdAt(new Date(System.currentTimeMillis() - 7200000))
                .expiresAt(new Date(System.currentTimeMillis() - 3600000))
                .build();
    }

    public static DeviceTokenBLM createDeviceTokenBLMWithFutureCreationDate() {
        return DeviceTokenBLM.builder()
                .token(DEFAULT_TOKEN)
                .uid(DEFAULT_UID)
                .deviceUid(DEFAULT_DEVICE_UID)
                .createdAt(new Date(System.currentTimeMillis() + 3600000))
                .expiresAt(new Date(System.currentTimeMillis() + 7200000))
                .build();
    }

    // DeviceAccessToken objects
    public static DeviceAccessTokenDTO createValidDeviceAccessTokenDTO() {
        return DeviceAccessTokenDTO.builder()
                .token(DEFAULT_TOKEN)
                .build();
    }

    public static DeviceAccessTokenBLM createValidDeviceAccessTokenBLM() {
        return DeviceAccessTokenBLM.builder()
                .token(DEFAULT_TOKEN)
                .uid(DEFAULT_UID)
                .deviceTokenUid(DEFAULT_DEVICE_TOKEN_UID)
                .createdAt(DEFAULT_CREATED_AT)
                .expiresAt(DEFAULT_EXPIRES_AT)
                .build();
    }

    public static DeviceAccessTokenDALM createValidDeviceAccessTokenDALM() {
        return DeviceAccessTokenDALM.builder()
                .uid(DEFAULT_UID)
                .deviceTokenUid(DEFAULT_DEVICE_TOKEN_UID)
                .token(DEFAULT_TOKEN)
                .createdAt(DEFAULT_CREATED_AT)
                .expiresAt(DEFAULT_EXPIRES_AT)
                .build();
    }

    public static DeviceAccessTokenDTO createDeviceAccessTokenDTOWithLongToken() {
        return DeviceAccessTokenDTO.builder()
                .token("a".repeat(513))
                .build();
    }

    public static DeviceAccessTokenBLM createDeviceAccessTokenBLMWithInvalidDates() {
        return DeviceAccessTokenBLM.builder()
                .token(DEFAULT_TOKEN)
                .uid(DEFAULT_UID)
                .deviceTokenUid(DEFAULT_DEVICE_TOKEN_UID)
                .createdAt(new Date(System.currentTimeMillis() + 3600000))
                .expiresAt(new Date(System.currentTimeMillis() - 3600000))
                .build();
    }

    // Helper methods
    public static DeviceTokenDALM createDeviceTokenForDevice(UUID deviceUid) {
        return DeviceTokenDALM.builder()
                .uid(UUID.randomUUID())
                .deviceUid(deviceUid)
                .token("token_for_" + deviceUid)
                .createdAt(DEFAULT_CREATED_AT)
                .expiresAt(DEFAULT_EXPIRES_AT)
                .build();
    }

    public static DeviceAccessTokenDALM createDeviceAccessTokenForDeviceToken(UUID deviceTokenUid) {
        return DeviceAccessTokenDALM.builder()
                .uid(UUID.randomUUID())
                .deviceTokenUid(deviceTokenUid)
                .token("access_token_for_" + deviceTokenUid)
                .createdAt(DEFAULT_CREATED_AT)
                .expiresAt(DEFAULT_EXPIRES_AT)
                .build();
    }
}