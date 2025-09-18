// BufferDeviceServiceImpl.java
package com.service.bufferdevice;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.connection.device.model.DeviceBLM;
import com.connection.processing.buffer.bufferdevice.converter.BufferDeviceConverter;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceBLM;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDALM;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDTO;
import com.connection.processing.buffer.bufferdevice.repository.BufferDeviceRepository;
import com.connection.processing.buffer.bufferdevice.validator.BufferDeviceValidator;
import com.connection.processing.buffer.model.BufferBLM;
import com.service.bufferdevice.client.AuthServiceClient;
import com.service.bufferdevice.client.BufferServiceClient;
import com.service.bufferdevice.client.DeviceServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@EnableAutoConfiguration(exclude = {
    JpaRepositoriesAutoConfiguration.class
})
@Transactional("atomicosTransactionManager")
public class BufferDeviceServiceImpl implements BufferDeviceService {
    
    private final BufferDeviceRepository bufferDeviceRepository;
    private final BufferDeviceConverter bufferDeviceConverter;
    private final BufferDeviceValidator bufferDeviceValidator;
    private final AuthServiceClient authServiceClient;
    private final BufferServiceClient bufferServiceClient;
    private final DeviceServiceClient deviceServiceClient;

    @Override
    public BufferDeviceBLM createBufferDevice(String accessToken, BufferDeviceDTO bufferDeviceDTO) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        bufferDeviceValidator.validate(bufferDeviceDTO);
        BufferDeviceBLM bufferDeviceBLM = bufferDeviceConverter.toBLM(bufferDeviceDTO);
        
