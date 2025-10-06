// BufferServiceImpl.java
package com.service.buffer;

import java.util.ArrayList;
import java.util.LinkedList;
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
import com.connection.processing.buffer.model.BufferDALM;
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
@Service
@EnableAutoConfiguration(exclude = {
        JpaRepositoriesAutoConfiguration.class
})
@Transactional("atomicosTransactionManager")
public class BufferServiceImpl implements BufferService {

    private final BufferRepository bufferRepository;
    private final BufferConverter bufferConverter;
    private final BufferValidator bufferValidator;

    private final DeviceConverter deviceConverter;

    private final TypedAuthKafkaClient authKafkaClient;
    private final TypedDeviceKafkaClient deviceKafkaClient;
    private final TypedConnectionSchemeKafkaClient connectionSchemeKafkaClient;

    @Override
    public BufferBLM createBuffer(UUID clientUid, BufferDTO bufferDTO) {
        bufferValidator.validate(bufferDTO);

        BufferBLM bufferBLM = bufferConverter.toBLM(bufferDTO);

        if (!deviceKafkaClient.deviceExistsAndBelongsToClient(
                bufferBLM.getDeviceUid(), clientUid)) {
            throw new SecurityException("Device doesn't exist or doesn't belong to the authenticated client");
        }

        // Проверяем, что буфер с таким UID не существует
        if (bufferRepository.exists(bufferBLM.getUid())) {
            throw new BufferAlreadyExistsException(
                    "Buffer with UID '" + bufferBLM.getUid() + "' already exists");
        }

        BufferDALM bufferDALM = bufferConverter.toDALM(bufferBLM);
        bufferRepository.add(bufferDALM);

        log.info("Buffer created: {} for device: {}", bufferBLM.getUid(), bufferBLM.getDeviceUid());
        return bufferBLM;
    }

