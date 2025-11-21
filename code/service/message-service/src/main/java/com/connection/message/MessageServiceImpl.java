package com.connection.message;

import com.connection.device.DeviceService;
import com.connection.device.converter.DeviceConverter;
import com.connection.message.config.SecurityUtils;
import com.connection.message.converter.MessageConverter;
import com.connection.message.model.MessageBlm;
import com.connection.message.repository.MessageRepository;
import com.connection.message.validator.MessageValidator;
import com.connection.processing.buffer.converter.BufferConverter;
import com.connection.processing.buffer.model.BufferBlm;
import com.connection.scheme.converter.ConnectionSchemeConverter;
import com.connection.scheme.model.ConnectionSchemeBlm;
import com.connection.service.auth.AuthService;
import com.service.buffer.BufferService;
import com.service.connectionscheme.ConnectionSchemeService;
import com.service.device.auth.DeviceAuthService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.stereotype.Service;

/** . */
@Slf4j
@RequiredArgsConstructor
@Service
@EnableAutoConfiguration(exclude = {JpaRepositoriesAutoConfiguration.class})
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
    public void addMessage(MessageBlm messageBlm) {
        // Для добавления сообщений требуется аутентификация устройства
        if (SecurityUtils.isDeviceAuthenticated()) {
            UUID currentDeviceUid = SecurityUtils.getCurrentDeviceUid();
            messageValidator.validate(messageBlm);

            // Проверяем, что устройство имеет доступ к буферу
            if (!hasDeviceAccessToBuffer(currentDeviceUid,
                    messageBlm.getBufferUid())) {
                throw new SecurityException(
                        "Device doesn't have access to this buffer");
            }

            messageRepository.add(messageBlm);
            processMessageMovement(messageBlm);
        } else if (SecurityUtils.isClientAuthenticated()) {
            UUID currentClientUid = SecurityUtils.getCurrentClientUid();
            messageValidator.validate(messageBlm);

            // Проверяем, что клиент имеет доступ к буферу через свои устройства
            if (!hasClientAccessToBuffer(currentClientUid,
                    messageBlm.getBufferUid())) {
                throw new SecurityException(
                        "Client doesn't have access to this buffer");
            }

            messageRepository.add(messageBlm);
            processMessageMovement(messageBlm);
        } else {
            throw new SecurityException(
                    "Cannot add messages without authorization");
        }
    }

    /** . */
    @Override
    public List<MessageBlm> getMessagesByBuffer(UUID bufferUuid,
            boolean deleteOnGet, int offset, int limit) {
        checkBufferAccess(bufferUuid);

        List<MessageBlm> messageBlms =
                messageRepository.findByBufferUid(bufferUuid);
        messageBlms = messageBlms.stream()
                .sorted((x, y) -> x.getCreatedAt().compareTo(y.getCreatedAt()))
                .toList()
                .subList(offset, Math.min(offset + limit, messageBlms.size()));

        if (deleteOnGet) {
            messageBlms.forEach(this::deleteMessage);
        }
        return messageBlms;
    }

    @Override
    public List<MessageBlm> getMessagesByScheme(UUID schemeUuid,
            boolean deleteOnGet, int offset, int limit) {

        // Проверка прав доступа
        validateSchemeAccess(schemeUuid);

        // Получение сообщений из всех буферов схемы
        List<MessageBlm> messages =
                getMessagesFromSchemeBuffers(schemeUuid, deleteOnGet);

        // Применение пагинации
        List<MessageBlm> paginatedMessages =
                applyPagination(messages, offset, limit);

        // Удаление сообщений если требуется
        if (deleteOnGet) {
            paginatedMessages.forEach(this::deleteMessage);
        }

        return paginatedMessages;
    }

    private void validateSchemeAccess(UUID schemeUuid) {
        if (SecurityUtils.isClientAuthenticated()) {
            validateClientAccess(schemeUuid);
        } else if (SecurityUtils.isDeviceAuthenticated()) {
            validateDeviceAccess(schemeUuid);
        } else {
            throw new SecurityException(
                    "Authentication required to access messages by scheme");
        }
    }

    private void validateClientAccess(UUID schemeUuid) {
        UUID currentClientUid = SecurityUtils.getCurrentClientUid();
        if (!connectionSchemeExistsAndBelongsToClient(schemeUuid,
                currentClientUid)) {
            throw new SecurityException(
                    "Connection scheme doesn't belong to the authenticated client");
        }
    }

    private void validateDeviceAccess(UUID schemeUuid) {
        UUID currentDeviceUid = SecurityUtils.getCurrentDeviceUid();
        if (!isSchemeAccessibleToDevice(schemeUuid, currentDeviceUid)) {
            throw new SecurityException(
                    "Device doesn't have access to this connection scheme");
        }
    }

    private List<MessageBlm> getMessagesFromSchemeBuffers(UUID schemeUuid,
            boolean deleteOnGet) {
        List<BufferBlm> buffers = getSchemeBuffers(schemeUuid);
        Set<MessageBlm> messages = new HashSet<>();

        for (BufferBlm buffer : buffers) {
            messages.addAll(getMessagesByBuffer(buffer.getUid(), deleteOnGet, 0,
                    Integer.MAX_VALUE));
        }

        return messages.stream()
                .sorted(Comparator.comparing(MessageBlm::getCreatedAt))
                .collect(Collectors.toList());
    }

    private List<MessageBlm> applyPagination(List<MessageBlm> messages,
            int offset, int limit) {
        int endIndex = Math.min(offset + limit, messages.size());
        return messages.subList(offset, endIndex);
    }

    @Override
    public List<MessageBlm> getMessagesByDevice(UUID deviceUuid,
            boolean deleteOnGet, int offset, int limit) {

        // Проверка прав доступа к устройству
        validateDeviceAccess(deviceUuid);

        // Получение сообщений из всех буферов устройства
        List<MessageBlm> messages =
                getMessagesFromDeviceBuffers(deviceUuid, deleteOnGet);

        // Применение пагинации
        List<MessageBlm> paginatedMessages =
                applyPagination(messages, offset, limit);

        // Удаление сообщений если требуется
        if (deleteOnGet) {
            paginatedMessages.forEach(this::deleteMessage);
        }

        return paginatedMessages;
    }

    private List<MessageBlm> getMessagesFromDeviceBuffers(UUID deviceUuid,
            boolean deleteOnGet) {
        List<BufferBlm> buffers = getDeviceBuffers(deviceUuid);
        Set<MessageBlm> messages = new HashSet<>();

        for (BufferBlm buffer : buffers) {
            messages.addAll(getMessagesByBuffer(buffer.getUid(), deleteOnGet, 0,
                    Integer.MAX_VALUE));
        }

        return messages.stream()
                .sorted(Comparator.comparing(MessageBlm::getCreatedAt))
                .collect(Collectors.toList());
    }


    /** . */
    @Override
    public Map<String, String> health() {
        try {
            var authHealth = authClient.getHealthStatus();

            var buufferHealth = bufferClient.getHealthStatus();

            var connectionSchemeHealth =
                    connectionSchemeClient.getHealthStatus();

            var deviceHealth = deviceClient.getHealthStatus();

            var deviceAuthHealth = deviceAuthClient.getHealthStatus();

            return Map.of("status", "OK", "service", "message-service",
                    "timestamp", String.valueOf(System.currentTimeMillis()),
                    "auth-service",
                    authHealth != null ? "AVAILABLE" : "UNAVAILABLE",
                    "buffer-service",
                    buufferHealth != null ? "AVAILABLE" : "UNAVAILABLE",
                    "connection-scheme-service",
                    connectionSchemeHealth != null ? "AVAILABLE"
                            : "UNAVAILABLE",
                    "device-service",
                    deviceHealth != null ? "AVAILABLE" : "UNAVAILABLE",
                    "device-auth-service",
                    deviceAuthHealth != null ? "AVAILABLE" : "UNAVAILABLE");

        } catch (Exception e) {
            log.error("Health check error: ", e);
            return Map.of("status", "DEGRADED", "service", "message-service",
                    "timestamp", String.valueOf(System.currentTimeMillis()),
                    "error", e.getMessage());
        }
    }

    /** . */
    private void checkBufferAccess(UUID bufferUuid) {
        if (SecurityUtils.isClientAuthenticated()) {
            // Клиент должен иметь доступ к буферу через свои устройства
            UUID currentClientUid = SecurityUtils.getCurrentClientUid();
            if (!hasClientAccessToBuffer(currentClientUid, bufferUuid)) {
                throw new SecurityException(
                        "Client doesn't have access to this buffer");
            }
        } else if (SecurityUtils.isDeviceAuthenticated()) {
            // Устройство должно иметь доступ к буферу
            UUID currentDeviceUid = SecurityUtils.getCurrentDeviceUid();
            if (!hasDeviceAccessToBuffer(currentDeviceUid, bufferUuid)) {
                throw new SecurityException(
                        "Device doesn't have access to this buffer");
            }
        } else {
            throw new SecurityException("Authentication required");
        }
    }

    private boolean hasClientAccessToBuffer(UUID clientUid, UUID bufferUuid) {
        try {
            // Получаем информацию о буфере и проверяем принадлежность устройства клиенту
            BufferBlm bufferBlm = bufferClient.getBufferByUid(bufferUuid);
            return deviceExistsAndBelongsToClient(bufferBlm.getDeviceUid(),
                    clientUid);

        } catch (Exception e) {
            log.error("Error checking client buffer access: {}",
                    e.getMessage());
            return false;
        }
    }

    private boolean hasDeviceAccessToBuffer(UUID deviceUid, UUID bufferUuid) {
        try {
            // Получаем информацию о буфере и проверяем принадлежность устройству
            BufferBlm bufferBlm = bufferClient.getBufferByUid(bufferUuid);
            return deviceUid.equals(bufferBlm.getDeviceUid());
        } catch (Exception e) {
            log.error("Error checking device buffer access: {}",
                    e.getMessage());
            return false;
        }
    }

    private boolean isSchemeAccessibleToDevice(UUID schemeUuid,
            UUID deviceUid) {
        try {
            // Получаем буферы устройства и проверяем, связаны ли они с данной схемой
            List<BufferBlm> schemeBuffers =
                    getBufferSchemesForDevice(schemeUuid, deviceUid);

            // Если у устройства есть буферы, связанные с этой схемой - доступ разрешен
            return !schemeBuffers.isEmpty();
        } catch (Exception e) {
            log.error("Error checking device scheme access: {}",
                    e.getMessage());
            return false;
        }
    }

    private List<BufferBlm> getBufferSchemesForDevice(UUID schemeUuid,
            UUID deviceUid) {
        try {
            // Получаем буферы устройства
            List<BufferBlm> deviceBuffers = getDeviceBuffers(deviceUid);

            // Получаем буферы схемы
            List<BufferBlm> schemeBuffers = getSchemeBuffers(schemeUuid);

            // Проверяем пересечение буферов устройства и схемы
            Set<UUID> deviceBufferUids = deviceBuffers.stream()
                    .map(BufferBlm::getUid).collect(Collectors.toSet());

            schemeBuffers = schemeBuffers.stream().filter(
                    buffer -> deviceBufferUids.contains(buffer.getUid()))
                    .toList();
            return schemeBuffers;

        } catch (Exception e) {
            log.error("Error getting buffer schemes for device: {}",
                    e.getMessage());
            return List.of();
        }
    }

    private void processMessageMovement(MessageBlm messageBlm) {
        if (!"OUTGOING".equals(messageBlm.getContentType())) {
            return;
        }
        List<ConnectionSchemeBlm> connectionSchemeBlms =
                getBufferSchemes(messageBlm.getBufferUid());
        Map<UUID, List<UUID>> bufferTransitions = new HashMap<>();
        for (ConnectionSchemeBlm cs : connectionSchemeBlms) {
            cs.getBufferTransitions().forEach((key, value) -> bufferTransitions
                    .merge(key, value, (oldList, newList) -> {
                        List<UUID> merged = new ArrayList<>(oldList);
                        merged.addAll(newList);
                        return merged;
                    }));
        }

        List<UUID> targetBuffers =
                bufferTransitions.get(messageBlm.getBufferUid());
        if (targetBuffers != null) {
            for (UUID b : targetBuffers) {
                MessageBlm messageBlmn = MessageBlm.builder().bufferUid(b)
                        .content(messageBlm.getContent())
                        .contentType("INCOMING").createdAt(new Date())
                        .uid(UUID.randomUUID()).build();
                messageValidator.validate(messageBlmn);
                messageRepository.add(messageBlmn);
            }
        }
    }

    private void deleteMessage(UUID messageUuid) {
        messageRepository.deleteByUid(messageUuid);
    }

    private void deleteMessage(MessageBlm messageBlm) {
        deleteMessage(messageBlm.getUid());
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

    private List<ConnectionSchemeBlm> getBufferSchemes(UUID bufferUuid) {
        return connectionSchemeClient.getSchemesByBuffer(bufferUuid);

    }

    private List<BufferBlm> getSchemeBuffers(UUID connectionSchemeUuid) {
        return bufferClient.getBuffersByConnectionScheme(connectionSchemeUuid);
    }

    private List<BufferBlm> getDeviceBuffers(UUID deviceUuid) {
        return bufferClient.getBuffersByDevice(deviceUuid);
    }
}
