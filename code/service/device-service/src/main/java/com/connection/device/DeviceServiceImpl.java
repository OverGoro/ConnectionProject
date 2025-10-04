package com.connection.device;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.connection.auth.events.responses.HealthCheckResponse;
import com.connection.device.converter.DeviceConverter;
import com.connection.device.exception.DeviceAlreadyExistsException;
import com.connection.device.kafka.TypedAuthKafkaClient;
import com.connection.device.model.DeviceBLM;
import com.connection.device.model.DeviceDALM;
import com.connection.device.repository.DeviceRepository;
import com.connection.device.validator.DeviceValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@EnableAutoConfiguration(exclude = {
        JpaRepositoriesAutoConfiguration.class
})
@Transactional("atomicosTransactionManager")
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceConverter deviceConverter;
    private final DeviceValidator deviceValidator;
    private final TypedAuthKafkaClient authKafkaClient;

    @Override
    public DeviceBLM createDevice(UUID clientUid, DeviceBLM deviceBLM) {
        deviceValidator.validate(deviceBLM);

        if (!clientUid.equals(deviceBLM.getClientUuid())) {
            throw new SecurityException("Client UID from token doesn't match device client UID");
        }

        if (deviceRepository.existsByClientAndName(clientUid, deviceBLM.getDeviceName())) {
            throw new DeviceAlreadyExistsException(
                    "Device with name '" + deviceBLM.getDeviceName() + "' already exists for this client");
        }

        DeviceDALM deviceDALM = deviceConverter.toDALM(deviceBLM);
        deviceRepository.add(deviceDALM);

        log.info("Device created: {} for client: {}", deviceBLM.getUid(), clientUid);
        return deviceBLM;
    }

    @Override
    public DeviceBLM getDevice(UUID clientUid, UUID deviceUid) {
        DeviceDALM deviceDALM = deviceRepository.findByUid(deviceUid);

        if (!clientUid.equals(deviceDALM.getClientUuid())) {
            throw new SecurityException("Device doesn't belong to the authenticated client");
        }

        return deviceConverter.toBLM(deviceDALM);
    }

    @Override
    public List<DeviceBLM> getDevicesByClient(UUID clientUid) {

        List<DeviceDALM> devicesDALM = deviceRepository.findByClientUuid(clientUid);
        return devicesDALM.stream()
                .map(deviceConverter::toBLM)
                .collect(Collectors.toList());
    }

    @Override
    public DeviceBLM updateDevice(UUID clientUid, DeviceBLM deviceBLM) {
        deviceValidator.validate(deviceBLM);

        DeviceDALM existingDevice = deviceRepository.findByUid(deviceBLM.getUid());
        if (!clientUid.equals(existingDevice.getClientUuid())) {
            throw new SecurityException("Device doesn't belong to the authenticated client");
        }

        if (!clientUid.equals(deviceBLM.getClientUuid())) {
            throw new SecurityException("Client UID from token doesn't match device client UID");
        }

        DeviceDALM deviceDALM = deviceConverter.toDALM(deviceBLM);
        deviceRepository.update(deviceDALM);

        log.info("Device updated: {} for client: {}", deviceBLM.getUid(), clientUid);
        return deviceBLM;
    }

    @Override
    public void deleteDevice(UUID clientUid, UUID deviceUid) {

        DeviceDALM existingDevice = deviceRepository.findByUid(deviceUid);
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
            HealthCheckResponse authHealth = authKafkaClient.healthCheck("device-service")
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);

            return Map.of(
                    "status", "OK",
                    "service", "device-service",
                    "timestamp", System.currentTimeMillis(),
                    "auth-service", authHealth.isSuccess() ? authHealth.getHealthStatus() : "UNAVAILABLE");
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