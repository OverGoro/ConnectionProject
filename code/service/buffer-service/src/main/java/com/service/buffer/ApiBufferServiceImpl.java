package com.service.buffer;

import com.connection.device.DeviceService;
import com.connection.device.model.DeviceBlm;
import com.connection.processing.buffer.exception.BufferAlreadyExistsException;
import com.connection.processing.buffer.model.BufferBlm;
import com.connection.processing.buffer.repository.BufferRepository;
import com.connection.processing.buffer.validator.BufferValidator;
import com.connection.service.auth.AuthService;
import com.service.buffer.config.SecurityUtils;
import com.service.connectionscheme.ConnectionSchemeService;
import java.util.ArrayList;
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
@Service("ApiBufferService")
@EnableAutoConfiguration(exclude = {JpaRepositoriesAutoConfiguration.class})
@Transactional("atomicosTransactionManager")
public class ApiBufferServiceImpl implements BufferService {

    private final BufferRepository bufferRepository;
    private final BufferValidator bufferValidator;

    private final AuthService authClient;
    private final DeviceService deviceClient;
    private final ConnectionSchemeService connectionSchemeClient;

    @Override
    public BufferBlm createBuffer(BufferBlm bufferBlm) {
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        bufferValidator.validate(bufferBlm);

        if (!deviceExistsAndBelongsToClient(bufferBlm.getDeviceUid(),
                clientUid)) {
            throw new SecurityException(
                    "Device doesn't exist or doesn't belong to the authenticated client");
        }

        if (bufferRepository.exists(bufferBlm.getUid())) {
            throw new BufferAlreadyExistsException("Buffer with UID '"
                    + bufferBlm.getUid() + "' already exists");
        }
        bufferRepository.add(bufferBlm);

        log.info("Buffer created: {} for device: {}", bufferBlm.getUid(),
                bufferBlm.getDeviceUid());
        return bufferBlm;
    }

    @Override
    public BufferBlm getBufferByUid(UUID bufferUid) {
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        BufferBlm bufferBlm = bufferRepository.findByUid(bufferUid);

        if (!deviceExistsAndBelongsToClient(bufferBlm.getDeviceUid(),
                clientUid)) {
            throw new SecurityException(
                    "Buffer doesn't exist or doesn't belong to the authenticated client");
        }

        return bufferBlm;
    }

    @Override
    public List<BufferBlm> getBuffersByClient(UUID clientUid) {
        UUID currentClientUid = SecurityUtils.getCurrentClientUid();
        if (!currentClientUid.equals(clientUid)) {
            throw new SecurityException("Access denied to client buffers");
        }

        try {

            List<DeviceBlm> deviceBlms =
                    deviceClient.getDevicesByClient(currentClientUid);
            List<BufferBlm> bufferBlms = new ArrayList<>();
            for (DeviceBlm device : deviceBlms) {
                try {
                    List<BufferBlm> deviceBuffers =
                            getBuffersByDevice(device.getUid());
                    bufferBlms.addAll(deviceBuffers);
                } catch (Exception e) {
                    log.error("Error getting buffers for device: {}",
                            device.getUid(), e);
                }
            }
            return bufferBlms;

        } catch (Exception e) {
            log.error("Unexpected error getting buffers for client: {}",
                    clientUid, e);
            throw new RuntimeException("Unexpected error", e);
        }
    }

    @Override
    public List<BufferBlm> getBuffersByDevice(UUID deviceUid) {
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        if (!deviceExistsAndBelongsToClient(deviceUid, clientUid)) {
            throw new SecurityException(
                    "Device doesn't belong to the authenticated client");
        }

        List<BufferBlm> bufferBlms =
                bufferRepository.findByDeviceUid(deviceUid);
        return bufferBlms;
    }

    @Override
    public List<BufferBlm> getBuffersByConnectionScheme(
            UUID connectionSchemeUid) {
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        if (!connectionSchemeExistsAndBelongsToClient(connectionSchemeUid,
                clientUid)) {
            throw new SecurityException(
                    "Connection scheme doesn't belong to the authenticated client");
        }

        List<BufferBlm> buffersBlm =
                bufferRepository.findByConnectionSchemeUid(connectionSchemeUid);
        return buffersBlm;
    }

