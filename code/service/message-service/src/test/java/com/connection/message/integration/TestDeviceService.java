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
import com.connection.device.model.DeviceBlm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Primary
@ActiveProfiles("integrationtest")
public class TestDeviceService implements DeviceService {
    // Хранилище тестовых данных
    private final Map<UUID, DeviceBlm> testDevices = new ConcurrentHashMap<>();
    private final Map<UUID, List<DeviceBlm>> clientDevices = new ConcurrentHashMap<>();

    // Методы для управления тестовыми данными
    public void addTestDevice(UUID deviceUid, UUID clientUid, String deviceName) {
        DeviceBlm device = new DeviceBlm();
        device.setUid(deviceUid);
        device.setClientUuid(clientUid);
        device.setDeviceName(deviceName);
        device.setDeviceDescription("Test device for integration tests");

        testDevices.put(deviceUid, device);

        List<DeviceBlm> clientDeviceList = clientDevices.computeIfAbsent(
                clientUid, k -> new java.util.ArrayList<>());
        clientDeviceList.add(device);

        log.info(" Test Responder: Added test device {} for client {}", deviceUid, clientUid);
    }

    public void removeTestDevice(UUID deviceUid) {
        DeviceBlm device = testDevices.remove(deviceUid);
        if (device != null) {
            UUID clientUid = device.getClientUuid();
            List<DeviceBlm> clientDevicesList = clientDevices.get(clientUid);
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
        DeviceBlm device = testDevices.get(deviceUid);
        return device != null && device.getClientUuid().equals(clientUid);
    }

    @Override
    public DeviceBlm createDevice(DeviceBlm deviceBlm) {
        if (deviceBlm.getUid() == null) {
            deviceBlm.setUid(UUID.randomUUID());
        }
        
        testDevices.put(deviceBlm.getUid(), deviceBlm);
        
        List<DeviceBlm> clientDeviceList = clientDevices.computeIfAbsent(
                deviceBlm.getClientUuid(), k -> new java.util.ArrayList<>());
        clientDeviceList.add(deviceBlm);
        
        log.info(" Test Responder: Created device {} for client {}", 
                deviceBlm.getUid(), deviceBlm.getClientUuid());
        
        return deviceBlm;
    }

    @Override
    public void deleteDevice(UUID deviceUid) {
        DeviceBlm device = testDevices.remove(deviceUid);
        if (device != null) {
            UUID clientUid = device.getClientUuid();
            List<DeviceBlm> clientDevicesList = clientDevices.get(clientUid);
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
    public DeviceBlm getDevice(UUID deviceUid) {
        DeviceBlm device = testDevices.get(deviceUid);
        if (device == null) {
            log.debug(" Test Responder: Device {} not found", deviceUid);
        }
        return device;
    }

    @Override
    public List<DeviceBlm> getDevicesByClient(UUID clientUid) {
        List<DeviceBlm> devices = clientDevices.getOrDefault(clientUid, java.util.Collections.emptyList());
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
    public DeviceBlm updateDevice(DeviceBlm deviceBlm) {
        if (deviceBlm.getUid() == null) {
            throw new IllegalArgumentException("Device UID cannot be null for update");
        }
        
        DeviceBlm existingDevice = testDevices.get(deviceBlm.getUid());
        if (existingDevice == null) {
            log.warn(" Test Responder: Attempted to update non-existent device {}", deviceBlm.getUid());
            return null;
        }
        
        // Обновляем поля устройства
        if (deviceBlm.getDeviceName() != null) {
            existingDevice.setDeviceName(deviceBlm.getDeviceName());
        }
        if (deviceBlm.getDeviceDescription() != null) {
            existingDevice.setDeviceDescription(deviceBlm.getDeviceDescription());
        }
        if (deviceBlm.getClientUuid() != null && !deviceBlm.getClientUuid().equals(existingDevice.getClientUuid())) {
            // Если изменился клиент, перемещаем устройство в другой список
            UUID oldClientUid = existingDevice.getClientUuid();
            UUID newClientUid = deviceBlm.getClientUuid();
            
            List<DeviceBlm> oldClientDevices = clientDevices.get(oldClientUid);
            if (oldClientDevices != null) {
                oldClientDevices.removeIf(d -> d.getUid().equals(deviceBlm.getUid()));
            }
            
            List<DeviceBlm> newClientDevices = clientDevices.computeIfAbsent(
                    newClientUid, k -> new java.util.ArrayList<>());
            newClientDevices.add(existingDevice);
            
            existingDevice.setClientUuid(newClientUid);
        }
        
        log.info(" Test Responder: Updated device {}", deviceBlm.getUid());
        return existingDevice;
    }
}