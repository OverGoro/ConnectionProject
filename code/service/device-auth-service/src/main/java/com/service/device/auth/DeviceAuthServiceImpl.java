// DeviceAuthServiceImpl.java
package com.service.device.auth;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;


import com.connection.device.token.generator.DeviceAccessTokenGenerator;
import com.connection.device.token.generator.DeviceTokenGenerator;
import com.connection.device.token.model.DeviceAccessTokenBLM;
import com.connection.device.token.model.DeviceTokenBLM;
import com.connection.device.token.repository.DeviceAccessTokenRepository;
import com.connection.device.token.repository.DeviceTokenRepository;
import com.connection.device.token.validator.DeviceAccessTokenValidator;
import com.connection.device.token.validator.DeviceTokenValidator;
import com.connection.service.auth.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@EnableAutoConfiguration(exclude = {
        JpaRepositoriesAutoConfiguration.class
})
//@EnableTransactionManagement
public class DeviceAuthServiceImpl implements DeviceAuthService {

    private final AuthService authService;


    private final DeviceTokenValidator deviceTokenValidator;
    private final DeviceAccessTokenValidator deviceAccessTokenValidator;

    private final DeviceTokenGenerator deviceTokenGenerator;
    private final DeviceAccessTokenGenerator deviceAccessTokenGenerator;

    private final DeviceTokenRepository deviceTokenRepository;
    private final DeviceAccessTokenRepository deviceAccessTokenRepository;

    @Qualifier("deviceTokenDuration")
    private final Duration deviceTokenDuration;
    @Qualifier("deviceAccessTokenDuration")
    private final Duration deviceAccessTokenDuration;

    @Override
    //@Transaction
    public DeviceTokenBLM createDeviceToken(UUID deviceUid) {
        log.info("Creating device token for device: {}", deviceUid);

        // Проверяем, нет ли уже активного токена
        if (deviceTokenRepository.existsByDeviceUid(deviceUid)) {
            throw new com.connection.device.token.exception.DeviceTokenAlreadyExistsException(
                    "Device token already exists for device: " + deviceUid);
        }

        Date createdAt = new Date();
        Date expiresAt = Date.from(createdAt.toInstant().plus(deviceTokenDuration));
        UUID tokenUid = UUID.randomUUID();

        // Генерируем токен
        String tokenString = deviceTokenGenerator.generateDeviceToken(deviceUid, tokenUid, createdAt, expiresAt);
        DeviceTokenBLM deviceTokenBLM = DeviceTokenBLM.builder()
                .token(tokenString)
                .uid(tokenUid)
                .deviceUid(deviceUid)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build();

        deviceTokenValidator.validate(deviceTokenBLM);

        // Сохраняем в БД

        deviceTokenRepository.add(deviceTokenBLM);

        log.info("Device token created successfully for device: {}", deviceUid);
        return deviceTokenBLM;
    }

    @Override
    //@Transaction(readOnly = true)
    public DeviceTokenBLM getDeviceToken(UUID deviceUid) {
        log.info("Getting device token for device: {}", deviceUid);

        DeviceTokenBLM deviceTokenBLM = deviceTokenRepository.findByDeviceUid(deviceUid);

        deviceTokenValidator.validate(deviceTokenBLM);
        return deviceTokenBLM;
    }

    @Override
    //@Transaction
    public void revokeDeviceToken(UUID deviceUid) {
        log.info("Revoking device token for device: {}", deviceUid);

        DeviceTokenBLM deviceTokenBLM = deviceTokenRepository.findByDeviceUid(deviceUid);
        deviceTokenRepository.revokeByDeviceUid(deviceUid);

        // Каскадно отзываем все access tokens
        deviceAccessTokenRepository.revokeByDeviceTokenUid(deviceTokenBLM.getUid());

        log.info("Device token revoked successfully for device: {}", deviceUid);
    }

    @Override
    public DeviceTokenBLM validateDeviceToken(DeviceTokenBLM deviceToken) {
        log.info("Validating device token for device: {}", deviceToken.getDeviceUid());
        deviceTokenValidator.validate(deviceToken);
        return deviceToken;
    }

