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

import com.connection.buffer.events.responses.GetBuffersByConnectionSchemeResponse;
import com.connection.buffer.events.responses.GetBuffersByDeviceResponse;
import com.connection.device.converter.DeviceConverter;
import com.connection.message.converter.MessageConverter;
import com.connection.message.kafka.TypedAuthKafkaClient;
import com.connection.message.kafka.TypedBufferKafkaClient;
import com.connection.message.kafka.TypedConnectionSchemeKafkaClient;
import com.connection.message.kafka.TypedDeviceAuthKafkaClient;
import com.connection.message.kafka.TypedDeviceKafkaClient;
import com.connection.message.model.MessageBLM;
import com.connection.message.model.MessageDALM;
import com.connection.message.repository.MessageRepository;
import com.connection.message.validator.MessageValidator;
import com.connection.processing.buffer.converter.BufferConverter;
import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferDTO;
import com.connection.scheme.converter.ConnectionSchemeConverter;
import com.connection.scheme.events.responses.GetConnectionSchemesByBufferResponse;
import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@EnableAutoConfiguration(exclude = {
        JpaRepositoriesAutoConfiguration.class
})
public class MessageServiceImpl implements MessageService {

    protected MessageRepository messageRepository;
    protected MessageValidator messageValidator;

    protected TypedAuthKafkaClient authKafkaClient;
    protected TypedBufferKafkaClient bufferKafkaClient;
    protected TypedConnectionSchemeKafkaClient connectionSchemeKafkaClient;
    protected TypedDeviceAuthKafkaClient deviceAuthKafkaClient;
    protected TypedDeviceKafkaClient deviceKafkaClient;

    protected BufferConverter bufferConverter;
    protected ConnectionSchemeConverter connectionSchemeConverter;
    protected DeviceConverter deviceConverter;
    protected MessageConverter messageConverter;

    @Override
    public void addMessage(MessageBLM messageBLM) {
        messageValidator.validate(messageBLM);
        MessageDALM messageDALM = messageConverter.toDALM(messageBLM);
        messageRepository.add(messageDALM);
        processMessageMovement(messageBLM);
    }

    @Override
    public List<MessageBLM> getMessagesByBuffer(UUID bufferUuid, boolean deleteOnGet, int offset, int limit) {
        List<MessageDALM> messageDALMs = messageRepository.findByBufferUid(bufferUuid)
                .stream()
                .sorted((x, y) -> {
                    return x.getCreatedAt().compareTo(y.getCreatedAt());
                })
                .toList()
                .subList(offset, offset + limit);

        List<MessageBLM> messageBLMs = messageDALMs.stream()
                .<MessageBLM>map(messageConverter::toBLM)
                .toList();

        if (deleteOnGet)
            messageBLMs.forEach(this::deleteMessage);

        return messageBLMs;
    }

    @Override
    public List<MessageBLM> getMessagesByScheme(UUID schemeUuid, boolean deleteOnGet, int offset, int limit) {
        List<BufferBLM> bufferBLMs = getSchemeBuffers(schemeUuid);
        Set<MessageBLM> set = new HashSet<>();
        for (BufferBLM b : bufferBLMs) {
            set.addAll(getMessagesByBuffer(b.getUid(), deleteOnGet, offset, limit));
        }
        List<MessageBLM> rMessageBLMs = new ArrayList<>(set)
                .stream()
                .sorted((x, y) -> {
                    return x.getCreatedAt().compareTo(y.getCreatedAt());
                })
                .toList()
                .subList(offset, offset + limit);
        if (deleteOnGet)
            rMessageBLMs.forEach(this::deleteMessage);
        return rMessageBLMs;
    }

    @Override
    public List<MessageBLM> getMessagesByDevice(UUID devicUuid, boolean deleteOnGet, int offset, int limit) {
        List<BufferBLM> bufferBLMs = getDeviceBuffers(devicUuid);
        Set<MessageBLM> set = new HashSet<>();
        for (BufferBLM b : bufferBLMs) {
            set.addAll(getMessagesByBuffer(b.getUid(), deleteOnGet, offset, limit));
        }
        List<MessageBLM> rMessageBLMs = new ArrayList<>(set)
                .stream()
                .sorted((x, y) -> {
                    return x.getCreatedAt().compareTo(y.getCreatedAt());
                })
                .toList()
                .subList(offset, offset + limit);
        if (deleteOnGet)
            rMessageBLMs.forEach(this::deleteMessage);
        return rMessageBLMs;
    }

    @Override
    public Map<String, String> health() {
        try {
            com.connection.auth.events.responses.HealthCheckResponse authHealth = authKafkaClient
                    .healthCheck("message-service")
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);

            com.connection.buffer.events.responses.HealthCheckResponse buufferHealth = bufferKafkaClient
                    .healthCheck("message-service")
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);

