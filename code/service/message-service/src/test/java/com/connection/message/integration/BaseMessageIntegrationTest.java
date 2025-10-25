// BaseMessageIntegrationTest.java
package com.connection.message.integration;

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
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@TestMethodOrder(MethodOrderer.Random.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public abstract class BaseMessageIntegrationTest {

    @Autowired
    protected TestAuthServiceResponder testAuthResponder;

    @Autowired
    protected TestDeviceAuthServiceResponder testDeviceAuthResponder;

    @Autowired
    protected TestDeviceServiceResponder testDeviceResponder;

    @Autowired
    protected TestBufferServiceResponder testBufferResponder;

    @Autowired
    protected TestConnectionSchemeServiceResponder testConnectionSchemeResponder;

    @Autowired
    protected Environment environment;

    protected TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    protected KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    protected NamedParameterJdbcTemplate messageJdbcTemplate;

    @DynamicPropertySource
    static void configureKafkaTopics(DynamicPropertyRegistry registry) {
        registry.add("app.kafka.topics.auth-commands", TestTopicUtils::getTestAuthCommandsTopic);
        registry.add("app.kafka.topics.device-auth-commands", TestTopicUtils::getTestDeviceAuthCommandsTopic);
        registry.add("app.kafka.topics.device-commands", TestTopicUtils::getTestDeviceCommandsTopic);
        registry.add("app.kafka.topics.buffer-commands", TestTopicUtils::getTestBufferCommandsTopic);
        registry.add("app.kafka.topics.connection-scheme-commands", TestTopicUtils::getTestConnectionSchemeCommandsTopic);
        registry.add("app.kafka.topics.message-commands", TestTopicUtils::getTestMessageCommandsTopic);
    }

    protected final Map<String, String> testData = new ConcurrentHashMap<>();
    protected UUID testClientUid;
    protected UUID testDeviceUid;
    protected String testClientToken;
    protected String testDeviceToken;
    protected UUID testBufferUid;
    protected UUID testSchemeUid;
    protected UUID testTargetBufferUid;

    @BeforeEach
    void setUp() {
        checkConfig();
        testClientUid = UUID.randomUUID();
        testDeviceUid = UUID.randomUUID();
        testBufferUid = UUID.randomUUID();
        testTargetBufferUid = UUID.randomUUID();
        testSchemeUid = UUID.randomUUID();
        testClientToken = "client-token-" + UUID.randomUUID().toString();
        testDeviceToken = "device-token-" + UUID.randomUUID().toString();

        // Инициализируем testData перед использованием
        testData.clear();

        // Очищаем тестовые данные перед каждым тестом
        testAuthResponder.clearTestData();
        testDeviceAuthResponder.clearTestData();
        testDeviceResponder.clearTestData();
        testBufferResponder.clearTestData();
        testConnectionSchemeResponder.clearTestData();

        // Инициализируем тестовые данные в БД
        initializeTestDataInDatabase();

        // Настраиваем responders после инициализации БД
        setupTestResponders();
    }

    @AfterEach
    void tearDown() {
        testData.clear();
        cleanupAllTestData();
        clearAuthentication();

        // Очищаем тестовые данные после каждого теста
        testAuthResponder.clearTestData();
        testDeviceAuthResponder.clearTestData();
        testDeviceResponder.clearTestData();
        testBufferResponder.clearTestData();
        testConnectionSchemeResponder.clearTestData();
    }

    /**
     * Инициализирует тестовые данные в БД
     */
    private void initializeTestDataInDatabase() {
        try {
            // 1. Создаем тестового клиента
            String insertClientSql = """
                    INSERT INTO core.client (uid, email, birth_date, username, password)
                    VALUES (:uid, :email, CURRENT_DATE - INTERVAL '25 years', :username, :password)
                    ON CONFLICT (uid) DO NOTHING
                    """;

            int clientsInserted = messageJdbcTemplate.update(insertClientSql, Map.of(
                    "uid", testClientUid,
                    "email", "test.client." + testClientUid + "@example.com",
                    "username", "testclient_" + testClientUid.toString().substring(0, 8),
                    "password", "testpassword123"));

            if (clientsInserted > 0) {
                log.info("✅ Created test client: {}", testClientUid);
            }

            // 2. Создаем тестовое устройство
            String insertDeviceSql = """
                    INSERT INTO core.device (uid, client_uuid, device_name, device_description)
                    VALUES (:uid, :clientUuid, :deviceName, :deviceDescription)
                    ON CONFLICT (uid) DO NOTHING
                    """;

            int devicesInserted = messageJdbcTemplate.update(insertDeviceSql, Map.of(
                    "uid", testDeviceUid,
                    "clientUuid", testClientUid,
                    "deviceName", "Test Device " + testDeviceUid.toString().substring(0, 8),
                    "deviceDescription", "Integration test device for message service"));

            if (devicesInserted > 0) {
                log.info("✅ Created test device: {} for client: {}", testDeviceUid, testClientUid);
            }

            // 3. Создаем тестовые буферы
            String insertBufferSql = """
                    INSERT INTO processing.buffer (uid, device_uid, max_messages_number, max_message_size, message_prototype)
                    VALUES (:uid, :deviceUid, :maxMessages, :maxSize, :prototype)
                    ON CONFLICT (uid) DO NOTHING
                    """;

            // Основной буфер
            int buffersInserted = messageJdbcTemplate.update(insertBufferSql, Map.of(
                    "uid", testBufferUid,
                    "deviceUid", testDeviceUid,
                    "maxMessages", 1000,
                    "maxSize", 1024,
                    "prototype", "{}"));

            if (buffersInserted > 0) {
                log.info("✅ Created test buffer: {} for device: {}", testBufferUid, testDeviceUid);
            }

            // Целевой буфер для перемещения сообщений
            int targetBuffersInserted = messageJdbcTemplate.update(insertBufferSql, Map.of(
                    "uid", testTargetBufferUid,
                    "deviceUid", testDeviceUid,
                    "maxMessages", 1000,
                    "maxSize", 1024,
                    "prototype", "{}"));

            if (targetBuffersInserted > 0) {
                log.info("✅ Created target test buffer: {} for device: {}", testTargetBufferUid, testDeviceUid);
            }

            // 4. Создаем тестовую схему соединения
            String insertSchemeSql = """
                    INSERT INTO processing.connection_scheme (uid, client_uid, scheme_json)
                    VALUES (:uid, :clientUid, :schemeJson::jsonb)
                    ON CONFLICT (uid) DO NOTHING
                    """;

            String schemeJson = String.format("""
                    {
                        "usedBuffers": ["%s", "%s"],
                        "bufferTransitions": {
                            "%s": ["%s"]
                        }
                    }
                    """, testBufferUid, testTargetBufferUid, testBufferUid, testTargetBufferUid);

            int schemesInserted = messageJdbcTemplate.update(insertSchemeSql, Map.of(
                    "uid", testSchemeUid,
                    "clientUid", testClientUid,
                    "schemeJson", schemeJson));

            if (schemesInserted > 0) {
                log.info("✅ Created test connection scheme: {}", testSchemeUid);
            }

            // 5. Создаем связь схемы с буферами
            String insertSchemeBufferSql = """
                    INSERT INTO processing.connection_scheme_buffer (uid, scheme_uid, buffer_uid)
                    VALUES (:uid, :schemeUid, :bufferUid)
                    ON CONFLICT (uid) DO NOTHING
                    """;

            // Связываем схему с основным буфером
            messageJdbcTemplate.update(insertSchemeBufferSql, Map.of(
                    "uid", UUID.randomUUID(),
                    "schemeUid", testSchemeUid,
                    "bufferUid", testBufferUid));

            // Связываем схему с целевым буфером
            messageJdbcTemplate.update(insertSchemeBufferSql, Map.of(
                    "uid", UUID.randomUUID(),
                    "schemeUid", testSchemeUid,
                    "bufferUid", testTargetBufferUid));

            log.info("✅ Linked scheme {} to buffers {} and {}", testSchemeUid, testBufferUid, testTargetBufferUid);

        } catch (Exception e) {
            log.error("❌ Failed to initialize test data in database: {}", e.getMessage(), e);
            throw new RuntimeException("Test data initialization failed", e);
        }
    }

    /**
     * Настраивает responders для корректной работы проверок доступа
     */
    private void setupTestResponders() {
        // 1. Настраиваем валидные токены
        testAuthResponder.addValidToken(testClientToken, testClientUid);
        testDeviceAuthResponder.addValidDeviceToken(testDeviceToken, testDeviceUid);

        // 2. Настраиваем тестовые устройства в responders
        testDeviceResponder.addTestDevice(testDeviceUid, testClientUid, "Test Device");

        // 3. Настраиваем тестовые буферы в responders
        testBufferResponder.addTestBuffer(testBufferUid, testDeviceUid, 1000, 1024);
        testBufferResponder.addTestBuffer(testTargetBufferUid, testDeviceUid, 1000, 1024);

        // 4. Настраиваем тестовую схему соединения в responders
        List<UUID> usedBuffers = List.of(testBufferUid, testTargetBufferUid);
        Map<UUID, List<UUID>> bufferTransitions = Map.of(
                testBufferUid, List.of(testTargetBufferUid));
        testConnectionSchemeResponder.addTestConnectionScheme(testSchemeUid, testClientUid, usedBuffers,
                bufferTransitions);

        // 5. Связываем схему с буферами в responders
        testConnectionSchemeResponder.linkSchemeToBuffer(testSchemeUid, testBufferUid);
        testConnectionSchemeResponder.linkSchemeToBuffer(testSchemeUid, testTargetBufferUid);

        log.info("✅ Test responders setup completed");
    }

    /**
     * Настраивает аутентификацию клиента через SecurityContext
     */
    protected void setupClientAuthentication() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                testClientUid,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        log.info("✅ Test client authentication setup for client: {}", testClientUid);
    }

    /**
     * Настраивает аутентификацию устройства через SecurityContext
     */
    protected void setupDeviceAuthentication() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                testDeviceUid,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_DEVICE")));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        log.info("✅ Test device authentication setup for device: {}", testDeviceUid);
    }

    protected void clearAuthentication() {
        SecurityContextHolder.clearContext();
        log.info("🔒 Test authentication cleared");
    }

    /**
     * Настраивает тестовые устройства
     */
    protected void setupTestDevices() {
        testDeviceResponder.addTestDevice(testDeviceUid, testClientUid, "Test Device");
        log.info("✅ Test device setup: {} for client {}", testDeviceUid, testClientUid);
    }

    /**
     * Настраивает тестовые буферы
     */
    protected void setupTestBuffers() {
        UUID bufferUid = UUID.randomUUID();
        testBufferResponder.addTestBuffer(bufferUid, testDeviceUid, 1000, 1024);
        log.info("✅ Test buffer setup: {} for device {}", bufferUid, testDeviceUid);
    }

    /**
     * Настраивает тестовую схему соединения с переходами между буферами
     */
    protected UUID setupTestConnectionSchemeWithTransitions(UUID sourceBufferUid, UUID targetBufferUid) {
        UUID schemeUid = UUID.randomUUID();
        List<UUID> usedBuffers = List.of(sourceBufferUid, targetBufferUid);
        Map<UUID, List<UUID>> bufferTransitions = Map.of(
                sourceBufferUid, List.of(targetBufferUid));

        testConnectionSchemeResponder.addTestConnectionScheme(schemeUid, testClientUid, usedBuffers, bufferTransitions);
        log.info("✅ Test connection scheme setup: {} with transitions {} -> {}", schemeUid, sourceBufferUid,
                targetBufferUid);

        return schemeUid;
    }

    protected void checkConfig() {
        log.info("=== Message Service Integration Test Configuration ===");
        log.info("Active profiles: {}", Arrays.toString(environment.getActiveProfiles()));
        log.info("Kafka servers: {}", environment.getProperty("spring.kafka.bootstrap-servers"));
        log.info("Database URL: {}", environment.getProperty("app.datasource.message.xa-properties.url"));
        log.info("Database user: {}", environment.getProperty("app.datasource.message.xa-properties.user"));
        log.info("Service name: {}", environment.getProperty("spring.application.name"));
        log.info("=====================================================");
    }

   // В BaseMessageIntegrationTest - улучшить метод очистки
