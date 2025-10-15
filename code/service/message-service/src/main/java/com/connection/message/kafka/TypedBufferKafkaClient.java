package com.connection.message.kafka;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.connection.buffer.events.BufferEventConstants;
import com.connection.buffer.events.BufferEventUtils;
import com.connection.buffer.events.commands.GetBufferByUidCommand;
import com.connection.buffer.events.commands.GetBuffersByClientUidCommand;
import com.connection.buffer.events.commands.GetBuffersByConnectionSchemeUidCommand;
import com.connection.buffer.events.commands.GetBuffersByDeviceUidCommand;
import com.connection.buffer.events.commands.HealthCheckCommand;
import com.connection.buffer.events.responses.GetBufferByUidResponse;
import com.connection.buffer.events.responses.GetBuffersByClientResponse;
import com.connection.buffer.events.responses.GetBuffersByConnectionSchemeResponse;
import com.connection.buffer.events.responses.GetBuffersByDeviceResponse;
import com.connection.buffer.events.responses.HealthCheckResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TypedBufferKafkaClient {

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

    public CompletableFuture<GetBufferByUidResponse> getBufferByUid(UUID bufferUid, String sourceService) {
        return sendRequest(
            GetBufferByUidCommand.builder()
                .bufferUid(bufferUid)
                .sourceService(sourceService)
                .replyTopic(BufferEventConstants.BUFFER_RESPONSES_TOPIC)
                .correlationId(BufferEventUtils.generateCorrelationId())
                .build(),
            GetBufferByUidResponse.class
        );
    }

    public CompletableFuture<GetBuffersByClientResponse> getBuffersByClientUid(UUID clientUid, String sourceService) {
        return sendRequest(
            GetBuffersByClientUidCommand.builder()
                .clientUid(clientUid)
                .sourceService(sourceService)
                .replyTopic(BufferEventConstants.BUFFER_RESPONSES_TOPIC)
                .correlationId(BufferEventUtils.generateCorrelationId())
                .build(),
            GetBuffersByClientResponse.class
        );
    }

    public CompletableFuture<GetBuffersByDeviceResponse> getBuffersByDeviceUid(UUID deviceUid, String sourceService) {
        return sendRequest(
            GetBuffersByDeviceUidCommand.builder()
                .deviceUid(deviceUid)
                .sourceService(sourceService)
                .replyTopic(BufferEventConstants.BUFFER_RESPONSES_TOPIC)
                .correlationId(BufferEventUtils.generateCorrelationId())
                .build(),
            GetBuffersByDeviceResponse.class
        );
    }

    public CompletableFuture<GetBuffersByConnectionSchemeResponse> getBuffersByConnectionSchemeUid(UUID connectionSchemeUid, String sourceService) {
        return sendRequest(
            GetBuffersByConnectionSchemeUidCommand.builder()
                .connectionSchemeUid(connectionSchemeUid)
                .sourceService(sourceService)
                .replyTopic(BufferEventConstants.BUFFER_RESPONSES_TOPIC)
                .correlationId(BufferEventUtils.generateCorrelationId())
                .build(),
            GetBuffersByConnectionSchemeResponse.class
        );
    }

    public CompletableFuture<HealthCheckResponse> healthCheck(String sourceService) {
        return sendRequest(
            HealthCheckCommand.builder()
                .eventId(UUID.randomUUID().toString())
                .sourceService(sourceService)
                .timestamp(new Date().toInstant())
                .replyTopic(BufferEventConstants.BUFFER_RESPONSES_TOPIC)
                .correlationId(BufferEventUtils.generateCorrelationId())
                .commandType(BufferEventConstants.COMMAND_HEALTH_CHECK)
                .build(),
            HealthCheckResponse.class
        );
    }

    private <T> CompletableFuture<T> sendRequest(Object command, Class<T> responseType) {
        String correlationId;
        
        if (command instanceof GetBufferByUidCommand) {
            correlationId = ((GetBufferByUidCommand) command).getCorrelationId();
        } else if (command instanceof GetBuffersByClientUidCommand) {
            correlationId = ((GetBuffersByClientUidCommand) command).getCorrelationId();
        } else if (command instanceof GetBuffersByDeviceUidCommand) {
            correlationId = ((GetBuffersByDeviceUidCommand) command).getCorrelationId();
        } else if (command instanceof GetBuffersByConnectionSchemeUidCommand) {
            correlationId = ((GetBuffersByConnectionSchemeUidCommand) command).getCorrelationId();
        } else if (command instanceof HealthCheckCommand) {
            correlationId = ((HealthCheckCommand) command).getCorrelationId();
        } else {
            throw new IllegalArgumentException("Unsupported command type: " + command.getClass());
        }

        CompletableFuture<T> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, new PendingRequest<>(future, responseType));

        kafkaTemplate.send(BufferEventConstants.BUFFER_COMMANDS_TOPIC, correlationId, command)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        future.completeExceptionally(ex);
                        pendingRequests.remove(correlationId);
                        log.error("Failed to send buffer command: {}", ex.getMessage());
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
                        new ClassCastException("Type mismatch in buffer response")
                    );
                }
            } catch (Exception e) {
                pendingRequest.future.completeExceptionally(e);
            }
        } else {
            log.warn("Received buffer response for unknown correlationId: {}", correlationId);
        }
    }
}