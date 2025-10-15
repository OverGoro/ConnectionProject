// TypedConnectionSchemeKafkaClient.java
package com.connection.message.kafka;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.connection.scheme.events.ConnectionSchemeEventConstants;
import com.connection.scheme.events.ConnectionSchemeEventUtils;
import com.connection.scheme.events.commands.GetConnectionSchemeByUidCommand;
import com.connection.scheme.events.commands.GetConnectionSchemesByBufferUid;
import com.connection.scheme.events.commands.HealthCheckCommand;
import com.connection.scheme.events.responses.GetConnectionSchemeByUidResponse;
import com.connection.scheme.events.responses.GetConnectionSchemesByBufferResponse;
import com.connection.scheme.events.responses.HealthCheckResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TypedConnectionSchemeKafkaClient {

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

    public CompletableFuture<GetConnectionSchemeByUidResponse> getConnectionSchemeByUid(UUID connectionSchemeUid, UUID clientUid, String sourceService) {
        return sendRequest(
            GetConnectionSchemeByUidCommand.builder()
                .connectionSchemeUid(connectionSchemeUid)
                .clientUid(clientUid)
                .sourceService(sourceService)
                .replyTopic(ConnectionSchemeEventConstants.CONNECTION_SCHEME_RESPONSES_TOPIC)
                .correlationId(ConnectionSchemeEventUtils.generateCorrelationId())
                .build(),
            GetConnectionSchemeByUidResponse.class
        );
    }

    public CompletableFuture<GetConnectionSchemesByBufferResponse> getConnectionSchemesByBufferUid(UUID bufferUid, String sourceService) {
        return sendRequest(
            GetConnectionSchemesByBufferUid.builder()
                .bufferUid(bufferUid)
                .sourceService(sourceService)
                .replyTopic(ConnectionSchemeEventConstants.CONNECTION_SCHEME_RESPONSES_TOPIC)
                .correlationId(ConnectionSchemeEventUtils.generateCorrelationId())
                .build(),
            GetConnectionSchemesByBufferResponse.class
        );
    }

    public CompletableFuture<HealthCheckResponse> healthCheck(String sourceService) {
        return sendRequest(
            HealthCheckCommand.builder()
                .sourceService(sourceService)
                .replyTopic(ConnectionSchemeEventConstants.CONNECTION_SCHEME_RESPONSES_TOPIC)
                .correlationId(ConnectionSchemeEventUtils.generateCorrelationId())
                .build(),
            HealthCheckResponse.class
        );
    }

    // Вспомогательные методы для удобства
    public boolean connectionSchemeExistsAndBelongsToClient(UUID connectionSchemeUid, UUID clientUid) {
        try {
            GetConnectionSchemeByUidResponse response = getConnectionSchemeByUid(connectionSchemeUid, clientUid, "buffer-service")
                    .get(10, java.util.concurrent.TimeUnit.SECONDS);
            return response.isSuccess() && response.getConnectionSchemeDTO() != null;
        } catch (Exception e) {
            log.error("Error checking connection scheme existence: {}", e.getMessage());
            return false;
        }
    }

    private <T> CompletableFuture<T> sendRequest(Object command, Class<T> responseType) {
        String correlationId = extractCorrelationId(command);
        
        CompletableFuture<T> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, new PendingRequest<>(future, responseType));

        kafkaTemplate.send(ConnectionSchemeEventConstants.CONNECTION_SCHEME_COMMANDS_TOPIC, correlationId, command)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        future.completeExceptionally(ex);
                        pendingRequests.remove(correlationId);
                        log.error("Failed to send connection scheme command: {}", ex.getMessage());
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
}