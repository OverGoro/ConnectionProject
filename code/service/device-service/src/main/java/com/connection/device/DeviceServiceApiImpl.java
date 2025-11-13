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
//@Transaction("atomicosTransactionManager")
public class DeviceServiceApiImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceValidator deviceValidator;
    private final AuthService authClient;

    @Override
    public DeviceBLM createDevice(DeviceBLM deviceBLM) {
        log.info("Starting device creation for device: {}", deviceBLM.getDeviceName());
        deviceValidator.validate(deviceBLM);
        
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.debug("Authenticated client UID: {}", clientUid);
        
        if (!clientUid.equals(deviceBLM.getClientUuid())) {
            log.warn("Client UID mismatch. Token client: {}, Device client: {}", clientUid, deviceBLM.getClientUuid());
            throw new SecurityException("Client UID from token doesn't match device client UID");
        }

        log.debug("Checking if device already exists for client: {}", clientUid);
        if (deviceRepository.existsByClientAndName(clientUid, deviceBLM.getDeviceName())) {
            log.warn("Device already exists with name: {} for client: {}", deviceBLM.getDeviceName(), clientUid);
            throw new DeviceAlreadyExistsException(
                    "Device with name '" + deviceBLM.getDeviceName() + "' already exists for this client");
        }

        log.debug("Adding device to repository");
        deviceRepository.add(deviceBLM);

        log.info("Device created successfully: {} for client: {}", deviceBLM.getUid(), clientUid);
        return deviceBLM;
    }

    @Override
    public DeviceBLM getDevice(UUID deviceUid) {
        log.info("Fetching device with UID: {}", deviceUid);
        
        DeviceBLM deviceBLM = deviceRepository.findByUid(deviceUid);
        if (deviceBLM == null) {
            log.warn("Device not found with UID: {}", deviceUid);
            return null;
        }
        
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.debug("Authenticated client UID: {}", clientUid);
        
        if (!clientUid.equals(deviceBLM.getClientUuid())) {
            log.warn("Device access denied. Device client: {}, Authenticated client: {}", 
                    deviceBLM.getClientUuid(), clientUid);
            throw new SecurityException("Device doesn't belong to the authenticated client");
        }

        log.debug("Device retrieved successfully: {}", deviceUid);
        return deviceBLM;
    }

    @Override
    public List<DeviceBLM> getDevicesByClient(UUID clientUid) {
        log.info("Fetching all devices for client: {}", clientUid);
        
        UUID authClientUid = SecurityUtils.getCurrentClientUid();
        log.debug("Authenticated client UID: {}", authClientUid);
        
        if (!clientUid.equals(authClientUid)) {
            log.warn("Unauthorized access attempt. Requested client: {}, Authenticated client: {}", 
                    clientUid, authClientUid);
            throw new SecurityException("Client uid is not authorized client uid");
        }
        
        List<DeviceBLM> devicesBLM = deviceRepository.findByClientUuid(clientUid);
        log.info("Found {} devices for client: {}", devicesBLM.size(), clientUid);
        
        return devicesBLM;
    }

    @Override
    public DeviceBLM updateDevice(DeviceBLM deviceBLM) {
        log.info("Starting device update for device UID: {}", deviceBLM.getUid());
        
        deviceValidator.validate(deviceBLM);
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.debug("Authenticated client UID: {}", clientUid);
        
        DeviceBLM existingDevice = deviceRepository.findByUid(deviceBLM.getUid());
        if (existingDevice == null) {
            log.warn("Device not found for update: {}", deviceBLM.getUid());
            throw new IllegalArgumentException("Device not found");
        }

        if (!clientUid.equals(existingDevice.getClientUuid())) {
            log.warn("Update denied - device ownership mismatch. Device client: {}, Authenticated client: {}", 
                    existingDevice.getClientUuid(), clientUid);
            throw new SecurityException("Device doesn't belong to the authenticated client");
        }

        if (!clientUid.equals(deviceBLM.getClientUuid())) {
            log.warn("Update denied - client UID mismatch. Token client: {}, Device client: {}", 
                    clientUid, deviceBLM.getClientUuid());
            throw new SecurityException("Client UID from token doesn't match device client UID");
        }

        log.debug("Updating device in repository");
        deviceRepository.update(deviceBLM);

        log.info("Device updated successfully: {} for client: {}", deviceBLM.getUid(), clientUid);
        return deviceBLM;
    }

    @Override
    public void deleteDevice(UUID deviceUid) {
        log.info("Starting device deletion for device UID: {}", deviceUid);
        
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.debug("Authenticated client UID: {}", clientUid);
        
        DeviceBLM existingDevice = deviceRepository.findByUid(deviceUid);
        if (existingDevice == null) {
            log.warn("Device not found for deletion: {}", deviceUid);
            throw new IllegalArgumentException("Device not found");
        }

        if (!clientUid.equals(existingDevice.getClientUuid())) {
            log.warn("Delete denied - device ownership mismatch. Device client: {}, Authenticated client: {}", 
                    existingDevice.getClientUuid(), clientUid);
            throw new SecurityException("Device doesn't belong to the authenticated client");
        }

        log.debug("Deleting device from repository");
        deviceRepository.delete(deviceUid);
        
        log.info("Device deleted successfully: {} for client: {}", deviceUid, clientUid);
    }

    @Override
    public boolean deviceExists(UUID deviceUid) {
        log.debug("Checking device existence for UID: {}", deviceUid);
        boolean exists = deviceRepository.exists(deviceUid);
        log.debug("Device existence check result for {}: {}", deviceUid, exists);
        return exists;
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        log.debug("Starting health status check");
        try {
            var authHealth = authClient.getHealthStatus();
            log.debug("Auth service health status: {}", authHealth);

            Map<String, Object> healthStatus = Map.of(
                    "status", "OK",
                    "service", "device-service",
                    "timestamp", System.currentTimeMillis(),
                    "auth-service", authHealth != null ? authHealth : "UNAVAILABLE");
            
            log.info("Health status: OK");
            return healthStatus;
        } catch (Exception e) {
            log.error("Health check failed with error: {}", e.getMessage(), e);
            Map<String, Object> errorStatus = Map.of(
                    "status", "DEGRADED",
                    "service", "device-service",
                    "timestamp", System.currentTimeMillis(),
                    "auth-service", "UNAVAILABLE",
                    "error", e.getMessage());
            
            log.warn("Health status: DEGRADED");
            return errorStatus;
        }
    }
}