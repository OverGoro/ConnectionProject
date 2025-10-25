// TestConnectionSchemeServiceResponder.java
package com.connection.message.integration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import com.connection.scheme.events.commands.GetConnectionSchemeByUidCommand;
import com.connection.scheme.events.commands.GetConnectionSchemesByBufferUid;
import com.connection.scheme.events.commands.GetConnectionSchemesByClientUid;
import com.connection.scheme.events.commands.HealthCheckCommand;
import com.connection.scheme.events.responses.GetConnectionSchemeByUidResponse;
import com.connection.scheme.events.responses.GetConnectionSchemesByBufferResponse;
import com.connection.scheme.events.responses.GetConnectionSchemesByClientResponse;
import com.connection.scheme.events.responses.HealthCheckResponse;
import com.connection.scheme.model.ConnectionSchemeDTO;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
@ActiveProfiles("integrationtest")
public class TestConnectionSchemeServiceResponder {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Хранилище тестовых данных
    private final Map<UUID, ConnectionSchemeDTO> testSchemes = new ConcurrentHashMap<>();
    private final Map<UUID, List<ConnectionSchemeDTO>> bufferSchemes = new ConcurrentHashMap<>();
    private final Map<UUID, List<ConnectionSchemeDTO>> clientSchemes = new ConcurrentHashMap<>();

    @Value("${app.kafka.topics.connection-scheme-commands:connection.scheme.commands}")
    private String connectionSchemeCommandsTopic;

    @PostConstruct
    public void logKafkaConfiguration() {
        log.info("""
            🧪 Test Connection Scheme Responder Kafka Configuration:
               📨 Listening Topic: {}
            """, connectionSchemeCommandsTopic);
    }

    @KafkaListener(topics = "${app.kafka.topics.connection-scheme-commands:connection.scheme.commands}", groupId = "test-connection-scheme-responder")
    public void handleConnectionSchemeCommand(ConsumerRecord<String, Object> record) {
        try {
            Object command = record.value();
            log.info("📥 Test Responder: Received command: {} with key: {}", 
                    command.getClass().getSimpleName(), record.key());

            if (command instanceof GetConnectionSchemeByUidCommand) {
                handleGetConnectionSchemeByUid((GetConnectionSchemeByUidCommand) command);
            } else if (command instanceof GetConnectionSchemesByBufferUid) {
                handleGetConnectionSchemesByBuffer((GetConnectionSchemesByBufferUid) command);
            } else if (command instanceof GetConnectionSchemesByClientUid) {
                handleGetConnectionSchemesByClient((GetConnectionSchemesByClientUid) command);
            } else if (command instanceof HealthCheckCommand) {
                handleHealthCheck((HealthCheckCommand) command);
            } else {
                log.warn("⚠️ Test Responder: Unknown command type: {}", command.getClass().getSimpleName());
            }

        } catch (Exception e) {
            log.error("❌ Error in test connection scheme responder", e);
        }
    }

