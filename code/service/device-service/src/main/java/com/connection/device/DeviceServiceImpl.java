package com.connection.device;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.connection.device.client.AuthServiceClient;
import com.connection.device.converter.DeviceConverter;
import com.connection.device.exception.DeviceAlreadyExistsException;
import com.connection.device.model.DeviceBLM;
import com.connection.device.model.DeviceDALM;
import com.connection.device.model.DeviceDTO;
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
    private final AuthServiceClient authServiceClient;

    @Override
    public DeviceBLM createDevice(String accessToken, DeviceDTO deviceDTO) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);

        deviceValidator.validate(deviceDTO);
        DeviceBLM deviceBLM = deviceConverter.toBLM(deviceDTO);

        // Проверяем, что клиент из токена совпадает с клиентом устройства
        if (!clientUid.equals(deviceBLM.getClientUuid())) {
            throw new SecurityException("Client UID from token doesn't match device client UID");
        }

        // Проверяем, что устройство с таким именем не существует для этого клиента
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
    public DeviceBLM getDevice(String accessToken, UUID deviceUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);

        DeviceDALM deviceDALM = deviceRepository.findByUid(deviceUid);

        // Проверяем, что устройство принадлежит клиенту из токена
        if (!clientUid.equals(deviceDALM.getClientUuid())) {
            throw new SecurityException("Device doesn't belong to the authenticated client");
        }

        return deviceConverter.toBLM(deviceDALM);
    }

    @Override
    public List<DeviceBLM> getDevicesByClient(String accessToken) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);

        List<DeviceDALM> devicesDALM = deviceRepository.findByClientUuid(clientUid);
        return devicesDALM.stream()
                .map(deviceConverter::toBLM)
                .collect(Collectors.toList());
    }

    @Override
    public DeviceBLM updateDevice(String accessToken, UUID deviceUid, DeviceDTO deviceDTO) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);

        // Проверяем существование устройства и принадлежность клиенту
        DeviceDALM existingDevice = deviceRepository.findByUid(deviceUid);
        if (!clientUid.equals(existingDevice.getClientUuid())) {
            throw new SecurityException("Device doesn't belong to the authenticated client");
        }

        deviceValidator.validate(deviceDTO);
        DeviceBLM deviceBLM = deviceConverter.toBLM(deviceDTO);

        // Проверяем, что клиент из токена совпадает с клиентом устройства
        if (!clientUid.equals(deviceBLM.getClientUuid())) {
            throw new SecurityException("Client UID from token doesn't match device client UID");
        }

        DeviceDALM deviceDALM = deviceConverter.toDALM(deviceBLM);
        deviceRepository.update(deviceDALM);

        log.info("Device updated: {} for client: {}", deviceUid, clientUid);
        return deviceBLM;
    }

    @Override
    public void deleteDevice(String accessToken, UUID deviceUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);

        // Проверяем существование устройства и принадлежность клиенту
        DeviceDALM existingDevice = deviceRepository.findByUid(deviceUid);
        if (!clientUid.equals(existingDevice.getClientUuid())) {
            throw new SecurityException("Device doesn't belong to the authenticated client");
        }

        deviceRepository.delete(deviceUid);
        log.info("Device deleted: {} for client: {}", deviceUid, clientUid);
    }

    @Override
    public boolean deviceExists(String accessToken, UUID deviceUid) {
        validateToken(accessToken);
        return deviceRepository.exists(deviceUid);
    }

    private UUID validateTokenAndGetClientUid(String accessToken) {
        validateToken(accessToken);
        return authServiceClient.getAccessTokenClientUID(accessToken);
    }

    private void validateToken(String accessToken) {
        authServiceClient.validateAccessToken(accessToken);
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> authServiceStatus;
        try {
            authServiceStatus = authServiceClient.healthCheck();
        } catch (Exception e) {
            // В случае ошибки возвращаем информацию об исключении
            authServiceStatus = Map.of(
                    "error", e.getClass().getSimpleName(),
                    "message", e.getMessage(),
                    "status", "DOWN");
        }

        return Map.of(
                "status", "OK",
                "service", "device-service",
                "timestamp", System.currentTimeMillis(),
                "auth-service", authServiceStatus);
    }

}