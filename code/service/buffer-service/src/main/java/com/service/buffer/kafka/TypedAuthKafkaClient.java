// TypedAuthKafkaClient.java
package com.service.buffer.kafka;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.connection.auth.events.AuthEventConstants;
import com.connection.auth.events.AuthEventUtils;
import com.connection.auth.events.commands.ExtractClientUidCommand;
import com.connection.auth.events.commands.HealthCheckCommand;
import com.connection.auth.events.commands.ValidateTokenCommand;
import com.connection.auth.events.responses.HealthCheckResponse;
import com.connection.auth.events.responses.TokenValidationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TypedAuthKafkaClient {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private final Map<String, PendingRequest<?>> pendingRequests = new ConcurrentHashMap<>();

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
                .replyTopic(AuthEventConstants.AUTH_RESPONSES_TOPIC)
                .correlationId(AuthEventUtils.generateCorrelationId())
                .build(),
            TokenValidationResponse.class
        );
    }

    public CompletableFuture<HealthCheckResponse> healthCheck(String sourceService) {
        return sendRequest(
            HealthCheckCommand.builder()
                .eventId(UUID.randomUUID().toString())
                .sourceService(sourceService)
                .timestamp(new Date().toInstant())
                .replyTopic(AuthEventConstants.AUTH_RESPONSES_TOPIC)
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

        kafkaTemplate.send(AuthEventConstants.AUTH_COMMANDS_TOPIC, correlationId, command)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        future.completeExceptionally(ex);
                        pendingRequests.remove(correlationId);
                        log.error("Failed to send auth command: {}", ex.getMessage());
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
}