package com.connection.message.kafka;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.connection.device.auth.events.DeviceAuthEventConstants;
import com.connection.device.auth.events.DeviceAuthEventUtils;
import com.connection.device.auth.events.commands.ExtractDeviceUidCommand;
import com.connection.device.auth.events.commands.HealthCheckCommand;
import com.connection.device.auth.events.commands.ValidateTokenCommand;
import com.connection.device.auth.events.responses.DeviceUidResponse;
import com.connection.device.auth.events.responses.HealthCheckResponse;
import com.connection.device.auth.events.responses.TokenValidationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TypedDeviceAuthKafkaClient {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private final Map<String, PendingRequest<?>> pendingRequests = new ConcurrentHashMap<>();

    private final String instanceReplyTopic = "device.auth.responses." + UUID.randomUUID().toString();

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
                .correlationId(DeviceAuthEventUtils.generateCorrelationId())
                .build(),
            TokenValidationResponse.class
        );
    }

    public CompletableFuture<TokenValidationResponse> validateDeviceToken(String token, String sourceService) {
        return sendRequest(
            ValidateTokenCommand.builder()
                .token(token)
                .tokenType(ValidateTokenCommand.TokenType.ACCESS)
                .sourceService(sourceService)
                .replyTopic(instanceReplyTopic)
                .correlationId(DeviceAuthEventUtils.generateCorrelationId())
                .build(),
            TokenValidationResponse.class
        );
    }

    public CompletableFuture<DeviceUidResponse> getDeviceUid(String token, String sourceService) {
        return sendRequest(
            ExtractDeviceUidCommand.builder()
                .token(token)
                .tokenType(ExtractDeviceUidCommand.TokenType.ACCESS)
                .sourceService(sourceService)
                .replyTopic(instanceReplyTopic)
                .correlationId(DeviceAuthEventUtils.generateCorrelationId())
                .build(),
            DeviceUidResponse.class
        );
    }

    public CompletableFuture<DeviceUidResponse> getDeviceUidFromDeviceToken(String token, String sourceService) {
        return sendRequest(
            ExtractDeviceUidCommand.builder()
                .token(token)
                .tokenType(ExtractDeviceUidCommand.TokenType.ACCESS)
                .sourceService(sourceService)
                .replyTopic(instanceReplyTopic)
                .correlationId(DeviceAuthEventUtils.generateCorrelationId())
                .build(),
            DeviceUidResponse.class
        );
    }

    public CompletableFuture<HealthCheckResponse> healthCheck(String sourceService) {
        return sendRequest(
            HealthCheckCommand.builder()
                .eventId(UUID.randomUUID().toString())
                .sourceService(sourceService)
                .timestamp(new Date().toInstant())
                .replyTopic(instanceReplyTopic)
                .correlationId(DeviceAuthEventUtils.generateCorrelationId())
                .commandType(DeviceAuthEventConstants.COMMAND_HEALTH_CHECK)
                .build(),
            HealthCheckResponse.class
        );
    }

    private <T> CompletableFuture<T> sendRequest(Object command, Class<T> responseType) {
        String correlationId;
        
        if (command instanceof ValidateTokenCommand) {
            correlationId = ((ValidateTokenCommand) command).getCorrelationId();
        } else if (command instanceof ExtractDeviceUidCommand) {
            correlationId = ((ExtractDeviceUidCommand) command).getCorrelationId();
        } else if (command instanceof HealthCheckCommand) {
            correlationId = ((HealthCheckCommand) command).getCorrelationId();
        } else {
            throw new IllegalArgumentException("Unsupported command type: " + command.getClass());
        }

        CompletableFuture<T> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, new PendingRequest<>(future, responseType));

        kafkaTemplate.send(DeviceAuthEventConstants.DEVICE_AUTH_COMMANDS_TOPIC, correlationId, command)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        future.completeExceptionally(ex);
                        pendingRequests.remove(correlationId);
                        log.error("Failed to send device auth command: {}", ex.getMessage());
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
                        new ClassCastException("Type mismatch in device auth response")
                    );
                }
            } catch (Exception e) {
                pendingRequest.future.completeExceptionally(e);
            }
        } else {
            log.warn("Received device auth response for unknown correlationId: {}", correlationId);
        }
    }
    // 👇 Геттер для получения уникального топика инстанса
    public String getInstanceReplyTopic() {
        return instanceReplyTopic;
    }
}