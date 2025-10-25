package com.connection.message.kafka;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.connection.auth.events.AuthEventConstants;
import com.connection.auth.events.AuthEventUtils;
import com.connection.auth.events.commands.ExtractClientUidCommand;
import com.connection.auth.events.commands.HealthCheckCommand;
import com.connection.auth.events.commands.ValidateTokenCommand;
import com.connection.auth.events.responses.ClientUidResponse;
import com.connection.auth.events.responses.HealthCheckResponse;
import com.connection.auth.events.responses.TokenValidationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TypedAuthKafkaClient {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.auth-commands:auth.commands}")
    String authcommands;
    @Value("${app.kafka.topics.auth-responses:auth.responses}")
    String authresponses;
    @Value("${app.kafka.topics.device-auth-commands:device.auth.commands}")
    String deviceauthcommands;
    @Value("${app.kafka.topics.device-auth-responses:device.auth.responses}")
    String deviceauthresponses;
    @Value("${app.kafka.topics.device-commands:device.commands}")
    String devicecommands;
    @Value("${app.kafka.topics.device-responses:device.responses}")
    String deviceresponses;
    @Value("${app.kafka.topics.connection-scheme-commands:connection-scheme.commands}")
    String connectionschemecommands;
    @Value("${app.kafka.topics.connection-scheme-responses:connection-scheme.responses}")
    String connectionschemeresponses;
    @Value("${app.kafka.topics.buffer-commands:buffer.commands}")
    String buffercommands;
    @Value("${app.kafka.topics.buffer-responses:buffer.responses}")
    String bufferresponses;
    @Value("${app.kafka.topics.message-commands:message.commands}")
    String messagecommands;
    @Value("${app.kafka.topics.message-responses:message.responses}")
    String messageresponses;
    @Value("${app.kafka.topics.message-events:message.events}")
    String messageevents;
    
    private final Map<String, PendingRequest<?>> pendingRequests = new ConcurrentHashMap<>();
    
    // 👇 Уникальный топик для этого инстанса
    private final String instanceReplyTopic = "auth.responses." + UUID.randomUUID().toString();

    private static class PendingRequest<T> {
        final CompletableFuture<T> future;
        final Class<T> responseType;

        PendingRequest(CompletableFuture<T> future, Class<T> responseType) {
            this.future = future;
            this.responseType = responseType;
        }
    }

    public CompletableFuture<TokenValidationResponse> validateToken(String token, String sourceService) {
        return sendRequest(
            ValidateTokenCommand.builder()
                .token(token)
                .tokenType(ValidateTokenCommand.TokenType.ACCESS)
                .sourceService(sourceService)
                .replyTopic(instanceReplyTopic) 
                .correlationId(AuthEventUtils.generateCorrelationId())
                .build(),
            TokenValidationResponse.class
        );
    }

    public CompletableFuture<ClientUidResponse> getClientUid(String token, String sourceService) {
        return sendRequest(
            ExtractClientUidCommand.builder()
                .token(token)
                .tokenType(ExtractClientUidCommand.TokenType.ACCESS)
                .sourceService(sourceService)
                .replyTopic(instanceReplyTopic) 
                .correlationId(AuthEventUtils.generateCorrelationId())
                .build(),
            ClientUidResponse.class
        );
    }

    public CompletableFuture<HealthCheckResponse> healthCheck(String sourceService) {
        return sendRequest(
            HealthCheckCommand.builder()
                .eventId(UUID.randomUUID().toString())
                .sourceService(sourceService)
                .timestamp(new Date().toInstant())
                .replyTopic(instanceReplyTopic) 
                .correlationId(AuthEventUtils.generateCorrelationId())
                .commandType(AuthEventConstants.COMMAND_HEALTH_CHECK)
                .build(),
            HealthCheckResponse.class
        );
    }

    private <T> CompletableFuture<T> sendRequest(Object command, Class<T> responseType) {
        String correlationId;
        
        if (command instanceof ValidateTokenCommand) {
            correlationId = ((ValidateTokenCommand) command).getCorrelationId();
        } else if (command instanceof ExtractClientUidCommand) {
            correlationId = ((ExtractClientUidCommand) command).getCorrelationId();
        } else if (command instanceof HealthCheckCommand) {
            correlationId = ((HealthCheckCommand) command).getCorrelationId();
        } else {
            throw new IllegalArgumentException("Unsupported command type: " + command.getClass());
        }

        CompletableFuture<T> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, new PendingRequest<>(future, responseType));

        // 👇 Добавляем таймаут 30 секунд
        future.orTimeout(30, TimeUnit.SECONDS).whenComplete((result, ex) -> {
            if (ex != null) {
                pendingRequests.remove(correlationId);
                log.warn("Auth request timeout or error for correlationId: {}", correlationId);
            }
        });

        kafkaTemplate.send(authcommands, correlationId, command)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        future.completeExceptionally(ex);
                        pendingRequests.remove(correlationId);
                        log.error("Failed to send auth command: {}", ex.getMessage());
                    } else {
                        log.info("Auth command sent successfully: correlationId={}, topic={}", 
                                correlationId, authcommands);
                    }
                });

        return future;
    }

    @SuppressWarnings("unchecked")
    public void handleResponse(String correlationId, Object response) {
        PendingRequest<?> pendingRequest = pendingRequests.remove(correlationId);
        if (pendingRequest != null) {
            try {
                if (pendingRequest.responseType.isInstance(response)) {
                    CompletableFuture<Object> future = (CompletableFuture<Object>) pendingRequest.future;
                    future.complete(response);
                    log.info("Auth response handled successfully: correlationId={}", correlationId);
                } else {
                    log.warn("Type mismatch for correlationId: {}. Expected: {}, Got: {}", 
                            correlationId, pendingRequest.responseType, response.getClass());
                    pendingRequest.future.completeExceptionally(
                        new ClassCastException("Type mismatch in auth response")
                    );
                }
            } catch (Exception e) {
                pendingRequest.future.completeExceptionally(e);
            }
        } else {
            log.warn("Received auth response for unknown correlationId: {}", correlationId);
        }
    }
    
    // 👇 Геттер для получения уникального топика инстанса
    public String getInstanceReplyTopic() {
        return instanceReplyTopic;
    }
}