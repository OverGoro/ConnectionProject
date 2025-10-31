package com.connection.client.integration;

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
public abstract class BaseClientRepositoryIntegrationTest {

    protected NamedParameterJdbcTemplate jdbcTemplate;
    protected UUID testClientUid;
    protected String testEmail;
    protected String testUsername;
    protected String testPassword;

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
        testEmail = "test.client." + testClientUid + "@example.com";
        testUsername = "testclient_" + testClientUid.toString().substring(0, 8);
        testPassword = "TestPassword123";
        
        cleanupTestData();
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    protected void cleanupTestData() {
        try {
            String deleteSql = "DELETE FROM core.client WHERE email = :email OR username = :username";
            int deleted = jdbcTemplate.update(deleteSql, Map.of(
                "email", testEmail,
                "username", testUsername
            ));
            
            if (deleted > 0) {
                log.debug("🧹 Cleaned up {} test client records", deleted);
            }
        } catch (Exception e) {
            log.warn("⚠️ Cleanup warning: {}", e.getMessage());
        }
    }

    protected void createTestClientInDatabase() {
        String insertClientSql = """
            INSERT INTO core.client (uid, email, birth_date, username, password)
            VALUES (:uid, :email, CURRENT_DATE - INTERVAL '25 years', :username, :password)
            """;

        jdbcTemplate.update(insertClientSql, Map.of(
            "uid", testClientUid,
            "email", testEmail,
            "username", testUsername,
            "password", testPassword
        ));

        log.info("✅ Created test client in database: {}", testClientUid);
    }

    protected UUID getTestClientUid() {
        return testClientUid;
    }

    protected String getTestEmail() {
        return testEmail;
    }

    protected String getTestUsername() {
        return testUsername;
    }

    protected String getTestPassword() {
        return testPassword;
    }
}