    @Override
    //@Transaction
    public Pair<DeviceAccessTokenBLM, DeviceTokenBLM> createDeviceAccessToken(DeviceTokenBLM deviceToken) {
        log.info("Creating device access token for device token: {}", deviceToken.getUid());

        deviceTokenValidator.validate(deviceToken);

        // Проверяем, нет ли активного access token
        if (deviceAccessTokenRepository.hasDeviceAccessToken(deviceToken.getUid())) {
            throw new com.connection.device.token.exception.DeviceAccessTokenExistsException(
                    "Active device access token already exists for device token: " + deviceToken.getUid());
        }

        Date createdAt = new Date();
        Date expiresAt = Date.from(createdAt.toInstant().plus(deviceAccessTokenDuration));
        UUID accessTokenUid = UUID.randomUUID();

        // Генерируем access token
        String accessTokenString = deviceAccessTokenGenerator.generateDeviceAccessToken(
                deviceToken.getUid(), createdAt, expiresAt);

        DeviceAccessTokenBLM deviceAccessTokenBLM = DeviceAccessTokenBLM.builder()
                .token(accessTokenString)
                .uid(accessTokenUid)
                .deviceTokenUid(deviceToken.getUid())
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build();

        deviceAccessTokenValidator.validate(deviceAccessTokenBLM);

        // Сохраняем в БД

        deviceAccessTokenRepository.add(deviceAccessTokenBLM);

        log.info("Device access token created successfully for device token: {}", deviceToken.getUid());
        return Pair.of(deviceAccessTokenBLM, deviceToken);
    }

    @Override
    //@Transaction
    public DeviceAccessTokenBLM refreshDeviceAccessToken(DeviceAccessTokenBLM deviceAccessToken) {
        log.info("Refreshing device access token: {}", deviceAccessToken.getUid());

        deviceAccessTokenValidator.validate(deviceAccessToken);

        // Отзываем старый токен
        // DeviceAccessTokenBLM oldAccessTokenBLM =

        deviceAccessTokenRepository.revoke(deviceAccessToken.getUid());

        Date createdAt = new Date();
        Date expiresAt = Date.from(createdAt.toInstant().plus(deviceAccessTokenDuration));
        UUID newAccessTokenUid = UUID.randomUUID();

        // Генерируем новый access token
        String newAccessTokenString = deviceAccessTokenGenerator.generateDeviceAccessToken(
                deviceAccessToken.getDeviceTokenUid(), createdAt, expiresAt);

        DeviceAccessTokenBLM newDeviceAccessTokenBLM = DeviceAccessTokenBLM.builder()
                .token(newAccessTokenString)
                .uid(newAccessTokenUid)
                .deviceTokenUid(deviceAccessToken.getDeviceTokenUid())
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build();

        deviceAccessTokenValidator.validate(newDeviceAccessTokenBLM);

        // Сохраняем новый токен

        deviceAccessTokenRepository.add(newDeviceAccessTokenBLM);

        log.info("Device access token refreshed successfully");
        return newDeviceAccessTokenBLM;
    }

    @Override
    public DeviceAccessTokenBLM validateDeviceAccessToken(DeviceAccessTokenBLM deviceAccessToken) {
        log.info("Validating device access token: {}", deviceAccessToken.getUid());
        // Только JWT валидация, без проверки в БД
        deviceAccessTokenValidator.validate(deviceAccessToken);
        return deviceAccessToken;
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        try {
            var authHealth = authService.getHealthStatus();

            return Map.of(
                    "status", "OK",
                    "service", "device-service",
                    "timestamp", System.currentTimeMillis(),
                    "auth-service", authHealth!= null ? authHealth : "UNAVAILABLE");
        } catch (Exception e) {
            log.error("Kafka Client: ", e);
            return Map.of(
                    "status", "DEGRADED",
                    "service", "device-service",
                    "timestamp", System.currentTimeMillis(),
                    "auth-service", "UNAVAILABLE",
                    "error", e.getMessage());
        }
    }

    @Override
    public DeviceTokenBLM validateDeviceToken(String deviceTokenString) { 
        DeviceTokenBLM deviceTokenBLM = deviceTokenGenerator.getDeviceTokenBLM(deviceTokenString);
        deviceTokenValidator.validate(deviceTokenBLM);
        return validateDeviceToken(deviceTokenBLM);
    }

    @Override
    public DeviceAccessTokenBLM validateDeviceAccessToken(String deviceAccessTokenString) {
        DeviceAccessTokenBLM deviceAccessTokenBLM = deviceAccessTokenGenerator.getDeviceAccessTokenBLM(deviceAccessTokenString);
        deviceAccessTokenValidator.validate(deviceAccessTokenBLM);
        return validateDeviceAccessToken(deviceAccessTokenBLM);
    }
}