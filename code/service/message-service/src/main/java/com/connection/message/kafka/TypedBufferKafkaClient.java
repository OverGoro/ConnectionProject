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
    // 👇 Уникальный топик для этого инстанса
    private final String instanceReplyTopic = "buffer.responses." + UUID.randomUUID().toString();

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
                .replyTopic(instanceReplyTopic) 
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
                .replyTopic(instanceReplyTopic) 
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
                .replyTopic(instanceReplyTopic) 
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
                .replyTopic(instanceReplyTopic) 
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
                .replyTopic(instanceReplyTopic) 
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

        // 👇 Добавляем таймаут 30 секунд
        future.orTimeout(30, TimeUnit.SECONDS).whenComplete((result, ex) -> {
            if (ex != null) {
                pendingRequests.remove(correlationId);
                log.warn("Buffer request timeout or error for correlationId: {}", correlationId);
            }
        });

        kafkaTemplate.send(buffercommands, correlationId, command)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        future.completeExceptionally(ex);
                        pendingRequests.remove(correlationId);
                        log.error("Failed to send buffer command: {}", ex.getMessage());
                    } else {
                        log.info("Buffer command sent successfully: correlationId={}, topic={}", 
                                correlationId, buffercommands);
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
                    log.info("Buffer response handled successfully: correlationId={}", correlationId);
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
    
    // 👇 Геттер для получения уникального топика инстанса
    public String getInstanceReplyTopic() {
        return instanceReplyTopic;
    }
}