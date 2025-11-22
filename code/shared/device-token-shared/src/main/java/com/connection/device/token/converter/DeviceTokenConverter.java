
package com.connection.device.token.converter;

import com.connection.device.token.generator.DeviceTokenGenerator;
import com.connection.device.token.model.DeviceTokenBlm;
import com.connection.device.token.model.DeviceTokenDalm;
import com.connection.device.token.model.DeviceTokenDto;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

/** . */
@RequiredArgsConstructor
public class DeviceTokenConverter {

    private final DeviceTokenGenerator deviceTokenGenerator;

    /** . */
    public DeviceTokenBlm toBlm(DeviceTokenDalm dalm) {
        try {
            String token = deviceTokenGenerator.generateDeviceToken(
                    dalm.getDeviceUid(), dalm.getUid(), dalm.getCreatedAt(),
                    dalm.getExpiresAt());

            return DeviceTokenBlm.builder().token(token).uid(dalm.getUid())
                    .deviceUid(dalm.getDeviceUid())
                    .createdAt(dalm.getCreatedAt())
                    .expiresAt(dalm.getExpiresAt()).build();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(),
                    e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Invalid token parameters: " + e.getMessage(), e);
        }
    }

    /** . */
    public DeviceTokenBlm toBlm(DeviceTokenDto dto) {
        try {
            return deviceTokenGenerator.getDeviceTokenBlm(dto.getToken());
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(),
                    e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Malformed JWT token", e);
        }
    }

    /** . */
    public DeviceTokenDto toDto(DeviceTokenBlm blm) {
        return DeviceTokenDto.builder().token(blm.getToken()).build();
    }

    /** . */
    public DeviceTokenDalm toDalm(DeviceTokenBlm blm) {
        return DeviceTokenDalm.builder().uid(blm.getUid())
                .deviceUid(blm.getDeviceUid()).token(blm.getToken())
                .createdAt(blm.getCreatedAt()).expiresAt(blm.getExpiresAt())
                .build();
    }
}
