// BufferCommandConsumer.java
package com.service.buffer.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.connection.buffer.events.commands.GetBufferByUidCommand;
import com.connection.buffer.events.commands.GetBuffersByClientUidCommand;
import com.connection.buffer.events.commands.GetBuffersByConnectionSchemeUidCommand;
import com.connection.buffer.events.commands.GetBuffersByDeviceUidCommand;
import com.connection.buffer.events.commands.HealthCheckCommand;
import com.connection.buffer.events.responses.GetBufferByUidResponse;
import com.connection.buffer.events.responses.GetBuffersByClientResponse;
import com.connection.buffer.events.responses.GetBuffersByConnectionSchemeResponse;
import com.connection.buffer.events.responses.GetBuffersByDeviceResponse;
import com.connection.buffer.events.responses.HealthCheckResponse;
import com.connection.common.events.Command;
import com.service.buffer.BufferService;
import com.connection.processing.buffer.converter.BufferConverter;
import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BufferCommandConsumer {

    private final BufferService bufferService;
    private final BufferConverter bufferConverter;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "${app.kafka.topics.buffer-commands:buffer.commands}")
    public void handleBufferCommand(ConsumerRecord<String, Command> record) {
        try {
            Command command = record.value();
            String key = record.key();

            log.info("Received buffer command: {} with key: {}", command.getClass().getSimpleName(), key);

            if (command instanceof GetBufferByUidCommand) {
                GetBufferByUidCommand getBufferCommand = (GetBufferByUidCommand) command;
                handleGetBufferByUidCommand(getBufferCommand, key);
            } else if (command instanceof GetBuffersByClientUidCommand) {
                GetBuffersByClientUidCommand getBuffersCommand = (GetBuffersByClientUidCommand) command;
                handleGetBuffersByClientCommand(getBuffersCommand, key);
            } else if (command instanceof GetBuffersByDeviceUidCommand) {
                GetBuffersByDeviceUidCommand getBuffersCommand = (GetBuffersByDeviceUidCommand) command;
                handleGetBuffersByDeviceCommand(getBuffersCommand, key);
            } else if (command instanceof GetBuffersByConnectionSchemeUidCommand) {
                GetBuffersByConnectionSchemeUidCommand getBuffersCommand = (GetBuffersByConnectionSchemeUidCommand) command;
                handleGetBuffersByConnectionSchemeCommand(getBuffersCommand, key);
            } else if (command instanceof HealthCheckCommand) {
                HealthCheckCommand healthCommand = (HealthCheckCommand) command;
                handleHealthCheckCommand(healthCommand, key);
            } else {
                log.warn("Unknown buffer command type: {}", command.getClass().getCanonicalName());
            }

        } catch (Exception e) {
            log.error("Error processing buffer command: key={}", record.key(), e);
        }
    }

    private void handleGetBufferByUidCommand(GetBufferByUidCommand command, String key) {
        try {
            log.info("Processing GetBufferByUidCommand for buffer UID: {}", command.getBufferUid());

            BufferBLM bufferBLM = bufferService.getBufferByUid(command.getClientUid(), command.getBufferUid());
            BufferDTO bufferDTO = bufferConverter.toDTO(bufferBLM);

            GetBufferByUidResponse response = GetBufferByUidResponse.success(
                    command.getCorrelationId(),
                    bufferDTO);

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.info("Successfully processed GetBufferByUidCommand for buffer: {}", command.getBufferUid());

        } catch (Exception e) {
            log.error("Error processing GetBufferByUidCommand for buffer UID: {}", command.getBufferUid(), e);

            GetBufferByUidResponse response = GetBufferByUidResponse.error(
                    command.getCorrelationId(),
                    e.getMessage());

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
        }
    }

    private void handleGetBuffersByClientCommand(GetBuffersByClientUidCommand command, String key) {
        try {
            log.info("Processing GetBuffersByClientCommand for client UID: {}", command.getClientUid());

            List<BufferBLM> buffersBLM = bufferService.getBuffersByClient(command.getClientUid());
            List<BufferDTO> bufferDTOs = buffersBLM.stream()
                    .map(bufferConverter::toDTO)
                    .collect(Collectors.toList());

            GetBuffersByClientResponse response = GetBuffersByClientResponse.success(
                    command.getCorrelationId(),
                    bufferDTOs);

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

            log.info("Successfully processed GetBuffersByClientCommand for client: {}, found {} buffers",
                    command.getClientUid(), bufferDTOs.size());

        } catch (Exception e) {
            log.error("Error processing GetBuffersByClientCommand for client UID: {}", command.getClientUid(), e);

            GetBuffersByClientResponse response = GetBuffersByClientResponse.error(
                    command.getCorrelationId(),
                    e.getMessage());

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
        }
    }

    private void handleGetBuffersByDeviceCommand(GetBuffersByDeviceUidCommand command, String key) {
        try {
            log.info("Processing GetBuffersByDeviceCommand for device UID: {}", command.getDeviceUid());

            List<BufferBLM> buffersBLM = bufferService.getBuffersByDevice(command.getClientUid(), command.getDeviceUid());
            List<BufferDTO> bufferDTOs = buffersBLM.stream()
                    .map(bufferConverter::toDTO)
                    .collect(Collectors.toList());

            GetBuffersByDeviceResponse response = GetBuffersByDeviceResponse.success(
                    command.getCorrelationId(),
                    bufferDTOs);

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

            log.info("Successfully processed GetBuffersByDeviceCommand for device: {}, found {} buffers",
                    command.getDeviceUid(), bufferDTOs.size());

        } catch (Exception e) {
            log.error("Error processing GetBuffersByDeviceCommand for device UID: {}", command.getDeviceUid(), e);

            GetBuffersByDeviceResponse response = GetBuffersByDeviceResponse.error(
                    command.getCorrelationId(),
                    e.getMessage());

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
        }
    }

    private void handleGetBuffersByConnectionSchemeCommand(GetBuffersByConnectionSchemeUidCommand command, String key) {
        try {
            log.info("Processing GetBuffersByConnectionSchemeCommand for connection scheme UID: {}", command.getConnectionSchemeUid());

            List<BufferBLM> buffersBLM = bufferService.getBuffersByConnectionScheme(command.getClientUid(), command.getConnectionSchemeUid());
            List<BufferDTO> bufferDTOs = buffersBLM.stream()
                    .map(bufferConverter::toDTO)
                    .collect(Collectors.toList());

            GetBuffersByConnectionSchemeResponse response = GetBuffersByConnectionSchemeResponse.success(
                    command.getCorrelationId(),
                    bufferDTOs);

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

            log.info("Successfully processed GetBuffersByConnectionSchemeCommand for connection scheme: {}, found {} buffers",
                    command.getConnectionSchemeUid(), bufferDTOs.size());

        } catch (Exception e) {
            log.error("Error processing GetBuffersByConnectionSchemeCommand for connection scheme UID: {}", command.getConnectionSchemeUid(), e);

            GetBuffersByConnectionSchemeResponse response = GetBuffersByConnectionSchemeResponse.error(
                    command.getCorrelationId(),
                    e.getMessage());

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
        }
    }

    private void handleHealthCheckCommand(HealthCheckCommand command, String key) {
        try {
            log.info("Processing HealthCheckCommand");

            var healthStatus = bufferService.getHealthStatus();

            HealthCheckResponse response = HealthCheckResponse.success(
                    command.getCorrelationId(),
                    healthStatus);

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

            log.info("Successfully processed HealthCheckCommand");

        } catch (Exception e) {
            log.error("Error processing HealthCheckCommand", e);

            HealthCheckResponse response = HealthCheckResponse.error(
                    command.getCorrelationId(),
                    e.getMessage());

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
        }
    }
}