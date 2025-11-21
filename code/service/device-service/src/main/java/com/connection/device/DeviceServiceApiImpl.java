package com.connection.device;

import com.connection.device.config.SecurityUtils;
import com.connection.device.exception.DeviceAlreadyExistsException;
import com.connection.device.model.DeviceBlm;
import com.connection.device.repository.DeviceRepository;
import com.connection.device.validator.DeviceValidator;
import com.connection.service.auth.AuthService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** . */
@Slf4j
@RequiredArgsConstructor
@Service("DeviceServiceApiImpl")
@EnableAutoConfiguration(exclude = {JpaRepositoriesAutoConfiguration.class})
@Transactional("atomicosTransactionManager")
public class DeviceServiceApiImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceValidator deviceValidator;
    private final AuthService authClient;

    @Override
    public DeviceBlm createDevice(DeviceBlm deviceBlm) {
        log.info("Starting device creation for device: {}",
                deviceBlm.getDeviceName());
        deviceValidator.validate(deviceBlm);

        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.debug("Authenticated client UID: {}", clientUid);

        if (!clientUid.equals(deviceBlm.getClientUuid())) {
            log.warn("Client UID mismatch. Token client: {}, Device client: {}",
                    clientUid, deviceBlm.getClientUuid());
            throw new SecurityException(
                    "Client UID from token doesn't match device client UID");
        }

        log.debug("Checking if device already exists for client: {}",
                clientUid);
        if (deviceRepository.existsByClientAndName(clientUid,
                deviceBlm.getDeviceName())) {
            log.warn("Device already exists with name: {} for client: {}",
                    deviceBlm.getDeviceName(), clientUid);
            throw new DeviceAlreadyExistsException(
                    "Device with name '" + deviceBlm.getDeviceName()
                            + "' already exists for this client");
        }

        log.debug("Adding device to repository");
        deviceRepository.add(deviceBlm);

        log.info("Device created successfully: {} for client: {}",
                deviceBlm.getUid(), clientUid);
        return deviceBlm;
    }

    @Override
    public DeviceBlm getDevice(UUID deviceUid) {
        log.info("Fetching device with UID: {}", deviceUid);

        DeviceBlm deviceBlm = deviceRepository.findByUid(deviceUid);
        if (deviceBlm == null) {
            log.warn("Device not found with UID: {}", deviceUid);
            return null;
        }

        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.debug("Authenticated client UID: {}", clientUid);

        if (!clientUid.equals(deviceBlm.getClientUuid())) {
            log.warn(
                    "Device access denied. Device client: {}, Authenticated client: {}",
                    deviceBlm.getClientUuid(), clientUid);
            throw new SecurityException(
                    "Device doesn't belong to the authenticated client");
        }

        log.debug("Device retrieved successfully: {}", deviceUid);
        return deviceBlm;
    }

    @Override
    public List<DeviceBlm> getDevicesByClient(UUID clientUid) {
        log.info("Fetching all devices for client: {}", clientUid);

        UUID authClientUid = SecurityUtils.getCurrentClientUid();
        log.debug("Authenticated client UID: {}", authClientUid);

        if (!clientUid.equals(authClientUid)) {
            log.warn(
                    "Unauthorized access attempt. Requested client: {}, Authenticated client: {}",
                    clientUid, authClientUid);
            throw new SecurityException(
                    "Client uid is not authorized client uid");
        }

        List<DeviceBlm> devicesBlm =
                deviceRepository.findByClientUuid(clientUid);
        log.info("Found {} devices for client: {}", devicesBlm.size(),
                clientUid);

        return devicesBlm;
    }

    @Override
    public DeviceBlm updateDevice(DeviceBlm deviceBlm) {
        log.info("Starting device update for device UID: {}",
                deviceBlm.getUid());

        deviceValidator.validate(deviceBlm);
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.debug("Authenticated client UID: {}", clientUid);

        DeviceBlm existingDevice =
                deviceRepository.findByUid(deviceBlm.getUid());
        if (existingDevice == null) {
            log.warn("Device not found for update: {}", deviceBlm.getUid());
            throw new IllegalArgumentException("Device not found");
        }

        if (!clientUid.equals(existingDevice.getClientUuid())) {
            log.warn(
                    "Update denied - Device client: {}, Authenticated client: {}",
                    existingDevice.getClientUuid(), clientUid);
            throw new SecurityException(
                    "Device doesn't belong to the authenticated client");
        }

        if (!clientUid.equals(deviceBlm.getClientUuid())) {
            log.warn(
                    "Update denied - client UID mismatch. Token client: {}, Device client: {}",
                    clientUid, deviceBlm.getClientUuid());
            throw new SecurityException(
                    "Client UID from token doesn't match device client UID");
        }

        log.debug("Updating device in repository");
        deviceRepository.update(deviceBlm);

        log.info("Device updated successfully: {} for client: {}",
                deviceBlm.getUid(), clientUid);
        return deviceBlm;
    }

    @Override
    public void deleteDevice(UUID deviceUid) {
        log.info("Starting device deletion for device UID: {}", deviceUid);

        UUID clientUid = SecurityUtils.getCurrentClientUid();
        log.debug("Authenticated client UID: {}", clientUid);

        DeviceBlm existingDevice = deviceRepository.findByUid(deviceUid);
        if (existingDevice == null) {
            log.warn("Device not found for deletion: {}", deviceUid);
            throw new IllegalArgumentException("Device not found");
        }

        if (!clientUid.equals(existingDevice.getClientUuid())) {
            log.warn(
                    "Delete denied -  mismatch. Device client: {}, Authenticated client: {}",
                    existingDevice.getClientUuid(), clientUid);
            throw new SecurityException(
                    "Device doesn't belong to the authenticated client");
        }

        log.debug("Deleting device from repository");
        deviceRepository.delete(deviceUid);

        log.info("Device deleted successfully: {} for client: {}", deviceUid,
                clientUid);
    }

    @Override
    public boolean deviceExists(UUID deviceUid) {
        log.debug("Checking device existence for UID: {}", deviceUid);
        boolean exists = deviceRepository.exists(deviceUid);
        log.debug("Device existence check result for {}: {}", deviceUid,
                exists);
        return exists;
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        log.debug("Starting health status check");
        try {
            var authHealth = authClient.getHealthStatus();
            log.debug("Auth service health status: {}", authHealth);

            Map<String, Object> healthStatus = Map.of("status", "OK", "service",
                    "device-service", "timestamp", System.currentTimeMillis(),
                    "auth-service",
                    authHealth != null ? authHealth : "UNAVAILABLE");

            log.info("Health status: OK");
            return healthStatus;
        } catch (Exception e) {
            log.error("Health check failed with error: {}", e.getMessage(), e);
            Map<String, Object> errorStatus = Map.of("status", "DEGRADED",
                    "service", "device-service", "timestamp",
                    System.currentTimeMillis(), "auth-service", "UNAVAILABLE",
                    "error", e.getMessage());

            log.warn("Health status: DEGRADED");
            return errorStatus;
        }
    }
}
