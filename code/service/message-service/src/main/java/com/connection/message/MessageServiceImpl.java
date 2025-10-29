package com.connection.message;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.stereotype.Service;

import com.connection.device.DeviceService;
import com.connection.device.converter.DeviceConverter;
import com.connection.message.config.SecurityUtils;
import com.connection.message.converter.MessageConverter;
import com.connection.message.model.MessageBLM;
import com.connection.message.repository.MessageRepository;
import com.connection.message.validator.MessageValidator;
import com.connection.processing.buffer.converter.BufferConverter;
import com.connection.processing.buffer.model.BufferBLM;
import com.connection.scheme.converter.ConnectionSchemeConverter;
import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.service.auth.AuthService;
import com.service.buffer.BufferService;
import com.service.connectionscheme.ConnectionSchemeService;
import com.service.device.auth.DeviceAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@EnableAutoConfiguration(exclude = {
        JpaRepositoriesAutoConfiguration.class
})
public class MessageServiceImpl implements MessageService {

    protected final MessageRepository messageRepository;
    protected final MessageValidator messageValidator;
    
    protected final AuthService authClient;
    protected final BufferService bufferClient;
    protected final ConnectionSchemeService connectionSchemeClient;
    protected final DeviceAuthService deviceAuthClient;
    protected final DeviceService deviceClient;

    protected final BufferConverter bufferConverter;
    protected final ConnectionSchemeConverter connectionSchemeConverter;
    protected final DeviceConverter deviceConverter;
    protected final MessageConverter messageConverter;

    @Override
    public void addMessage(MessageBLM messageBLM) {
        // Для добавления сообщений требуется аутентификация устройства
        if (SecurityUtils.isDeviceAuthenticated()) {
            UUID currentDeviceUid = SecurityUtils.getCurrentDeviceUid();
            messageValidator.validate(messageBLM);

            // Проверяем, что устройство имеет доступ к буферу
            if (!hasDeviceAccessToBuffer(currentDeviceUid, messageBLM.getBufferUid())) {
                throw new SecurityException("Device doesn't have access to this buffer");
            }

            messageRepository.add(messageBLM);
            processMessageMovement(messageBLM);
        } else if (SecurityUtils.isClientAuthenticated()) {
            UUID currentClientUid = SecurityUtils.getCurrentClientUid();
            messageValidator.validate(messageBLM);

            // Проверяем, что клиент имеет доступ к буферу через свои устройства
            if (!hasClientAccessToBuffer(currentClientUid, messageBLM.getBufferUid())) {
                throw new SecurityException("Client doesn't have access to this buffer");
            }

            messageRepository.add(messageBLM);
            processMessageMovement(messageBLM);
        } else {
            throw new SecurityException("Cannot add messages without authorization");
        }
    }

    @Override
    public List<MessageBLM> getMessagesByBuffer(UUID bufferUuid, boolean deleteOnGet, int offset, int limit) {
        checkBufferAccess(bufferUuid);

        List<MessageBLM> messageBLMs = messageRepository.findByBufferUid(bufferUuid);
        messageBLMs = messageBLMs.stream()
                .sorted((x, y) -> x.getCreatedAt().compareTo(y.getCreatedAt()))
                .toList()
                .subList(offset, Math.min(offset + limit, messageBLMs.size()));

        if (deleteOnGet)
            messageBLMs.forEach(this::deleteMessage);

        return messageBLMs;
    }

    @Override
    public List<MessageBLM> getMessagesByScheme(UUID schemeUuid, boolean deleteOnGet, int offset, int limit) {
        // Для схем требуется аутентификация клиента ИЛИ устройства
        if (SecurityUtils.isClientAuthenticated()) {
            UUID currentClientUid = SecurityUtils.getCurrentClientUid();

            // Проверяем, что схема принадлежит клиенту
            if (!connectionSchemeExistsAndBelongsToClient(schemeUuid, currentClientUid)) {
                throw new SecurityException("Connection scheme doesn't belong to the authenticated client");
            }
        } else if (SecurityUtils.isDeviceAuthenticated()) {
            UUID currentDeviceUid = SecurityUtils.getCurrentDeviceUid();
            
            // Для устройства проверяем, что схема связана с его буферами
            if (!isSchemeAccessibleToDevice(schemeUuid, currentDeviceUid)) {
                throw new SecurityException("Device doesn't have access to this connection scheme");
            }
        } else {
            throw new SecurityException("Authentication required to access messages by scheme");
        }

        List<BufferBLM> bufferBLMs = getSchemeBuffers(schemeUuid);
        Set<MessageBLM> set = new HashSet<>();
        for (BufferBLM b : bufferBLMs) {
            set.addAll(getMessagesByBuffer(b.getUid(), deleteOnGet, offset, limit));
        }

        List<MessageBLM> rMessageBLMs = new ArrayList<>(set);

        rMessageBLMs = rMessageBLMs.stream()
                .sorted((x, y) -> x.getCreatedAt().compareTo(y.getCreatedAt()))
                .toList()
                .subList(offset, Math.min(offset + limit, rMessageBLMs.size()));

        if (deleteOnGet)
            rMessageBLMs.forEach(this::deleteMessage);

        return rMessageBLMs;
    }