    @Override
    public BufferBlm updateBuffer(UUID bufferUid, BufferBlm bufferBlm) {
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        bufferValidator.validate(bufferBlm);

        BufferBlm existingBuffer = bufferRepository.findByUid(bufferUid);

        if (!deviceExistsAndBelongsToClient(existingBuffer.getDeviceUid(),
                clientUid)) {
            throw new SecurityException(
                    "Buffer doesn't exist or doesn't belong to the authenticated client");
        }

        if (!deviceExistsAndBelongsToClient(bufferBlm.getDeviceUid(),
                clientUid)) {
            throw new SecurityException(
                    "New device doesn't belong to the authenticated client");
        }

        if (!bufferUid.equals(bufferBlm.getUid())) {
            throw new IllegalArgumentException("Cannot change buffer UID");
        }

        bufferRepository.update(bufferBlm);

        log.info("Buffer updated: {} for device: {}", bufferUid,
                bufferBlm.getDeviceUid());
        return bufferBlm;
    }

    @Override
    public void deleteBuffer(UUID bufferUid) {
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        BufferBlm existingBuffer = bufferRepository.findByUid(bufferUid);

        if (!deviceExistsAndBelongsToClient(existingBuffer.getDeviceUid(),
                clientUid)) {
            throw new SecurityException(
                    "Buffer doesn't exist or doesn't belong to the authenticated client");
        }

        bufferRepository.delete(bufferUid);
        log.info("Buffer deleted: {} for client: {}, device: {}", bufferUid,
                clientUid, existingBuffer.getDeviceUid());
    }

    @Override
    public boolean bufferExists(UUID bufferUid) {
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        if (!bufferRepository.exists(bufferUid)) {
            return false;
        }

        try {
            BufferBlm buffer = bufferRepository.findByUid(bufferUid);
            return deviceExistsAndBelongsToClient(buffer.getDeviceUid(),
                    clientUid);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        try {
            var authHealth = authClient.getHealthStatus();

            var deviceHealth = deviceClient.getHealthStatus();

            var connectionSchemeHealth =
                    connectionSchemeClient.getHealthStatus();

            return Map.of("status", "OK", "service", "buffer-service",
                    "timestamp", System.currentTimeMillis(), "auth-service",
                    authHealth != null ? "AVAILABLE" : "UNAVAILABLE",
                    "device-service",
                    deviceHealth != null ? "AVAILABLE" : "UNAVAILABLE",
                    "connection-scheme-service",
                    connectionSchemeHealth != null ? "AVAILABLE"
                            : "UNAVAILABLE",
                    "database", "CONNECTED");

        } catch (Exception e) {
            log.error("Health check error: ", e);
            return Map.of("status", "DEGRADED", "service", "buffer-service",
                    "timestamp", System.currentTimeMillis(), "auth-service",
                    "UNAVAILABLE", "device-service", "UNAVAILABLE",
                    "connection-scheme-service", "UNAVAILABLE", "database",
                    "CONNECTED", "error", e.getMessage());
        }
    }

    @Override
    public void deleteBufferFromConnectionScheme(UUID connectionSchemeUid,
            UUID bufferUid) {
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        if (!connectionSchemeExistsAndBelongsToClient(connectionSchemeUid,
                clientUid)) {
            throw new SecurityException(
                    "Connection scheme doesn't belong to the authenticated client");
        }

        List<BufferBlm> buffers =
                getBuffersByConnectionScheme(connectionSchemeUid);
        for (BufferBlm b : buffers) {
            bufferRepository.removeBufferFromConnectionScheme(b.getUid(),
                    connectionSchemeUid);
        }

        log.info("Deleted buffers for connection scheme: {}",
                connectionSchemeUid);
    }

    @Override
    public void deleteAllBuffersFromConnectionScheme(UUID connectionSchemeUid) {
        UUID clientUid = SecurityUtils.getCurrentClientUid();
        if (!connectionSchemeExistsAndBelongsToClient(connectionSchemeUid,
                clientUid)) {
            throw new SecurityException(
                    "Connection scheme doesn't belong to the authenticated client");
        }

        List<BufferBlm> buffers =
                getBuffersByConnectionScheme(connectionSchemeUid);
        for (BufferBlm b : buffers) {
            bufferRepository.removeBufferFromConnectionScheme(b.getUid(),
                    connectionSchemeUid);
        }

        log.info("Deleted buffers for connection scheme: {}",
                connectionSchemeUid);
    }

    /** . */
    protected boolean deviceExistsAndBelongsToClient(UUID deviceUuid,
            UUID clientUuid) {
        return deviceClient.getDevice(deviceUuid).getClientUuid()
                .equals(clientUuid);
    }

    /** . */
    protected boolean connectionSchemeExistsAndBelongsToClient(
            UUID connectionSchemeUid, UUID clientUid) {
        return connectionSchemeClient.schemeExists(connectionSchemeUid)
                && connectionSchemeClient.getSchemeByUid(connectionSchemeUid)
                        .getClientUid().equals(clientUid);
    }
}