    @Override
    public BufferBLM getBufferByUid(UUID clientUid, UUID bufferUid) {
        BufferDALM bufferDALM = bufferRepository.findByUid(bufferUid);

        if (!deviceKafkaClient.deviceExistsAndBelongsToClient(
                bufferDALM.getDeviceUid(), clientUid)) {
            throw new SecurityException("Buffer doesn't exist or doesn't belong to the authenticated client");
        }

        return bufferConverter.toBLM(bufferDALM);
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

            List<BufferBLM> bufferBlms = new ArrayList<>();
            for (DeviceBLM device : deviceBLMs) {
                try {
                    List<BufferBLM> deviceBuffers = getBuffersByDevice(clientUid, device.getUid());
                    bufferBlms.addAll(deviceBuffers);
                } catch (Exception e) {
                    log.error("Error getting buffers for device: {}", device.getUid(), e);
                    // Продолжаем обработку других устройств
                }
            }
            return bufferBlms;

        } catch (java.util.concurrent.TimeoutException e) {
            log.error("Timeout getting devices for client: {}", clientUid, e);
            throw new RuntimeException("Device service timeout", e);
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Error getting devices for client: {}", clientUid, e);
            throw new RuntimeException("Device service error", e);
        } catch (Exception e) {
            log.error("Unexpected error getting buffers for client: {}", clientUid, e);
            throw new RuntimeException("Unexpected error", e);
        }
    }

    @Override
    public List<BufferBLM> getBuffersByDevice(UUID clientUid, UUID deviceUid) {
        if (!deviceKafkaClient.deviceExistsAndBelongsToClient(deviceUid, clientUid)) {
            throw new SecurityException("Device doesn't belong to the authenticated client");
        }

        List<BufferDALM> bufferDALMs = bufferRepository.findByDeviceUid(deviceUid);
        return bufferDALMs.stream().map(bufferConverter::toBLM).collect(Collectors.toList());
    }

    @Override
    public List<BufferBLM> getBuffersByConnectionScheme(UUID clientUid, UUID connectionSchemeUid) {
        if (!connectionSchemeKafkaClient.connectionSchemeExistsAndBelongsToClient(
                connectionSchemeUid, clientUid)) {
            throw new SecurityException("Connection scheme doesn't belong to the authenticated client");
        }

        List<BufferDALM> buffersDALM = bufferRepository.findByConnectionSchemeUid(connectionSchemeUid);

        return buffersDALM.stream()
                .map(bufferConverter::toBLM)
                .collect(Collectors.toList());
    }

    @Override
    public BufferBLM updateBuffer(UUID clientUid, UUID bufferUid, BufferDTO bufferDTO) {
        bufferValidator.validate(bufferDTO);

        BufferDALM existingBuffer = bufferRepository.findByUid(bufferUid);

        if (!deviceKafkaClient.deviceExistsAndBelongsToClient(
                existingBuffer.getDeviceUid(), clientUid)) {
            throw new SecurityException("Buffer doesn't exist or doesn't belong to the authenticated client");
        }

        BufferBLM bufferBLM = bufferConverter.toBLM(bufferDTO);

        // Проверяем принадлежность новой схемы подключения (если изменилось)
        if (!deviceKafkaClient.deviceExistsAndBelongsToClient(
                bufferBLM.getDeviceUid(), clientUid)) {
            throw new SecurityException("Buffer doesn't exist or doesn't belong to the authenticated client");
        }

        // Проверяем, что UID не изменяется
        if (!bufferUid.equals(bufferBLM.getUid())) {
            throw new IllegalArgumentException("Cannot change buffer UID");
        }

        BufferDALM bufferDALM = bufferConverter.toDALM(bufferBLM);
        bufferRepository.update(bufferDALM);

        log.info("Buffer updated: {} for device: {}", bufferUid, bufferBLM.getDeviceUid());
        return bufferBLM;
    }

    @Override
    public void deleteBuffer(UUID clientUid, UUID bufferUid) {
        BufferDALM existingBuffer = bufferRepository.findByUid(bufferUid);

        if (!deviceKafkaClient.deviceExistsAndBelongsToClient(
                existingBuffer.getDeviceUid(), clientUid)) {
            throw new SecurityException("Buffer doesn't exist or doesn't belong to the authenticated client");
        }

        bufferRepository.delete(bufferUid);
        log.info("Buffer deleted: {} for client: {}, device: {}", bufferUid, clientUid, existingBuffer.getDeviceUid());
    }

    @Override
    public boolean bufferExists(UUID clientUid, UUID bufferUid) {
        if (!bufferRepository.exists(bufferUid)) {
            return false;
        }

        try {
            BufferDALM buffer = bufferRepository.findByUid(bufferUid);
            return deviceKafkaClient.deviceExistsAndBelongsToClient(
                    buffer.getDeviceUid(), clientUid);
        } catch (Exception e) {
            return false;
        }
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
    public void deleteBufferFromConnectionScheme(UUID clientUid, UUID connectionSchemeUid, UUID bufferUid) {
        // Проверяем принадлежность схемы подключения клиенту
        if (!connectionSchemeKafkaClient.connectionSchemeExistsAndBelongsToClient(
                connectionSchemeUid, clientUid)) {
            throw new SecurityException("Connection scheme doesn't belong to the authenticated client");
        }

        List<BufferBLM> buffers = getBuffersByConnectionScheme(clientUid, connectionSchemeUid);

        for (BufferBLM b : buffers) {
            bufferRepository.removeBufferFromConnectionScheme(b.getUid(), connectionSchemeUid);
        }

        log.info("Deleted buffers for connection scheme: {}", connectionSchemeUid);
    }

    @Override
    public void deleteAllBuffersFromConnectionScheme(UUID clientUid, UUID connectionSchemeUid) {
        // Проверяем принадлежность схемы подключения клиенту
        if (!connectionSchemeKafkaClient.connectionSchemeExistsAndBelongsToClient(
                connectionSchemeUid, clientUid)) {
            throw new SecurityException("Connection scheme doesn't belong to the authenticated client");
        }

        List<BufferBLM> buffers = getBuffersByConnectionScheme(clientUid, connectionSchemeUid);

        for (BufferBLM b : buffers) {
            bufferRepository.removeBufferFromConnectionScheme(b.getUid(), connectionSchemeUid);
        }

        log.info("Deleted buffers for connection scheme: {}", connectionSchemeUid);
    }
}