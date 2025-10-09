// DeviceTokenConverter.java
package com.connection.device.token.converter;

import com.connection.device.token.generator.DeviceTokenGenerator;
import com.connection.device.token.model.DeviceTokenBLM;
import com.connection.device.token.model.DeviceTokenDALM;
import com.connection.device.token.model.DeviceTokenDTO;

import io.jsonwebtoken.JwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeviceTokenConverter {
    @NonNull
    private final DeviceTokenGenerator deviceTokenGenerator;

    public DeviceTokenBLM toBLM(DeviceTokenDALM dalm) {
        try {
            String token = deviceTokenGenerator.generateDeviceToken(
                dalm.getDeviceUid(), dalm.getCreatedAt(), dalm.getExpiresAt());
            
            return DeviceTokenBLM.builder()
                    .token(token)
                    .uid(dalm.getUid())
                    .deviceUid(dalm.getDeviceUid())
                    .createdAt(dalm.getCreatedAt())
                    .expiresAt(dalm.getExpiresAt())
                    .build();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid token parameters: " + e.getMessage(), e);
        }
    }

    public DeviceTokenBLM toBLM(DeviceTokenDTO dto) {
        try {
            return deviceTokenGenerator.getDeviceTokenBLM(dto.getToken());
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Malformed JWT token", e);
        }
    }

    public DeviceTokenDTO toDTO(DeviceTokenBLM blm) {
        return DeviceTokenDTO.builder()
                .token(blm.getToken())
                .build();
    }

    public DeviceTokenDALM toDALM(DeviceTokenBLM blm) {
        return DeviceTokenDALM.builder()
                .uid(blm.getUid())
                .deviceUid(blm.getDeviceUid())
                .token(blm.getToken())
                .createdAt(blm.getCreatedAt())
                .expiresAt(blm.getExpiresAt())
                .build();
    }
}