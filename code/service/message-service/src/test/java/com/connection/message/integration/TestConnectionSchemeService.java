// TestConnectionSchemeServiceResponder.java
package com.connection.message.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import com.connection.scheme.model.ConnectionSchemeBLM;
import com.service.connectionscheme.ConnectionSchemeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ActiveProfiles("integrationtest")
public class TestConnectionSchemeService implements ConnectionSchemeService {
    // Хранилище тестовых данных
    private final Map<UUID, ConnectionSchemeBLM> testSchemes = new ConcurrentHashMap<>();
    private final Map<UUID, List<ConnectionSchemeBLM>> bufferSchemes = new ConcurrentHashMap<>();
    private final Map<UUID, List<ConnectionSchemeBLM>> clientSchemes = new ConcurrentHashMap<>();

    /**
     * Добавляет тестовую схему подключения
     */
    public void addTestConnectionScheme(UUID schemeUid, UUID clientUid, List<UUID> usedBuffers, Map<UUID, List<UUID>> bufferTransitions) {
        ConnectionSchemeBLM scheme = createTestConnectionSchemeBLM(schemeUid, clientUid, usedBuffers, bufferTransitions);
        testSchemes.put(schemeUid, scheme);
        
        // Связываем схему с клиентом
        linkSchemeToClient(schemeUid, clientUid);
        
        // Связываем схему с буферами
        if (usedBuffers != null) {
            for (UUID bufferUid : usedBuffers) {
                linkSchemeToBuffer(schemeUid, bufferUid);
            }
        }
        
        log.info("Test Responder: Added connection scheme {} for client {}", schemeUid, clientUid);
    }

    /**
     * Связывает схему с буфером
     */
    public void linkSchemeToBuffer(UUID schemeUid, UUID bufferUid) {
        ConnectionSchemeBLM scheme = testSchemes.get(schemeUid);
        if (scheme != null) {
            List<ConnectionSchemeBLM> bufferSchemeList = bufferSchemes.computeIfAbsent(
                    bufferUid, k -> new ArrayList<>());
            if (!bufferSchemeList.contains(scheme)) {
                bufferSchemeList.add(scheme);
            }
            log.info("Test Responder: Linked scheme {} to buffer {}", schemeUid, bufferUid);
        }
    }

    /**
     * Связывает схему с клиентом
     */
    public void linkSchemeToClient(UUID schemeUid, UUID clientUid) {
        ConnectionSchemeBLM scheme = testSchemes.get(schemeUid);
        if (scheme != null) {
            List<ConnectionSchemeBLM> clientSchemeList = clientSchemes.computeIfAbsent(
                    clientUid, k -> new ArrayList<>());
            if (!clientSchemeList.contains(scheme)) {
                clientSchemeList.add(scheme);
            }
            log.info("Test Responder: Linked scheme {} to client {}", schemeUid, clientUid);
        }
    }

    /**
     * Очищает все тестовые данные
     */
    public void clearTestData() {
        testSchemes.clear();
        bufferSchemes.clear();
        clientSchemes.clear();
        log.info("Test Responder: All connection scheme test data cleared");
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
        ConnectionSchemeBLM scheme = testSchemes.get(schemeUid);
        return scheme != null && scheme.getClientUid().equals(clientUid);
    }

    /**
     * Получает схему по UID
     */
    public ConnectionSchemeBLM getConnectionScheme(UUID schemeUid) {
        return testSchemes.get(schemeUid);
    }

    /**
     * Получает все схемы для буфера
     */
    public List<ConnectionSchemeBLM> getConnectionSchemesForBuffer(UUID bufferUid) {
        return bufferSchemes.getOrDefault(bufferUid, List.of());
    }

    /**
     * Получает все схемы для клиента
     */
    public List<ConnectionSchemeBLM> getConnectionSchemesForClient(UUID clientUid) {
        return clientSchemes.getOrDefault(clientUid, List.of());
    }

