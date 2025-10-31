package com.connection.scheme.integration;

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
public abstract class BaseConnectionSchemeRepositoryIntegrationTest {

    protected NamedParameterJdbcTemplate jdbcTemplate;
    protected UUID testClientUid;
    protected UUID testSchemeUid;
    protected UUID testBufferUid1;
    protected UUID testBufferUid2;
    protected UUID testBufferUid3;

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
        testSchemeUid = UUID.randomUUID();
        testBufferUid1 = UUID.randomUUID();
        testBufferUid2 = UUID.randomUUID();
        testBufferUid3 = UUID.randomUUID();
        
        cleanupTestData();
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    protected void cleanupTestData() {
        try {
            // Удаляем в правильном порядке из-за foreign key constraints
            String deleteSchemeBuffersSql = "DELETE FROM processing.connection_scheme_buffer WHERE scheme_uid = :scheme_uid";
            jdbcTemplate.update(deleteSchemeBuffersSql, Map.of("scheme_uid", testSchemeUid));
            
            String deleteSchemeSql = "DELETE FROM processing.connection_scheme WHERE uid = :uid";
            jdbcTemplate.update(deleteSchemeSql, Map.of("uid", testSchemeUid));
            
            log.debug("🧹 Cleaned up test connection scheme data");
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

    protected void createTestBuffersInDatabase() {
        String insertBufferSql = """
            INSERT INTO processing.buffer (uid, device_uid, max_messages_number, max_message_size, message_prototype)
            VALUES (:uid, :device_uid, :max_messages, :max_size, :prototype)
            ON CONFLICT (uid) DO NOTHING
            """;

        // Создаем тестовое устройство для буферов
        UUID testDeviceUid = UUID.randomUUID();
        String insertDeviceSql = """
            INSERT INTO core.device (uid, client_uuid, device_name, device_description)
            VALUES (:uid, :client_uuid, :device_name, :device_description)
            ON CONFLICT (uid) DO NOTHING
            """;
        
        jdbcTemplate.update(insertDeviceSql, Map.of(
            "uid", testDeviceUid,
            "client_uuid", testClientUid,
            "device_name", "Test Device",
            "device_description", "Test device for connection scheme"
        ));

        // Создаем буферы
        jdbcTemplate.update(insertBufferSql, Map.of(
            "uid", testBufferUid1,
            "device_uid", testDeviceUid,
            "max_messages", 1000,
            "max_size", 1024,
            "prototype", "{}"
        ));

        jdbcTemplate.update(insertBufferSql, Map.of(
            "uid", testBufferUid2,
            "device_uid", testDeviceUid,
            "max_messages", 1000,
            "max_size", 1024,
            "prototype", "{}"
        ));

        jdbcTemplate.update(insertBufferSql, Map.of(
            "uid", testBufferUid3,
            "device_uid", testDeviceUid,
            "max_messages", 1000,
            "max_size", 1024,
            "prototype", "{}"
        ));
    }

    protected void createTestConnectionSchemeInDatabase() {
        createTestClientInDatabase();
        createTestBuffersInDatabase();

        String schemeJson = String.format("""
            {
                "%s": ["%s"],
                "%s": ["%s"]
            }
            """, testBufferUid1, testBufferUid2, testBufferUid2, testBufferUid3);

        // Создаем схему
        String insertSchemeSql = """
            INSERT INTO processing.connection_scheme (uid, client_uid, scheme_json)
            VALUES (:uid, :client_uid, :scheme_json::jsonb)
            """;

        jdbcTemplate.update(insertSchemeSql, Map.of(
            "uid", testSchemeUid,
            "client_uid", testClientUid,
            "scheme_json", schemeJson
        ));

        // Создаем связи с буферами
        String insertSchemeBufferSql = """
            INSERT INTO processing.connection_scheme_buffer (uid, scheme_uid, buffer_uid)
            VALUES (:uid, :scheme_uid, :buffer_uid)
            """;

        jdbcTemplate.update(insertSchemeBufferSql, Map.of(
            "uid", UUID.randomUUID(),
            "scheme_uid", testSchemeUid,
            "buffer_uid", testBufferUid1
        ));

        jdbcTemplate.update(insertSchemeBufferSql, Map.of(
            "uid", UUID.randomUUID(),
            "scheme_uid", testSchemeUid,
            "buffer_uid", testBufferUid2
        ));

        jdbcTemplate.update(insertSchemeBufferSql, Map.of(
            "uid", UUID.randomUUID(),
            "scheme_uid", testSchemeUid,
            "buffer_uid", testBufferUid3
        ));

        log.info("✅ Created test connection scheme in database: {}", testSchemeUid);
    }

    protected UUID getTestClientUid() {
        return testClientUid;
    }

    protected UUID getTestSchemeUid() {
        return testSchemeUid;
    }

    protected UUID getTestBufferUid1() {
        return testBufferUid1;
    }

    protected UUID getTestBufferUid2() {
        return testBufferUid2;
    }

    protected UUID getTestBufferUid3() {
        return testBufferUid3;
    }
}