    @Override
    public List<MessageBLM> getMessagesByDevice(UUID deviceUuid, boolean deleteOnGet, int offset, int limit) {
        if (SecurityUtils.isClientAuthenticated()) {
            // Клиент может получать сообщения своих устройств
            UUID currentClientUid = SecurityUtils.getCurrentClientUid();
            if (!deviceExistsAndBelongsToClient(deviceUuid, currentClientUid)) {
                throw new SecurityException("Device doesn't belong to the authenticated client");
            }
        } else if (SecurityUtils.isDeviceAuthenticated()) {
            // Устройство может получать только свои сообщения
            UUID currentDeviceUid = SecurityUtils.getCurrentDeviceUid();
            if (!currentDeviceUid.equals(deviceUuid)) {
                throw new SecurityException("Device can only access its own messages");
            }
        } else {
            throw new SecurityException("Authentication required");
        }

        List<BufferBLM> bufferBLMs = getDeviceBuffers(deviceUuid);
        Set<MessageBLM> set = new HashSet<>();
        for (BufferBLM b : bufferBLMs) {
            set.addAll(getMessagesByBuffer(b.getUid(), deleteOnGet, offset, limit));
        }
        List<MessageBLM> rMessageBLMs = new ArrayList<>(set);

        rMessageBLMs = rMessageBLMs.stream()
                .sorted((x, y) -> x.getCreatedAt().compareTo(y.getCreatedAt()))
                .toList()
                .subList(offset, Math.min(offset + limit, rMessageBLMs.size()));

        if (deleteOnGet)
            rMessageBLMs.forEach(this::deleteMessage);

        return rMessageBLMs;
    }

    @Override
    public Map<String, String> health() {
        try {
            var authHealth = authClient.getHealthStatus();

            var buufferHealth = bufferClient.getHealthStatus();

            var connectionSchemeHealth = connectionSchemeClient.getHealthStatus();

            var deviceHealth = deviceClient.getHealthStatus();

            var deviceAuthHealth = deviceAuthClient.getHealthStatus();

            return Map.of(
                    "status", "OK",
                    "service", "message-service",
                    "timestamp", String.valueOf(System.currentTimeMillis()),
                    "auth-service", authHealth!= null ? "AVAILABLE" : "UNAVAILABLE",
                    "buffer-service", buufferHealth!= null ? "AVAILABLE" : "UNAVAILABLE",
                    "connection-scheme-service", connectionSchemeHealth!= null ? "AVAILABLE" : "UNAVAILABLE",
                    "device-service", deviceHealth!= null ? "AVAILABLE" : "UNAVAILABLE",
                    "device-auth-service", deviceAuthHealth!= null ? "AVAILABLE" : "UNAVAILABLE");

        } catch (Exception e) {
            log.error("Health check error: ", e);
            return Map.of(
                    "status", "DEGRADED",
                    "service", "message-service",
                    "timestamp", String.valueOf(System.currentTimeMillis()),
                    "error", e.getMessage());
        }
    }

    private void checkBufferAccess(UUID bufferUuid) {
        if (SecurityUtils.isClientAuthenticated()) {
            // Клиент должен иметь доступ к буферу через свои устройства
            UUID currentClientUid = SecurityUtils.getCurrentClientUid();
            if (!hasClientAccessToBuffer(currentClientUid, bufferUuid)) {
                throw new SecurityException("Client doesn't have access to this buffer");
            }
        } else if (SecurityUtils.isDeviceAuthenticated()) {
            // Устройство должно иметь доступ к буферу
            UUID currentDeviceUid = SecurityUtils.getCurrentDeviceUid();
            if (!hasDeviceAccessToBuffer(currentDeviceUid, bufferUuid)) {
                throw new SecurityException("Device doesn't have access to this buffer");
            }
        } else {
            throw new SecurityException("Authentication required");
        }
    }

    private boolean hasClientAccessToBuffer(UUID clientUid, UUID bufferUuid) {
        try {
            // Получаем информацию о буфере и проверяем принадлежность устройства клиенту
            BufferBLM bufferBLM = bufferClient.getBufferByUid(bufferUuid);
            return deviceExistsAndBelongsToClient(bufferBLM.getDeviceUid(), clientUid);

        } catch (Exception e) {
            log.error("Error checking client buffer access: {}", e.getMessage());
            return false;
        }
    }

