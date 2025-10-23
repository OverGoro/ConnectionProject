// DeviceAccessTokenConverter.java
package com.connection.device.token.converter;

import com.connection.device.token.generator.DeviceAccessTokenGenerator;
import com.connection.device.token.model.DeviceAccessTokenBLM;
import com.connection.device.token.model.DeviceAccessTokenDALM;
import com.connection.device.token.model.DeviceAccessTokenDTO;

import io.jsonwebtoken.JwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

// DeviceAccessTokenConverter.java
@RequiredArgsConstructor
public class DeviceAccessTokenConverter {
    
    private final DeviceAccessTokenGenerator deviceTokenGenerator;

    public DeviceAccessTokenBLM toBLM(DeviceAccessTokenDALM dalm) {
        try {
            // ВАЛИДАЦИЯ существующего токена вместо генерации нового
            DeviceAccessTokenBLM validatedToken = deviceTokenGenerator.getDeviceAccessTokenBLM(dalm.getToken());
            
            return DeviceAccessTokenBLM.builder()
                    .token(dalm.getToken()) // Используем исходный токен
                    .uid(dalm.getUid())
                    .deviceTokenUid(validatedToken.getDeviceTokenUid()) // Извлекаем из токена
                    .createdAt(dalm.getCreatedAt())
                    .expiresAt(dalm.getExpiresAt())
                    .build();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid token parameters: " + e.getMessage(), e);
        }
    }

    public DeviceAccessTokenBLM toBLM(DeviceAccessTokenDTO dto) {
        try {
            return deviceTokenGenerator.getDeviceAccessTokenBLM(dto.getToken());
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Malformed JWT token", e);
        }
    }

    public DeviceAccessTokenDTO toDTO(DeviceAccessTokenBLM blm) {
        return DeviceAccessTokenDTO.builder()
                .token(blm.getToken())
                .build();
    }

    public DeviceAccessTokenDALM toDALM(DeviceAccessTokenBLM blm) {
        return DeviceAccessTokenDALM.builder()
                .uid(blm.getUid())
                .deviceTokenUid(blm.getDeviceTokenUid())
                .token(blm.getToken()) // Сохраняем тот же токен
                .createdAt(blm.getCreatedAt())
                .expiresAt(blm.getExpiresAt())
                .build();
    }
}