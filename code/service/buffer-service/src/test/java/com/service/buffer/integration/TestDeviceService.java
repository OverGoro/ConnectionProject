package com.service.buffer.integration;

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
@Primary
@Component
@RequiredArgsConstructor
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
        
        // Также добавляем в список устройств клиента
        List<DeviceBlm> clientDeviceList = clientDevices.computeIfAbsent(
            clientUid, k -> new java.util.ArrayList<>()
        );
        clientDeviceList.add(device);
        
        log.info("📝 Test Responder: Added test device {} for client {}", deviceUid, clientUid);
    }
    
    public void addTestDevice(DeviceBlm device) {
        UUID deviceUid = (device.getUid());
        UUID clientUid = (device.getClientUuid());
        
        testDevices.put(deviceUid, device);
        
        List<DeviceBlm> clientDeviceList = clientDevices.computeIfAbsent(
            clientUid, k -> new java.util.ArrayList<>()
        );
        clientDeviceList.add(device);
    }
    
    public void removeTestDevice(UUID deviceUid) {
        DeviceBlm device = testDevices.remove(deviceUid);
        if (device != null) {
            UUID clientUid = (device.getClientUuid());
            List<DeviceBlm> clientDevicesList = clientDevices.get(clientUid);
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
    public DeviceBlm createDevice(DeviceBlm deviceBlm) {
        throw new UnsupportedOperationException("Unimplemented method 'createDevice'");
    }

    @Override
    public boolean deviceExists(UUID deviceUid) {
        boolean exists = testDevices.containsKey(deviceUid);
        log.debug("🔍 Test Responder: Check if device {} exists: {}", deviceUid, exists);
        return exists;
    }

    @Override
    public DeviceBlm getDevice(UUID deviceUid) {
        DeviceBlm deviceBlm = testDevices.get(deviceUid);
        if (deviceBlm != null) {
            log.debug(" Test Responder: Retrieved device {}", deviceUid);
            return deviceBlm;
        } else {
            log.warn(" Test Responder: Device {} not found", deviceUid);
            return null;
        }
    }

    @Override
    public List<DeviceBlm> getDevicesByClient(UUID clientUid) {
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
    public DeviceBlm updateDevice(DeviceBlm deviceBlm) {
        throw new UnsupportedOperationException("Unimplemented method 'createDevice'");
    }

    @Override
    public void deleteDevice(UUID deviceUid) {
        throw new UnsupportedOperationException("Unimplemented method 'deleteDevice'");
    }
}