    private void handleGetConnectionSchemeByUid(GetConnectionSchemeByUidCommand command) {
        try {
            UUID schemeUid = command.getConnectionSchemeUid();
            ConnectionSchemeDTO scheme = testSchemes.get(schemeUid);

            GetConnectionSchemeByUidResponse response;
            if (scheme != null) {
                response = GetConnectionSchemeByUidResponse.success(
                        command.getCorrelationId(),
                        scheme);
                log.info("✅ Test Responder: Connection Scheme {} found, sending to {}", 
                        schemeUid, command.getReplyTopic());
            } else {
                response = GetConnectionSchemeByUidResponse.error(
                        command.getCorrelationId(),
                        "Connection scheme not found in test data");
                log.warn("⚠️ Test Responder: Connection Scheme {} not found", schemeUid);
            }

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.info("📤 Test Responder: Response sent to topic: {}", command.getReplyTopic());

        } catch (Exception e) {
            log.error("❌ Error handling GetConnectionSchemeByUid", e);
            // Отправляем ошибку обратно
            GetConnectionSchemeByUidResponse errorResponse = GetConnectionSchemeByUidResponse.error(
                    command.getCorrelationId(),
                    "Internal server error: " + e.getMessage());
            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), errorResponse);
        }
    }

    private void handleGetConnectionSchemesByBuffer(GetConnectionSchemesByBufferUid command) {
        try {
            UUID bufferUid = command.getBufferUid();
            List<ConnectionSchemeDTO> schemes = bufferSchemes.get(bufferUid);

            GetConnectionSchemesByBufferResponse response;
            if (schemes != null && !schemes.isEmpty()) {
                response = GetConnectionSchemesByBufferResponse.valid(
                        command.getCorrelationId(),
                        schemes);
                log.info("✅ Test Responder: Found {} connection schemes for buffer {}", 
                        schemes.size(), bufferUid);
            } else {
                response = GetConnectionSchemesByBufferResponse.valid(
                        command.getCorrelationId(),
                        List.of());
                log.info("ℹ️ Test Responder: No connection schemes found for buffer {}", bufferUid);
            }

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.info("📤 Test Responder: Buffer schemes response sent to topic: {}", command.getReplyTopic());

        } catch (Exception e) {
            log.error("❌ Error handling GetConnectionSchemesByBuffer", e);
            GetConnectionSchemesByBufferResponse errorResponse = GetConnectionSchemesByBufferResponse.error(
                    command.getCorrelationId(),
                    "Internal server error: " + e.getMessage());
            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), errorResponse);
        }
    }

    private void handleGetConnectionSchemesByClient(GetConnectionSchemesByClientUid command) {
        try {
            UUID clientUid = command.getClientUid();
            List<ConnectionSchemeDTO> schemes = clientSchemes.get(clientUid);

            GetConnectionSchemesByClientResponse response;
            if (schemes != null && !schemes.isEmpty()) {
                response = GetConnectionSchemesByClientResponse.valid(
                        command.getCorrelationId(),
                        schemes);
                log.info("✅ Test Responder: Found {} connection schemes for client {}", 
                        schemes.size(), clientUid);
            } else {
                response = GetConnectionSchemesByClientResponse.valid(
                        command.getCorrelationId(),
                        List.of());
                log.info("ℹ️ Test Responder: No connection schemes found for client {}", clientUid);
            }

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.info("📤 Test Responder: Client schemes response sent to topic: {}", command.getReplyTopic());

        } catch (Exception e) {
            log.error("❌ Error handling GetConnectionSchemesByClient", e);
            GetConnectionSchemesByClientResponse errorResponse = GetConnectionSchemesByClientResponse.error(
                    command.getCorrelationId(),
                    "Internal server error: " + e.getMessage());
            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), errorResponse);
        }
    }

    private void handleHealthCheck(HealthCheckCommand command) {
        try {
            Map<String, Object> healthStatus = Map.of(
                    "status", "OK",
                    "service", "test-connection-scheme-responder",
                    "timestamp", System.currentTimeMillis(),
                    "testDataCount", testSchemes.size()
            );

            HealthCheckResponse response = HealthCheckResponse.success(
                    command.getCorrelationId(),
                    healthStatus);

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.info("✅ Test Responder: Health check responded to {}", command.getReplyTopic());

        } catch (Exception e) {
            log.error("❌ Error handling HealthCheck", e);
            HealthCheckResponse errorResponse = HealthCheckResponse.error(
                    command.getCorrelationId(),
                    "Health check failed: " + e.getMessage());
            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), errorResponse);
        }
    }

    // Методы для управления тестовыми данными

    /**
     * Добавляет тестовую схему подключения
     */
    public void addTestConnectionScheme(UUID schemeUid, UUID clientUid, List<UUID> usedBuffers, Map<UUID, List<UUID>> bufferTransitions) {
        ConnectionSchemeDTO scheme = createTestConnectionSchemeDTO(schemeUid, clientUid, usedBuffers, bufferTransitions);
        testSchemes.put(schemeUid, scheme);
        
        // Связываем схему с клиентом
        linkSchemeToClient(schemeUid, clientUid);
        
        // Связываем схему с буферами
        if (usedBuffers != null) {
            for (UUID bufferUid : usedBuffers) {
                linkSchemeToBuffer(schemeUid, bufferUid);
            }
        }
        
        log.info("📝 Test Responder: Added connection scheme {} for client {}", schemeUid, clientUid);
    }

    /**
     * Связывает схему с буфером
     */
    public void linkSchemeToBuffer(UUID schemeUid, UUID bufferUid) {
        ConnectionSchemeDTO scheme = testSchemes.get(schemeUid);
        if (scheme != null) {
            List<ConnectionSchemeDTO> bufferSchemeList = bufferSchemes.computeIfAbsent(
                    bufferUid, k -> new ArrayList<>());
            if (!bufferSchemeList.contains(scheme)) {
                bufferSchemeList.add(scheme);
            }
            log.info("🔗 Test Responder: Linked scheme {} to buffer {}", schemeUid, bufferUid);
        }
    }

    /**
     * Связывает схему с клиентом
     */
    public void linkSchemeToClient(UUID schemeUid, UUID clientUid) {
        ConnectionSchemeDTO scheme = testSchemes.get(schemeUid);
        if (scheme != null) {
            List<ConnectionSchemeDTO> clientSchemeList = clientSchemes.computeIfAbsent(
                    clientUid, k -> new ArrayList<>());
            if (!clientSchemeList.contains(scheme)) {
                clientSchemeList.add(scheme);
            }
            log.info("👤 Test Responder: Linked scheme {} to client {}", schemeUid, clientUid);
        }
    }

    /**
     * Очищает все тестовые данные
     */
    public void clearTestData() {
        testSchemes.clear();
        bufferSchemes.clear();
        clientSchemes.clear();
        log.info("🧹 Test Responder: All connection scheme test data cleared");
    }

    /**
     * Проверяет наличие схемы
     */
    public boolean hasConnectionScheme(UUID schemeUid) {
        return testSchemes.containsKey(schemeUid);
    }

    /**
     * Проверяет принадлежность схемы клиенту
     */
    public boolean connectionSchemeBelongsToClient(UUID schemeUid, UUID clientUid) {
        ConnectionSchemeDTO scheme = testSchemes.get(schemeUid);
        return scheme != null && scheme.getClientUid().equals(clientUid.toString());
    }

    /**
     * Получает схему по UID
     */
    public ConnectionSchemeDTO getConnectionScheme(UUID schemeUid) {
        return testSchemes.get(schemeUid);
    }

    /**
     * Получает все схемы для буфера
     */
    public List<ConnectionSchemeDTO> getConnectionSchemesForBuffer(UUID bufferUid) {
        return bufferSchemes.getOrDefault(bufferUid, List.of());
    }

    /**
     * Получает все схемы для клиента
     */
    public List<ConnectionSchemeDTO> getConnectionSchemesForClient(UUID clientUid) {
        return clientSchemes.getOrDefault(clientUid, List.of());
    }

    /**
     * Удаляет схему
     */
    public void removeConnectionScheme(UUID schemeUid) {
        ConnectionSchemeDTO removedScheme = testSchemes.remove(schemeUid);
        if (removedScheme != null) {
            // Удаляем из связей с буферами
            bufferSchemes.values().forEach(schemes -> schemes.remove(removedScheme));
            // Удаляем из связей с клиентами
            clientSchemes.values().forEach(schemes -> schemes.remove(removedScheme));
            log.info("🗑️ Test Responder: Removed connection scheme {}", schemeUid);
        }
    }

    /**
     * Создает тестовый DTO схемы подключения
     */
    private ConnectionSchemeDTO createTestConnectionSchemeDTO(UUID schemeUid, UUID clientUid, 
                                                            List<UUID> usedBuffers, 
                                                            Map<UUID, List<UUID>> bufferTransitions) {
        try {
            // Создаем корректный JSON для схемы
            Map<String, Object> schemeData = new HashMap<>();
            if (bufferTransitions != null && !bufferTransitions.isEmpty()) {
                schemeData.put("bufferTransitions", bufferTransitions);
            } else {
                schemeData.put("bufferTransitions", new HashMap<>());
            }

            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String schemeJson = objectMapper.writeValueAsString(schemeData);

            return ConnectionSchemeDTO.builder()
                    .uid(schemeUid.toString())
                    .clientUid(clientUid.toString())
                    .usedBuffers(usedBuffers != null ? usedBuffers : new ArrayList<>())
                    .schemeJson(schemeJson)
                    .build();
        } catch (Exception e) {
            log.error("❌ Error creating test connection scheme DTO", e);
            // Fallback: создаем простой DTO без JSON
            return ConnectionSchemeDTO.builder()
                    .uid(schemeUid.toString())
                    .clientUid(clientUid.toString())
                    .usedBuffers(usedBuffers != null ? usedBuffers : new ArrayList<>())
                    .schemeJson("{}")
                    .build();
        }
    }
}