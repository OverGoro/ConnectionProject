package com.service.auth.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.connection.auth.events.commands.ExtractClientUidCommand;
import com.connection.auth.events.commands.HealthCheckCommand;
import com.connection.auth.events.commands.ValidateTokenCommand;
import com.connection.auth.events.responses.ClientUidResponse;
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

    @KafkaListener(topics = "${app.kafka.topics.auth-commands:auth.commands}")
    public void handleAuthCommand(ConsumerRecord<String, Command> record) {
        try {
            Command command = record.value();
            String key = record.key();

            if (command instanceof ValidateTokenCommand) {
                ValidateTokenCommand validateCommand = (ValidateTokenCommand) command;
                handleValidateTokenCommand(validateCommand, key);
            } else if (command instanceof ExtractClientUidCommand) {

                ExtractClientUidCommand extractCommand = (ExtractClientUidCommand) command;
                handleExtractClientUidCommand(extractCommand, key);
            } else if (command instanceof HealthCheckCommand) {

                HealthCheckCommand healthCommand = (HealthCheckCommand) command;
                handleHealthCheckCommand(healthCommand, key);
            }

            else {
                log.warn("Unknown command typeId: {}", command.getClass().getCanonicalName());
            }

        } catch (

        Exception e) {
            log.error("Error processing auth command: key={}", record.key(), e);
        }
    }

    private void handleValidateTokenCommand(ValidateTokenCommand command, String key) {
        log.warn("Command {}, key {}", command, key);
        try {
            log.warn("Command {}, key {}", command, key);
            AccessTokenBLM tokenBLM = accessTokenConverter.toBLM(
                    new com.connection.token.model.AccessTokenDTO(command.getToken()));
            authService.validateAccessToken(tokenBLM);
            log.info("kafka validated token: {}", tokenBLM.getToken());
            TokenValidationResponse response = TokenValidationResponse.valid(
                    command.getCorrelationId(),
                    tokenBLM.getClientUID(),
                    command.getTokenType().name());
            log.info("Kafka formed response {}", response.toString());

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.info("Kafka send token validation successful: clientUid={}", response.toString());

        } catch (Exception e) {
            TokenValidationResponse response = TokenValidationResponse.error(
                    command.getCorrelationId(),
                    e.getMessage());
            log.warn("Command {}, key {}", command, key);
            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.error("Token validation failed: {}", e.getMessage());
            throw e;
        }
    }

    private void handleExtractClientUidCommand(ExtractClientUidCommand command, String key) {
        try {
            AccessTokenBLM tokenBLM = accessTokenConverter.toBLM(
                    new com.connection.token.model.AccessTokenDTO(command.getToken()));
            authService.validateAccessToken(tokenBLM);

            ClientUidResponse response = ClientUidResponse.success(
                    command.getCorrelationId(),
                    tokenBLM.getClientUID(),
                    command.getTokenType().name());

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.info("Client UID extracted: {}", tokenBLM.getClientUID());

        } catch (Exception e) {
            ClientUidResponse response = ClientUidResponse.error(
                    command.getCorrelationId(),
                    e.getMessage());
            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.error("Client UID extraction failed: {}", e.getMessage());
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
            log.info("Health check completed successfully");

        } catch (Exception e) {
            HealthCheckResponse response = HealthCheckResponse.error(
                    command.getCorrelationId(),
                    e.getMessage());
            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.error("Health check failed: {}", e.getMessage());
        }
    }
}