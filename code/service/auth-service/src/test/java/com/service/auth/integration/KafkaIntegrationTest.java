package com.service.auth.integration;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.connection.auth.events.commands.HealthCheckCommand;
import com.connection.auth.events.commands.ValidateTokenCommand;
import com.connection.auth.events.responses.HealthCheckResponse;
import com.connection.auth.events.responses.TokenValidationResponse;
import com.connection.client.model.ClientBLM;
import com.connection.token.model.AccessTokenBLM;
import com.connection.token.model.RefreshTokenBLM;
import com.service.auth.AuthService;
import com.service.auth.mother.AuthObjectMother;

import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootTest
@ActiveProfiles("integrationtest")
@DisplayName("Kafka Integration Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
public class KafkaIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuthService authService;

    private Consumer<String, Object> responseConsumer;
    private String replyTopic;
    private String testEmail;
    private ClientBLM testClient;

    @BeforeEach
    void setUpKafkaConsumer() {
        // Создаем уникальный топик для ответов для каждого теста
        String testId = UUID.randomUUID().toString().substring(0, 8);
        replyTopic = "auth.responses.test-" + testId;
        log.info("Using unique reply topic: {}", replyTopic);

        // Создаем топик заранее с нужным количеством партиций
        createTopic(replyTopic, 1);

        // Настройка consumer для получения ответов
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29093");
        // Уникальный group.id для каждого теста
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-" + testId);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        // Конфигурация для JsonDeserializer
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES,
                "com.connection.auth.events.commands," +
                        "com.connection.auth.events.responses," +
                        "com.connection.common.events");

        // Конфигурация для ErrorHandlingDeserializer
        consumerProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS,
                org.apache.kafka.common.serialization.StringDeserializer.class);
        consumerProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        consumerProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);
        consumerProps.put(JsonDeserializer.REMOVE_TYPE_INFO_HEADERS, false);
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.connection.common.events.Command");

        // Критически важные настройки для тестов
        consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
        consumerProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        consumerProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 30000);
        consumerProps.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 11000);
        consumerProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 100);
        consumerProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        DefaultKafkaConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);

        responseConsumer = consumerFactory.createConsumer();

        // Подписываемся ДО отправки сообщений
        responseConsumer.subscribe(Collections.singletonList(replyTopic));

        // Ждем инициализации consumer'а
        await().atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> {
                    responseConsumer.poll(Duration.ofMillis(100));
                    return !responseConsumer.assignment().isEmpty();
                });

        log.info("Kafka consumer setup completed for topic: {}, assigned partitions: {}",
                replyTopic, responseConsumer.assignment());

        // Создаем тестового клиента
        String timestamp = String.valueOf(System.currentTimeMillis()) + UUID.randomUUID().toString().substring(0, 8);
        testEmail = "kafka_test_" + timestamp + "@example.com";
        
        testClient = new ClientBLM(
            UUID.randomUUID(),
            new Date(System.currentTimeMillis() - 25L * 365 * 24 * 60 * 60 * 1000),
            testEmail,
            "SecurePassword123!" + timestamp,
            "kafka_user_" + timestamp
        );
        
        // Регистрируем клиента для тестов валидации токенов
        authService.register(testClient);
        log.info("Created test client for Kafka tests: {}", testEmail);
    }

    private void createTopic(String topicName, int partitions) {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29093");

            try (AdminClient adminClient = AdminClient.create(config)) {
                NewTopic newTopic = new NewTopic(topicName, partitions, (short) 1);
                CreateTopicsResult result = adminClient.createTopics(Collections.singleton(newTopic));
                result.all().get(30, java.util.concurrent.TimeUnit.SECONDS);
                log.info("Created topic: {}", topicName);
            }
        } catch (Exception e) {
            log.warn("Topic creation failed (might already exist): {}", e.getMessage());
        }
    }

    @AfterEach
    void tearDownKafkaConsumer() {
        // Очищаем данные после каждого теста
        if (testEmail != null) {
            cleanupClientData(testEmail);
            log.info("Cleaned up test data for email: {}", testEmail);
        }

        if (responseConsumer != null) {
            try {
                responseConsumer.unsubscribe();
                responseConsumer.close(Duration.ofSeconds(5));
                log.info("Kafka consumer closed for topic: {}", replyTopic);
            } catch (Exception e) {
                log.warn("Error closing Kafka consumer: {}", e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Should process HealthCheckCommand and return response")
    void shouldProcessHealthCheckCommand() {
        // Given
        String correlationId = UUID.randomUUID().toString();
        HealthCheckCommand command = HealthCheckCommand.builder()
                .correlationId(correlationId)
                .sourceService("test-service")
                .replyTopic(replyTopic)
                .build();

        log.info("Sending HealthCheckCommand with correlationId: {} to replyTopic: {}",
                correlationId, replyTopic);

        // When
        kafkaTemplate.send("auth.commands", correlationId, command);
        kafkaTemplate.flush();

        // Then
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    ConsumerRecords<String, Object> records = responseConsumer.poll(Duration.ofMillis(2000));
                    log.info("Polled {} records from topic: {}", records.count(), replyTopic);

                    assertThat(records).isNotEmpty();

                    boolean found = false;
                    for (ConsumerRecord<String, Object> record : records) {
                        log.info("Checking record - key: {}, value type: {}",
                                record.key(), record.value() != null ?
                                        record.value().getClass().getSimpleName() : "null");

                        if (record.key().equals(correlationId) && record.value() instanceof HealthCheckResponse) {
                            HealthCheckResponse response = (HealthCheckResponse) record.value();
                            log.info("Found matching response: correlationId={}, success={}",
                                    response.getCorrelationId(), response.isSuccess());

                            assertThat(response.getCorrelationId()).isEqualTo(correlationId);
                            assertThat(response.isSuccess()).isTrue();
                            assertThat(response.getHealthStatus()).isNotNull();
                            assertThat(response.getHealthStatus().get("status")).isEqualTo("OK");
                            assertThat(response.getHealthStatus().get("service")).isEqualTo("auth-service");
                            found = true;
                            break;
                        }
                    }

                    assertThat(found).withFailMessage("Response with correlationId %s not found", correlationId).isTrue();
                });

        log.info("Successfully processed HealthCheckCommand with correlationId: {}", correlationId);
    }

    @Test
    @DisplayName("Should validate access token successfully via Kafka")
    void shouldValidateAccessTokenSuccessfullyViaKafka() {
        // Given - получаем валидный access token
        var tokens = authService.authorizeByEmail(testEmail, testClient.getPassword());
        String validAccessToken = tokens.getFirst().getToken();
        
        String correlationId = UUID.randomUUID().toString();
        ValidateTokenCommand command = ValidateTokenCommand.builder()
                .correlationId(correlationId)
                .sourceService("test-service")
                .replyTopic(replyTopic)
                .token(validAccessToken)
                .tokenType(ValidateTokenCommand.TokenType.ACCESS)
                .build();

        log.info("Sending ValidateTokenCommand for access token with correlationId: {}", correlationId);

        // When
        kafkaTemplate.send("auth.commands", correlationId, command);
        kafkaTemplate.flush();

        // Then
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    ConsumerRecords<String, Object> records = responseConsumer.poll(Duration.ofMillis(2000));
                    
                    boolean found = false;
                    for (ConsumerRecord<String, Object> record : records) {
                        if (record.key().equals(correlationId) && record.value() instanceof TokenValidationResponse) {
                            TokenValidationResponse response = (TokenValidationResponse) record.value();
                            log.info("Found token validation response: correlationId={}, isValid={}, clientUid={}",
                                    response.getCorrelationId(), response.isValid(), response.getClientUid());

                            assertThat(response.getCorrelationId()).isEqualTo(correlationId);
                            assertThat(response.isSuccess()).isTrue();
                            assertThat(response.isValid()).isTrue();
                            assertThat(response.getClientUid()).isEqualTo(testClient.getUid());
                            assertThat(response.getTokenType()).isEqualTo("ACCESS");
                            found = true;
                            break;
                        }
                    }

                    assertThat(found).withFailMessage("Token validation response with correlationId %s not found", correlationId).isTrue();
                });

        log.info("Successfully validated access token via Kafka with correlationId: {}", correlationId);
    }

    @Test
    @DisplayName("Should reject invalid access token via Kafka")
    void shouldRejectInvalidAccessTokenViaKafka() {
        // Given
        String invalidToken = "invalid.token.string";
        String correlationId = UUID.randomUUID().toString();
        ValidateTokenCommand command = ValidateTokenCommand.builder()
                .correlationId(correlationId)
                .sourceService("test-service")
                .replyTopic(replyTopic)
                .token(invalidToken)
                .tokenType(ValidateTokenCommand.TokenType.ACCESS)
                .build();

        log.info("Sending ValidateTokenCommand for invalid token with correlationId: {}", correlationId);

        // When
        kafkaTemplate.send("auth.commands", correlationId, command);
        kafkaTemplate.flush();

        // Then
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    ConsumerRecords<String, Object> records = responseConsumer.poll(Duration.ofMillis(2000));
                    
                    boolean found = false;
                    for (ConsumerRecord<String, Object> record : records) {
                        if (record.key().equals(correlationId) && record.value() instanceof TokenValidationResponse) {
                            TokenValidationResponse response = (TokenValidationResponse) record.value();
                            log.info("Found token validation response for invalid token: correlationId={}, success={}, isValid={}",
                                    response.getCorrelationId(), response.isSuccess(), response.isValid());

                            // Может быть либо success=false, либо success=true но isValid=false
                            if (!response.isSuccess() || (response.isSuccess() && !response.isValid())) {
                                assertThat(response.getCorrelationId()).isEqualTo(correlationId);
                                assertThat(response.isValid()).isFalse();
                                found = true;
                                break;
                            }
                        }
                    }

                    assertThat(found).withFailMessage("Token validation response for invalid token with correlationId %s not found", correlationId).isTrue();
                });

        log.info("Successfully rejected invalid access token via Kafka with correlationId: {}", correlationId);
    }

    @Test
    @DisplayName("Should validate refresh token successfully via Kafka")
    void shouldValidateRefreshTokenSuccessfullyViaKafka() {
        // Given - получаем валидный refresh token
        var tokens = authService.authorizeByEmail(testEmail, testClient.getPassword());
        String validRefreshToken = tokens.getSecond().getToken();
        
        String correlationId = UUID.randomUUID().toString();
        ValidateTokenCommand command = ValidateTokenCommand.builder()
                .correlationId(correlationId)
                .sourceService("test-service")
                .replyTopic(replyTopic)
                .token(validRefreshToken)
                .tokenType(ValidateTokenCommand.TokenType.REFRESH)
                .build();

        log.info("Sending ValidateTokenCommand for refresh token with correlationId: {}", correlationId);

        // When
        kafkaTemplate.send("auth.commands", correlationId, command);
        kafkaTemplate.flush();

        // Then
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    ConsumerRecords<String, Object> records = responseConsumer.poll(Duration.ofMillis(2000));
                    
                    boolean found = false;
                    for (ConsumerRecord<String, Object> record : records) {
                        if (record.key().equals(correlationId) && record.value() instanceof TokenValidationResponse) {
                            TokenValidationResponse response = (TokenValidationResponse) record.value();
                            log.info("Found refresh token validation response: correlationId={}, isValid={}, clientUid={}",
                                    response.getCorrelationId(), response.isValid(), response.getClientUid());

                            assertThat(response.getCorrelationId()).isEqualTo(correlationId);
                            assertThat(response.isSuccess()).isTrue();
                            assertThat(response.isValid()).isTrue();
                            assertThat(response.getClientUid()).isEqualTo(testClient.getUid());
                            assertThat(response.getTokenType()).isEqualTo("REFRESH");
                            found = true;
                            break;
                        }
                    }

                    assertThat(found).withFailMessage("Refresh token validation response with correlationId %s not found", correlationId).isTrue();
                });

        log.info("Successfully validated refresh token via Kafka with correlationId: {}", correlationId);
    }

    @Test
    @DisplayName("Should handle expired refresh token via Kafka")
    void shouldHandleExpiredRefreshTokenViaKafka() {
        // Given - создаем expired refresh token
        RefreshTokenBLM expiredToken = AuthObjectMother.createExpiredRefreshTokenBLM();
        String expiredTokenString = expiredToken.getToken();
        
        String correlationId = UUID.randomUUID().toString();
        ValidateTokenCommand command = ValidateTokenCommand.builder()
                .correlationId(correlationId)
                .sourceService("test-service")
                .replyTopic(replyTopic)
                .token(expiredTokenString)
                .tokenType(ValidateTokenCommand.TokenType.REFRESH)
                .build();

        log.info("Sending ValidateTokenCommand for expired refresh token with correlationId: {}", correlationId);

        // When
        kafkaTemplate.send("auth.commands", correlationId, command);
        kafkaTemplate.flush();

        // Then
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    ConsumerRecords<String, Object> records = responseConsumer.poll(Duration.ofMillis(2000));
                    
                    boolean found = false;
                    for (ConsumerRecord<String, Object> record : records) {
                        if (record.key().equals(correlationId) && record.value() instanceof TokenValidationResponse) {
                            TokenValidationResponse response = (TokenValidationResponse) record.value();
                            log.info("Found expired token validation response: correlationId={}, success={}, isValid={}",
                                    response.getCorrelationId(), response.isSuccess(), response.isValid());

                            // Для expired token ожидаем либо ошибку, либо invalid результат
                            if (!response.isSuccess() || (response.isSuccess() && !response.isValid())) {
                                assertThat(response.getCorrelationId()).isEqualTo(correlationId);
                                assertThat(response.isValid()).isFalse();
                                found = true;
                                break;
                            }
                        }
                    }

                    assertThat(found).withFailMessage("Expired token validation response with correlationId %s not found", correlationId).isTrue();
                });

        log.info("Successfully handled expired refresh token via Kafka with correlationId: {}", correlationId);
    }

    @Test
    @DisplayName("Should handle multiple concurrent commands")
    void shouldHandleMultipleConcurrentCommands() {
        // Given
        String correlationId1 = UUID.randomUUID().toString();
        String correlationId2 = UUID.randomUUID().toString();
        String correlationId3 = UUID.randomUUID().toString();

        HealthCheckCommand healthCommand = HealthCheckCommand.builder()
                .correlationId(correlationId1)
                .sourceService("concurrent-test")
                .replyTopic(replyTopic)
                .build();

        // Получаем валидный токен для теста
        var tokens = authService.authorizeByEmail(testEmail, testClient.getPassword());
        String validToken = tokens.getFirst().getToken();

        ValidateTokenCommand tokenCommand1 = ValidateTokenCommand.builder()
                .correlationId(correlationId2)
                .sourceService("concurrent-test")
                .replyTopic(replyTopic)
                .token(validToken)
                .tokenType(ValidateTokenCommand.TokenType.ACCESS)
                .build();

        ValidateTokenCommand tokenCommand2 = ValidateTokenCommand.builder()
                .correlationId(correlationId3)
                .sourceService("concurrent-test")
                .replyTopic(replyTopic)
                .token("invalid.token")
                .tokenType(ValidateTokenCommand.TokenType.ACCESS)
                .build();

        log.info("Sending multiple concurrent commands with correlationIds: {}, {}, {}",
                correlationId1, correlationId2, correlationId3);

        // When
        kafkaTemplate.send("auth.commands", correlationId1, healthCommand);
        kafkaTemplate.send("auth.commands", correlationId2, tokenCommand1);
        kafkaTemplate.send("auth.commands", correlationId3, tokenCommand2);
        kafkaTemplate.flush();

        // Then
        Map<String, Object> receivedResponses = new ConcurrentHashMap<>();

        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    ConsumerRecords<String, Object> records = responseConsumer.poll(Duration.ofMillis(2000));
                    log.info("Polled {} records from topic: {}", records.count(), replyTopic);

                    for (ConsumerRecord<String, Object> record : records) {
                        if (record.value() instanceof HealthCheckResponse || record.value() instanceof TokenValidationResponse) {
                            receivedResponses.put(record.key(), record.value());
                            log.info("Received response for correlationId: {}, total received: {}",
                                    record.key(), receivedResponses.size());
                        }
                    }

                    // Коммитим оффсеты
                    if (!records.isEmpty()) {
                        responseConsumer.commitSync();
                    }

                    // Проверяем аккумулятивно - получили ли мы ВСЕ ожидаемые сообщения
                    assertThat(receivedResponses).hasSize(3);
                    assertThat(receivedResponses).containsKeys(correlationId1, correlationId2, correlationId3);

                    // Проверяем каждый ответ
                    Object response1 = receivedResponses.get(correlationId1);
                    Object response2 = receivedResponses.get(correlationId2);
                    Object response3 = receivedResponses.get(correlationId3);

                    assertThat(response1).isInstanceOf(HealthCheckResponse.class);
                    HealthCheckResponse healthResponse = (HealthCheckResponse) response1;
                    assertThat(healthResponse.isSuccess()).isTrue();

                    assertThat(response2).isInstanceOf(TokenValidationResponse.class);
                    TokenValidationResponse tokenResponse1 = (TokenValidationResponse) response2;
                    assertThat(tokenResponse1.isSuccess()).isTrue();
                    assertThat(tokenResponse1.isValid()).isTrue();

                    assertThat(response3).isInstanceOf(TokenValidationResponse.class);
                    TokenValidationResponse tokenResponse2 = (TokenValidationResponse) response3;
                    // Для invalid token ожидаем либо ошибку, либо invalid результат
                    assertThat(tokenResponse2.isValid()).isFalse();
                });

        log.info("Successfully handled multiple concurrent commands. Total responses received: {}",
                receivedResponses.size());
    }

    @Test
    @DisplayName("Should handle malformed commands gracefully")
    void shouldHandleMalformedCommandsGracefully() {
        // Given - отправляем некорректное сообщение
        String correlationId = UUID.randomUUID().toString();
        
        // Создаем объект с неправильной структурой
        Map<String, Object> malformedCommand = new HashMap<>();
        malformedCommand.put("type", "UNKNOWN_COMMAND");
        malformedCommand.put("correlationId", correlationId);
        malformedCommand.put("invalidField", "invalidValue");

        log.info("Sending malformed command with correlationId: {}", correlationId);

        // When
        kafkaTemplate.send("auth.commands", correlationId, malformedCommand);
        kafkaTemplate.flush();

        // Then - сервис должен обработать это без падения
        // Мы не ожидаем ответа, но сервис не должен упасть
        await().atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    // Просто проверяем что сервис жив, отправляя health check
                    String healthCorrelationId = UUID.randomUUID().toString();
                    HealthCheckCommand healthCommand = HealthCheckCommand.builder()
                            .correlationId(healthCorrelationId)
                            .sourceService("recovery-test")
                            .replyTopic(replyTopic)
                            .build();

                    kafkaTemplate.send("auth.commands", healthCorrelationId, healthCommand);
                    kafkaTemplate.flush();

                    ConsumerRecords<String, Object> records = responseConsumer.poll(Duration.ofMillis(2000));
                    
                    boolean found = false;
                    for (ConsumerRecord<String, Object> record : records) {
                        if (record.key().equals(healthCorrelationId) && record.value() instanceof HealthCheckResponse) {
                            HealthCheckResponse response = (HealthCheckResponse) record.value();
                            assertThat(response.isSuccess()).isTrue();
                            found = true;
                            break;
                        }
                    }

                    assertThat(found).withFailMessage("Service did not recover from malformed command").isTrue();
                });

        log.info("Service successfully handled malformed command without crashing");
    }
}