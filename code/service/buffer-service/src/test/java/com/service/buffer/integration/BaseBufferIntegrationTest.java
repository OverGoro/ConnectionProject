package com.service.buffer.integration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.connection.device.model.DeviceBLM;
import com.connection.service.auth.AuthService;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@TestMethodOrder(MethodOrderer.Random.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public abstract class BaseBufferIntegrationTest {

    @Autowired
    protected TestDeviceService testDeviceService;
    @Autowired
    protected TestConnectionSchemeService testConnectionSchemeService;

    @MockitoBean
    protected AuthService authService;

    @Autowired
    protected Environment environment;

    @Autowired
    @Qualifier("BufferJdbcTemplate")
    protected NamedParameterJdbcTemplate bufferJdbcTemplate;

    protected final Map<String, String> testData = new ConcurrentHashMap<>();
    protected UUID testClientUid;

    @BeforeEach
    void setUp() {
        checkConfig();
        testClientUid = UUID.randomUUID();

        // Инициализируем testData перед использованием
        testData.clear();
        
        // Очищаем тестовые данные перед каждым тестом
        testDeviceService.clearTestData();
        testConnectionSchemeService.clearTestData();
    }

    @AfterEach
    void tearDown() {
        testData.clear();
        cleanupAllTestData();
        clearAuthentication();

        // Очищаем тестовые данные после каждого теста
        testDeviceService.clearTestData();
        testConnectionSchemeService.clearTestData();
    }

    /**
     * Настраивает тестовые устройства для клиента
     */
    protected void setupTestDevices(UUID clientUid, UUID... deviceUids) {
        for (UUID deviceUid : deviceUids) {
            testDeviceService.addTestDevice(
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
            testConnectionSchemeService.addTestConnectionSchemeWithBuffers(
                    schemeUid,
                    clientUid
            );
        }
        log.info("✅ Test connection schemes setup for client {}: {}", clientUid, List.of(schemeUids));
    }

    /**
     * Настраивает connection scheme с указанными буферами
     */
    protected void setupTestConnectionSchemeWithBuffers(UUID schemeUid, UUID clientUid, UUID... bufferUids) {
        testConnectionSchemeService.addTestConnectionSchemeWithBuffers(schemeUid, clientUid, bufferUids);
        log.info("✅ Test connection scheme {} setup for client {} with buffers: {}",
                schemeUid, clientUid, List.of(bufferUids));
    }

    /**
     * Связывает connection scheme с buffer
     */
    protected void linkSchemeToBuffer(UUID schemeUid, UUID bufferUid) {
        testConnectionSchemeService.linkSchemeToBuffer(schemeUid, bufferUid);
        testConnectionSchemeService.addBufferToScheme(schemeUid, bufferUid);
        log.info("🔗 Linked scheme {} to buffer {}", schemeUid, bufferUid);
    }

    /**
     * Добавляет буфер в usedBuffers схемы
     */
    protected void addBufferToScheme(UUID schemeUid, UUID bufferUid) {
        testConnectionSchemeService.addBufferToScheme(schemeUid, bufferUid);
        log.info("➕ Added buffer {} to scheme {} usedBuffers", bufferUid, schemeUid);
    }

    /**
     * Проверяет, зарегистрировано ли устройство в тестовом ответчике
     */
    protected boolean isDeviceRegistered(UUID deviceUid) {
        return testDeviceService.hasDevice(deviceUid);
    }

    /**
     * Проверяет, зарегистрирована ли connection scheme в тестовом ответчике
     */
    protected boolean isConnectionSchemeRegistered(UUID schemeUid) {
        return testConnectionSchemeService.hasConnectionScheme(schemeUid);
    }

    /**
     * Проверяет, принадлежит ли connection scheme клиенту
     */
    protected boolean connectionSchemeBelongsToClient(UUID schemeUid, UUID clientUid) {
        return testConnectionSchemeService.connectionSchemeBelongsToClient(schemeUid, clientUid);
    }

    /**
     * Проверяет, связана ли схема с буфером
     */
    protected boolean isSchemeLinkeBLMBuffer(UUID schemeUid, UUID bufferUid) {
        // Эта логика будет зависеть от реализации, можно добавить соответствующий метод
        // в Service
        return testConnectionSchemeService.hasConnectionScheme(schemeUid);
    }

    /**
     * Настраивает тестовое устройство с конкретными данными
     */
    protected void setupTestDevice(DeviceBLM device) {
        testDeviceService.addTestDevice(device);
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

    protected void cleanupAllTestData() {
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

    protected UUID getTestClientUid() {
        return testClientUid;
    }

    
}