protected void cleanupAllTestData() {
    try {
        UUID currentClientUid = getTestClientUid();
        log.info("Cleaning up data for client: {}", currentClientUid);

        // 1. Очистка сообщений через буферы клиента
        String deleteMessagesSql = """
            DELETE FROM processing.message 
            WHERE buffer_uid IN (
                SELECT b.uid FROM processing.buffer b
                JOIN core.device d ON b.device_uid = d.uid
                WHERE d.client_uuid = :clientUid
            )
            """;
        try {
            int messagesDeleted = messageJdbcTemplate.update(deleteMessagesSql, 
                    Map.of("clientUid", currentClientUid));
            if (messagesDeleted > 0) {
                log.debug("Deleted {} messages for client: {}", messagesDeleted, currentClientUid);
            }
        } catch (Exception e) {
            log.debug("No messages to delete for client: {}", currentClientUid);
        }

        // 2. Очистка связей схемы с буферами
        String deleteSchemeBuffersSql = """
            DELETE FROM processing.connection_scheme_buffer 
            WHERE scheme_uid IN (
                SELECT uid FROM processing.connection_scheme 
                WHERE client_uid = :clientUid
            ) OR buffer_uid IN (
                SELECT b.uid FROM processing.buffer b
                JOIN core.device d ON b.device_uid = d.uid
                WHERE d.client_uuid = :clientUid
            )
            """;
        try {
            int schemeBuffersDeleted = messageJdbcTemplate.update(deleteSchemeBuffersSql,
                    Map.of("clientUid", currentClientUid));
            if (schemeBuffersDeleted > 0) {
                log.debug("Deleted {} scheme-buffer links for client: {}", schemeBuffersDeleted, currentClientUid);
            }
        } catch (Exception e) {
            log.debug("No scheme-buffer links to delete for client: {}", currentClientUid);
        }

        // 3. Очистка схем соединения клиента
        String deleteSchemesSql = "DELETE FROM processing.connection_scheme WHERE client_uid = :clientUid";
        try {
            int schemesDeleted = messageJdbcTemplate.update(deleteSchemesSql, Map.of("clientUid", currentClientUid));
            if (schemesDeleted > 0) {
                log.debug("Deleted {} connection schemes for client: {}", schemesDeleted, currentClientUid);
            }
        } catch (Exception e) {
            log.debug("No connection schemes to delete for client: {}", currentClientUid);
        }

        // 4. Очистка буферов через устройства клиента
        String deleteBuffersSql = """
            DELETE FROM processing.buffer 
            WHERE device_uid IN (
                SELECT uid FROM core.device 
                WHERE client_uuid = :clientUid
            )
            """;
        try {
            int buffersDeleted = messageJdbcTemplate.update(deleteBuffersSql, Map.of("clientUid", currentClientUid));
            if (buffersDeleted > 0) {
                log.debug("Deleted {} buffers for client: {}", buffersDeleted, currentClientUid);
            }
        } catch (Exception e) {
            log.debug("No buffers to delete for client: {}", currentClientUid);
        }

        // 5. Очистка устройств клиента
        String deleteDevicesSql = "DELETE FROM core.device WHERE client_uuid = :clientUid";
        try {
            int devicesDeleted = messageJdbcTemplate.update(deleteDevicesSql, Map.of("clientUid", currentClientUid));
            if (devicesDeleted > 0) {
                log.debug("Deleted {} devices for client: {}", devicesDeleted, currentClientUid);
            }
        } catch (Exception e) {
            log.debug("No devices to delete for client: {}", currentClientUid);
        }

        // 6. Очистка самого клиента
        String deleteClientSql = "DELETE FROM core.client WHERE uid = :clientUid";
        try {
            int clientsDeleted = messageJdbcTemplate.update(deleteClientSql, Map.of("clientUid", currentClientUid));
            if (clientsDeleted > 0) {
                log.info("✅ Cleaned up client and all related data: {}", currentClientUid);
            }
        } catch (Exception e) {
            log.debug("No client to delete: {}", currentClientUid);
        }

    } catch (Exception e) {
        log.warn("Cleanup warning for client {}: {}", getTestClientUid(), e.getMessage());
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

    protected HttpEntity<Object> createHttpEntityWithClientAuth(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(testClientToken);
        return new HttpEntity<>(body, headers);
    }

    protected HttpEntity<Object> createHttpEntityWithDeviceAuth(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Device-Authorization", "Bearer " + testDeviceToken);
        return new HttpEntity<>(body, headers);
    }

    protected UUID getTestClientUid() {
        return testClientUid;
    }

    protected UUID getTestDeviceUid() {
        return testDeviceUid;
    }

    protected String getTestClientToken() {
        return testClientToken;
    }

    protected String getTestDeviceToken() {
        return testDeviceToken;
    }

    protected UUID getTestBufferUid() {
        return testBufferUid;
    }

    protected UUID getTestSchemeUid() {
        return testSchemeUid;
    }

    protected UUID getTestTargetBufferUid() {
        return testTargetBufferUid;
    }
}