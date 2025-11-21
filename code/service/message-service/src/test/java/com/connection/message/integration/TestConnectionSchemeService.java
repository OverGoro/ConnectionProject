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

import com.connection.scheme.model.ConnectionSchemeBlm;
import com.service.connectionscheme.ConnectionSchemeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ActiveProfiles("integrationtest")
public class TestConnectionSchemeService implements ConnectionSchemeService {
    // Хранилище тестовых данных
    private final Map<UUID, ConnectionSchemeBlm> testSchemes = new ConcurrentHashMap<>();
    private final Map<UUID, List<ConnectionSchemeBlm>> bufferSchemes = new ConcurrentHashMap<>();
    private final Map<UUID, List<ConnectionSchemeBlm>> clientSchemes = new ConcurrentHashMap<>();

    /**
     * Добавляет тестовую схему подключения
     */
    public void addTestConnectionScheme(UUID schemeUid, UUID clientUid, List<UUID> usedBuffers, Map<UUID, List<UUID>> bufferTransitions) {
        ConnectionSchemeBlm scheme = createTestConnectionSchemeBlm(schemeUid, clientUid, usedBuffers, bufferTransitions);
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
        ConnectionSchemeBlm scheme = testSchemes.get(schemeUid);
        if (scheme != null) {
            List<ConnectionSchemeBlm> bufferSchemeList = bufferSchemes.computeIfAbsent(
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
        ConnectionSchemeBlm scheme = testSchemes.get(schemeUid);
        if (scheme != null) {
            List<ConnectionSchemeBlm> clientSchemeList = clientSchemes.computeIfAbsent(
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
        ConnectionSchemeBlm scheme = testSchemes.get(schemeUid);
        return scheme != null && scheme.getClientUid().equals(clientUid);
    }

    /**
     * Получает схему по UID
     */
    public ConnectionSchemeBlm getConnectionScheme(UUID schemeUid) {
        return testSchemes.get(schemeUid);
    }

    /**
     * Получает все схемы для буфера
     */
    public List<ConnectionSchemeBlm> getConnectionSchemesForBuffer(UUID bufferUid) {
        return bufferSchemes.getOrDefault(bufferUid, List.of());
    }

    /**
     * Получает все схемы для клиента
     */
    public List<ConnectionSchemeBlm> getConnectionSchemesForClient(UUID clientUid) {
        return clientSchemes.getOrDefault(clientUid, List.of());
    }

    /**
     * Удаляет схему
     */
    public void removeConnectionScheme(UUID schemeUid) {
        ConnectionSchemeBlm removedScheme = testSchemes.remove(schemeUid);
        if (removedScheme != null) {
            // Удаляем из связей с буферами
            bufferSchemes.values().forEach(schemes -> schemes.remove(removedScheme));
            // Удаляем из связей с клиентами
            clientSchemes.values().forEach(schemes -> schemes.remove(removedScheme));
            log.info("🗑️ Test Responder: Removed connection scheme {}", schemeUid);
        }
    }

    /**
     * Создает тестовый Blm схемы подключения
     */
    private ConnectionSchemeBlm createTestConnectionSchemeBlm(UUID schemeUid, UUID clientUid, 
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

            ConnectionSchemeBlm scheme = new ConnectionSchemeBlm();
            scheme.setUid(schemeUid);
            scheme.setClientUid(clientUid);
            scheme.setUsedBuffers(usedBuffers != null ? usedBuffers : new ArrayList<>());
            scheme.setSchemeJson(schemeJson);
            scheme.setBufferTransitions(bufferTransitions != null ? bufferTransitions : new HashMap<>());
            
            return scheme;
        } catch (Exception e) {
            log.error("❌ Error creating test connection scheme Blm", e);
            // Fallback: создаем простой Blm без JSON
            ConnectionSchemeBlm scheme = new ConnectionSchemeBlm();
            scheme.setUid(schemeUid);
            scheme.setClientUid(clientUid);
            scheme.setUsedBuffers(usedBuffers != null ? usedBuffers : new ArrayList<>());
            scheme.setSchemeJson("{}");
            scheme.setBufferTransitions(bufferTransitions != null ? bufferTransitions : new HashMap<>());
            return scheme;
        }
    }

    @Override
    public ConnectionSchemeBlm createScheme(ConnectionSchemeBlm schemeBlm) {
        if (schemeBlm.getUid() == null) {
            schemeBlm.setUid(UUID.randomUUID());
        }
        
        testSchemes.put(schemeBlm.getUid(), schemeBlm);
        
        // Связываем с клиентом
        linkSchemeToClient(schemeBlm.getUid(), schemeBlm.getClientUid());
        
        // Связываем с буферами
        if (schemeBlm.getUsedBuffers() != null) {
            for (UUID bufferUid : schemeBlm.getUsedBuffers()) {
                linkSchemeToBuffer(schemeBlm.getUid(), bufferUid);
            }
        }
        
        log.info("📝 Test Responder: Created connection scheme {} for client {}", 
                schemeBlm.getUid(), schemeBlm.getClientUid());
        
        return schemeBlm;
    }

    @Override
    public void deleteScheme(UUID schemeUid) {
        ConnectionSchemeBlm removedScheme = testSchemes.remove(schemeUid);
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
    public ConnectionSchemeBlm getSchemeByUid(UUID schemeUid) {
        ConnectionSchemeBlm scheme = testSchemes.get(schemeUid);
        if (scheme == null) {
            log.debug("🔍 Test Responder: Scheme {} not found", schemeUid);
        }
        return scheme;
    }

    @Override
    public List<ConnectionSchemeBlm> getSchemeByUid(List<UUID> schemeUids) {
        List<ConnectionSchemeBlm> result = new ArrayList<>();
        for (UUID schemeUid : schemeUids) {
            ConnectionSchemeBlm scheme = testSchemes.get(schemeUid);
            if (scheme != null) {
                result.add(scheme);
            }
        }
        log.debug("🔍 Test Responder: Found {} schemes out of {} requested", result.size(), schemeUids.size());
        return result;
    }

    @Override
    public List<ConnectionSchemeBlm> getSchemesByBuffer(UUID bufferUuid) {
        List<ConnectionSchemeBlm> schemes = bufferSchemes.getOrDefault(bufferUuid, new ArrayList<>());
        log.debug("🔍 Test Responder: Found {} schemes for buffer {}", schemes.size(), bufferUuid);
        return new ArrayList<>(schemes); // Возвращаем копию для безопасности
    }

    @Override
    public List<ConnectionSchemeBlm> getSchemesByBuffer(List<UUID> bufferUuids) {
        List<ConnectionSchemeBlm> result = new ArrayList<>();
        for (UUID bufferUuid : bufferUuids) {
            List<ConnectionSchemeBlm> schemes = bufferSchemes.get(bufferUuid);
            if (schemes != null) {
                for (ConnectionSchemeBlm scheme : schemes) {
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
    public List<ConnectionSchemeBlm> getSchemesByClient(UUID clientUuid) {
        List<ConnectionSchemeBlm> schemes = clientSchemes.getOrDefault(clientUuid, new ArrayList<>());
        log.debug("🔍 Test Responder: Found {} schemes for client {}", schemes.size(), clientUuid);
        return new ArrayList<>(schemes); // Возвращаем копию для безопасности
    }

    @Override
    public boolean schemeExists(UUID schemeUid) {
        return testSchemes.containsKey(schemeUid);
    }

    @Override
    public ConnectionSchemeBlm updateScheme(UUID schemeUid, ConnectionSchemeBlm schemeBlm) {
        ConnectionSchemeBlm existingScheme = testSchemes.get(schemeUid);
        if (existingScheme == null) {
            log.warn("⚠️ Test Responder: Attempted to update non-existent scheme {}", schemeUid);
            return null;
        }
        
        // Обновляем поля схемы
        if (schemeBlm.getSchemeJson() != null) {
            existingScheme.setSchemeJson(schemeBlm.getSchemeJson());
        }
        if (schemeBlm.getUsedBuffers() != null) {
            // Обновляем связи с буферами
            List<UUID> oldBuffers = existingScheme.getUsedBuffers();
            List<UUID> newBuffers = schemeBlm.getUsedBuffers();
            
            // Удаляем старые связи
            for (UUID bufferUid : oldBuffers) {
                List<ConnectionSchemeBlm> bufferSchemesList = bufferSchemes.get(bufferUid);
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
        if (schemeBlm.getBufferTransitions() != null) {
            existingScheme.setBufferTransitions(new HashMap<>(schemeBlm.getBufferTransitions()));
        }
        
        log.info("✏️ Test Responder: Updated connection scheme {}", schemeUid);
        return existingScheme;
    }
}