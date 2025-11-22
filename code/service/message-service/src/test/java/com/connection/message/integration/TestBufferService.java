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

import com.connection.processing.buffer.model.BufferBlm;
import com.service.buffer.BufferService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ActiveProfiles("integrationtest")
public class TestBufferService implements BufferService {

    // Хранилище тестовых данных
    private final Map<UUID, BufferBlm> testBuffers = new ConcurrentHashMap<>();
    private final Map<UUID, List<BufferBlm>> deviceBuffers = new ConcurrentHashMap<>();
    private final Map<UUID, List<BufferBlm>> schemeBuffers = new ConcurrentHashMap<>();
    private final Map<UUID, List<BufferBlm>> clientBuffers = new ConcurrentHashMap<>();

    // Методы для управления тестовыми данными
    public void addTestBuffer(UUID bufferUid, UUID deviceUid, int maxMessages, int maxSize) {
        BufferBlm buffer = new BufferBlm(
                bufferUid,
                deviceUid,
                maxMessages,
                maxSize,
                "{}"
        );

        testBuffers.put(bufferUid, buffer);

        List<BufferBlm> deviceBufferList = deviceBuffers.computeIfAbsent(
                deviceUid, k -> new java.util.ArrayList<>());
        deviceBufferList.add(buffer);

        log.info("📝 Test Responder: Added test buffer {} for device {}", bufferUid, deviceUid);
    }

    public void linkBufferToScheme(UUID bufferUid, UUID schemeUid) {
        BufferBlm buffer = testBuffers.get(bufferUid);
        if (buffer != null) {
            List<BufferBlm> schemeBufferList = schemeBuffers.computeIfAbsent(
                    schemeUid, k -> new java.util.ArrayList<>());
            if (!schemeBufferList.contains(buffer)) {
                schemeBufferList.add(buffer);
            }
            log.info("🔗 Test Responder: Linked buffer {} to scheme {}", bufferUid, schemeUid);
        }
    }

