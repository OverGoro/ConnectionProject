package com.service.buffer.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.test.context.TestPropertySource;

import com.connection.device.model.DeviceDTO;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.Collections;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@TestMethodOrder(MethodOrderer.Random.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public abstract class BaseBufferIntegrationTest {

    @Autowired
    protected TestDeviceServiceResponder testDeviceResponder;
    @Autowired
    protected TestConnectionSchemeServiceResponder testConnectionSchemeResponder;

    @Autowired
    protected Environment environment;

    protected TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    protected KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    @Qualifier("BufferJdbcTemplate")
    protected NamedParameterJdbcTemplate bufferJdbcTemplate;

    protected final Map<String, String> testData = new ConcurrentHashMap();
    protected UUID testClientUid;

    @BeforeEach
    void setUp() {
        checkConfig();
        testClientUid = UUID.randomUUID();

        // Очищаем тестовые данные перед каждым тестом
        testDeviceResponder.clearTestData();
        testConnectionSchemeResponder.clearTestData();
    }

    @AfterEach
    void tearDown() {
        testData.clear();
        cleanupAllTestData();
        clearAuthentication();

        // Очищаем тестовые данные после каждого теста
        testDeviceResponder.clearTestData();
        testConnectionSchemeResponder.clearTestData();
    }

    /**
     * Настраивает тестовые устройства для клиента
     */
    protected void setupTestDevices(UUID clientUid, UUID... deviceUids) {
        for (UUID deviceUid : deviceUids) {
            testDeviceResponder.addTestDevice(
                    deviceUid,
                    clientUid,
                    "Test Device " + deviceUid.toString().substring(0, 8));
        }
        log.info("✅ Test devices setup for client {}: {}", clientUid, List.of(deviceUids));
    }

    /**
     * Настраивает тестовые connection schemes для клиента
     */
    protected void setupTestConnectionSchemes(UUID clientUid, UUID... schemeUids) {
        for (UUID schemeUid : schemeUids) {
            testConnectionSchemeResponder.addTestConnectionSchemeWithBuffers(
                    schemeUid,
                    clientUid
            // без буферов по умолчанию
            );
        }
        log.info("✅ Test connection schemes setup for client {}: {}", clientUid, List.of(schemeUids));
    }

    /**
     * Настраивает connection scheme с указанными буферами
     */
    protected void setupTestConnectionSchemeWithBuffers(UUID schemeUid, UUID clientUid, UUID... bufferUids) {
        testConnectionSchemeResponder.addTestConnectionSchemeWithBuffers(schemeUid, clientUid, bufferUids);
        log.info("✅ Test connection scheme {} setup for client {} with buffers: {}",
                schemeUid, clientUid, List.of(bufferUids));
    }

    /**
     * Связывает connection scheme с buffer
     */
    protected void linkSchemeToBuffer(UUID schemeUid, UUID bufferUid) {
        testConnectionSchemeResponder.linkSchemeToBuffer(schemeUid, bufferUid);
        testConnectionSchemeResponder.addBufferToScheme(schemeUid, bufferUid);
        log.info("🔗 Linked scheme {} to buffer {}", schemeUid, bufferUid);
    }

    /**
     * Добавляет буфер в usedBuffers схемы
     */
    protected void addBufferToScheme(UUID schemeUid, UUID bufferUid) {
        testConnectionSchemeResponder.addBufferToScheme(schemeUid, bufferUid);
        log.info("➕ Added buffer {} to scheme {} usedBuffers", bufferUid, schemeUid);
    }

    /**
     * Проверяет, зарегистрировано ли устройство в тестовом ответчике
     */
    protected boolean isDeviceRegistered(UUID deviceUid) {
        return testDeviceResponder.hasDevice(deviceUid);
    }

    /**
     * Проверяет, зарегистрирована ли connection scheme в тестовом ответчике
     */
    protected boolean isConnectionSchemeRegistered(UUID schemeUid) {
        return testConnectionSchemeResponder.hasConnectionScheme(schemeUid);
    }

    /**
     * Проверяет, принадлежит ли connection scheme клиенту
     */
    protected boolean connectionSchemeBelongsToClient(UUID schemeUid, UUID clientUid) {
        return testConnectionSchemeResponder.connectionSchemeBelongsToClient(schemeUid, clientUid);
    }

    /**
     * Проверяет, связана ли схема с буфером
     */
    protected boolean isSchemeLinkedToBuffer(UUID schemeUid, UUID bufferUid) {
        // Эта логика будет зависеть от реализации, можно добавить соответствующий метод
        // в responder
        return testConnectionSchemeResponder.hasConnectionScheme(schemeUid);
    }

    /**
     * Настраивает тестовое устройство с конкретными данными
     */
    protected void setupTestDevice(DeviceDTO device) {
        testDeviceResponder.addTestDevice(device);
    }

    protected void checkConfig() {
        log.info("=== Buffer Service Integration Test Configuration ===");
        log.info("Active profiles: {}", Arrays.toString(environment.getActiveProfiles()));
        log.info("Kafka servers: {}", environment.getProperty("spring.kafka.bootstrap-servers"));
        log.info("Database URL: {}", environment.getProperty("app.datasource.buffer.xa-properties.url"));
        log.info("Database user: {}", environment.getProperty("app.datasource.buffer.xa-properties.user"));
        log.info("Service name: {}", environment.getProperty("spring.application.name"));
        log.info("=====================================================");
    }

    /**
     * Устанавливает аутентификацию для текущего тестового клиента
     */
    protected void setupAuthentication() {
        setupAuthentication(this.testClientUid);
    }

    /**
     * Устанавливает аутентификацию для указанного clientUid
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

    protected void cleanupBufferData(UUID bufferUid) {
        try {
            log.info("Cleaning up buffer data for UID: {}", bufferUid);

            String deleteBufferSql = "DELETE FROM processing.buffer WHERE uid = :bufferUid";
            int buffersDeleted = bufferJdbcTemplate.update(deleteBufferSql, Map.of("bufferUid", bufferUid));

            if (buffersDeleted > 0) {
                log.info("Deleted {} buffers for UID: {}", buffersDeleted, bufferUid);
            }

        } catch (Exception e) {
            log.warn("Cleanup warning for buffer UID {}: {}", bufferUid, e.getMessage());
        }
    }

    // BaseBufferIntegrationTest.java - упрощаем метод cleanupAllTestData
    protected void cleanupAllTestData() {
        // Этот метод теперь не используется, так как каждый тест очищает только свои
        // данные
        log.debug("Global cleanup is disabled - each test cleans up its own client data");
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
}