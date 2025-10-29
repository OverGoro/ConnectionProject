package com.connection.device;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.connection.device.config.SecurityUtils;
import com.connection.device.exception.DeviceAlreadyExistsException;
import com.connection.device.model.DeviceBLM;
import com.connection.device.repository.DeviceRepository;
import com.connection.device.validator.DeviceValidator;
import com.connection.service.auth.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service("DeviceServiceApiImpl")
@EnableAutoConfiguration(exclude = {
        JpaRepositoriesAutoConfiguration.class
})
@Transactional("atomicosTransactionManager")
public class DeviceServiceApiImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceValidator deviceValidator;
    private final AuthService authClient;

    @Override
    public DeviceBLM createDevice(DeviceBLM deviceBLM) {
        deviceValidator.validate(deviceBLM);
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        if (!clientUid.equals(deviceBLM.getClientUuid())) {
            throw new SecurityException("Client UID from token doesn't match device client UID");
        }

        if (deviceRepository.existsByClientAndName(clientUid, deviceBLM.getDeviceName())) {
            throw new DeviceAlreadyExistsException(
                    "Device with name '" + deviceBLM.getDeviceName() + "' already exists for this client");
        }

        deviceRepository.add(deviceBLM);

        log.info("Device created: {} for client: {}", deviceBLM.getUid(), clientUid);
        return deviceBLM;
    }

    @Override
    public DeviceBLM getDevice(UUID deviceUid) {
        DeviceBLM deviceBLM = deviceRepository.findByUid(deviceUid);
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        if (!clientUid.equals(deviceBLM.getClientUuid())) {
            throw new SecurityException("Device doesn't belong to the authenticated client");
        }

        return (deviceBLM);
    }

    @Override
    public List<DeviceBLM> getDevicesByClient(UUID clientUid) {
        UUID authClientUid = SecurityUtils.getCurrentClientUid();
        if (!clientUid.equals(authClientUid)) {
            throw new SecurityException("Client uid is not authorized client uid");
        }
        
        List<DeviceBLM> devicesBLM = deviceRepository.findByClientUuid(clientUid);
        return devicesBLM;
    }

    @Override
    public DeviceBLM updateDevice(DeviceBLM deviceBLM) {
        deviceValidator.validate(deviceBLM);
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        DeviceBLM existingDevice = deviceRepository.findByUid(deviceBLM.getUid());

        if (!clientUid.equals(existingDevice.getClientUuid())) {
            throw new SecurityException("Device doesn't belong to the authenticated client");
        }

        if (!clientUid.equals(deviceBLM.getClientUuid())) {
            throw new SecurityException("Client UID from token doesn't match device client UID");
        }

        deviceRepository.update(deviceBLM);

        log.info("Device updated: {} for client: {}", deviceBLM.getUid(), clientUid);
        return deviceBLM;
    }

    @Override
    public void deleteDevice(UUID deviceUid) {
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        
        DeviceBLM existingDevice = deviceRepository.findByUid(deviceUid);
        if (!clientUid.equals(existingDevice.getClientUuid())) {
            throw new SecurityException("Device doesn't belong to the authenticated client");
        }

        deviceRepository.delete(deviceUid);
        log.info("Device deleted: {} for client: {}", deviceUid, clientUid);
    }

    @Override
    public boolean deviceExists(UUID deviceUid) {
        // Просто проверяем существование устройства без проверки принадлежности
        return deviceRepository.exists(deviceUid);
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        try {
            var authHealth = authClient.getHealthStatus();

            return Map.of(
                    "status", "OK",
                    "service", "device-service",
                    "timestamp", System.currentTimeMillis(),
                    "auth-service", authHealth != null ? authHealth : "UNAVAILABLE");
        } catch (Exception e) {
            log.error("Kafka Client: ", e);
            return Map.of(
                    "status", "DEGRADED",
                    "service", "device-service",
                    "timestamp", System.currentTimeMillis(),
                    "auth-service", "UNAVAILABLE",
                    "error", e.getMessage());
        }
    }
}