            com.connection.scheme.events.responses.HealthCheckResponse connectionSchemeHealth = connectionSchemeKafkaClient
                    .healthCheck("message-service")
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);

            com.connection.device.events.responses.HealthCheckResponse deviceHealth = deviceKafkaClient
                    .healthCheck("message-service")
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);

            com.connection.device.auth.events.responses.HealthCheckResponse deviceAuthHealth = deviceAuthKafkaClient
                    .healthCheck("message-service")
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);

            return Map.of(
                    "status", "OK",
                    "service", "message-service",
                    "timestamp", String.valueOf(System.currentTimeMillis()),
                    "auth-service", authHealth.isSuccess() ? "AVAILABLE" : "UNAVAILABLE",
                    "auth-service", buufferHealth.isSuccess() ? "AVAILABLE" : "UNAVAILABLE",
                    "auth-service", connectionSchemeHealth.isSuccess() ? "AVAILABLE" : "UNAVAILABLE",
                    "device-service", deviceHealth.isSuccess() ? "AVAILABLE" : "UNAVAILABLE",
                    "device-auth-service", deviceAuthHealth.isSuccess() ? "AVAILABLE" : "UNAVAILABLE",
                    "database", "CONNECTED");

        } catch (Exception e) {
            log.error("Health check error: ", e);
            return Map.of(
                    "status", "DEGRADED",
                    "service", "buffer-service",
                    "timestamp", String.valueOf(System.currentTimeMillis()),
                    "error", e.getMessage());
        }
    }

    private void processMessageMovement(MessageBLM messageBLM) {
        if (messageBLM.getContentType() != "OUTGOING") {
            return;
        }
        List<ConnectionSchemeBLM> connectionSchemeBLMs = getBufferSchemes(messageBLM.getBufferUid());
        Map<UUID, List<UUID>> bufferTranstions = new HashMap<>();
        for (ConnectionSchemeBLM cs : connectionSchemeBLMs) {
            cs.getBufferTransitions().forEach((key, value) -> bufferTranstions.merge(key, value, (oldList, newList) -> {
                List<UUID> merged = new ArrayList<>(oldList);
                merged.addAll(newList);
                return merged;
            }));
        }

        for (UUID b : bufferTranstions.get(messageBLM.getBufferUid())) {
            MessageDALM messageDALM = MessageDALM.builder().bufferUid(b)
                    .content(messageBLM.getContent())
                    .contentType("INCOMMING")
                    .createdAt(new Date())
                    .uid(UUID.randomUUID())
                    .build();
            messageValidator.validate(messageDALM);
            messageRepository.add(messageDALM);
        }
    }

    private void deleteMessage(UUID messageUuid) {
        messageRepository.deleteByUid(messageUuid);
    }

    private void deleteMessage(MessageBLM messageBLM) {
        deleteMessage(messageBLM.getUid());
    }

    private List<ConnectionSchemeBLM> getBufferSchemes(UUID bufferUuid) {
        try {
            GetConnectionSchemesByBufferResponse response = connectionSchemeKafkaClient
                    .getConnectionSchemesByBufferUid(bufferUuid, "message-service")
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);

            if (!response.isSuccess() || response.getConnectionSchemeDTOs() == null) {
                log.warn("Failed to get connection schemes for buffer: {}", bufferUuid);
                return List.of();
            }

            List<ConnectionSchemeDTO> connectionSchemeDTOs = response.getConnectionSchemeDTOs();
            List<ConnectionSchemeBLM> connectionSchemeBLMs = connectionSchemeDTOs.stream()
                    .map(connectionSchemeConverter::toBLM)
                    .collect(Collectors.toList());

            return connectionSchemeBLMs;

        } catch (java.util.concurrent.TimeoutException e) {
            log.error("Timeout getting connection schemes for bufferUuid: {}", bufferUuid, e);
            throw new RuntimeException("Device service timeout", e);
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Error getting devices for bufferUuid: {}", bufferUuid, e);
            throw new RuntimeException("Device service error", e);
        } catch (Exception e) {
            log.error("Unexpected error getting buffers for bufferUuid: {}", bufferUuid, e);
            throw new RuntimeException("Unexpected error", e);
        }
    }

    private List<BufferBLM> getSchemeBuffers(UUID connectionSchemeUuid) {
        try {
            GetBuffersByConnectionSchemeResponse response = bufferKafkaClient
                    .getBuffersByConnectionSchemeUid(connectionSchemeUuid, "message-service")
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);

            if (!response.isSuccess() || response.getBufferDTOs() == null) {
                log.warn("Failed to get buffers for connection schemes: {}", connectionSchemeUuid);
                return List.of();
            }

            List<BufferDTO> bufferDTOs = response.getBufferDTOs();
            List<BufferBLM> bufferBLMs = bufferDTOs.stream()
                    .map(bufferConverter::toBLM)
                    .collect(Collectors.toList());

            return bufferBLMs;

        } catch (java.util.concurrent.TimeoutException e) {
            log.error("Timeout getting connection schemes for connectionSchemeUuid: {}", connectionSchemeUuid, e);
            throw new RuntimeException("Device service timeout", e);
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Error getting devices for connectionSchemeUuid: {}", connectionSchemeUuid, e);
            throw new RuntimeException("Device service error", e);
        } catch (Exception e) {
            log.error("Unexpected error getting buffers for connectionSchemeUuid: {}", connectionSchemeUuid, e);
            throw new RuntimeException("Unexpected error", e);
        }
    }

    private List<BufferBLM> getDeviceBuffers(UUID deviceUuid) {
        try {

            GetBuffersByDeviceResponse response = bufferKafkaClient
                    .getBuffersByDeviceUid(deviceUuid, "message-service")
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);

            if (!response.isSuccess() || response.getBufferDTOs() == null) {
                log.warn("Failed to get buffers for device: {}", deviceUuid);
                return List.of();
            }

            List<BufferDTO> bufferDTOs = response.getBufferDTOs();
            List<BufferBLM> bufferBLMs = bufferDTOs.stream()
                    .map(bufferConverter::toBLM)
                    .collect(Collectors.toList());

            return bufferBLMs;

        } catch (java.util.concurrent.TimeoutException e) {
            log.error("Timeout getting connection schemes for bufferUuid: {}", deviceUuid, e);
            throw new RuntimeException("Device service timeout", e);
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Error getting devices for bufferUuid: {}", deviceUuid, e);
            throw new RuntimeException("Device service error", e);
        } catch (Exception e) {
            log.error("Unexpected error getting buffers for bufferUuid: {}", deviceUuid, e);
            throw new RuntimeException("Unexpected error", e);
        }
    }
}