        // Проверяем, что буфер принадлежит клиенту
        BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, bufferDeviceBLM.getBufferUid());
        
        // Проверяем, что устройство принадлежит клиенту
        DeviceBLM device = deviceServiceClient.getDevice(accessToken, bufferDeviceBLM.getDeviceUid());
        
        if (!clientUid.equals(buffer.getConnectionSchemeUid())) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }
        
        if (!clientUid.equals(device.getClientUuid())) {
            throw new SecurityException("Device doesn't belong to the authenticated client");
        }

        BufferDeviceDALM bufferDeviceDALM = bufferDeviceConverter.toDALM(bufferDeviceBLM);
        bufferDeviceRepository.add(bufferDeviceDALM);
        
        log.info("Buffer-Device binding created: buffer={}, device={}", 
                bufferDeviceBLM.getBufferUid(), bufferDeviceBLM.getDeviceUid());
        return bufferDeviceBLM;
    }

    @Override
    public void deleteBufferDevice(String accessToken, UUID bufferUid, UUID deviceUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что буфер принадлежит клиенту
        BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, bufferUid);
        
        // Проверяем, что устройство принадлежит клиенту
        DeviceBLM device = deviceServiceClient.getDevice(accessToken, deviceUid);
        
        if (!clientUid.equals(buffer.getConnectionSchemeUid())) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }
        
        if (!clientUid.equals(device.getClientUuid())) {
            throw new SecurityException("Device doesn't belong to the authenticated client");
        }

        BufferDeviceDALM bufferDeviceDALM = new BufferDeviceDALM(bufferUid, deviceUid);
        bufferDeviceRepository.delete(bufferDeviceDALM);
        
        log.info("Buffer-Device binding deleted: buffer={}, device={}", bufferUid, deviceUid);
    }

    @Override
    public void deleteAllBufferDevicesForBuffer(String accessToken, UUID bufferUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что буфер принадлежит клиенту
        BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, bufferUid);
        
        if (!clientUid.equals(buffer.getConnectionSchemeUid())) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }

        bufferDeviceRepository.deleteAllByBufferUid(bufferUid);
        log.info("All Buffer-Device bindings deleted for buffer: {}", bufferUid);
    }

    @Override
    public void deleteAllBufferDevicesForDevice(String accessToken, UUID deviceUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что устройство принадлежит клиенту
        DeviceBLM device = deviceServiceClient.getDevice(accessToken, deviceUid);
        
        if (!clientUid.equals(device.getClientUuid())) {
            throw new SecurityException("Device doesn't belong to the authenticated client");
        }

        bufferDeviceRepository.deleteAllByDeviceUid(deviceUid);
        log.info("All Buffer-Device bindings deleted for device: {}", deviceUid);
    }

    @Override
    public List<BufferDeviceBLM> getBufferDevicesByBuffer(String accessToken, UUID bufferUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что буфер принадлежит клиенту
        BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, bufferUid);
        
        if (!clientUid.equals(buffer.getConnectionSchemeUid())) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }

        List<UUID> deviceUids = bufferDeviceRepository.findDeviceUidsByBufferUid(bufferUid);
        return deviceUids.stream()
                .map(deviceUid -> new BufferDeviceBLM(bufferUid, deviceUid))
                .collect(Collectors.toList());
    }

    @Override
    public List<BufferDeviceBLM> getBufferDevicesByDevice(String accessToken, UUID deviceUid) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что устройство принадлежит клиенту
        DeviceBLM device = deviceServiceClient.getDevice(accessToken, deviceUid);
        
        if (!clientUid.equals(device.getClientUuid())) {
            throw new SecurityException("Device doesn't belong to the authenticated client");
        }

        List<UUID> bufferUids = bufferDeviceRepository.findBufferUidsByDeviceUid(deviceUid);
        return bufferUids.stream()
                .map(bufferUid -> new BufferDeviceBLM(bufferUid, deviceUid))
                .collect(Collectors.toList());
    }

    @Override
    public boolean bufferDeviceExists(String accessToken, UUID bufferUid, UUID deviceUid) {
        validateToken(accessToken);
        return bufferDeviceRepository.exists(bufferUid, deviceUid);
    }

    @Override
    public void addDevicesToBuffer(String accessToken, UUID bufferUid, List<UUID> deviceUids) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что буфер принадлежит клиенту
        BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, bufferUid);
        
        if (!clientUid.equals(buffer.getConnectionSchemeUid())) {
            throw new SecurityException("Buffer doesn't belong to the authenticated client");
        }

        // Проверяем, что все устройства принадлежат клиенту
        for (UUID deviceUid : deviceUids) {
            DeviceBLM device = deviceServiceClient.getDevice(accessToken, deviceUid);
            if (!clientUid.equals(device.getClientUuid())) {
                throw new SecurityException("Device " + deviceUid + " doesn't belong to the authenticated client");
            }
        }

        bufferDeviceRepository.addDevicesToBuffer(bufferUid, deviceUids);
        log.info("Added {} devices to buffer: {}", deviceUids.size(), bufferUid);
    }

    @Override
    public void addBuffersToDevice(String accessToken, UUID deviceUid, List<UUID> bufferUids) {
        UUID clientUid = validateTokenAndGetClientUid(accessToken);
        
        // Проверяем, что устройство принадлежит клиенту
        DeviceBLM device = deviceServiceClient.getDevice(accessToken, deviceUid);
        
        if (!clientUid.equals(device.getClientUuid())) {
            throw new SecurityException("Device doesn't belong to the authenticated client");
        }

        // Проверяем, что все буферы принадлежат клиенту
        for (UUID bufferUid : bufferUids) {
            BufferBLM buffer = bufferServiceClient.getBuffer(accessToken, bufferUid);
            if (!clientUid.equals(buffer.getConnectionSchemeUid())) {
                throw new SecurityException("Buffer " + bufferUid + " doesn't belong to the authenticated client");
            }
        }

        bufferDeviceRepository.addBuffersToDevice(deviceUid, bufferUids);
        log.info("Added {} buffers to device: {}", bufferUids.size(), deviceUid);
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
        return Map.of(
                "status", "OK",
                "service", "buffer-device-service",
                "timestamp", System.currentTimeMillis(),
                "auth-service", authServiceClient.healthCheck(),
                "buffer-service", bufferServiceClient.healthCheck(),
                "device-service", deviceServiceClient.healthCheck());
    }
}