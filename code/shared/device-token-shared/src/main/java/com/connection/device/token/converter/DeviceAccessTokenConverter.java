
package com.connection.device.token.converter;

import com.connection.device.token.generator.DeviceAccessTokenGenerator;
import com.connection.device.token.model.DeviceAccessTokenBlm;
import com.connection.device.token.model.DeviceAccessTokenDalm;
import com.connection.device.token.model.DeviceAccessTokenDto;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;


/** . */
@RequiredArgsConstructor
public class DeviceAccessTokenConverter {

    private final DeviceAccessTokenGenerator deviceTokenGenerator;

    /** . */
    public DeviceAccessTokenBlm toBlm(DeviceAccessTokenDalm dalm) {
        try {
            // ВАЛИДАЦИЯ существующего токена вместо генерации нового
            DeviceAccessTokenBlm validatedToken = deviceTokenGenerator
                    .getDeviceAccessTokenBlm(dalm.getToken());

            return DeviceAccessTokenBlm.builder().token(dalm.getToken()) 
                    .uid(dalm.getUid())
                    .deviceTokenUid(validatedToken.getDeviceTokenUid()) // Извлекаем из токена
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
    public DeviceAccessTokenBlm toBlm(DeviceAccessTokenDto dto) {
        try {
            return deviceTokenGenerator.getDeviceAccessTokenBlm(dto.getToken());
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(),
                    e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Malformed JWT token", e);
        }
    }

    /** . */
    public DeviceAccessTokenDto toDto(DeviceAccessTokenBlm blm) {
        return DeviceAccessTokenDto.builder().token(blm.getToken()).build();
    }

    /** . */
    public DeviceAccessTokenDalm toDalm(DeviceAccessTokenBlm blm) {
        return DeviceAccessTokenDalm.builder().uid(blm.getUid())
                .deviceTokenUid(blm.getDeviceTokenUid()).token(blm.getToken()) 
                .createdAt(blm.getCreatedAt()).expiresAt(blm.getExpiresAt())
                .build();
    }
}
