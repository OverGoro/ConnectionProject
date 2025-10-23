package com.service.buffer;

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
import com.connection.device.events.responses.GetDevicesByClientResponse;
import com.connection.device.model.DeviceBLM;
import com.connection.device.model.DeviceDTO;
import com.connection.processing.buffer.converter.BufferConverter;
import com.connection.processing.buffer.exception.BufferAlreadyExistsException;
import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferDTO;
import com.connection.processing.buffer.repository.BufferRepository;
import com.connection.processing.buffer.validator.BufferValidator;
import com.service.buffer.kafka.TypedAuthKafkaClient;
import com.service.buffer.kafka.TypedConnectionSchemeKafkaClient;
import com.service.buffer.kafka.TypedDeviceKafkaClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service("KafkaBufferService")
@EnableAutoConfiguration(exclude = {
        JpaRepositoriesAutoConfiguration.class
})
@Transactional("atomicosTransactionManager")
public class KafkaBufferServiceImpl implements BufferService {

    private final BufferRepository bufferRepository;
    private final BufferConverter bufferConverter;
    private final BufferValidator bufferValidator;

    private final DeviceConverter deviceConverter;

    private final TypedAuthKafkaClient authKafkaClient;
    private final TypedDeviceKafkaClient deviceKafkaClient;
    private final TypedConnectionSchemeKafkaClient connectionSchemeKafkaClient;

    @Override
    public BufferBLM createBuffer(BufferDTO bufferDTO) {
        bufferValidator.validate(bufferDTO);
        BufferBLM bufferBLM = bufferConverter.toBLM(bufferDTO);

        if (bufferRepository.exists(bufferBLM.getUid())) {
            throw new BufferAlreadyExistsException(
                    "Buffer with UID '" + bufferBLM.getUid() + "' already exists");
        }

        bufferRepository.add(bufferBLM);

        log.info("Buffer created via Kafka: {} for device: {}", bufferBLM.getUid(), bufferBLM.getDeviceUid());
        return bufferBLM;
    }

    @Override
    public BufferBLM getBufferByUid(UUID bufferUid) {
        BufferBLM bufferBLM = bufferRepository.findByUid(bufferUid);
        return (bufferBLM); // Без проверки безопасности
    }

    @Override
    public List<BufferBLM> getBuffersByClient(UUID clientUid) {
        try {
            GetDevicesByClientResponse devicesResponse = deviceKafkaClient
                    .getDevicesByClient(clientUid, "buffer-service")
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);

            if (!devicesResponse.isSuccess() || devicesResponse.getDeviceDTOs() == null) {
                log.warn("Failed to get devices for client: {}", clientUid);
                return List.of();
            }

            List<DeviceDTO> deviceDTOs = devicesResponse.getDeviceDTOs();
            List<DeviceBLM> deviceBLMs = deviceDTOs.stream()
                    .map(deviceConverter::toBLM)
                    .collect(Collectors.toList());

            return deviceBLMs.stream()
                    .flatMap(device -> getBuffersByDevice(device.getUid()).stream())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting buffers for client: {}", clientUid, e);
            throw new RuntimeException("Error getting client buffers", e);
        }
    }

    @Override
    public List<BufferBLM> getBuffersByDevice(UUID deviceUid) {
        List<BufferBLM> bufferBLMs = bufferRepository.findByDeviceUid(deviceUid);
        return bufferBLMs;
    }

    @Override
    public List<BufferBLM> getBuffersByConnectionScheme(UUID connectionSchemeUid) {
        List<BufferBLM> buffersBLM = bufferRepository.findByConnectionSchemeUid(connectionSchemeUid);
        return buffersBLM;
    }

    @Override
    public BufferBLM updateBuffer(UUID bufferUid, BufferDTO bufferDTO) {
        bufferValidator.validate(bufferDTO);
        bufferRepository.findByUid(bufferUid); // Проверяем существование

        BufferBLM bufferBLM = bufferConverter.toBLM(bufferDTO);

        if (!bufferUid.equals(bufferBLM.getUid())) {
            throw new IllegalArgumentException("Cannot change buffer UID");
        }

        bufferRepository.update(bufferBLM);

        log.info("Buffer updated via Kafka: {} for device: {}", bufferUid, bufferBLM.getDeviceUid());
        return bufferBLM;
    }

    @Override
    public void deleteBuffer(UUID bufferUid) {
        bufferRepository.delete(bufferUid);
        log.info("Buffer deleted via Kafka: {}", bufferUid);
    }

    @Override
    public boolean bufferExists(UUID bufferUid) {
        return bufferRepository.exists(bufferUid);
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        try {
            HealthCheckResponse authHealth = authKafkaClient.healthCheck("buffer-service")
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);

            com.connection.device.events.responses.HealthCheckResponse deviceHealth = deviceKafkaClient
                    .healthCheck("buffer-service")
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);

            com.connection.scheme.events.responses.HealthCheckResponse connectionSchemeHealth = connectionSchemeKafkaClient
                    .healthCheck("buffer-service")
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);

            return Map.of(
                    "status", "OK",
                    "service", "buffer-service",
                    "timestamp", System.currentTimeMillis(),
                    "auth-service", authHealth.isSuccess() ? "AVAILABLE" : "UNAVAILABLE",
                    "device-service", deviceHealth.isSuccess() ? "AVAILABLE" : "UNAVAILABLE",
                    "connection-scheme-service", connectionSchemeHealth.isSuccess() ? "AVAILABLE" : "UNAVAILABLE",
                    "database", "CONNECTED");

        } catch (Exception e) {
            log.error("Health check error: ", e);
            return Map.of(
                    "status", "DEGRADED",
                    "service", "buffer-service",
                    "timestamp", System.currentTimeMillis(),
                    "auth-service", "UNAVAILABLE",
                    "device-service", "UNAVAILABLE",
                    "connection-scheme-service", "UNAVAILABLE",
                    "database", "CONNECTED",
                    "error", e.getMessage());
        }
    }

    @Override
    public void deleteBufferFromConnectionScheme(UUID connectionSchemeUid, UUID bufferUid) {
        bufferRepository.removeBufferFromConnectionScheme(bufferUid, connectionSchemeUid);
        log.info("Buffer {} removed from connection scheme {} via Kafka", bufferUid, connectionSchemeUid);
    }

    @Override
    public void deleteAllBuffersFromConnectionScheme(UUID connectionSchemeUid) {
        List<BufferBLM> buffers = getBuffersByConnectionScheme(connectionSchemeUid);
        for (BufferBLM b : buffers) {
            bufferRepository.removeBufferFromConnectionScheme(b.getUid(), connectionSchemeUid);
        }
        log.info("All buffers removed from connection scheme {} via Kafka", connectionSchemeUid);
    }
}