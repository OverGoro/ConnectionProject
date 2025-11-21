package com.connection.device.token.mother;

import java.util.Date;
import java.util.UUID;

import com.connection.device.token.model.DeviceAccessTokenBlm;
import com.connection.device.token.model.DeviceAccessTokenDalm;
import com.connection.device.token.model.DeviceAccessTokenDto;
import com.connection.device.token.model.DeviceTokenBlm;
import com.connection.device.token.model.DeviceTokenDalm;
import com.connection.device.token.model.DeviceTokenDto;

public class DeviceTokenObjectMother {

    private static final UUID DEFAULT_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID DEFAULT_DEVICE_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final UUID DEFAULT_DEVICE_TOKEN_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
    private static final String DEFAULT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
    private static final Date DEFAULT_CREATED_AT = new Date(System.currentTimeMillis() - 3600000);
    private static final Date DEFAULT_EXPIRES_AT = new Date(System.currentTimeMillis() + 3600000);

    // DeviceToken objects
    public static DeviceTokenDto createValidDeviceTokenDto() {
        return DeviceTokenDto.builder()
                .token(DEFAULT_TOKEN)
                .build();
    }

    public static DeviceTokenBlm createValidDeviceTokenBlm() {
        return DeviceTokenBlm.builder()
                .token(DEFAULT_TOKEN)
                .uid(DEFAULT_UID)
                .deviceUid(DEFAULT_DEVICE_UID)
                .createdAt(DEFAULT_CREATED_AT)
                .expiresAt(DEFAULT_EXPIRES_AT)
                .build();
    }

    public static DeviceTokenDalm createValidDeviceTokenDalm() {
        return DeviceTokenDalm.builder()
                .uid(DEFAULT_UID)
                .deviceUid(DEFAULT_DEVICE_UID)
                .token(DEFAULT_TOKEN)
                .createdAt(DEFAULT_CREATED_AT)
                .expiresAt(DEFAULT_EXPIRES_AT)
                .build();
    }

    public static DeviceTokenDto createDeviceTokenDtoWithEmptyToken() {
        return DeviceTokenDto.builder()
                .token("")
                .build();
    }

    public static DeviceTokenBlm createDeviceTokenBlmWithExpiredToken() {
        return DeviceTokenBlm.builder()
                .token(DEFAULT_TOKEN)
                .uid(DEFAULT_UID)
                .deviceUid(DEFAULT_DEVICE_UID)
                .createdAt(new Date(System.currentTimeMillis() - 7200000))
                .expiresAt(new Date(System.currentTimeMillis() - 3600000))
                .build();
    }

    public static DeviceTokenBlm createDeviceTokenBlmWithFutureCreationDate() {
        return DeviceTokenBlm.builder()
                .token(DEFAULT_TOKEN)
                .uid(DEFAULT_UID)
                .deviceUid(DEFAULT_DEVICE_UID)
                .createdAt(new Date(System.currentTimeMillis() + 3600000))
                .expiresAt(new Date(System.currentTimeMillis() + 7200000))
                .build();
    }

    // DeviceAccessToken objects
    public static DeviceAccessTokenDto createValidDeviceAccessTokenDto() {
        return DeviceAccessTokenDto.builder()
                .token(DEFAULT_TOKEN)
                .build();
    }

    public static DeviceAccessTokenBlm createValidDeviceAccessTokenBlm() {
        return DeviceAccessTokenBlm.builder()
                .token(DEFAULT_TOKEN)
                .uid(DEFAULT_UID)
                .deviceTokenUid(DEFAULT_DEVICE_TOKEN_UID)
                .createdAt(DEFAULT_CREATED_AT)
                .expiresAt(DEFAULT_EXPIRES_AT)
                .build();
    }

    public static DeviceAccessTokenDalm createValidDeviceAccessTokenDalm() {
        return DeviceAccessTokenDalm.builder()
                .uid(DEFAULT_UID)
                .deviceTokenUid(DEFAULT_DEVICE_TOKEN_UID)
                .token(DEFAULT_TOKEN)
                .createdAt(DEFAULT_CREATED_AT)
                .expiresAt(DEFAULT_EXPIRES_AT)
                .build();
    }

    public static DeviceAccessTokenDto createDeviceAccessTokenDtoWithLongToken() {
        return DeviceAccessTokenDto.builder()
                .token("a".repeat(513))
                .build();
    }

    public static DeviceAccessTokenBlm createDeviceAccessTokenBlmWithInvalidDates() {
        return DeviceAccessTokenBlm.builder()
                .token(DEFAULT_TOKEN)
                .uid(DEFAULT_UID)
                .deviceTokenUid(DEFAULT_DEVICE_TOKEN_UID)
                .createdAt(new Date(System.currentTimeMillis() + 3600000))
                .expiresAt(new Date(System.currentTimeMillis() - 3600000))
                .build();
    }

    // Helper methods
    public static DeviceTokenDalm createDeviceTokenForDevice(UUID deviceUid) {
        return DeviceTokenDalm.builder()
                .uid(UUID.randomUUID())
                .deviceUid(deviceUid)
                .token("token_for_" + deviceUid)
                .createdAt(DEFAULT_CREATED_AT)
                .expiresAt(DEFAULT_EXPIRES_AT)
                .build();
    }

    public static DeviceAccessTokenDalm createDeviceAccessTokenForDeviceToken(UUID deviceTokenUid) {
        return DeviceAccessTokenDalm.builder()
                .uid(UUID.randomUUID())
                .deviceTokenUid(deviceTokenUid)
                .token("access_token_for_" + deviceTokenUid)
                .createdAt(DEFAULT_CREATED_AT)
                .expiresAt(DEFAULT_EXPIRES_AT)
                .build();
    }
}