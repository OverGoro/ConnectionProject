package com.connection.device.token.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseDeviceTokenRepositoryIntegrationTest {

    protected NamedParameterJdbcTemplate jdbcTemplate;
    protected UUID testDeviceUid;
    protected UUID testDeviceTokenUid;
    protected UUID testDeviceAccessTokenUid;
    protected UUID testClientUid;

    @BeforeEach
    void setUp() {
        // Создаем DataSource напрямую
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5434/test_db");
        dataSource.setUsername("test_user");
        dataSource.setPassword("test_password");
        
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        
        testClientUid = UUID.randomUUID();
        testDeviceUid = UUID.randomUUID();
        testDeviceTokenUid = UUID.randomUUID();
        testDeviceAccessTokenUid = UUID.randomUUID();
        
        cleanupTestData();
        createTestDeviceInDatabase();
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    protected void cleanupTestData() {
        try {
            // Очищаем в правильном порядке из-за foreign key constraints
            String deleteAccessTokens = "DELETE FROM access.device_access_token WHERE uid = :uid OR device_token_uid = :device_token_uid";
            jdbcTemplate.update(deleteAccessTokens, Map.of(
                "uid", testDeviceAccessTokenUid,
                "device_token_uid", testDeviceTokenUid
            ));
            
            String deleteDeviceTokens = "DELETE FROM access.device_token WHERE uid = :uid OR device_uid = :device_uid";
            jdbcTemplate.update(deleteDeviceTokens, Map.of(
                "uid", testDeviceTokenUid,
                "device_uid", testDeviceUid
            ));
            
            String deleteDevice = "DELETE FROM core.device WHERE uid = :uid";
            jdbcTemplate.update(deleteDevice, Map.of("uid", testDeviceUid));
            
            String deleteClient = "DELETE FROM core.client WHERE uid = :uid";
            jdbcTemplate.update(deleteClient, Map.of("uid", testClientUid));
            
            log.debug("🧹 Cleaned up test device token data");
        } catch (Exception e) {
            log.warn("⚠️ Cleanup warning: {}", e.getMessage());
        }
    }

    protected void createTestDeviceInDatabase() {
        try {
            // Создаем клиента
            String insertClientSql = """
                INSERT INTO core.client (uid, email, birth_date, username, password)
                VALUES (:uid, :email, CURRENT_DATE - INTERVAL '25 years', :username, :password)
                """;
            jdbcTemplate.update(insertClientSql, Map.of(
                "uid", testClientUid,
                "email", "test.client." + testClientUid + "@example.com",
                "username", "testclient_" + testClientUid.toString().substring(0, 8),
                "password", "TestPassword123"
            ));

            // Создаем устройство
            String insertDeviceSql = """
                INSERT INTO core.device (uid, client_uuid, device_name, device_description)
                VALUES (:uid, :client_uuid, :device_name, :device_description)
                """;
            jdbcTemplate.update(insertDeviceSql, Map.of(
                "uid", testDeviceUid,
                "client_uuid", testClientUid,
                "device_name", "Test Device",
                "device_description", "Integration test device"
            ));

            log.info("✅ Created test device in database: {}", testDeviceUid);
        } catch (Exception e) {
            log.error("❌ Failed to create test device: {}", e.getMessage());
            throw new RuntimeException("Test device creation failed", e);
        }
    }

    protected void createTestDeviceTokenInDatabase() {
        String insertTokenSql = """
            INSERT INTO access.device_token (uid, device_uid, token, created_at, expires_at)
            VALUES (:uid, :device_uid, :token, :created_at, :expires_at)
            """;
        
        jdbcTemplate.update(insertTokenSql, Map.of(
            "uid", testDeviceTokenUid,
            "device_uid", testDeviceUid,
            "token", "test.device.token." + testDeviceTokenUid,
            "created_at", new java.sql.Timestamp(System.currentTimeMillis()),
            "expires_at", new java.sql.Timestamp(System.currentTimeMillis() + 3600000) // +1 hour
        ));

        log.info("✅ Created test device token in database: {}", testDeviceTokenUid);
    }

    protected void createTestDeviceAccessTokenInDatabase() {
        String insertAccessTokenSql = """
            INSERT INTO access.device_access_token (uid, device_token_uid, token, created_at, expires_at)
            VALUES (:uid, :device_token_uid, :token, :created_at, :expires_at)
            """;
        
        jdbcTemplate.update(insertAccessTokenSql, Map.of(
            "uid", testDeviceAccessTokenUid,
            "device_token_uid", testDeviceTokenUid,
            "token", "test.device.access.token." + testDeviceAccessTokenUid,
            "created_at", new java.sql.Timestamp(System.currentTimeMillis()),
            "expires_at", new java.sql.Timestamp(System.currentTimeMillis() + 3600000) // +1 hour
        ));

        log.info("✅ Created test device access token in database: {}", testDeviceAccessTokenUid);
    }

    protected UUID getTestDeviceUid() {
        return testDeviceUid;
    }

    protected UUID getTestDeviceTokenUid() {
        return testDeviceTokenUid;
    }

    protected UUID getTestDeviceAccessTokenUid() {
        return testDeviceAccessTokenUid;
    }

    protected UUID getTestClientUid() {
        return testClientUid;
    }
}