package com.service.buffer.kafka;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.connection.scheme.events.ConnectionSchemeEventConstants;
import com.connection.scheme.events.ConnectionSchemeEventUtils;
import com.connection.scheme.events.commands.GetConnectionSchemeByUidCommand;
import com.connection.scheme.events.commands.HealthCheckCommand;
import com.connection.scheme.events.responses.GetConnectionSchemeByUidResponse;
import com.connection.scheme.events.responses.HealthCheckResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TypedConnectionSchemeKafkaClient {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Map<String, PendingRequest<?>> pendingRequests = new ConcurrentHashMap<>();
    
    // 👇 Уникальный топик для этого инстанса
    private final String instanceReplyTopic = "connection-scheme.responses." + UUID.randomUUID().toString();

    private static class PendingRequest<T> {
        final CompletableFuture<T> future;
        final Class<T> responseType;

        PendingRequest(CompletableFuture<T> future, Class<T> responseType) {
            this.future = future;
            this.responseType = responseType;
        }
    }

    public CompletableFuture<GetConnectionSchemeByUidResponse> getConnectionSchemeByUid(UUID connectionSchemeUid, String sourceService) {
        return sendRequest(
            GetConnectionSchemeByUidCommand.builder()
                .connectionSchemeUid(connectionSchemeUid)
                .sourceService(sourceService)
                .replyTopic(instanceReplyTopic) // 👈 Уникальный топик инстанса
                .correlationId(ConnectionSchemeEventUtils.generateCorrelationId())
                .build(),
            GetConnectionSchemeByUidResponse.class
        );
    }

    public CompletableFuture<HealthCheckResponse> healthCheck(String sourceService) {
        return sendRequest(
            HealthCheckCommand.builder()
                .sourceService(sourceService)
                .replyTopic(instanceReplyTopic) // 👈 Уникальный топик инстанса
                .correlationId(ConnectionSchemeEventUtils.generateCorrelationId())
                .build(),
            HealthCheckResponse.class
        );
    }

    // Вспомогательные методы для удобства
    public boolean connectionSchemeExistsAndBelongsToClient(UUID connectionSchemeUid, UUID clientUuid) {
        try {
            GetConnectionSchemeByUidResponse response = getConnectionSchemeByUid(connectionSchemeUid, "buffer-service")
                    .get(10, TimeUnit.SECONDS);
            return response.isSuccess() && response.getConnectionSchemeDTO() != null && 
                   response.getConnectionSchemeDTO().getClientUid().equals(clientUuid.toString());
        } catch (Exception e) {
            log.error("Error checking connection scheme existence: {}", e.getMessage());
            return false;
        }
    }

    private <T> CompletableFuture<T> sendRequest(Object command, Class<T> responseType) {
        String correlationId = extractCorrelationId(command);
        
        CompletableFuture<T> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, new PendingRequest<>(future, responseType));

        // 👇 Добавляем таймаут 30 секунд
        future.orTimeout(30, TimeUnit.SECONDS).whenComplete((result, ex) -> {
            if (ex != null) {
                pendingRequests.remove(correlationId);
                log.warn("Connection scheme request timeout or error for correlationId: {}", correlationId);
            }
        });

        kafkaTemplate.send(ConnectionSchemeEventConstants.CONNECTION_SCHEME_COMMANDS_TOPIC, correlationId, command)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        future.completeExceptionally(ex);
                        pendingRequests.remove(correlationId);
                        log.error("Failed to send connection scheme command: {}", ex.getMessage());
                    } else {
                        log.info("Connection scheme command sent successfully: correlationId={}, topic={}", 
                                correlationId, ConnectionSchemeEventConstants.CONNECTION_SCHEME_COMMANDS_TOPIC);
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
                    log.info("Connection scheme response handled successfully: correlationId={}", correlationId);
                } else {
                    log.warn("Type mismatch for correlationId: {}. Expected: {}, Got: {}", 
                            correlationId, pendingRequest.responseType, response.getClass());
                    pendingRequest.future.completeExceptionally(
                        new ClassCastException("Type mismatch in connection scheme response")
                    );
                }
            } catch (Exception e) {
                pendingRequest.future.completeExceptionally(e);
            }
        } else {
            log.warn("Received connection scheme response for unknown correlationId: {}", correlationId);
        }
    }

    private String extractCorrelationId(Object command) {
        if (command instanceof GetConnectionSchemeByUidCommand) {
            return ((GetConnectionSchemeByUidCommand) command).getCorrelationId();
        } else if (command instanceof HealthCheckCommand) {
            return ((HealthCheckCommand) command).getCorrelationId();
        } else {
            throw new IllegalArgumentException("Unsupported connection scheme command type: " + command.getClass());
        }
    }
    
    // 👇 Геттер для получения уникального топика инстанса
    public String getInstanceReplyTopic() {
        return instanceReplyTopic;
    }
}