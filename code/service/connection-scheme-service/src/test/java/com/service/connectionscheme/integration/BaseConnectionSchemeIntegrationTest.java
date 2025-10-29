package com.service.connectionscheme.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
public abstract class BaseConnectionSchemeIntegrationTest {

    @Autowired
    protected Environment environment;

    protected TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    @Qualifier("ConnectionSchemeJdbcTemplate")
    protected NamedParameterJdbcTemplate connectionSchemeJdbcTemplate;

    protected final Map<String, String> testData = new ConcurrentHashMap<>();
    protected UUID testClientUid;

    @BeforeEach
    void setUp() {
        checkConfig();
        testClientUid = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        testData.clear();
        clearAuthentication();
    }

    protected void checkConfig() {
        log.info("=== Connection Scheme Service Integration Test Configuration ===");
        log.info("Active profiles: {}", Arrays.toString(environment.getActiveProfiles()));
        log.info("Kafka servers: {}", environment.getProperty("spring.kafka.bootstrap-servers"));
        log.info("Database URL: {}", environment.getProperty("app.datasource.connection-scheme.xa-properties.url"));
        log.info("Database user: {}", environment.getProperty("app.datasource.connection-scheme.xa-properties.user"));
        log.info("Service name: {}", environment.getProperty("spring.application.name"));
        log.info("================================================================");
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

    protected void cleanupAllClientData(UUID clientUid) {
        try {
            log.info("Cleaning up all data for client: {}", clientUid);

            // Порядок удаления по зависимостям:

            // 1. Удаляем транзакции тарифов
            String deleteTariffTransactionsSql = """
                    DELETE FROM transaction.tariff_transaction
                    WHERE transaction_uid IN (
                        SELECT uid FROM transaction.client_transaction
                        WHERE client_uid = :clientUid
                    )
                    """;
            try {
                connectionSchemeJdbcTemplate.update(deleteTariffTransactionsSql, Map.of("clientUid", clientUid));
            } catch (Exception e) {
                log.debug("No tariff transactions to delete for client: {}", clientUid);
            }

            // 2. Удаляем клиентские транзакции
            String deleteClientTransactionsSql = "DELETE FROM transaction.client_transaction WHERE client_uid = :clientUid";
            try {
                connectionSchemeJdbcTemplate.update(deleteClientTransactionsSql, Map.of("clientUid", clientUid));
            } catch (Exception e) {
                log.debug("No client transactions to delete for client: {}", clientUid);
            }

            // 3. Удаляем device access tokens
            String deleteDeviceAccessTokensSql = """
                    DELETE FROM access.device_access_token
                    WHERE device_token_uid IN (
                        SELECT uid FROM access.device_token
                        WHERE device_uid IN (
                            SELECT uid FROM core.device WHERE client_uuid = :clientUid
                        )
                    )
                    """;
            try {
                connectionSchemeJdbcTemplate.update(deleteDeviceAccessTokensSql, Map.of("clientUid", clientUid));
            } catch (Exception e) {
                log.debug("No device access tokens to delete for client: {}", clientUid);
            }

            // 4. Удаляем device tokens
            String deleteDeviceTokensSql = """
                    DELETE FROM access.device_token
                    WHERE device_uid IN (
                        SELECT uid FROM core.device WHERE client_uuid = :clientUid
                    )
                    """;
            try {
                connectionSchemeJdbcTemplate.update(deleteDeviceTokensSql, Map.of("clientUid", clientUid));
            } catch (Exception e) {
                log.debug("No device tokens to delete for client: {}", clientUid);
            }

            // 5. Удаляем refresh tokens
            String deleteRefreshTokensSql = "DELETE FROM access.refresh_token WHERE client_id = :clientUid";
            try {
                connectionSchemeJdbcTemplate.update(deleteRefreshTokensSql, Map.of("clientUid", clientUid));
            } catch (Exception e) {
                log.debug("No refresh tokens to delete for client: {}", clientUid);
            }

            // 6. Удаляем сообщения через буферы устройств клиента
            String deleteMessagesSql = """
                    DELETE FROM processing.message
                    WHERE buffer_uid IN (
                        SELECT b.uid FROM processing.buffer b
                        JOIN core.device d ON b.device_uid = d.uid
                        WHERE d.client_uuid = :clientUid
                    )
                    """;
            try {
                connectionSchemeJdbcTemplate.update(deleteMessagesSql, Map.of("clientUid", clientUid));
            } catch (Exception e) {
                log.debug("No messages to delete for client: {}", clientUid);
            }

            // 7. Удаляем связи схем с буферами
            String deleteSchemeBuffersSql = """
                    DELETE FROM processing.connection_scheme_buffer
                    WHERE scheme_uid IN (
                        SELECT uid FROM processing.connection_scheme WHERE client_uid = :clientUid
                    ) OR buffer_uid IN (
                        SELECT b.uid FROM processing.buffer b
                        JOIN core.device d ON b.device_uid = d.uid
                        WHERE d.client_uuid = :clientUid
                    )
                    """;
            try {
                connectionSchemeJdbcTemplate.update(deleteSchemeBuffersSql, Map.of("clientUid", clientUid));
            } catch (Exception e) {
                log.debug("No scheme-buffer links to delete for client: {}", clientUid);
            }

            // 8. Удаляем буферы устройств клиента
            String deleteBuffersSql = """
                    DELETE FROM processing.buffer
                    WHERE device_uid IN (
                        SELECT uid FROM core.device WHERE client_uuid = :clientUid
                    )
                    """;
            try {
                connectionSchemeJdbcTemplate.update(deleteBuffersSql, Map.of("clientUid", clientUid));
            } catch (Exception e) {
                log.debug("No buffers to delete for client: {}", clientUid);
            }

            // 9. Удаляем схемы соединения клиента
            String deleteSchemesSql = "DELETE FROM processing.connection_scheme WHERE client_uid = :clientUid";
            try {
                connectionSchemeJdbcTemplate.update(deleteSchemesSql, Map.of("clientUid", clientUid));
            } catch (Exception e) {
                log.debug("No connection schemes to delete for client: {}", clientUid);
            }

            // 10. Удаляем устройства клиента
            String deleteDevicesSql = "DELETE FROM core.device WHERE client_uuid = :clientUid";
            try {
                connectionSchemeJdbcTemplate.update(deleteDevicesSql, Map.of("clientUid", clientUid));
            } catch (Exception e) {
                log.debug("No devices to delete for client: {}", clientUid);
            }

            // 11. Удаляем самого клиента
            String deleteClientSql = "DELETE FROM core.client WHERE uid = :clientUid";
            try {
                connectionSchemeJdbcTemplate.update(deleteClientSql, Map.of("clientUid", clientUid));
            } catch (Exception e) {
                log.debug("No client to delete: {}", clientUid);
            }

            log.info("✅ Successfully cleaned up all data for client: {}", clientUid);

        } catch (Exception e) {
            log.error("❌ Error cleaning up data for client {}: {}", clientUid, e.getMessage(), e);
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

    protected UUID getTestClientUid() {
        return testClientUid;
    }

    /**
     * Инициализирует тестового клиента в БД с устройствами и буферами
     */
    protected void initializeTestClient() {
        try {
            // Создаем клиента
            String insertClientSql = """
                    INSERT INTO core.client (uid, email, birth_date, username, password)
                    VALUES (:uid, :email, CURRENT_DATE - INTERVAL '25 years', :username, :password)
                    ON CONFLICT (uid) DO NOTHING
                    """;

            int clientsInserted = connectionSchemeJdbcTemplate.update(insertClientSql, Map.of(
                    "uid", getTestClientUid(),
                    "email", "test.client." + getTestClientUid() + "@example.com",
                    "username", "testclient_" + getTestClientUid().toString().substring(0, 8),
                    "password", "testpassword123"));

            if (clientsInserted > 0) {
                log.info("✅ Created test client: {}", getTestClientUid());
            }

            // Создаем тестовые устройства для клиента
            createTestDevices();

            // Создаем тестовые буферы для устройств
            createTestBuffers();

        } catch (Exception e) {
            log.error("❌ Failed to initialize test client: {}", e.getMessage(), e);
            throw new RuntimeException("Test client initialization failed", e);
        }
    }

    /**
     * Создает тестовые устройства для клиента
     */
    protected void createTestDevices() {
        try {
            // Создаем несколько тестовых устройств
            UUID device1Uid = UUID.randomUUID();
            UUID device2Uid = UUID.randomUUID();

            String insertDeviceSql = """
                    INSERT INTO core.device (uid, client_uuid, device_name, device_description)
                    VALUES (:uid, :clientUuid, :deviceName, :deviceDescription)
                    ON CONFLICT (uid) DO NOTHING
                    """;

            // Устройство 1
            int devicesInserted1 = connectionSchemeJdbcTemplate.update(insertDeviceSql, Map.of(
                    "uid", device1Uid,
                    "clientUuid", getTestClientUid(),
                    "deviceName", "Test Device 1",
                    "deviceDescription", "Integration test device 1 for connection scheme service"));

            // Устройство 2
            int devicesInserted2 = connectionSchemeJdbcTemplate.update(insertDeviceSql, Map.of(
                    "uid", device2Uid,
                    "clientUuid", getTestClientUid(),
                    "deviceName", "Test Device 2",
                    "deviceDescription", "Integration test device 2 for connection scheme service"));

            if (devicesInserted1 > 0 || devicesInserted2 > 0) {
                log.info("✅ Created test devices for client: {} - Device1: {}, Device2: {}",
                        getTestClientUid(), device1Uid, device2Uid);
            }

            // Сохраняем UUID устройств для использования в тестах
            testData.put("device1Uid", device1Uid.toString());
            testData.put("device2Uid", device2Uid.toString());

        } catch (Exception e) {
            log.error("❌ Failed to create test devices: {}", e.getMessage(), e);
            throw new RuntimeException("Test devices creation failed", e);
        }
    }

    /**
     * Создает тестовые буферы для устройств
     */
    protected void createTestBuffers() {
        try {
            String device1UidStr = testData.get("device1Uid");
            String device2UidStr = testData.get("device2Uid");

            if (device1UidStr == null || device2UidStr == null) {
                throw new IllegalStateException("Devices not initialized");
            }

            UUID device1Uid = UUID.fromString(device1UidStr);
            UUID device2Uid = UUID.fromString(device2UidStr);

            // Создаем несколько тестовых буферов
            UUID buffer1Uid = UUID.randomUUID();
            UUID buffer2Uid = UUID.randomUUID();
            UUID buffer3Uid = UUID.randomUUID();

            String insertBufferSql = """
                    INSERT INTO processing.buffer (uid, device_uid, max_messages_number, max_message_size, message_prototype)
                    VALUES (:uid, :deviceUid, :maxMessagesNumber, :maxMessageSize, :messagePrototype)
                    ON CONFLICT (uid) DO NOTHING
                    """;

            // Буфер 1 для устройства 1
            int buffersInserted1 = connectionSchemeJdbcTemplate.update(insertBufferSql, Map.of(
                    "uid", buffer1Uid,
                    "deviceUid", device1Uid,
                    "maxMessagesNumber", 1000,
                    "maxMessageSize", 1024,
                    "messagePrototype", "{}"));

            // Буфер 2 для устройства 1
            int buffersInserted2 = connectionSchemeJdbcTemplate.update(insertBufferSql, Map.of(
                    "uid", buffer2Uid,
                    "deviceUid", device1Uid,
                    "maxMessagesNumber", 2000,
                    "maxMessageSize", 2048,
                    "messagePrototype", "{}"));

            // Буфер 3 для устройства 2
            int buffersInserted3 = connectionSchemeJdbcTemplate.update(insertBufferSql, Map.of(
                    "uid", buffer3Uid,
                    "deviceUid", device2Uid,
                    "maxMessagesNumber", 1500,
                    "maxMessageSize", 1536,
                    "messagePrototype", "{}"));

            if (buffersInserted1 > 0 || buffersInserted2 > 0 || buffersInserted3 > 0) {
                log.info("✅ Created test buffers for client: {} - Buffer1: {}, Buffer2: {}, Buffer3: {}",
                        getTestClientUid(), buffer1Uid, buffer2Uid, buffer3Uid);
            }

            // Сохраняем UUID буферов для использования в тестах
            testData.put("buffer1Uid", buffer1Uid.toString());
            testData.put("buffer2Uid", buffer2Uid.toString());
            testData.put("buffer3Uid", buffer3Uid.toString());

        } catch (Exception e) {
            log.error("❌ Failed to create test buffers: {}", e.getMessage(), e);
            throw new RuntimeException("Test buffers creation failed", e);
        }
    }

    /**
     * Получает UUID тестового буфера по индексу
     */
    protected UUID getTestBufferUid(int index) {
        try {
            String bufferUidStr = testData.get("buffer" + index + "Uid");
            if (bufferUidStr == null) {
                throw new IllegalArgumentException("Buffer with index " + index + " not found");
            }
            return UUID.fromString(bufferUidStr);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get test buffer UID for index: " + index, e);
        }
    }

    /**
     * Получает UUID тестового устройства по индексу
     */
    protected UUID getTestDeviceUid(int index) {
        try {
            String deviceUidStr = testData.get("device" + index + "Uid");
            if (deviceUidStr == null) {
                throw new IllegalArgumentException("Device with index " + index + " not found");
            }
            return UUID.fromString(deviceUidStr);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get test device UID for index: " + index, e);
        }
    }
}