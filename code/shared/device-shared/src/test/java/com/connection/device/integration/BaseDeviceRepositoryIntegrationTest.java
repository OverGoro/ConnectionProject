package com.connection.device.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseDeviceRepositoryIntegrationTest {

    protected NamedParameterJdbcTemplate jdbcTemplate;
    protected UUID testDeviceUid;
    protected UUID testClientUid;
    protected String testDeviceName;
    protected String testDeviceDescription;

    @BeforeEach
    void setUp() {
        // Создаем DataSource напрямую
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5434/test_db");
        dataSource.setUsername("test_user");
        dataSource.setPassword("test_password");
        
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        
        testDeviceUid = UUID.randomUUID();
        testClientUid = UUID.randomUUID();
        testDeviceName = "Test Device " + testDeviceUid.toString().substring(0, 8);
        testDeviceDescription = "Test Device Description for " + testDeviceUid;
        
        cleanupTestData();
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    protected void cleanupTestData() {
        try {
            String deleteSql = "DELETE FROM core.device WHERE uid = :uid OR device_name = :device_name";
            int deleted = jdbcTemplate.update(deleteSql, Map.of(
                "uid", testDeviceUid,
                "device_name", testDeviceName
            ));
            
            if (deleted > 0) {
                log.debug("🧹 Cleaned up {} test device records", deleted);
            }
        } catch (Exception e) {
            log.warn("⚠️ Cleanup warning: {}", e.getMessage());
        }
    }

    protected void createTestClientInDatabase() {
        String insertClientSql = """
            INSERT INTO core.client (uid, email, birth_date, username, password)
            VALUES (:uid, :email, CURRENT_DATE - INTERVAL '25 years', :username, :password)
            ON CONFLICT (uid) DO NOTHING
            """;

        jdbcTemplate.update(insertClientSql, Map.of(
            "uid", testClientUid,
            "email", "test.client." + testClientUid + "@example.com",
            "username", "testclient_" + testClientUid.toString().substring(0, 8),
            "password", "TestPassword123"
        ));
    }

    protected void createTestDeviceInDatabase() {
        createTestClientInDatabase();
        
        String insertDeviceSql = """
            INSERT INTO core.device (uid, client_uuid, device_name, device_description)
            VALUES (:uid, :client_uuid, :device_name, :device_description)
            """;

        jdbcTemplate.update(insertDeviceSql, Map.of(
            "uid", testDeviceUid,
            "client_uuid", testClientUid,
            "device_name", testDeviceName,
            "device_description", testDeviceDescription
        ));

        log.info("✅ Created test device in database: {}", testDeviceUid);
    }

    protected UUID getTestDeviceUid() {
        return testDeviceUid;
    }

    protected UUID getTestClientUid() {
        return testClientUid;
    }

    protected String getTestDeviceName() {
        return testDeviceName;
    }

    protected String getTestDeviceDescription() {
        return testDeviceDescription;
    }
}