    public void linkBufferToClient(UUID bufferUid, UUID clientUid) {
        BufferBlm buffer = testBuffers.get(bufferUid);
        if (buffer != null) {
            List<BufferBlm> clientBufferList = clientBuffers.computeIfAbsent(
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
        BufferBlm buffer = testBuffers.get(bufferUid);
        return buffer != null && buffer.getDeviceUid().equals(deviceUid);
    }

    @Override
    public boolean bufferExists(UUID bufferUid) {
        boolean exists = testBuffers.containsKey(bufferUid);
        log.debug("🔍 Test Responder: Buffer {} exists: {}", bufferUid, exists);
        return exists;
    }

    @Override
    public BufferBlm createBuffer(BufferBlm bufferBlm) {
        if (bufferBlm.getUid() == null) {
            bufferBlm.setUid(UUID.randomUUID());
        }
        
        testBuffers.put(bufferBlm.getUid(), bufferBlm);
        
        // Связываем с устройством
        List<BufferBlm> deviceBufferList = deviceBuffers.computeIfAbsent(
                bufferBlm.getDeviceUid(), k -> new ArrayList<>());
        deviceBufferList.add(bufferBlm);
        
        log.info("📝 Test Responder: Created buffer {} for device {}", 
                bufferBlm.getUid(), bufferBlm.getDeviceUid());
        
        return bufferBlm;
    }

    @Override
    public void deleteAllBuffersFromConnectionScheme(UUID connectionSchemeUid) {
        List<BufferBlm> schemeBuffersList = schemeBuffers.remove(connectionSchemeUid);
        if (schemeBuffersList != null) {
            log.info("🗑️ Test Responder: Removed all {} buffers from scheme {}", 
                    schemeBuffersList.size(), connectionSchemeUid);
        } else {
            log.debug("🔍 Test Responder: No buffers found for scheme {}", connectionSchemeUid);
        }
    }

    @Override
    public void deleteBuffer(UUID bufferUid) {
        BufferBlm removedBuffer = testBuffers.remove(bufferUid);
        if (removedBuffer != null) {
            // Удаляем из связей с устройствами
            List<BufferBlm> deviceBuffersList = deviceBuffers.get(removedBuffer.getDeviceUid());
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
        List<BufferBlm> schemeBuffersList = schemeBuffers.get(connectionSchemeUid);
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
    public BufferBlm getBufferByUid(UUID bufferUid) {
        BufferBlm buffer = testBuffers.get(bufferUid);
        if (buffer == null) {
            log.debug("🔍 Test Responder: Buffer {} not found", bufferUid);
        }
        return buffer;
    }

    @Override
    public List<BufferBlm> getBuffersByClient(UUID clientUid) {
        List<BufferBlm> buffers = clientBuffers.getOrDefault(clientUid, new ArrayList<>());
        log.debug("🔍 Test Responder: Found {} buffers for client {}", buffers.size(), clientUid);
        return new ArrayList<>(buffers); // Возвращаем копию для безопасности
    }

    @Override
    public List<BufferBlm> getBuffersByConnectionScheme(UUID connectionSchemeUid) {
        List<BufferBlm> buffers = schemeBuffers.getOrDefault(connectionSchemeUid, new ArrayList<>());
        log.debug("🔍 Test Responder: Found {} buffers for scheme {}", buffers.size(), connectionSchemeUid);
        return new ArrayList<>(buffers); // Возвращаем копию для безопасности
    }

    @Override
    public List<BufferBlm> getBuffersByDevice(UUID deviceUid) {
        List<BufferBlm> buffers = deviceBuffers.getOrDefault(deviceUid, new ArrayList<>());
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
    public BufferBlm updateBuffer(UUID bufferUid, BufferBlm bufferBlm) {
        BufferBlm existingBuffer = testBuffers.get(bufferUid);
        if (existingBuffer == null) {
            log.warn("⚠️ Test Responder: Attempted to update non-existent buffer {}", bufferUid);
            return null;
        }
        
        // Обновляем поля буфера
        if (bufferBlm.getMaxMessagesNumber() != null) {
            existingBuffer.setMaxMessagesNumber(bufferBlm.getMaxMessagesNumber());
        }
        if (bufferBlm.getMaxMessageSize() != null) {
            existingBuffer.setMaxMessageSize(bufferBlm.getMaxMessageSize());
        }
        if (bufferBlm.getMessagePrototype() != null) {
            existingBuffer.setMessagePrototype(bufferBlm.getMessagePrototype());
        }
        
        // Обрабатываем смену устройства
        if (bufferBlm.getDeviceUid() != null && !bufferBlm.getDeviceUid().equals(existingBuffer.getDeviceUid())) {
            UUID oldDeviceUid = existingBuffer.getDeviceUid();
            UUID newDeviceUid = bufferBlm.getDeviceUid();
            
            // Удаляем из старого устройства
            List<BufferBlm> oldDeviceBuffers = deviceBuffers.get(oldDeviceUid);
            if (oldDeviceBuffers != null) {
                oldDeviceBuffers.removeIf(b -> b.getUid().equals(bufferUid));
            }
            
            // Добавляем в новое устройство
            List<BufferBlm> newDeviceBuffers = deviceBuffers.computeIfAbsent(
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
    public List<BufferBlm> getAllBuffers() {
        return new ArrayList<>(testBuffers.values());
    }

    public int getTotalBufferCount() {
        return testBuffers.size();
    }

    public int getDeviceBufferCount(UUID deviceUid) {
        List<BufferBlm> buffers = deviceBuffers.get(deviceUid);
        return buffers != null ? buffers.size() : 0;
    }

    public int getSchemeBufferCount(UUID schemeUid) {
        List<BufferBlm> buffers = schemeBuffers.get(schemeUid);
        return buffers != null ? buffers.size() : 0;
    }
}