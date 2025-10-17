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
import com.connection.device.kafka.TypedAuthKafkaClient;
import com.connection.device.model.DeviceBLM;
import com.connection.device.model.DeviceDALM;
import com.connection.device.repository.DeviceRepository;
import com.connection.device.validator.DeviceValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service("DeviceServiceKafkaImpl")
@EnableAutoConfiguration(exclude = {
        JpaRepositoriesAutoConfiguration.class
})
@Transactional("atomicosTransactionManager")
public class DeviceServiceKafkaImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceConverter deviceConverter;
    private final DeviceValidator deviceValidator;
    private final TypedAuthKafkaClient authKafkaClient;

    @Override
    public DeviceBLM createDevice(DeviceBLM deviceBLM) {
        deviceValidator.validate(deviceBLM);
        DeviceDALM deviceDALM = deviceConverter.toDALM(deviceBLM);
        deviceRepository.add(deviceDALM);

        log.info("Kafka: Device created: {}", deviceBLM.getUid());
        return deviceBLM;
    }

    @Override
    public DeviceBLM getDevice(UUID deviceUid) {
        DeviceDALM deviceDALM = deviceRepository.findByUid(deviceUid);
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
    public DeviceBLM updateDevice(DeviceBLM deviceBLM) {
        deviceValidator.validate(deviceBLM);
        DeviceDALM existingDevice = deviceRepository.findByUid(deviceBLM.getUid());

        DeviceDALM deviceDALM = deviceConverter.toDALM(deviceBLM);
        deviceRepository.update(deviceDALM);

        log.info("Kafka: Device updated: {}", deviceBLM.getUid());
        return deviceBLM;
    }

    @Override
    public void deleteDevice(UUID deviceUid) {
        deviceRepository.delete(deviceUid);
        log.info("Kafka: Device deleted: {}", deviceUid);
    }

    @Override
    public boolean deviceExists(UUID deviceUid) {
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