// TestBufferServiceResponder.java
package com.connection.message.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import com.connection.processing.buffer.model.BufferBLM;
import com.service.buffer.BufferService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ActiveProfiles("integrationtest")
public class TestBufferService implements BufferService {

    // Хранилище тестовых данных
    private final Map<UUID, BufferBLM> testBuffers = new ConcurrentHashMap<>();
    private final Map<UUID, List<BufferBLM>> deviceBuffers = new ConcurrentHashMap<>();
    private final Map<UUID, List<BufferBLM>> schemeBuffers = new ConcurrentHashMap<>();
    private final Map<UUID, List<BufferBLM>> clientBuffers = new ConcurrentHashMap<>();

    // Методы для управления тестовыми данными
    public void addTestBuffer(UUID bufferUid, UUID deviceUid, int maxMessages, int maxSize) {
        BufferBLM buffer = new BufferBLM(
                bufferUid,
                deviceUid,
                maxMessages,
                maxSize,
                "{}"
        );

        testBuffers.put(bufferUid, buffer);

        List<BufferBLM> deviceBufferList = deviceBuffers.computeIfAbsent(
                deviceUid, k -> new java.util.ArrayList<>());
        deviceBufferList.add(buffer);

        log.info("📝 Test Responder: Added test buffer {} for device {}", bufferUid, deviceUid);
    }

    public void linkBufferToScheme(UUID bufferUid, UUID schemeUid) {
        BufferBLM buffer = testBuffers.get(bufferUid);
        if (buffer != null) {
            List<BufferBLM> schemeBufferList = schemeBuffers.computeIfAbsent(
                    schemeUid, k -> new java.util.ArrayList<>());
            if (!schemeBufferList.contains(buffer)) {
                schemeBufferList.add(buffer);
            }
            log.info("🔗 Test Responder: Linked buffer {} to scheme {}", bufferUid, schemeUid);
        }
    }

    public void linkBufferToClient(UUID bufferUid, UUID clientUid) {
        BufferBLM buffer = testBuffers.get(bufferUid);
        if (buffer != null) {
            List<BufferBLM> clientBufferList = clientBuffers.computeIfAbsent(
                    clientUid, k -> new java.util.ArrayList<>());
            if (!clientBufferList.contains(buffer)) {
                clientBufferList.add(buffer);
            }
            log.info("🔗 Test Responder: Linked buffer {} to client {}", bufferUid, clientUid);
        }
    }

    public void clearTestData() {
        testBuffers.clear();
        deviceBuffers.clear();
        schemeBuffers.clear();
        clientBuffers.clear();
        log.info("🧹 Test Responder: All buffer test data cleared");
    }

    public boolean hasBuffer(UUID bufferUid) {
        return testBuffers.containsKey(bufferUid);
    }

    public boolean bufferBelongsToDevice(UUID bufferUid, UUID deviceUid) {
        BufferBLM buffer = testBuffers.get(bufferUid);
        return buffer != null && buffer.getDeviceUid().equals(deviceUid);
    }

    @Override
    public boolean bufferExists(UUID bufferUid) {
        boolean exists = testBuffers.containsKey(bufferUid);
        log.debug("🔍 Test Responder: Buffer {} exists: {}", bufferUid, exists);
        return exists;
    }

    @Override
    public BufferBLM createBuffer(BufferBLM bufferBLM) {
        if (bufferBLM.getUid() == null) {
            bufferBLM.setUid(UUID.randomUUID());
        }
        
        testBuffers.put(bufferBLM.getUid(), bufferBLM);
        
        // Связываем с устройством
        List<BufferBLM> deviceBufferList = deviceBuffers.computeIfAbsent(
                bufferBLM.getDeviceUid(), k -> new ArrayList<>());
        deviceBufferList.add(bufferBLM);
        
        log.info("📝 Test Responder: Created buffer {} for device {}", 
                bufferBLM.getUid(), bufferBLM.getDeviceUid());
        
        return bufferBLM;
    }

    @Override
    public void deleteAllBuffersFromConnectionScheme(UUID connectionSchemeUid) {
        List<BufferBLM> schemeBuffersList = schemeBuffers.remove(connectionSchemeUid);
        if (schemeBuffersList != null) {
            log.info("🗑️ Test Responder: Removed all {} buffers from scheme {}", 
                    schemeBuffersList.size(), connectionSchemeUid);
        } else {
            log.debug("🔍 Test Responder: No buffers found for scheme {}", connectionSchemeUid);
        }
    }

    @Override
    public void deleteBuffer(UUID bufferUid) {
        BufferBLM removedBuffer = testBuffers.remove(bufferUid);
        if (removedBuffer != null) {
            // Удаляем из связей с устройствами
            List<BufferBLM> deviceBuffersList = deviceBuffers.get(removedBuffer.getDeviceUid());
            if (deviceBuffersList != null) {
                deviceBuffersList.removeIf(b -> b.getUid().equals(bufferUid));
            }
            
            // Удаляем из связей со схемами
            schemeBuffers.values().forEach(buffers -> buffers.removeIf(b -> b.getUid().equals(bufferUid)));
            
            // Удаляем из связей с клиентами
            clientBuffers.values().forEach(buffers -> buffers.removeIf(b -> b.getUid().equals(bufferUid)));
            
            log.info("🗑️ Test Responder: Deleted buffer {}", bufferUid);
        } else {
            log.warn("⚠️ Test Responder: Attempted to delete non-existent buffer {}", bufferUid);
        }
    }

