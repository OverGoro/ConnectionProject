// TestDeviceServiceResponder.java
package com.connection.message.integration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import com.connection.device.events.commands.GetDeviceByUidCommand;
import com.connection.device.events.commands.GetDevicesByClientUid;
import com.connection.device.events.commands.HealthCheckCommand;
import com.connection.device.events.responses.GetDeviceByUidResponse;
import com.connection.device.events.responses.GetDevicesByClientResponse;
import com.connection.device.events.responses.HealthCheckResponse;
import com.connection.device.model.DeviceDTO;

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
public class TestDeviceServiceResponder {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Хранилище тестовых данных
    private final Map<UUID, DeviceDTO> testDevices = new ConcurrentHashMap<>();
    private final Map<UUID, List<DeviceDTO>> clientDevices = new ConcurrentHashMap<>();

    @Value("${app.kafka.topics.device-commands:device.commands}")
    private String deviceCommandsTopic;

    @PostConstruct
    public void logKafkaConfiguration() {
        log.info("""
            🧪 Test Device Responder Kafka Configuration:
               📨 Listening Topic: {}
            """, deviceCommandsTopic);
    }

    @KafkaListener(topics = "${app.kafka.topics.device-commands:device.commands}", groupId = "test-device-responder")
    public void handleDeviceCommand(ConsumerRecord<String, Object> record) {
        try {
            Object command = record.value();
            if (command instanceof GetDeviceByUidCommand) {
                handleGetDeviceByUid((GetDeviceByUidCommand) command);
            } else if (command instanceof GetDevicesByClientUid) {
                handleGetDevicesByClient((GetDevicesByClientUid) command);
            } else if (command instanceof HealthCheckCommand) {
                handleHealthCheck((HealthCheckCommand) command);
            }

        } catch (Exception e) {
            log.error("❌ Error in test device responder", e);
        }
    }

    private void handleGetDeviceByUid(GetDeviceByUidCommand command) {
        try {
            UUID deviceUid = command.getDeviceUid();
            DeviceDTO device = testDevices.get(deviceUid);

            GetDeviceByUidResponse response;
            if (device != null) {
                response = GetDeviceByUidResponse.builder()
                .correlationId(command.getCorrelationId())
                .deviceDTO(device)
                .success(true).build();
                

                log.info("✅ Test Responder: Device {} found", deviceUid);
            } else {
                response = GetDeviceByUidResponse.error(
                        command.getCorrelationId(),
                        "Device not found in test data");
                log.warn("⚠️ Test Responder: Device {} not found", deviceUid);
            }

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

        } catch (Exception e) {
            log.error("❌ Error handling GetDeviceByUid", e);
        }
    }

    private void handleGetDevicesByClient(GetDevicesByClientUid command) {
        try {
            UUID clientUid = command.getClientUid();
            List<DeviceDTO> devices = clientDevices.get(clientUid);

            GetDevicesByClientResponse response;
            if (devices != null && !devices.isEmpty()) {
                response = GetDevicesByClientResponse.valid(
                        command.getCorrelationId(),
                        devices);
                response.setSuccess(true);
                response.setCorrelationId(command.getCorrelationId());
                response.setSourceService("test-device-responser");
                
                log.info("✅ Test Responder: Found {} devices for client {}", devices.size(), clientUid);
            } else {
                response = GetDevicesByClientResponse.valid(
                        command.getCorrelationId(),
                        List.of());
                log.info("ℹ️ Test Responder: No devices found for client {}", clientUid);
            }

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

        } catch (Exception e) {
            log.error("❌ Error handling GetDevicesByClient", e);
        }
    }

    private void handleHealthCheck(HealthCheckCommand command) {
        try {
            HealthCheckResponse response = HealthCheckResponse.success(
                    command.getCorrelationId(),
                    Map.of("status", "OK", "service", "test-device-responder"));

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.info("✅ Test Responder: Health check responded");

        } catch (Exception e) {
            log.error("❌ Error handling HealthCheck", e);
        }
    }

    // Методы для управления тестовыми данными
    public void addTestDevice(UUID deviceUid, UUID clientUid, String deviceName) {
        DeviceDTO device = new DeviceDTO();
        device.setUid(deviceUid.toString());
        device.setClientUuid(clientUid.toString());
        device.setDeviceName(deviceName);
        device.setDeviceDescription("Test device for integration tests");

        testDevices.put(deviceUid, device);

        List<DeviceDTO> clientDeviceList = clientDevices.computeIfAbsent(
                clientUid, k -> new java.util.ArrayList<>());
        clientDeviceList.add(device);

        log.info("📝 Test Responder: Added test device {} for client {}", deviceUid, clientUid);
    }

    public void removeTestDevice(UUID deviceUid) {
        DeviceDTO device = testDevices.remove(deviceUid);
        if (device != null) {
            UUID clientUid = UUID.fromString(device.getClientUuid());
            List<DeviceDTO> clientDevicesList = clientDevices.get(clientUid);
            if (clientDevicesList != null) {
                clientDevicesList.removeIf(d -> d.getUid().equals(deviceUid.toString()));
            }
        }
    }

    public void clearTestData() {
        testDevices.clear();
        clientDevices.clear();
        log.info("🧹 Test Responder: All device test data cleared");
    }

    public boolean hasDevice(UUID deviceUid) {
        return testDevices.containsKey(deviceUid);
    }

    public boolean deviceBelongsToClient(UUID deviceUid, UUID clientUid) {
        DeviceDTO device = testDevices.get(deviceUid);
        return device != null && device.getClientUuid().equals(clientUid.toString());
    }
}