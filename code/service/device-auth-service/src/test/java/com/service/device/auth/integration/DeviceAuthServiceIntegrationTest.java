
package com.service.device.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.connection.device.token.model.DeviceAccessTokenBlm;
import com.connection.device.token.model.DeviceTokenBlm;
import com.connection.service.auth.AuthService;
import com.service.device.auth.DeviceAuthService;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Slf4j
@SpringBootTest
@ActiveProfiles("integrationtest")
@DisplayName("Device Auth Service Integration Tests")
public class DeviceAuthServiceIntegrationTest extends BaseDeviceAuthIntegrationTest {

    @Autowired
    private DeviceAuthService deviceAuthService;

    @MockitoBean
    protected AuthService authService;

    private UUID testDeviceUid;
    private UUID testDeviceUid2;

    @BeforeEach
    void setUpTestData() {
        testDeviceUid = UUID.randomUUID();
        testDeviceUid2 = UUID.randomUUID();

        // Инициализируем тестовые устройства в БД
        initializeTestDevices();

        log.info("Created test IDs - Device1: {}, Device2: {}, Client: {}", 
                testDeviceUid, testDeviceUid2, getTestClientUid());
    }

    @AfterEach
    void cleanupTestData() {
        cleanupAllTestData();
    }

    @Test
    @DisplayName("Should create device token successfully")
    @Order(1)
    void shouldCreateDeviceTokenSuccessfully() {
        // Given
        setupAuthentication();

        // When
        DeviceTokenBlm createdToken = deviceAuthService.createDeviceToken(testDeviceUid);

        // Then
        assertThat(createdToken).isNotNull();
        assertThat(createdToken.getDeviceUid()).isEqualTo(testDeviceUid);
        assertThat(createdToken.getToken()).isNotBlank();
        assertThat(createdToken.getExpiresAt()).isAfter(new Date());

        log.info("Successfully created device token for device: {}", testDeviceUid);
    }

    @Test
    @Order(2)
    @DisplayName("Should get device token by device UID")
    void shouldGetDeviceTokenByDeviceUid() {
        // Given
        setupAuthentication();
        DeviceTokenBlm originalToken = deviceAuthService.createDeviceToken(testDeviceUid);

        // When
        DeviceTokenBlm foundToken = deviceAuthService.getDeviceToken(testDeviceUid);

        // Then
        assertThat(foundToken).isNotNull();
        assertThat(foundToken.getUid()).isEqualTo(originalToken.getUid());
        assertThat(foundToken.getDeviceUid()).isEqualTo(testDeviceUid);
        assertThat(foundToken.getToken()).isEqualTo(originalToken.getToken());

        log.info("Successfully retrieved device token for device: {}", testDeviceUid);
    }

    @Test
    @Order(3)
    @DisplayName("Should revoke device token successfully")
    void shouldRevokeDeviceTokenSuccessfully() {
        // Given
        setupAuthentication();
        DeviceTokenBlm deviceToken = deviceAuthService.createDeviceToken(testDeviceUid);

        // Verify token exists
        DeviceTokenBlm foundToken = deviceAuthService.getDeviceToken(testDeviceUid);
        assertThat(foundToken).isNotNull();

        // When
        deviceAuthService.revokeDeviceToken(testDeviceUid);

        // Then
        assertThatThrownBy(() -> deviceAuthService.getDeviceToken(testDeviceUid))
                .isInstanceOf(com.connection.device.token.exception.DeviceTokenNotFoundException.class);

        log.info("Successfully revoked device token for device: {}", testDeviceUid);
    }

    @Test
    @Order(4)
    @DisplayName("Should validate device token successfully")
    void shouldValidateDeviceTokenSuccessfully() {
        // Given
        setupAuthentication();
        DeviceTokenBlm deviceToken = deviceAuthService.createDeviceToken(testDeviceUid);

        // When & Then - No exception should be thrown
        deviceAuthService.validateDeviceToken(deviceToken);

        log.info("Successfully validated device token for device: {}", testDeviceUid);
    }

    @Test
    @Order(5)
    @DisplayName("Should create device access token successfully")
    void shouldCreateDeviceAccessTokenSuccessfully() {
        // Given
        setupAuthentication();
        DeviceTokenBlm deviceToken = deviceAuthService.createDeviceToken(testDeviceUid);

        // When
        Pair<DeviceAccessTokenBlm, DeviceTokenBlm> result = deviceAuthService.createDeviceAccessToken(deviceToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFirst()).isNotNull(); // DeviceAccessTokenBlm
        assertThat(result.getSecond()).isNotNull(); // DeviceTokenBlm
        
        DeviceAccessTokenBlm accessToken = result.getFirst();
        assertThat(accessToken.getToken()).isNotBlank();
        assertThat(accessToken.getExpiresAt()).isAfter(new Date());
        assertThat(accessToken.getDeviceTokenUid()).isEqualTo(deviceToken.getUid());

        log.info("Successfully created device access token for device: {}", testDeviceUid);
    }