    @Override
    public void deleteBufferFromConnectionScheme(UUID connectionSchemeUid, UUID bufferUid) {
        List<BufferBLM> schemeBuffersList = schemeBuffers.get(connectionSchemeUid);
        if (schemeBuffersList != null) {
            boolean removed = schemeBuffersList.removeIf(b -> b.getUid().equals(bufferUid));
            if (removed) {
                log.info("🗑️ Test Responder: Removed buffer {} from scheme {}", bufferUid, connectionSchemeUid);
            } else {
                log.debug("🔍 Test Responder: Buffer {} not found in scheme {}", bufferUid, connectionSchemeUid);
            }
        }
    }

    @Override
    public BufferBLM getBufferByUid(UUID bufferUid) {
        BufferBLM buffer = testBuffers.get(bufferUid);
        if (buffer == null) {
            log.debug("🔍 Test Responder: Buffer {} not found", bufferUid);
        }
        return buffer;
    }

    @Override
    public List<BufferBLM> getBuffersByClient(UUID clientUid) {
        List<BufferBLM> buffers = clientBuffers.getOrDefault(clientUid, new ArrayList<>());
        log.debug("🔍 Test Responder: Found {} buffers for client {}", buffers.size(), clientUid);
        return new ArrayList<>(buffers); // Возвращаем копию для безопасности
    }

    @Override
    public List<BufferBLM> getBuffersByConnectionScheme(UUID connectionSchemeUid) {
        List<BufferBLM> buffers = schemeBuffers.getOrDefault(connectionSchemeUid, new ArrayList<>());
        log.debug("🔍 Test Responder: Found {} buffers for scheme {}", buffers.size(), connectionSchemeUid);
        return new ArrayList<>(buffers); // Возвращаем копию для безопасности
    }

    @Override
    public List<BufferBLM> getBuffersByDevice(UUID deviceUid) {
        List<BufferBLM> buffers = deviceBuffers.getOrDefault(deviceUid, new ArrayList<>());
        log.debug("🔍 Test Responder: Found {} buffers for device {}", buffers.size(), deviceUid);
        return new ArrayList<>(buffers); // Возвращаем копию для безопасности
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("totalBuffers", testBuffers.size());
        healthStatus.put("totalDevices", deviceBuffers.size());
        healthStatus.put("totalSchemes", schemeBuffers.size());
        healthStatus.put("totalClients", clientBuffers.size());
        healthStatus.put("timestamp", java.time.Instant.now());
        healthStatus.put("service", "TestBufferService");
        
        log.debug(" Test Responder: Health check - {} buffers, {} devices, {} schemes, {} clients", 
                testBuffers.size(), deviceBuffers.size(), schemeBuffers.size(), clientBuffers.size());
        
        return healthStatus;
    }

    @Override
    public BufferBLM updateBuffer(UUID bufferUid, BufferBLM bufferBLM) {
        BufferBLM existingBuffer = testBuffers.get(bufferUid);
        if (existingBuffer == null) {
            log.warn("⚠️ Test Responder: Attempted to update non-existent buffer {}", bufferUid);
            return null;
        }
        
        // Обновляем поля буфера
        if (bufferBLM.getMaxMessagesNumber() != null) {
            existingBuffer.setMaxMessagesNumber(bufferBLM.getMaxMessagesNumber());
        }
        if (bufferBLM.getMaxMessageSize() != null) {
            existingBuffer.setMaxMessageSize(bufferBLM.getMaxMessageSize());
        }
        if (bufferBLM.getMessagePrototype() != null) {
            existingBuffer.setMessagePrototype(bufferBLM.getMessagePrototype());
        }
        
        // Обрабатываем смену устройства
        if (bufferBLM.getDeviceUid() != null && !bufferBLM.getDeviceUid().equals(existingBuffer.getDeviceUid())) {
            UUID oldDeviceUid = existingBuffer.getDeviceUid();
            UUID newDeviceUid = bufferBLM.getDeviceUid();
            
            // Удаляем из старого устройства
            List<BufferBLM> oldDeviceBuffers = deviceBuffers.get(oldDeviceUid);
            if (oldDeviceBuffers != null) {
                oldDeviceBuffers.removeIf(b -> b.getUid().equals(bufferUid));
            }
            
            // Добавляем в новое устройство
            List<BufferBLM> newDeviceBuffers = deviceBuffers.computeIfAbsent(
                    newDeviceUid, k -> new ArrayList<>());
            if (!newDeviceBuffers.contains(existingBuffer)) {
                newDeviceBuffers.add(existingBuffer);
            }
            
            existingBuffer.setDeviceUid(newDeviceUid);
        }
        
        log.info(" Test Responder: Updated buffer {}", bufferUid);
        return existingBuffer;
    }

    // Вспомогательные методы для тестов
    public List<BufferBLM> getAllBuffers() {
        return new ArrayList<>(testBuffers.values());
    }

    public int getTotalBufferCount() {
        return testBuffers.size();
    }

    public int getDeviceBufferCount(UUID deviceUid) {
        List<BufferBLM> buffers = deviceBuffers.get(deviceUid);
        return buffers != null ? buffers.size() : 0;
    }

    public int getSchemeBufferCount(UUID schemeUid) {
        List<BufferBLM> buffers = schemeBuffers.get(schemeUid);
        return buffers != null ? buffers.size() : 0;
    }
}