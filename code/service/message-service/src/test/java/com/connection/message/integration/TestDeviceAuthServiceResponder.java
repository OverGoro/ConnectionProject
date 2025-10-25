// TestDeviceAuthServiceResponder.java
package com.connection.message.integration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import com.connection.device.auth.events.commands.ValidateTokenCommand;
import com.connection.device.auth.events.commands.ExtractDeviceUidCommand;
import com.connection.device.auth.events.commands.HealthCheckCommand;
import com.connection.device.auth.events.responses.TokenValidationResponse;
import com.connection.device.auth.events.responses.DeviceUidResponse;
import com.connection.device.auth.events.responses.HealthCheckResponse;

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
public class TestDeviceAuthServiceResponder {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Хранилище валидных device токенов и соответствующих deviceUid
    private final Map<String, UUID> validDeviceTokens = new ConcurrentHashMap<>();

    @Value("${app.kafka.topics.device-auth-commands:device.auth.commands}")
    private String deviceAuthCommandsTopic;

    @PostConstruct
    public void logKafkaConfiguration() {
        log.info("""
            🧪 Test Device Auth Responder Kafka Configuration:
               📨 Listening Topic: {}
            """, deviceAuthCommandsTopic);
    }

    @KafkaListener(topics = "${app.kafka.topics.device-auth-commands:device.auth.commands}", groupId = "test-device-auth-responder")
    public void handleDeviceAuthCommand(ConsumerRecord<String, Object> record) {
        try {
            Object command = record.value();
            if (command instanceof ValidateTokenCommand) {
                handleValidateToken((ValidateTokenCommand) command);
            } else if (command instanceof ExtractDeviceUidCommand) {
                handleExtractDeviceUid((ExtractDeviceUidCommand) command);
            } else if (command instanceof HealthCheckCommand) {
                handleHealthCheck((HealthCheckCommand) command);
            }

        } catch (Exception e) {
            log.error("❌ Error in test device auth responder", e);
        }
    }

    private void handleValidateToken(ValidateTokenCommand command) {
        try {
            String token = command.getToken();
            UUID deviceUid = validDeviceTokens.get(token);

            TokenValidationResponse response;
            if (deviceUid != null) {
                response = TokenValidationResponse.builder()
                        .correlationId(command.getCorrelationId())
                        .isValid(true)
                        .deviceUid(deviceUid)
                        .build();
                log.info("✅ Test Responder: Device token validation successful for device {}", deviceUid);
            } else {
                response = TokenValidationResponse.builder()
                        .correlationId(command.getCorrelationId())
                        .isValid(false)
                        .error("Invalid device token")
                        .build();
                log.warn("⚠️ Test Responder: Device token validation failed");
            }

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

        } catch (Exception e) {
            log.error("❌ Error handling Device ValidateToken", e);
        }
    }

    private void handleExtractDeviceUid(ExtractDeviceUidCommand command) {
        try {
            String token = command.getToken();
            UUID deviceUid = validDeviceTokens.get(token);

            DeviceUidResponse response;
            if (deviceUid != null) {
                response = DeviceUidResponse.builder()
                        .correlationId(command.getCorrelationId())
                        .deviceUid(deviceUid)
                        .build();
                log.info("✅ Test Responder: Extracted device UID {} from token", deviceUid);
            } else {
                response = DeviceUidResponse.builder()
                        .correlationId(command.getCorrelationId())
                        .error("Invalid device token")
                        .build();
                log.warn("⚠️ Test Responder: Failed to extract device UID from token");
            }

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

        } catch (Exception e) {
            log.error("❌ Error handling ExtractDeviceUid", e);
        }
    }

    private void handleHealthCheck(HealthCheckCommand command) {
        try {
            HealthCheckResponse response = HealthCheckResponse.builder()
                    .correlationId(command.getCorrelationId())
                    .success(true)
                    .healthStatus(Map.of("status", "OK", "service", "test-device-auth-responder"))
                    .build();

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.info("✅ Test Responder: Device auth health check responded");

        } catch (Exception e) {
            log.error("❌ Error handling Device Auth HealthCheck", e);
        }
    }

    // Методы для управления тестовыми данными
    public void addValidDeviceToken(String token, UUID deviceUid) {
        validDeviceTokens.put(token, deviceUid);
        log.info("🔑 Test Responder: Added valid device token for device {}", deviceUid);
    }

    public void removeDeviceToken(String token) {
        validDeviceTokens.remove(token);
        log.info("🗑️ Test Responder: Removed device token");
    }

    public void clearTestData() {
        validDeviceTokens.clear();
        log.info("🧹 Test Responder: All device auth test data cleared");
    }

    public boolean hasValidDeviceToken(String token) {
        return validDeviceTokens.containsKey(token);
    }
}