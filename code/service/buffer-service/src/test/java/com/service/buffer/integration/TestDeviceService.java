package com.service.buffer.integration;

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
@Primary
@Component
@RequiredArgsConstructor
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
        
        // Также добавляем в список устройств клиента
        List<DeviceBLM> clientDeviceList = clientDevices.computeIfAbsent(
            clientUid, k -> new java.util.ArrayList<>()
        );
        clientDeviceList.add(device);
        
        log.info("📝 Test Responder: Added test device {} for client {}", deviceUid, clientUid);
    }
    
    public void addTestDevice(DeviceBLM device) {
        UUID deviceUid = (device.getUid());
        UUID clientUid = (device.getClientUuid());
        
        testDevices.put(deviceUid, device);
        
        List<DeviceBLM> clientDeviceList = clientDevices.computeIfAbsent(
            clientUid, k -> new java.util.ArrayList<>()
        );
        clientDeviceList.add(device);
    }
    
    public void removeTestDevice(UUID deviceUid) {
        DeviceBLM device = testDevices.remove(deviceUid);
        if (device != null) {
            UUID clientUid = (device.getClientUuid());
            List<DeviceBLM> clientDevicesList = clientDevices.get(clientUid);
            if (clientDevicesList != null) {
                clientDevicesList.removeIf(d -> d.getUid().equals(deviceUid));
            }
        }
    }
    
    public void clearTestData() {
        testDevices.clear();
        clientDevices.clear();
        log.info("🧹 Test Responder: All test data cleared");
    }
    
    public boolean hasDevice(UUID deviceUid) {
        return testDevices.containsKey(deviceUid);
    }

    @Override
    public DeviceBLM createDevice(DeviceBLM deviceBLM) {
        throw new UnsupportedOperationException("Unimplemented method 'createDevice'");
    }

    @Override
    public boolean deviceExists(UUID deviceUid) {
        boolean exists = testDevices.containsKey(deviceUid);
        log.debug("🔍 Test Responder: Check if device {} exists: {}", deviceUid, exists);
        return exists;
    }

    @Override
    public DeviceBLM getDevice(UUID deviceUid) {
        DeviceBLM deviceBLM = testDevices.get(deviceUid);
        if (deviceBLM != null) {
            log.debug(" Test Responder: Retrieved device {}", deviceUid);
            return deviceBLM;
        } else {
            log.warn(" Test Responder: Device {} not found", deviceUid);
            return null;
        }
    }

    @Override
    public List<DeviceBLM> getDevicesByClient(UUID clientUid) {
        throw new UnsupportedOperationException("Unimplemented method 'createDevice'");
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> healthStatus = new java.util.HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("testDevicesCount", testDevices.size());
        healthStatus.put("clientsWithDevicesCount", clientDevices.size());
        healthStatus.put("timestamp", java.time.LocalDateTime.now().toString());
        
        log.debug(" Test Responder: Health status checked");
        return healthStatus;
    }

    @Override
    public DeviceBLM updateDevice(DeviceBLM deviceBLM) {
        throw new UnsupportedOperationException("Unimplemented method 'createDevice'");
    }

    @Override
    public void deleteDevice(UUID deviceUid) {
        throw new UnsupportedOperationException("Unimplemented method 'deleteDevice'");
    }
}