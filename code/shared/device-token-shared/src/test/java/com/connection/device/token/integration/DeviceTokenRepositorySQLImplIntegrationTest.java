package com.connection.device.token.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.connection.device.token.exception.DeviceTokenAlreadyExistsException;
import com.connection.device.token.exception.DeviceTokenNotFoundException;
import com.connection.device.token.generator.DeviceTokenGenerator;
import com.connection.device.token.model.DeviceTokenBLM;
import com.connection.device.token.repository.DeviceTokenRepository;
import com.connection.device.token.repository.DeviceTokenRepositorySQLImpl;

import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisplayName("Device Token Repository SQL Implementation Integration Tests")
public class DeviceTokenRepositorySQLImplIntegrationTest extends BaseDeviceTokenRepositoryIntegrationTest {

    private DeviceTokenRepository deviceTokenRepository;
    private DeviceTokenGenerator deviceTokenGenerator;

    @BeforeEach
    void setUpRepository() {
        // Создаем генератор токенов для тестов
        var secretKey = Keys.hmacShaKeyFor("test-secret-key-256-bits-long-123456789012".getBytes());
        this.deviceTokenGenerator = new DeviceTokenGenerator(secretKey, "test-app", "device-token");
        this.deviceTokenRepository = new DeviceTokenRepositorySQLImpl(jdbcTemplate, deviceTokenGenerator);
    }

    @Test
    @DisplayName("Should add device token successfully")
    void shouldAddDeviceTokenSuccessfully() {
        // Given
        DeviceTokenBLM testToken = DeviceTokenBLM.builder()
                .uid(testDeviceTokenUid)
                .deviceUid(testDeviceUid)
                .token("test.token." + UUID.randomUUID())
                .createdAt(new Date())
                .expiresAt(new Date(System.currentTimeMillis() + 3600000))
                .build();

        // When
        deviceTokenRepository.add(testToken);

        // Then
        DeviceTokenBLM foundToken = deviceTokenRepository.findByUid(testDeviceTokenUid);
        
        assertThat(foundToken).isNotNull();
        assertThat(foundToken.getUid()).isEqualTo(testDeviceTokenUid);
        assertThat(foundToken.getDeviceUid()).isEqualTo(testDeviceUid);

        log.info("✅ Successfully added and retrieved device token: {}", testDeviceTokenUid);
    }

    @Test
    @DisplayName("Should throw exception when adding device token with duplicate UID")
    void shouldThrowExceptionWhenAddingDeviceTokenWithDuplicateUid() {
        // Given
        createTestDeviceTokenInDatabase();

        DeviceTokenBLM duplicateToken = DeviceTokenBLM.builder()
                .uid(testDeviceTokenUid) // тот же UID
                .deviceUid(testDeviceUid)
                .token("different.token")
                .createdAt(new Date())
                .expiresAt(new Date(System.currentTimeMillis() + 3600000))
                .build();

        // When & Then
        assertThatThrownBy(() -> deviceTokenRepository.add(duplicateToken))
                .isInstanceOf(DeviceTokenAlreadyExistsException.class);

        log.info("✅ Correctly prevented duplicate device token UID");
    }

    @Test
    @DisplayName("Should find device token by UID")
    void shouldFindDeviceTokenByUid() {
        // Given
        createTestDeviceTokenInDatabase();

        // When
        DeviceTokenBLM foundToken = deviceTokenRepository.findByUid(testDeviceTokenUid);

        // Then
        assertThat(foundToken).isNotNull();
        assertThat(foundToken.getUid()).isEqualTo(testDeviceTokenUid);
        assertThat(foundToken.getDeviceUid()).isEqualTo(testDeviceUid);

        log.info("✅ Successfully found device token by UID: {}", testDeviceTokenUid);
    }

