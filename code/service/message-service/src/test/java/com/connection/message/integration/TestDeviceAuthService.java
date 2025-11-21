// TestDeviceAuthServiceResponder.java
package com.connection.message.integration;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Primary;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import com.connection.device.token.model.DeviceAccessTokenBlm;
import com.connection.device.token.model.DeviceTokenBlm;
import com.service.device.auth.DeviceAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
@ActiveProfiles("integrationtest")
public class TestDeviceAuthService implements DeviceAuthService {
    // Хранилище валидных device токенов и соответствующих deviceUid
    private final Map<String, UUID> validDeviceTokens = new ConcurrentHashMap<>();

    // Методы для управления тестовыми данными
    public void addValidDeviceToken(String token, UUID deviceUid) {
        validDeviceTokens.put(token, deviceUid);
        log.info("🔑 Test Responder: Added valid device token for device {}", deviceUid);
    }

    public void removeDeviceToken(String token) {
        validDeviceTokens.remove(token);
        log.info("🗑️ Test Responder: Removed device token");
    }

    public void clearTestData() {
        validDeviceTokens.clear();
        log.info("🧹 Test Responder: All device auth test data cleared");
    }

    public boolean hasValidDeviceToken(String token) {
        return validDeviceTokens.containsKey(token);
    }

    @Override
    public Pair<DeviceAccessTokenBlm, DeviceTokenBlm> createDeviceAccessToken(DeviceTokenBlm deviceToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createDeviceAccessToken'");
    }

    @Override
    public DeviceTokenBlm createDeviceToken(UUID deviceUid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createDeviceToken'");
    }

    @Override
    public DeviceTokenBlm getDeviceToken(UUID deviceUid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDeviceToken'");
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHealthStatus'");
    }

    @Override
    public DeviceAccessTokenBlm refreshDeviceAccessToken(DeviceAccessTokenBlm deviceAccessToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'refreshDeviceAccessToken'");
    }

    @Override
    public void revokeDeviceToken(UUID deviceUid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'revokeDeviceToken'");
    }

    @Override
    public DeviceAccessTokenBlm validateDeviceAccessToken(DeviceAccessTokenBlm deviceAccessToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateDeviceAccessToken'");
    }

    @Override
    public DeviceAccessTokenBlm validateDeviceAccessToken(String deviceAccessTokenString) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateDeviceAccessToken'");
    }

    @Override
    public DeviceTokenBlm validateDeviceToken(DeviceTokenBlm deviceToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateDeviceToken'");
    }

    @Override
    public DeviceTokenBlm validateDeviceToken(String deviceTokenString) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateDeviceToken'");
    }
}