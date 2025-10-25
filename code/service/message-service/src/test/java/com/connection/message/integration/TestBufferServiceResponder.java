// TestBufferServiceResponder.java
package com.connection.message.integration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import com.connection.buffer.events.commands.GetBufferByUidCommand;
import com.connection.buffer.events.commands.GetBuffersByDeviceUidCommand;
import com.connection.buffer.events.commands.GetBuffersByConnectionSchemeUidCommand;
import com.connection.buffer.events.commands.HealthCheckCommand;
import com.connection.buffer.events.responses.GetBufferByUidResponse;
import com.connection.buffer.events.responses.GetBuffersByDeviceResponse;
import com.connection.buffer.events.responses.GetBuffersByConnectionSchemeResponse;
import com.connection.buffer.events.responses.HealthCheckResponse;
import com.connection.processing.buffer.model.BufferDTO;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
@ActiveProfiles("integrationtest")
public class TestBufferServiceResponder {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Хранилище тестовых данных
    private final Map<UUID, BufferDTO> testBuffers = new ConcurrentHashMap<>();
    private final Map<UUID, List<BufferDTO>> deviceBuffers = new ConcurrentHashMap<>();
    private final Map<UUID, List<BufferDTO>> schemeBuffers = new ConcurrentHashMap<>();

    @Value("${app.kafka.topics.buffer-commands:buffer.commands}")
    private String bufferCommandsTopic;

    @PostConstruct
    public void logKafkaConfiguration() {
        log.info("""
            🧪 Test Buffer Responder Kafka Configuration:
               📨 Listening Topic: {}
            """, bufferCommandsTopic);
    }

    @KafkaListener(topics = "${app.kafka.topics.buffer-commands:buffer.commands}", groupId = "test-buffer-responder")
    public void handleBufferCommand(ConsumerRecord<String, Object> record) {
        try {
            Object command = record.value();
            if (command instanceof GetBufferByUidCommand) {
                handleGetBufferByUid((GetBufferByUidCommand) command);
            } else if (command instanceof GetBuffersByDeviceUidCommand) {
                handleGetBuffersByDevice((GetBuffersByDeviceUidCommand) command);
            } else if (command instanceof GetBuffersByConnectionSchemeUidCommand) {
                handleGetBuffersByConnectionScheme((GetBuffersByConnectionSchemeUidCommand) command);
            } else if (command instanceof HealthCheckCommand) {
                handleHealthCheck((HealthCheckCommand) command);
            }

        } catch (Exception e) {
            log.error("❌ Error in test buffer responder", e);
        }
    }

    private void handleGetBufferByUid(GetBufferByUidCommand command) {
        try {
            UUID bufferUid = command.getBufferUid();
            BufferDTO buffer = testBuffers.get(bufferUid);

            GetBufferByUidResponse response;
            if (buffer != null) {
                response = GetBufferByUidResponse.success(
                        command.getCorrelationId(),
                        buffer);
                log.info("✅ Test Responder: Buffer {} found", bufferUid);
            } else {
                response = GetBufferByUidResponse.error(
                        command.getCorrelationId(),
                        "Buffer not found in test data");
                log.warn("⚠️ Test Responder: Buffer {} not found", bufferUid);
            }

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

        } catch (Exception e) {
            log.error("❌ Error handling GetBufferByUid", e);
        }
    }

    private void handleGetBuffersByDevice(GetBuffersByDeviceUidCommand command) {
        try {
            UUID deviceUid = command.getDeviceUid();
            List<BufferDTO> buffers = deviceBuffers.get(deviceUid);

            GetBuffersByDeviceResponse response;
            if (buffers != null && !buffers.isEmpty()) {
                response = GetBuffersByDeviceResponse.success(
                        command.getCorrelationId(),
                        buffers);
                log.info("✅ Test Responder: Found {} buffers for device {}", buffers.size(), deviceUid);
            } else {
                response = GetBuffersByDeviceResponse.success(
                        command.getCorrelationId(),
                        List.of());
                log.info("ℹ️ Test Responder: No buffers found for device {}", deviceUid);
            }

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

        } catch (Exception e) {
            log.error("❌ Error handling GetBuffersByDevice", e);
        }
    }

    private void handleGetBuffersByConnectionScheme(GetBuffersByConnectionSchemeUidCommand command) {
        try {
            UUID schemeUid = command.getConnectionSchemeUid();
            List<BufferDTO> buffers = schemeBuffers.get(schemeUid);

            GetBuffersByConnectionSchemeResponse response;
            if (buffers != null && !buffers.isEmpty()) {
                response = GetBuffersByConnectionSchemeResponse.success(
                        command.getCorrelationId(),
                        buffers);
                log.info("✅ Test Responder: Found {} buffers for scheme {}", buffers.size(), schemeUid);
            } else {
                response = GetBuffersByConnectionSchemeResponse.success(
                        command.getCorrelationId(),
                        List.of());
                log.info("ℹ️ Test Responder: No buffers found for scheme {}", schemeUid);
            }

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

        } catch (Exception e) {
            log.error("❌ Error handling GetBuffersByConnectionScheme", e);
        }
    }

    private void handleHealthCheck(HealthCheckCommand command) {
        try {
            HealthCheckResponse response = HealthCheckResponse.success(
                    command.getCorrelationId(),
                    Map.of("status", "OK", "service", "test-buffer-responder"));

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.info("✅ Test Responder: Health check responded");

        } catch (Exception e) {
            log.error("❌ Error handling HealthCheck", e);
        }
    }

    // Методы для управления тестовыми данными
    public void addTestBuffer(UUID bufferUid, UUID deviceUid, int maxMessages, int maxSize) {
        BufferDTO buffer = new BufferDTO(
                bufferUid.toString(),
                deviceUid.toString(),
                maxMessages,
                maxSize,
                "{}"
        );

        testBuffers.put(bufferUid, buffer);

        List<BufferDTO> deviceBufferList = deviceBuffers.computeIfAbsent(
                deviceUid, k -> new java.util.ArrayList<>());
        deviceBufferList.add(buffer);

        log.info("📝 Test Responder: Added test buffer {} for device {}", bufferUid, deviceUid);
    }

    public void linkBufferToScheme(UUID bufferUid, UUID schemeUid) {
        BufferDTO buffer = testBuffers.get(bufferUid);
        if (buffer != null) {
            List<BufferDTO> schemeBufferList = schemeBuffers.computeIfAbsent(
                    schemeUid, k -> new java.util.ArrayList<>());
            if (!schemeBufferList.contains(buffer)) {
                schemeBufferList.add(buffer);
            }
            log.info("🔗 Test Responder: Linked buffer {} to scheme {}", bufferUid, schemeUid);
        }
    }

    public void clearTestData() {
        testBuffers.clear();
        deviceBuffers.clear();
        schemeBuffers.clear();
        log.info("🧹 Test Responder: All buffer test data cleared");
    }

    public boolean hasBuffer(UUID bufferUid) {
        return testBuffers.containsKey(bufferUid);
    }

    public boolean bufferBelongsToDevice(UUID bufferUid, UUID deviceUid) {
        BufferDTO buffer = testBuffers.get(bufferUid);
        return buffer != null && buffer.getDeviceUid().equals(deviceUid.toString());
    }
}