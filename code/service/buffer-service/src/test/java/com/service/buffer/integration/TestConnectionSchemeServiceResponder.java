package com.service.buffer.integration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
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
    private final Map<UUID, List<ConnectionSchemeDTO>> clientSchemes = new ConcurrentHashMap<>();
    private final Map<UUID, List<ConnectionSchemeDTO>> bufferSchemes = new ConcurrentHashMap<>();
    
    @KafkaListener(
        topics = "${app.kafka.topics.connection-scheme-commands:scheme.commands}",
        groupId = "test-connection-scheme-responder"
    )
    public void handleConnectionSchemeCommand(ConsumerRecord<String, Object> record) {
        try {
            Object command = record.value();
            log.info("🧪 Test Connection Scheme Responder received command: {}", command.getClass().getSimpleName());
            
            if (command instanceof GetConnectionSchemeByUidCommand) {
                handleGetConnectionSchemeByUid((GetConnectionSchemeByUidCommand) command);
            } else if (command instanceof GetConnectionSchemesByClientUid) {
                handleGetConnectionSchemesByClient((GetConnectionSchemesByClientUid) command);
            } else if (command instanceof GetConnectionSchemesByBufferUid) {
                handleGetConnectionSchemesByBuffer((GetConnectionSchemesByBufferUid) command);
            } else if (command instanceof HealthCheckCommand) {
                handleHealthCheck((HealthCheckCommand) command);
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
                    scheme
                );
                log.info("✅ Test Responder: Connection Scheme {} found", schemeUid);
            } else {
                response = GetConnectionSchemeByUidResponse.error(
                    command.getCorrelationId(),
                    "Connection scheme not found in test data"
                );
                log.warn("⚠️ Test Responder: Connection Scheme {} not found", schemeUid);
            }
            
            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            
        } catch (Exception e) {
            log.error("❌ Error handling GetConnectionSchemeByUid", e);
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
                    schemes
                );
                log.info("✅ Test Responder: Found {} connection schemes for client {}", schemes.size(), clientUid);
            } else {
                response = GetConnectionSchemesByClientResponse.valid(
                    command.getCorrelationId(),
                    List.of()
                );
                log.info("ℹ️ Test Responder: No connection schemes found for client {}", clientUid);
            }
            
            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            
        } catch (Exception e) {
            log.error("❌ Error handling GetConnectionSchemesByClient", e);
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
                    schemes
                );
                log.info("✅ Test Responder: Found {} connection schemes for buffer {}", schemes.size(), bufferUid);
            } else {
                response = GetConnectionSchemesByBufferResponse.valid(
                    command.getCorrelationId(),
                    List.of()
                );
                log.info("ℹ️ Test Responder: No connection schemes found for buffer {}", bufferUid);
            }
            
            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            
        } catch (Exception e) {
            log.error("❌ Error handling GetConnectionSchemesByBuffer", e);
        }
    }
    
    private void handleHealthCheck(HealthCheckCommand command) {
        try {
            HealthCheckResponse response = HealthCheckResponse.success(
                command.getCorrelationId(),
                Map.of("status", "OK", "service", "test-connection-scheme-responder")
            );
            
            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.info("✅ Test Responder: Health check responded");
            
        } catch (Exception e) {
            log.error("❌ Error handling HealthCheck", e);
        }
    }
    
    // Методы для управления тестовыми данными
    
    public void addTestConnectionScheme(UUID schemeUid, UUID clientUid, List<UUID> usedBuffers) {
        ConnectionSchemeDTO scheme = createTestConnectionSchemeDTO(schemeUid, clientUid, usedBuffers);
        addTestConnectionScheme(scheme);
    }
    
    public void addTestConnectionScheme(ConnectionSchemeDTO scheme) {
        UUID schemeUid = UUID.fromString(scheme.getUid());
        UUID clientUid = UUID.fromString(scheme.getClientUid());
        
        testSchemes.put(schemeUid, scheme);
        
        // Добавляем в список схем клиента
        List<ConnectionSchemeDTO> clientSchemeList = clientSchemes.computeIfAbsent(
            clientUid, k -> new ArrayList<>()
        );
        clientSchemeList.add(scheme);
        
        // Автоматически создаем связи с буферами из usedBuffers
        if (scheme.getUsedBuffers() != null) {
            for (UUID bufferUid : scheme.getUsedBuffers()) {
                linkSchemeToBuffer(schemeUid, bufferUid);
            }
        }
        
        log.info("📝 Test Responder: Added connection scheme {} for client {} with {} used buffers", 
                schemeUid, clientUid, 
                scheme.getUsedBuffers() != null ? scheme.getUsedBuffers().size() : 0);
    }
    
    public void linkSchemeToBuffer(UUID schemeUid, UUID bufferUid) {
        ConnectionSchemeDTO scheme = testSchemes.get(schemeUid);
        if (scheme != null) {
            List<ConnectionSchemeDTO> bufferSchemeList = bufferSchemes.computeIfAbsent(
                bufferUid, k -> new ArrayList<>()
            );
            if (!bufferSchemeList.contains(scheme)) {
                bufferSchemeList.add(scheme);
            }
            log.info("🔗 Test Responder: Linked scheme {} to buffer {}", schemeUid, bufferUid);
        } else {
            log.warn("⚠️ Test Responder: Cannot link - scheme {} not found", schemeUid);
        }
    }
    
    public void removeTestConnectionScheme(UUID schemeUid) {
        ConnectionSchemeDTO scheme = testSchemes.remove(schemeUid);
        if (scheme != null) {
            UUID clientUid = UUID.fromString(scheme.getClientUid());
            
            // Удаляем из списка клиента
            List<ConnectionSchemeDTO> clientSchemesList = clientSchemes.get(clientUid);
            if (clientSchemesList != null) {
                clientSchemesList.removeIf(s -> s.getUid().equals(schemeUid.toString()));
            }
            
            // Удаляем из всех связей с буферами
            bufferSchemes.values().forEach(schemeList -> 
                schemeList.removeIf(s -> s.getUid().equals(schemeUid.toString()))
            );
        }
    }
    
    public void clearTestData() {
        testSchemes.clear();
        clientSchemes.clear();
        bufferSchemes.clear();
        log.info("🧹 Test Responder: All connection scheme test data cleared");
    }
    
    public boolean hasConnectionScheme(UUID schemeUid) {
        return testSchemes.containsKey(schemeUid);
    }
    
    public boolean connectionSchemeBelongsToClient(UUID schemeUid, UUID clientUid) {
        ConnectionSchemeDTO scheme = testSchemes.get(schemeUid);
        return scheme != null && scheme.getClientUid().equals(clientUid.toString());
    }
    
    private ConnectionSchemeDTO createTestConnectionSchemeDTO(UUID schemeUid, UUID clientUid, List<UUID> usedBuffers) {
        return ConnectionSchemeDTO.builder()
            .uid(schemeUid.toString())
            .clientUid(clientUid.toString())
            .usedBuffers(usedBuffers != null ? usedBuffers : new ArrayList<>())
            .schemeJson("{\"test\": true, \"schemeType\": \"integration-test\", \"buffers\": " + 
                       (usedBuffers != null ? usedBuffers.toString() : "[]") + "}")
            .build();
    }
    
    // Дополнительные методы для удобства
    
    public void addTestConnectionSchemeWithBuffers(UUID schemeUid, UUID clientUid, UUID... bufferUids) {
        List<UUID> usedBuffers = bufferUids != null ? Arrays.asList(bufferUids) : new ArrayList<>();
        addTestConnectionScheme(schemeUid, clientUid, usedBuffers);
    }
    
    public void addBufferToScheme(UUID schemeUid, UUID bufferUid) {
        ConnectionSchemeDTO scheme = testSchemes.get(schemeUid);
        if (scheme != null) {
            List<UUID> usedBuffers = scheme.getUsedBuffers();
            if (usedBuffers == null) {
                usedBuffers = new ArrayList<>();
                scheme.setUsedBuffers(usedBuffers);
            }
            if (!usedBuffers.contains(bufferUid)) {
                usedBuffers.add(bufferUid);
            }
            linkSchemeToBuffer(schemeUid, bufferUid);
            log.info("➕ Test Responder: Added buffer {} to scheme {}", bufferUid, schemeUid);
        }
    }
    
    public void removeBufferFromScheme(UUID schemeUid, UUID bufferUid) {
        ConnectionSchemeDTO scheme = testSchemes.get(schemeUid);
        if (scheme != null && scheme.getUsedBuffers() != null) {
            scheme.getUsedBuffers().remove(bufferUid);
            
            // Удаляем связь
            List<ConnectionSchemeDTO> bufferSchemesList = bufferSchemes.get(bufferUid);
            if (bufferSchemesList != null) {
                bufferSchemesList.removeIf(s -> s.getUid().equals(schemeUid.toString()));
            }
            log.info("➖ Test Responder: Removed buffer {} from scheme {}", bufferUid, schemeUid);
        }
    }
}