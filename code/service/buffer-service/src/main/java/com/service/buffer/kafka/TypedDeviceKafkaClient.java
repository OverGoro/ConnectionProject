// TypedDeviceKafkaClient.java
package com.service.buffer.kafka;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.connection.device.events.DeviceEventConstants;
import com.connection.device.events.DeviceEventUtils;
import com.connection.device.events.commands.GetDeviceByUidCommand;
import com.connection.device.events.commands.GetDevicesByClientUid;
import com.connection.device.events.commands.HealthCheckCommand;
import com.connection.device.events.responses.GetDeviceByUidResponse;
import com.connection.device.events.responses.GetDevicesByClientResponse;
import com.connection.device.events.responses.HealthCheckResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TypedDeviceKafkaClient {

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

    public CompletableFuture<GetDeviceByUidResponse> getDeviceByUid(UUID deviceUid, UUID clientUid, String sourceService) {
        return sendRequest(
            GetDeviceByUidCommand.builder()
                .deviceUid(deviceUid)
                .clientUid(clientUid)
                .sourceService(sourceService)
                .replyTopic(DeviceEventConstants.DEVICE_RESPONSES_TOPIC)
                .correlationId(DeviceEventUtils.generateCorrelationId())
                .build(),
            GetDeviceByUidResponse.class
        );
    }

    public CompletableFuture<GetDevicesByClientResponse> getDevicesByClient(UUID clientUid, String sourceService) {
        return sendRequest(
            GetDevicesByClientUid.builder()
                .clientUid(clientUid)
                .sourceService(sourceService)
                .replyTopic(DeviceEventConstants.DEVICE_RESPONSES_TOPIC)
                .correlationId(DeviceEventUtils.generateCorrelationId())
                .build(),
            GetDevicesByClientResponse.class
        );
    }

    public CompletableFuture<HealthCheckResponse> healthCheck(String sourceService) {
        return sendRequest(
            HealthCheckCommand.builder()
                .sourceService(sourceService)
                .replyTopic(DeviceEventConstants.DEVICE_RESPONSES_TOPIC)
                .correlationId(DeviceEventUtils.generateCorrelationId())
                .build(),
            HealthCheckResponse.class
        );
    }

    // Вспомогательные методы для удобства
    public boolean deviceExistsAndBelongsToClient(UUID deviceUid, UUID clientUid) {
        try {
            GetDeviceByUidResponse response = getDeviceByUid(deviceUid, clientUid, "buffer-service")
                    .get(10, java.util.concurrent.TimeUnit.SECONDS);
            return response.isSuccess() && response.getDeviceDTO() != null;
        } catch (Exception e) {
            log.error("Error checking device existence: {}", e.getMessage());
            return false;
        }
    }

    public List<UUID> getClientDeviceUids(UUID clientUid) {
        try {
            GetDevicesByClientResponse response = getDevicesByClient(clientUid, "buffer-service")
                    .get(10, java.util.concurrent.TimeUnit.SECONDS);
            
            if (response.isSuccess() && response.getDeviceDTOs() != null) {
                return response.getDeviceDTOs().stream()
                        .map(device -> UUID.fromString(device.getUid()))
                        .collect(java.util.stream.Collectors.toList());
            }
            return List.of();
        } catch (Exception e) {
            log.error("Error getting client devices: {}", e.getMessage());
            return List.of();
        }
    }

    private <T> CompletableFuture<T> sendRequest(Object command, Class<T> responseType) {
        String correlationId = extractCorrelationId(command);
        
        CompletableFuture<T> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, new PendingRequest<>(future, responseType));

        kafkaTemplate.send(DeviceEventConstants.DEVICE_COMMANDS_TOPIC, correlationId, command)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        future.completeExceptionally(ex);
                        pendingRequests.remove(correlationId);
                        log.error("Failed to send device command: {}", ex.getMessage());
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
                        new ClassCastException("Type mismatch in device response")
                    );
                }
            } catch (Exception e) {
                pendingRequest.future.completeExceptionally(e);
            }
        } else {
            log.warn("Received device response for unknown correlationId: {}", correlationId);
        }
    }

    private String extractCorrelationId(Object command) {
        if (command instanceof GetDeviceByUidCommand) {
            return ((GetDeviceByUidCommand) command).getCorrelationId();
        } else if (command instanceof GetDevicesByClientUid) {
            return ((GetDevicesByClientUid) command).getCorrelationId();
        } else if (command instanceof HealthCheckCommand) {
            return ((HealthCheckCommand) command).getCorrelationId();
        } else {
            throw new IllegalArgumentException("Unsupported device command type: " + command.getClass());
        }
    }
}