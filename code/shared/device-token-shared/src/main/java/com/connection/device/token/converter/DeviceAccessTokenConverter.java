// DeviceAccessTokenConverter.java
package com.connection.device.token.converter;

import com.connection.device.token.generator.DeviceAccessTokenGenerator;
import com.connection.device.token.model.DeviceAccessTokenBLM;
import com.connection.device.token.model.DeviceAccessTokenDALM;
import com.connection.device.token.model.DeviceAccessTokenDTO;

import io.jsonwebtoken.JwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeviceAccessTokenConverter {
    @NonNull
    private final DeviceAccessTokenGenerator deviceTokenGenerator;

    public DeviceAccessTokenBLM toBLM(DeviceAccessTokenDALM dalm) {
        try {
            String token = deviceTokenGenerator.generateDeviceAccessToken(
                dalm.getDeviceTokenUid(), dalm.getCreatedAt(), dalm.getExpiresAt());
            
            return DeviceAccessTokenBLM.builder()
                    .token(token)
                    .uid(dalm.getUid())
                    .deviceTokenUid(dalm.getDeviceTokenUid())
                    .createdAt(dalm.getCreatedAt())
                    .expiresAt(dalm.getExpiresAt())
                    .build();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(), e);
        }
    }

    public DeviceAccessTokenBLM toBLM(DeviceAccessTokenDTO dto) {
        try {
            return deviceTokenGenerator.getDeviceAccessTokenBLM(dto.getToken());
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(), e);
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
                .token(blm.getToken()) // Сохраняем сгенерированный токен
                .createdAt(blm.getCreatedAt())
                .expiresAt(blm.getExpiresAt())
                .build();
    }
}