    @Test
    @Order(6)
    @DisplayName("Should refresh device access token successfully")
    void shouldRefreshDeviceAccessTokenSuccessfully() {
        // Given
        setupAuthentication();
        DeviceTokenBlm deviceToken = deviceAuthService.createDeviceToken(testDeviceUid);
        Pair<DeviceAccessTokenBlm, DeviceTokenBlm> originalResult = deviceAuthService.createDeviceAccessToken(deviceToken);
        DeviceAccessTokenBlm originalAccessToken = originalResult.getFirst();
        sleep(1000);
        // When
        DeviceAccessTokenBlm refreshedToken = deviceAuthService.refreshDeviceAccessToken(originalAccessToken);

        // Then
        assertThat(refreshedToken).isNotNull();
        assertThat(refreshedToken.getToken()).isNotBlank();
        assertThat(refreshedToken.getToken()).isNotEqualTo(originalAccessToken.getToken());
        assertThat(refreshedToken.getExpiresAt()).isAfter(new Date());
        assertThat(refreshedToken.getDeviceTokenUid()).isEqualTo(originalAccessToken.getDeviceTokenUid());

        log.info("Successfully refreshed device access token for device: {}", testDeviceUid);
    }

    @Test
    @Order(7)
    @DisplayName("Should validate device access token successfully")
    void shouldValidateDeviceAccessTokenSuccessfully() {
        // Given
        setupAuthentication();
        DeviceTokenBlm deviceToken = deviceAuthService.createDeviceToken(testDeviceUid);
        Pair<DeviceAccessTokenBlm, DeviceTokenBlm> result = deviceAuthService.createDeviceAccessToken(deviceToken);
        DeviceAccessTokenBlm accessToken = result.getFirst();

        // When & Then - No exception should be thrown
        deviceAuthService.validateDeviceAccessToken(accessToken);

        log.info("Successfully validated device access token for device: {}", testDeviceUid);
    }

    @Test
    @Order(9)
    @DisplayName("Should throw exception when creating duplicate device token")
    void shouldThrowExceptionWhenCreatingDuplicateDeviceToken() {
        // Given
        setupAuthentication();
        deviceAuthService.createDeviceToken(testDeviceUid);

        // When & Then
        assertThatThrownBy(() -> deviceAuthService.createDeviceToken(testDeviceUid))
                .isInstanceOf(com.connection.device.token.exception.DeviceTokenAlreadyExistsException.class);

        log.info("✅ Correctly prevented duplicate device token creation");
    }

    @Test
    @Order(10)
    @DisplayName("Should throw exception when device token not found")
    void shouldThrowExceptionWhenDeviceTokenNotFound() {
        // Given
        setupAuthentication();
        UUID nonExistentDeviceUid = UUID.randomUUID();

        // When & Then
        assertThatThrownBy(() -> deviceAuthService.getDeviceToken(nonExistentDeviceUid))
                .isInstanceOf(com.connection.device.token.exception.DeviceTokenNotFoundException.class);

        log.info("✅ Correctly handled non-existent device token");
    }

    @Test
    @Order(11)
    @DisplayName("Should throw exception when creating duplicate device access token")
    void shouldThrowExceptionWhenCreatingDuplicateDeviceAccessToken() {
        // Given
        setupAuthentication();
        DeviceTokenBlm deviceToken = deviceAuthService.createDeviceToken(testDeviceUid);
        deviceAuthService.createDeviceAccessToken(deviceToken);

        // When & Then
        assertThatThrownBy(() -> deviceAuthService.createDeviceAccessToken(deviceToken))
                .isInstanceOf(com.connection.device.token.exception.DeviceAccessTokenExistsException.class);

        log.info("✅ Correctly prevented duplicate device access token creation");
    }


    @Test
    @Order(13)
    @DisplayName("Should connect to databases")
    void shouldConnectToDatabases() {
        // Given
        String testQuery = "SELECT 1";

        // When
        Integer deviceTokenResult = deviceTokenJdbcTemplate.getJdbcTemplate()
                .queryForObject(testQuery, Integer.class);
        Integer deviceAccessTokenResult = deviceAccessTokenJdbcTemplate.getJdbcTemplate()
                .queryForObject(testQuery, Integer.class);

        // Then
        assertThat(deviceTokenResult).isEqualTo(1);
        assertThat(deviceAccessTokenResult).isEqualTo(1);
        log.info("✅ Both database connections test passed");
    }

    /**
     * Инициализирует тестовые устройства в БД
     */
    private void initializeTestDevices() {
        try {
            // Создаем тестовые устройства
            String insertDeviceSql = """
                    INSERT INTO core.device (uid, client_uuid, device_name, device_description)
                    VALUES (:uid, :clientUuid, :deviceName, :deviceDescription)
                    ON CONFLICT (uid) DO NOTHING
                    """;

            // Устройство 1
            int devicesInserted1 = deviceTokenJdbcTemplate.update(insertDeviceSql, Map.of(
                    "uid", testDeviceUid,
                    "clientUuid", getTestClientUid(),
                    "deviceName", "Test Device " + testDeviceUid.toString().substring(0, 8),
                    "deviceDescription", "Integration test device 1"));

            // Устройство 2
            int devicesInserted2 = deviceTokenJdbcTemplate.update(insertDeviceSql, Map.of(
                    "uid", testDeviceUid2,
                    "clientUuid", getTestClientUid(),
                    "deviceName", "Test Device " + testDeviceUid2.toString().substring(0, 8),
                    "deviceDescription", "Integration test device 2"));

            if (devicesInserted1 > 0 || devicesInserted2 > 0) {
                log.info("✅ Created test devices: {} and {}", testDeviceUid, testDeviceUid2);
            }

        } catch (Exception e) {
            log.error("❌ Failed to initialize test devices: {}", e.getMessage(), e);
            throw new RuntimeException("Test devices initialization failed", e);
        }
    }
}