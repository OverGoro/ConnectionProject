// BaseDeviceAuthIntegrationTest.java
package com.service.device.auth.integration;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@TestMethodOrder(MethodOrderer.Random.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public abstract class BaseDeviceAuthIntegrationTest {

    @Autowired
    protected Environment environment;

    protected TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    protected NamedParameterJdbcTemplate deviceTokenJdbcTemplate;

    @Autowired
    protected NamedParameterJdbcTemplate deviceAccessTokenJdbcTemplate;

    protected final Map<String, String> testData = new ConcurrentHashMap<>();
    protected UUID testClientUid;
    protected String testAuthToken;

    @BeforeEach
    void setUp() {
        checkConfig();
        testClientUid = UUID.randomUUID();
        testAuthToken = "test-token-" + UUID.randomUUID().toString();

        // Инициализируем testData перед использованием
        testData.clear();
        
        // Инициализируем тестовые данные в БД
        initializeTestClient();
    }

    @AfterEach
    void tearDown() {
        testData.clear();
        cleanupAllTestData();
        clearAuthentication();
    }

    /**
     * Настраивает аутентификацию через SecurityContext
     */
    protected void setupAuthentication() {
        setupAuthentication(this.testClientUid);
    }

    /**
     * Настраивает аутентификацию для указанного clientUid
     */
    protected void setupAuthentication(UUID clientUid) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                clientUid,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        log.info("✅ Test authentication setup for client: {}", clientUid);
    }

    protected void clearAuthentication() {
        SecurityContextHolder.clearContext();
        log.info("🔒 Test authentication cleared");
    }

    /**
     * Настраивает валидный токен для клиента
     */
    protected void setupValidToken(UUID clientUid, String token) {
        log.info("✅ Test token setup for client: {}", clientUid);
    }

    protected void checkConfig() {
        log.info("=== Device Auth Service Integration Test Configuration ===");
        log.info("Active profiles: {}", Arrays.toString(environment.getActiveProfiles()));
        log.info("Device Token DB URL: {}", environment.getProperty("app.datasource.device-token.xa-properties.url"));
        log.info("Device Access Token DB URL: {}", environment.getProperty("app.datasource.device-access-token.xa-properties.url"));
        log.info("Service name: {}", environment.getProperty("spring.application.name"));
        log.info("==========================================================");
    }

    /**
     * Инициализирует тестового клиента в БД
     */
    protected void initializeTestClient() {
        try {
            // Создаем тестового клиента
            String insertClientSql = """
                    INSERT INTO core.client (uid, email, birth_date, username, password)
                    VALUES (:uid, :email, CURRENT_DATE - INTERVAL '25 years', :username, :password)
                    ON CONFLICT (uid) DO NOTHING
                    """;

            int clientsInserted = deviceTokenJdbcTemplate.update(insertClientSql, Map.of(
                    "uid", testClientUid,
                    "email", "test.client." + testClientUid + "@example.com",
                    "username", "testclient_" + testClientUid.toString().substring(0, 8),
                    "password", "testpassword123"));

            if (clientsInserted > 0) {
                log.info("✅ Created test client: {}", testClientUid);
            }

        } catch (Exception e) {
            log.error("❌ Failed to initialize test client: {}", e.getMessage(), e);
            throw new RuntimeException("Test client initialization failed", e);
        }
    }

    protected void cleanupAllTestData() {
        try {
            log.info("Cleaning up data for client: {}", testClientUid);

            // Очистка device access tokens через device tokens
            String deleteAccessTokensSql = """
                    DELETE FROM device_access_token dat
                    WHERE EXISTS (
                        SELECT 1 FROM device_token dt 
                        WHERE dt.uid = dat.device_token_uid 
                        AND dt.device_uid IN (
                            SELECT uid FROM core.device WHERE client_uuid = :clientUid
                        )
                    )
                    """;
            try {
                int accessTokensDeleted = deviceAccessTokenJdbcTemplate.update(deleteAccessTokensSql, 
                        Map.of("clientUid", testClientUid));
                if (accessTokensDeleted > 0) {
                    log.debug("Deleted {} device access tokens for client: {}", accessTokensDeleted, testClientUid);
                }
            } catch (Exception e) {
                log.debug("No device access tokens to delete for client: {}", testClientUid);
            }

            // Очистка device tokens
            String deleteDeviceTokensSql = """
                    DELETE FROM device_token 
                    WHERE device_uid IN (
                        SELECT uid FROM core.device WHERE client_uuid = :clientUid
                    )
                    """;
            try {
                int deviceTokensDeleted = deviceTokenJdbcTemplate.update(deleteDeviceTokensSql, 
                        Map.of("clientUid", testClientUid));
                if (deviceTokensDeleted > 0) {
                    log.debug("Deleted {} device tokens for client: {}", deviceTokensDeleted, testClientUid);
                }
            } catch (Exception e) {
                log.debug("No device tokens to delete for client: {}", testClientUid);
            }

            // Очистка устройств клиента
            String deleteDevicesSql = "DELETE FROM core.device WHERE client_uuid = :clientUid";
            try {
                int devicesDeleted = deviceTokenJdbcTemplate.update(deleteDevicesSql, 
                        Map.of("clientUid", testClientUid));
                if (devicesDeleted > 0) {
                    log.debug("Deleted {} devices for client: {}", devicesDeleted, testClientUid);
                }
            } catch (Exception e) {
                log.debug("No devices to delete for client: {}", testClientUid);
            }

            // Очистка самого клиента
            String deleteClientSql = "DELETE FROM core.client WHERE uid = :clientUid";
            try {
                int clientsDeleted = deviceTokenJdbcTemplate.update(deleteClientSql, 
                        Map.of("clientUid", testClientUid));
                if (clientsDeleted > 0) {
                    log.info("✅ Cleaned up client and all related data: {}", testClientUid);
                }
            } catch (Exception e) {
                log.debug("No client to delete: {}", testClientUid);
            }

        } catch (Exception e) {
            log.warn("Cleanup warning for client {}: {}", testClientUid, e.getMessage());
        }
    }

    protected void cleanupDeviceTokenData(UUID deviceUid) {
        try {
            log.info("Cleaning up device token data for device UID: {}", deviceUid);

            // Сначала удаляем access tokens
            String deleteAccessTokensSql = """
                    DELETE FROM device_access_token 
                    WHERE device_token_uid IN (
                        SELECT uid FROM device_token WHERE device_uid = :deviceUid
                    )
                    """;
            int accessTokensDeleted = deviceAccessTokenJdbcTemplate.update(deleteAccessTokensSql, 
                    Map.of("deviceUid", deviceUid));

            // Затем удаляем device token
            String deleteDeviceTokenSql = "DELETE FROM device_token WHERE device_uid = :deviceUid";
            int deviceTokensDeleted = deviceTokenJdbcTemplate.update(deleteDeviceTokenSql, 
                    Map.of("deviceUid", deviceUid));

            if (accessTokensDeleted > 0 || deviceTokensDeleted > 0) {
                log.info("Deleted {} device tokens and {} access tokens for device: {}", 
                        deviceTokensDeleted, accessTokensDeleted, deviceUid);
            }

        } catch (Exception e) {
            log.warn("Cleanup warning for device UID {}: {}", deviceUid, e.getMessage());
        }
    }

    protected void sleep(long milliseconds) {
        try {
            log.debug("Sleeping for {} ms", milliseconds);
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Sleep interrupted", e);
        }
    }

    protected UUID getTestClientUid() {
        return testClientUid;
    }

    protected String getTestAuthToken() {
        return testAuthToken;
    }
}