    @Test
    @DisplayName("Should find device token by device UID")
    void shouldFindDeviceTokenByDeviceUid() {
        // Given
        createTestDeviceTokenInDatabase();

        // When
        DeviceTokenBLM foundToken = deviceTokenRepository.findByDeviceUid(testDeviceUid);

        // Then
        assertThat(foundToken).isNotNull();
        assertThat(foundToken.getDeviceUid()).isEqualTo(testDeviceUid);
        assertThat(foundToken.getUid()).isEqualTo(testDeviceTokenUid);

        log.info("✅ Successfully found device token by device UID: {}", testDeviceUid);
    }

    @Test
    @DisplayName("Should throw exception when device token not found by UID")
    void shouldThrowExceptionWhenDeviceTokenNotFoundByUid() {
        // Given
        UUID nonExistentUid = UUID.randomUUID();

        // When & Then
        assertThatThrownBy(() -> deviceTokenRepository.findByUid(nonExistentUid))
                .isInstanceOf(DeviceTokenNotFoundException.class);

        log.info("✅ Correctly handled non-existent device token UID");
    }

    @Test
    @DisplayName("Should revoke device token successfully")
    void shouldRevokeDeviceTokenSuccessfully() {
        // Given
        createTestDeviceTokenInDatabase();
        
        // Verify token exists
        DeviceTokenBLM existingToken = deviceTokenRepository.findByUid(testDeviceTokenUid);
        assertThat(existingToken).isNotNull();

        // When
        deviceTokenRepository.revoke(testDeviceTokenUid);

        // Then - token should not exist
        assertThatThrownBy(() -> deviceTokenRepository.findByUid(testDeviceTokenUid))
                .isInstanceOf(DeviceTokenNotFoundException.class);

        log.info("✅ Successfully revoked device token: {}", testDeviceTokenUid);
    }

    @Test
    @DisplayName("Should revoke all device tokens by device UID")
    void shouldRevokeAllDeviceTokensByDeviceUid() {
        // Given
        createTestDeviceTokenInDatabase();
        
        // Verify token exists
        DeviceTokenBLM existingToken = deviceTokenRepository.findByDeviceUid(testDeviceUid);
        assertThat(existingToken).isNotNull();

        // When
        deviceTokenRepository.revokeByDeviceUid(testDeviceUid);

        // Then - token should not exist
        assertThatThrownBy(() -> deviceTokenRepository.findByDeviceUid(testDeviceUid))
                .isInstanceOf(DeviceTokenNotFoundException.class);

        log.info("✅ Successfully revoked all device tokens for device: {}", testDeviceUid);
    }

    @Test
    @DisplayName("Should check if device token exists by device UID")
    void shouldCheckIfDeviceTokenExistsByDeviceUid() {
        // Given
        createTestDeviceTokenInDatabase();

        // When
        boolean exists = deviceTokenRepository.existsByDeviceUid(testDeviceUid);

        // Then
        assertThat(exists).isTrue();

        log.info("✅ Correctly checked device token existence for device: {}", testDeviceUid);
    }

    @Test
    @DisplayName("Should clean up expired device tokens")
    void shouldCleanUpExpiredDeviceTokens() {
        // Given - создаем истекший токен
        String insertExpiredTokenSql = """
            INSERT INTO access.device_token (uid, device_uid, token, created_at, expires_at)
            VALUES (:uid, :device_uid, :token, :created_at, :expires_at)
            """;
        
        UUID expiredTokenUid = UUID.randomUUID();
        jdbcTemplate.update(insertExpiredTokenSql, Map.of(
            "uid", expiredTokenUid,
            "device_uid", testDeviceUid,
            "token", "expired.token",
            "created_at", new java.sql.Timestamp(System.currentTimeMillis() - 7200000), // -2 hours
            "expires_at", new java.sql.Timestamp(System.currentTimeMillis() - 3600000)  // -1 hour
        ));

        // When
        deviceTokenRepository.cleanUpExpired();

        // Then - истекший токен должен быть удален
        assertThatThrownBy(() -> deviceTokenRepository.findByUid(expiredTokenUid))
                .isInstanceOf(DeviceTokenNotFoundException.class);

        log.info("✅ Successfully cleaned up expired device tokens");
    }
}