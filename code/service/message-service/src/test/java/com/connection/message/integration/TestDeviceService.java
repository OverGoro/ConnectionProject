// TestDeviceServiceResponder.java
package com.connection.message.integration;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import com.connection.device.DeviceService;
import com.connection.device.model.DeviceBLM;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Primary
@ActiveProfiles("integrationtest")
public class TestDeviceService implements DeviceService {
    // Хранилище тестовых данных
    private final Map<UUID, DeviceBLM> testDevices = new ConcurrentHashMap<>();
    private final Map<UUID, List<DeviceBLM>> clientDevices = new ConcurrentHashMap<>();

    // Методы для управления тестовыми данными
    public void addTestDevice(UUID deviceUid, UUID clientUid, String deviceName) {
        DeviceBLM device = new DeviceBLM();
        device.setUid(deviceUid);
        device.setClientUuid(clientUid);
        device.setDeviceName(deviceName);
        device.setDeviceDescription("Test device for integration tests");

        testDevices.put(deviceUid, device);

        List<DeviceBLM> clientDeviceList = clientDevices.computeIfAbsent(
                clientUid, k -> new java.util.ArrayList<>());
        clientDeviceList.add(device);

        log.info(" Test Responder: Added test device {} for client {}", deviceUid, clientUid);
    }

    public void removeTestDevice(UUID deviceUid) {
        DeviceBLM device = testDevices.remove(deviceUid);
        if (device != null) {
            UUID clientUid = device.getClientUuid();
            List<DeviceBLM> clientDevicesList = clientDevices.get(clientUid);
            if (clientDevicesList != null) {
                clientDevicesList.removeIf(d -> d.getUid().equals(deviceUid));
            }
        }
    }

    public void clearTestData() {
        testDevices.clear();
        clientDevices.clear();
        log.info(" Test Responder: All device test data cleared");
    }

    public boolean hasDevice(UUID deviceUid) {
        return testDevices.containsKey(deviceUid);
    }

    public boolean deviceBelongsToClient(UUID deviceUid, UUID clientUid) {
        DeviceBLM device = testDevices.get(deviceUid);
        return device != null && device.getClientUuid().equals(clientUid);
    }

    @Override
    public DeviceBLM createDevice(DeviceBLM deviceBLM) {
        if (deviceBLM.getUid() == null) {
            deviceBLM.setUid(UUID.randomUUID());
        }
        
        testDevices.put(deviceBLM.getUid(), deviceBLM);
        
        List<DeviceBLM> clientDeviceList = clientDevices.computeIfAbsent(
                deviceBLM.getClientUuid(), k -> new java.util.ArrayList<>());
        clientDeviceList.add(deviceBLM);
        
        log.info(" Test Responder: Created device {} for client {}", 
                deviceBLM.getUid(), deviceBLM.getClientUuid());
        
        return deviceBLM;
    }

    @Override
    public void deleteDevice(UUID deviceUid) {
        DeviceBLM device = testDevices.remove(deviceUid);
        if (device != null) {
            UUID clientUid = device.getClientUuid();
            List<DeviceBLM> clientDevicesList = clientDevices.get(clientUid);
            if (clientDevicesList != null) {
                clientDevicesList.removeIf(d -> d.getUid().equals(deviceUid));
            }
            log.info(" Test Responder: Deleted device {}", deviceUid);
        } else {
            log.warn(" Test Responder: Attempted to delete non-existent device {}", deviceUid);
        }
    }

    @Override
    public boolean deviceExists(UUID deviceUid) {
        return testDevices.containsKey(deviceUid);
    }

    @Override
    public DeviceBLM getDevice(UUID deviceUid) {
        DeviceBLM device = testDevices.get(deviceUid);
        if (device == null) {
            log.debug(" Test Responder: Device {} not found", deviceUid);
        }
        return device;
    }

    @Override
    public List<DeviceBLM> getDevicesByClient(UUID clientUid) {
        List<DeviceBLM> devices = clientDevices.getOrDefault(clientUid, java.util.Collections.emptyList());
        log.debug(" Test Responder: Found {} devices for client {}", devices.size(), clientUid);
        return new java.util.ArrayList<>(devices); // Возвращаем копию для безопасности
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> healthStatus = new java.util.HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("totalDevices", testDevices.size());
        healthStatus.put("totalClients", clientDevices.size());
        healthStatus.put("timestamp", java.time.Instant.now());
        healthStatus.put("service", "TestDeviceService");
        
        log.debug(" Test Responder: Health check - {} devices, {} clients", 
                testDevices.size(), clientDevices.size());
        
        return healthStatus;
    }

    @Override
    public DeviceBLM updateDevice(DeviceBLM deviceBLM) {
        if (deviceBLM.getUid() == null) {
            throw new IllegalArgumentException("Device UID cannot be null for update");
        }
        
        DeviceBLM existingDevice = testDevices.get(deviceBLM.getUid());
        if (existingDevice == null) {
            log.warn(" Test Responder: Attempted to update non-existent device {}", deviceBLM.getUid());
            return null;
        }
        
        // Обновляем поля устройства
        if (deviceBLM.getDeviceName() != null) {
            existingDevice.setDeviceName(deviceBLM.getDeviceName());
        }
        if (deviceBLM.getDeviceDescription() != null) {
            existingDevice.setDeviceDescription(deviceBLM.getDeviceDescription());
        }
        if (deviceBLM.getClientUuid() != null && !deviceBLM.getClientUuid().equals(existingDevice.getClientUuid())) {
            // Если изменился клиент, перемещаем устройство в другой список
            UUID oldClientUid = existingDevice.getClientUuid();
            UUID newClientUid = deviceBLM.getClientUuid();
            
            List<DeviceBLM> oldClientDevices = clientDevices.get(oldClientUid);
            if (oldClientDevices != null) {
                oldClientDevices.removeIf(d -> d.getUid().equals(deviceBLM.getUid()));
            }
            
            List<DeviceBLM> newClientDevices = clientDevices.computeIfAbsent(
                    newClientUid, k -> new java.util.ArrayList<>());
            newClientDevices.add(existingDevice);
            
            existingDevice.setClientUuid(newClientUid);
        }
        
        log.info(" Test Responder: Updated device {}", deviceBLM.getUid());
        return existingDevice;
    }
}