    private boolean hasDeviceAccessToBuffer(UUID deviceUid, UUID bufferUuid) {
        try {
            // Получаем информацию о буфере и проверяем принадлежность устройству
            BufferBLM bufferBLM = bufferClient.getBufferByUid(bufferUuid);
            return deviceUid.equals(bufferBLM.getDeviceUid());
        } catch (Exception e) {
            log.error("Error checking device buffer access: {}", e.getMessage());
            return false;
        }
    }

    private boolean isSchemeAccessibleToDevice(UUID schemeUuid, UUID deviceUid) {
        try {
            // Получаем буферы устройства и проверяем, связаны ли они с данной схемой
            List<BufferBLM> schemeBuffers = getBufferSchemesForDevice(schemeUuid, deviceUid);
            
            // Если у устройства есть буферы, связанные с этой схемой - доступ разрешен
            return !schemeBuffers.isEmpty();
        } catch (Exception e) {
            log.error("Error checking device scheme access: {}", e.getMessage());
            return false;
        }
    }

    private List<BufferBLM> getBufferSchemesForDevice(UUID schemeUuid, UUID deviceUid) {
        try {
            // Получаем буферы устройства
            List<BufferBLM> deviceBuffers = getDeviceBuffers(deviceUid);
            
            // Получаем буферы схемы
            List<BufferBLM> schemeBuffers = getSchemeBuffers(schemeUuid);
                
            // Проверяем пересечение буферов устройства и схемы
            Set<UUID> deviceBufferUids = deviceBuffers.stream()
                    .map(BufferBLM::getUid)
                    .collect(Collectors.toSet());
            
            schemeBuffers  = schemeBuffers.stream().filter(buffer -> deviceBufferUids.contains(buffer.getUid())).toList();
            return schemeBuffers;

        } catch (Exception e) {
            log.error("Error getting buffer schemes for device: {}", e.getMessage());
            return List.of();
        }
    }

    private void processMessageMovement(MessageBLM messageBLM) {
        if (!"OUTGOING".equals(messageBLM.getContentType())) {
            return;
        }
        List<ConnectionSchemeBLM> connectionSchemeBLMs = getBufferSchemes(messageBLM.getBufferUid());
        Map<UUID, List<UUID>> bufferTransitions = new HashMap<>();
        for (ConnectionSchemeBLM cs : connectionSchemeBLMs) {
            cs.getBufferTransitions().forEach((key, value) -> bufferTransitions.merge(key, value, (oldList, newList) -> {
                List<UUID> merged = new ArrayList<>(oldList);
                merged.addAll(newList);
                return merged;
            }));
        }

        List<UUID> targetBuffers = bufferTransitions.get(messageBLM.getBufferUid());
        if (targetBuffers != null) {
            for (UUID b : targetBuffers) {
                MessageBLM messageBLMn = MessageBLM.builder().bufferUid(b)
                        .content(messageBLM.getContent())
                        .contentType("INCOMING")
                        .createdAt(new Date())
                        .uid(UUID.randomUUID())
                        .build();
                messageValidator.validate(messageBLMn);
                messageRepository.add(messageBLMn);
            }
        }
    }

    private void deleteMessage(UUID messageUuid) {
        messageRepository.deleteByUid(messageUuid);
    }

    private void deleteMessage(MessageBLM messageBLM) {
        deleteMessage(messageBLM.getUid());
    }

    protected boolean deviceExistsAndBelongsToClient(UUID deviceUuid, UUID clientUuid){
        return deviceClient.getDevice(deviceUuid).getClientUuid().equals(clientUuid);
    }
    protected boolean connectionSchemeExistsAndBelongsToClient(UUID connectionSchemeUid, UUID clientUid){
        return connectionSchemeClient.schemeExists(connectionSchemeUid) &&
            connectionSchemeClient.getSchemeByUid(connectionSchemeUid).getClientUid().equals(clientUid);
    }

    private List<ConnectionSchemeBLM> getBufferSchemes(UUID bufferUuid) {
        return connectionSchemeClient.getSchemesByBuffer(bufferUuid);            
        
    }

    private List<BufferBLM> getSchemeBuffers(UUID connectionSchemeUuid) {
        return bufferClient.getBuffersByConnectionScheme(connectionSchemeUuid);
    }

    private List<BufferBLM> getDeviceBuffers(UUID deviceUuid) {
        return bufferClient.getBuffersByDevice(deviceUuid);
    }
}