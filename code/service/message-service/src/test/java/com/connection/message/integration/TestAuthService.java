// TestAuthServiceResponder.java
package com.connection.message.integration;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import com.connection.client.model.ClientBlm;
import com.connection.service.auth.AuthService;
import com.connection.token.model.AccessTokenBlm;
import com.connection.token.model.RefreshTokenBlm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ActiveProfiles("integrationtest")
public class TestAuthService implements AuthService{
    // Хранилище валидных токенов и соответствующих clientUid
    private final Map<String, UUID> validTokens = new ConcurrentHashMap<>();


    // Методы для управления тестовыми данными
    public void addValidToken(String token, UUID clientUid) {
        validTokens.put(token, clientUid);
        log.info("🔑 Test Responder: Added valid token for client {}", clientUid);
    }

    public void removeToken(String token) {
        validTokens.remove(token);
        log.info("🗑️ Test Responder: Removed token");
    }

    public void clearTestData() {
        validTokens.clear();
        log.info("🧹 Test Responder: All auth test data cleared");
    }

    public boolean hasValidToken(String token) {
        return validTokens.containsKey(token);
    }

    @Override
    public Pair<AccessTokenBlm, RefreshTokenBlm> authorizeByEmail(String email, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authorizeByEmail'");
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        // TODO Auto-generated method stub
        return Map.of("status", "OK");
    }

    @Override
    public Pair<AccessTokenBlm, RefreshTokenBlm> refresh(RefreshTokenBlm refreshTokenBlm) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'refresh'");
    }

    @Override
    public void register(ClientBlm clientBlm) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'register'");
    }

    @Override
    public void validateAccessToken(AccessTokenBlm accessTokenBlm) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateAccessToken'");
    }

    @Override
    public AccessTokenBlm validateAccessToken(String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateAccessToken'");
    }

    @Override
    public void validateRefreshToken(RefreshTokenBlm refreshTokenBlm) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateRefreshToken'");
    }
}