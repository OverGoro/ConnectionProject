package com.service.auth.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.connection.auth.events.commands.HealthCheckCommand;
import com.connection.auth.events.commands.ValidateTokenCommand;
import com.connection.auth.events.responses.HealthCheckResponse;
import com.connection.auth.events.responses.TokenValidationResponse;
import com.connection.common.events.Command;
import com.connection.token.converter.AccessTokenConverter;
import com.connection.token.model.AccessTokenBLM;
import com.service.auth.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthCommandConsumer {

    private final AuthService authService;
    private final AccessTokenConverter accessTokenConverter;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(
        topics = "${app.kafka.topics.auth-commands:auth.commands}",
        containerFactory = "kafkaListenerContainerFactory" // Указываем конкретную фабрику
    )
    public void handleAuthCommand(ConsumerRecord<String, Object> record) { // Изменен тип на Object
        try {
            log.debug("Received command from Kafka: key={}, topic={}, partition={}", 
                     record.key(), record.topic(), record.partition());

            Object command = record.value();

            if (command instanceof ValidateTokenCommand) {
                ValidateTokenCommand validateCommand = (ValidateTokenCommand) command;
                handleValidateTokenCommand(validateCommand, record.key());
            } else if (command instanceof HealthCheckCommand) {
                HealthCheckCommand healthCommand = (HealthCheckCommand) command;
                handleHealthCheckCommand(healthCommand, record.key());
            } else {
                log.warn("Unknown command type: {}", command != null ? command.getClass().getCanonicalName() : "null");
            }

        } catch (Exception e) {
            log.error("Error processing auth command from Kafka: key={}", record.key(), e);
        }
    }

    private void handleValidateTokenCommand(ValidateTokenCommand command, String key) {
        try {
            log.info("Processing token validation: correlationId={}", command.getCorrelationId());

            AccessTokenBLM tokenBLM = accessTokenConverter.toBLM(
                    new com.connection.token.model.AccessTokenDTO(command.getToken()));
            authService.validateAccessToken(tokenBLM);

            TokenValidationResponse response = TokenValidationResponse.valid(
                    command.getCorrelationId(),
                    tokenBLM.getClientUID(),
                    command.getTokenType().name());

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.info("Token validation response sent to {}: correlationId={}",
                    command.getReplyTopic(), command.getCorrelationId());

        } catch (Exception e) {
            TokenValidationResponse response = TokenValidationResponse.error(
                    command.getCorrelationId(),
                    e.getMessage());

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.error("Token validation failed: correlationId={}, error={}",
                    command.getCorrelationId(), e.getMessage());
        }
    }

    private void handleHealthCheckCommand(HealthCheckCommand command, String key) {
        try {
            java.util.Map<String, Object> healthStatus = java.util.Map.of(
                    "status", "OK",
                    "service", "auth-service",
                    "timestamp", System.currentTimeMillis());

            HealthCheckResponse response = HealthCheckResponse.success(
                    command.getCorrelationId(),
                    healthStatus);

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.info("Health check response sent to {}: correlationId={}",
                    command.getReplyTopic(), command.getCorrelationId());

        } catch (Exception e) {
            HealthCheckResponse response = HealthCheckResponse.error(
                    command.getCorrelationId(),
                    e.getMessage());

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.error("Health check failed: correlationId={}, error={}",
                    command.getCorrelationId(), e.getMessage());
        }
    }
}