    /**
     * Удаляет схему
     */
    public void removeConnectionScheme(UUID schemeUid) {
        ConnectionSchemeBLM removedScheme = testSchemes.remove(schemeUid);
        if (removedScheme != null) {
            // Удаляем из связей с буферами
            bufferSchemes.values().forEach(schemes -> schemes.remove(removedScheme));
            // Удаляем из связей с клиентами
            clientSchemes.values().forEach(schemes -> schemes.remove(removedScheme));
            log.info("🗑️ Test Responder: Removed connection scheme {}", schemeUid);
        }
    }

    /**
     * Создает тестовый BLM схемы подключения
     */
    private ConnectionSchemeBLM createTestConnectionSchemeBLM(UUID schemeUid, UUID clientUid, 
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

            ConnectionSchemeBLM scheme = new ConnectionSchemeBLM();
            scheme.setUid(schemeUid);
            scheme.setClientUid(clientUid);
            scheme.setUsedBuffers(usedBuffers != null ? usedBuffers : new ArrayList<>());
            scheme.setSchemeJson(schemeJson);
            scheme.setBufferTransitions(bufferTransitions != null ? bufferTransitions : new HashMap<>());
            
            return scheme;
        } catch (Exception e) {
            log.error("❌ Error creating test connection scheme BLM", e);
            // Fallback: создаем простой BLM без JSON
            ConnectionSchemeBLM scheme = new ConnectionSchemeBLM();
            scheme.setUid(schemeUid);
            scheme.setClientUid(clientUid);
            scheme.setUsedBuffers(usedBuffers != null ? usedBuffers : new ArrayList<>());
            scheme.setSchemeJson("{}");
            scheme.setBufferTransitions(bufferTransitions != null ? bufferTransitions : new HashMap<>());
            return scheme;
        }
    }

    @Override
    public ConnectionSchemeBLM createScheme(ConnectionSchemeBLM schemeBLM) {
        if (schemeBLM.getUid() == null) {
            schemeBLM.setUid(UUID.randomUUID());
        }
        
        testSchemes.put(schemeBLM.getUid(), schemeBLM);
        
        // Связываем с клиентом
        linkSchemeToClient(schemeBLM.getUid(), schemeBLM.getClientUid());
        
        // Связываем с буферами
        if (schemeBLM.getUsedBuffers() != null) {
            for (UUID bufferUid : schemeBLM.getUsedBuffers()) {
                linkSchemeToBuffer(schemeBLM.getUid(), bufferUid);
            }
        }
        
        log.info("📝 Test Responder: Created connection scheme {} for client {}", 
                schemeBLM.getUid(), schemeBLM.getClientUid());
        
        return schemeBLM;
    }

    @Override
    public void deleteScheme(UUID schemeUid) {
        ConnectionSchemeBLM removedScheme = testSchemes.remove(schemeUid);
        if (removedScheme != null) {
            // Удаляем из связей с буферами
            bufferSchemes.values().forEach(schemes -> schemes.removeIf(s -> s.getUid().equals(schemeUid)));
            // Удаляем из связей с клиентами
            clientSchemes.values().forEach(schemes -> schemes.removeIf(s -> s.getUid().equals(schemeUid)));
            log.info("🗑️ Test Responder: Deleted connection scheme {}", schemeUid);
        } else {
            log.warn("⚠️ Test Responder: Attempted to delete non-existent scheme {}", schemeUid);
        }
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("totalSchemes", testSchemes.size());
        healthStatus.put("totalClients", clientSchemes.size());
        healthStatus.put("totalBuffers", bufferSchemes.size());
        healthStatus.put("timestamp", java.time.Instant.now());
        healthStatus.put("service", "TestConnectionSchemeService");
        
        log.debug("❤️ Test Responder: Health check - {} schemes, {} clients, {} buffers", 
                testSchemes.size(), clientSchemes.size(), bufferSchemes.size());
        
        return healthStatus;
    }

    @Override
    public ConnectionSchemeBLM getSchemeByUid(UUID schemeUid) {
        ConnectionSchemeBLM scheme = testSchemes.get(schemeUid);
        if (scheme == null) {
            log.debug("🔍 Test Responder: Scheme {} not found", schemeUid);
        }
        return scheme;
    }

    @Override
    public List<ConnectionSchemeBLM> getSchemeByUid(List<UUID> schemeUids) {
        List<ConnectionSchemeBLM> result = new ArrayList<>();
        for (UUID schemeUid : schemeUids) {
            ConnectionSchemeBLM scheme = testSchemes.get(schemeUid);
            if (scheme != null) {
                result.add(scheme);
            }
        }
        log.debug("🔍 Test Responder: Found {} schemes out of {} requested", result.size(), schemeUids.size());
        return result;
    }

    @Override
    public List<ConnectionSchemeBLM> getSchemesByBuffer(UUID bufferUuid) {
        List<ConnectionSchemeBLM> schemes = bufferSchemes.getOrDefault(bufferUuid, new ArrayList<>());
        log.debug("🔍 Test Responder: Found {} schemes for buffer {}", schemes.size(), bufferUuid);
        return new ArrayList<>(schemes); // Возвращаем копию для безопасности
    }

    @Override
    public List<ConnectionSchemeBLM> getSchemesByBuffer(List<UUID> bufferUuids) {
        List<ConnectionSchemeBLM> result = new ArrayList<>();
        for (UUID bufferUuid : bufferUuids) {
            List<ConnectionSchemeBLM> schemes = bufferSchemes.get(bufferUuid);
            if (schemes != null) {
                for (ConnectionSchemeBLM scheme : schemes) {
                    if (!result.contains(scheme)) {
                        result.add(scheme);
                    }
                }
            }
        }
        log.debug("🔍 Test Responder: Found {} unique schemes for {} buffers", result.size(), bufferUuids.size());
        return result;
    }

    @Override
    public List<ConnectionSchemeBLM> getSchemesByClient(UUID clientUuid) {
        List<ConnectionSchemeBLM> schemes = clientSchemes.getOrDefault(clientUuid, new ArrayList<>());
        log.debug("🔍 Test Responder: Found {} schemes for client {}", schemes.size(), clientUuid);
        return new ArrayList<>(schemes); // Возвращаем копию для безопасности
    }

    @Override
    public boolean schemeExists(UUID schemeUid) {
        return testSchemes.containsKey(schemeUid);
    }

    @Override
    public ConnectionSchemeBLM updateScheme(UUID schemeUid, ConnectionSchemeBLM schemeBLM) {
        ConnectionSchemeBLM existingScheme = testSchemes.get(schemeUid);
        if (existingScheme == null) {
            log.warn("⚠️ Test Responder: Attempted to update non-existent scheme {}", schemeUid);
            return null;
        }
        
        // Обновляем поля схемы
        if (schemeBLM.getSchemeJson() != null) {
            existingScheme.setSchemeJson(schemeBLM.getSchemeJson());
        }
        if (schemeBLM.getUsedBuffers() != null) {
            // Обновляем связи с буферами
            List<UUID> oldBuffers = existingScheme.getUsedBuffers();
            List<UUID> newBuffers = schemeBLM.getUsedBuffers();
            
            // Удаляем старые связи
            for (UUID bufferUid : oldBuffers) {
                List<ConnectionSchemeBLM> bufferSchemesList = bufferSchemes.get(bufferUid);
                if (bufferSchemesList != null) {
                    bufferSchemesList.remove(existingScheme);
                }
            }
            
            // Добавляем новые связи
            existingScheme.setUsedBuffers(new ArrayList<>(newBuffers));
            for (UUID bufferUid : newBuffers) {
                linkSchemeToBuffer(schemeUid, bufferUid);
            }
        }
        if (schemeBLM.getBufferTransitions() != null) {
            existingScheme.setBufferTransitions(new HashMap<>(schemeBLM.getBufferTransitions()));
        }
        
        log.info("✏️ Test Responder: Updated connection scheme {}", schemeUid);
        return existingScheme;
    }
}