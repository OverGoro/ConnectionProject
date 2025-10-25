// BaseDeviceIntegrationTest.java
package com.connection.device.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.Collections;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@TestMethodOrder(MethodOrderer.Random.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public abstract class BaseDeviceIntegrationTest {
    @Autowired
    protected Environment environment;

    protected TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    protected KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    protected NamedParameterJdbcTemplate deviceJdbcTemplate;

    @DynamicPropertySource
    static void configureKafkaTopics(DynamicPropertyRegistry registry) {
        registry.add("app.kafka.topics.auth-commands", TestTopicUtils::getTestAuthCommandsTopic);
        registry.add("app.kafka.topics.device-commands", TestTopicUtils::getTestDeviceCommandsTopic);
    }

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
        
    }

    @AfterEach
    void tearDown() {
        testData.clear();
        cleanupAllTestData();
        clearAuthentication();
    }

    /**
     * Настраивает аутентификацию через SecurityContext (вместо реального auth service)
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

    protected void checkConfig() {
        log.info("=== Device Service Integration Test Configuration ===");
        log.info("Active profiles: {}", Arrays.toString(environment.getActiveProfiles()));
        log.info("Kafka servers: {}", environment.getProperty("spring.kafka.bootstrap-servers"));
        log.info("Database URL: {}", environment.getProperty("app.datasource.device.xa-properties.url"));
        log.info("Database user: {}", environment.getProperty("app.datasource.device.xa-properties.user"));
        log.info("Service name: {}", environment.getProperty("spring.application.name"));
        log.info("=====================================================");
    }

    protected void cleanupAllTestData() {
        try {
            UUID currentClientUid = getTestClientUid();
            log.info("Cleaning up data for client: {}", currentClientUid);

            // Очистка устройств клиента
            String deleteDevicesSql = "DELETE FROM core.device WHERE client_uuid = :clientUid";
            try {
                int devicesDeleted = deviceJdbcTemplate.update(deleteDevicesSql, 
                        Map.of("clientUid", currentClientUid));
                if (devicesDeleted > 0) {
                    log.debug("Deleted {} devices for client: {}", devicesDeleted, currentClientUid);
                }
            } catch (Exception e) {
                log.debug("No devices to delete for client: {}", currentClientUid);
            }

            log.info("✅ Cleaned up device data for client: {}", currentClientUid);

        } catch (Exception e) {
            log.warn("Cleanup warning for client {}: {}", getTestClientUid(), e.getMessage());
        }
    }

    protected void cleanupDeviceData(UUID deviceUid) {
        try {
            log.info("Cleaning up device data for UID: {}", deviceUid);

            String deleteDeviceSql = "DELETE FROM core.device WHERE uid = :deviceUid";
            int devicesDeleted = deviceJdbcTemplate.update(deleteDeviceSql, Map.of("deviceUid", deviceUid));

            if (devicesDeleted > 0) {
                log.info("Deleted {} devices for UID: {}", devicesDeleted, deviceUid);
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

    protected HttpEntity<Object> createHttpEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    protected HttpEntity<Object> createHttpEntityWithAuth(Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    protected UUID getTestClientUid() {
        return testClientUid;
    }

    protected String getTestAuthToken() {
        return testAuthToken;
    }
}