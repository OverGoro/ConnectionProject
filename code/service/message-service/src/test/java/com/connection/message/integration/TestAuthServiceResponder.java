// TestAuthServiceResponder.java
package com.connection.message.integration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import com.connection.auth.events.commands.ValidateTokenCommand;
import com.connection.auth.events.commands.ExtractClientUidCommand;
import com.connection.auth.events.commands.HealthCheckCommand;
import com.connection.auth.events.responses.TokenValidationResponse;
import com.connection.auth.events.responses.ClientUidResponse;
import com.connection.auth.events.responses.HealthCheckResponse;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
@ActiveProfiles("integrationtest")
public class TestAuthServiceResponder {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Хранилище валидных токенов и соответствующих clientUid
    private final Map<String, UUID> validTokens = new ConcurrentHashMap<>();

    @Value("${app.kafka.topics.auth-commands:auth.commands}")
    private String authCommandsTopic;

    @PostConstruct
    public void logKafkaConfiguration() {
        log.info("""
            🧪 Test Auth Responder Kafka Configuration:
               📨 Listening Topic: {}
            """, authCommandsTopic);
    }

    @KafkaListener(topics = "${app.kafka.topics.auth-commands:auth.commands}", groupId = "test-auth-responder")
    public void handleAuthCommand(ConsumerRecord<String, Object> record) {
        try {
            Object command = record.value();
            if (command instanceof ValidateTokenCommand) {
                handleValidateToken((ValidateTokenCommand) command);
            } else if (command instanceof ExtractClientUidCommand) {
                handleExtractClientUid((ExtractClientUidCommand) command);
            } else if (command instanceof HealthCheckCommand) {
                handleHealthCheck((HealthCheckCommand) command);
            }

        } catch (Exception e) {
            log.error("❌ Error in test auth responder", e);
        }
    }

    private void handleValidateToken(ValidateTokenCommand command) {
        try {
            String token = command.getToken();
            UUID clientUid = validTokens.get(token);

            TokenValidationResponse response;
            if (clientUid != null) {
                response = TokenValidationResponse.builder()
                        .correlationId(command.getCorrelationId())
                        .isValid(true)
                        .clientUid(clientUid)
                        .build();
                log.info("✅ Test Responder: Token validation successful for client {}", clientUid);
            } else {
                response = TokenValidationResponse.builder()
                        .correlationId(command.getCorrelationId())
                        .isValid(false)
                        .error("Invalid token")
                        .build();
                log.warn("⚠️ Test Responder: Token validation failed");
            }

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

        } catch (Exception e) {
            log.error("❌ Error handling ValidateToken", e);
        }
    }

    private void handleExtractClientUid(ExtractClientUidCommand command) {
        try {
            String token = command.getToken();
            UUID clientUid = validTokens.get(token);

            ClientUidResponse response;
            if (clientUid != null) {
                response = ClientUidResponse.builder()
                        .correlationId(command.getCorrelationId())
                        .clientUid(clientUid)
                        .build();
                log.info("✅ Test Responder: Extracted client UID {} from token", clientUid);
            } else {
                response = ClientUidResponse.builder()
                        .correlationId(command.getCorrelationId())
                        .error("Invalid token")
                        .build();
                log.warn("⚠️ Test Responder: Failed to extract client UID from token");
            }

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

        } catch (Exception e) {
            log.error("❌ Error handling ExtractClientUid", e);
        }
    }

    private void handleHealthCheck(HealthCheckCommand command) {
        try {
            HealthCheckResponse response = HealthCheckResponse.builder()
                    .correlationId(command.getCorrelationId())
                    .success(true)
                    .healthStatus(Map.of("status", "OK", "service", "test-auth-responder"))
                    .build();

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.info("✅ Test Responder: Health check responded");

        } catch (Exception e) {
            log.error("❌ Error handling HealthCheck", e);
        }
    }

    // Методы для управления тестовыми данными
    public void addValidToken(String token, UUID clientUid) {
        validTokens.put(token, clientUid);
        log.info("🔑 Test Responder: Added valid token for client {}", clientUid);
    }

    public void removeToken(String token) {
        validTokens.remove(token);
        log.info("🗑️ Test Responder: Removed token");
    }

    public void clearTestData() {
        validTokens.clear();
        log.info("🧹 Test Responder: All auth test data cleared");
    }

    public boolean hasValidToken(String token) {
        return validTokens.containsKey(token);
    }
}