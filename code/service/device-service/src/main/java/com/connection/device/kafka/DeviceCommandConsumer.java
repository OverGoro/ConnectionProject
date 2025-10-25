package com.connection.device.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.connection.device.events.commands.GetDeviceByUidCommand;
import com.connection.device.events.commands.GetDevicesByClientUid;
import com.connection.device.events.commands.HealthCheckCommand;
import com.connection.device.events.responses.GetDeviceByUidResponse;
import com.connection.device.events.responses.GetDevicesByClientResponse;
import com.connection.device.events.responses.HealthCheckResponse;
import com.connection.common.events.Command;
import com.connection.device.DeviceService;
import com.connection.device.converter.DeviceConverter;
import com.connection.device.model.DeviceBLM;
import com.connection.device.model.DeviceDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceCommandConsumer {
    
    @Qualifier("DeviceServiceKafkaImpl")
    private final DeviceService deviceService;

    private final DeviceConverter deviceConverter;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @KafkaListener(topics = "${app.kafka.topics.device-commands:device.commands}")
    public void handleDeviceCommand(ConsumerRecord<String, Command> record) {
        try {
            Command command = record.value();
            String key = record.key();

            log.info("Received device command: {} with key: {}", command.getClass().getSimpleName(), key);

            if (command instanceof GetDeviceByUidCommand) {
                GetDeviceByUidCommand getDeviceCommand = (GetDeviceByUidCommand) command;
                handleGetDeviceByUidCommand(getDeviceCommand, key);
            } else if (command instanceof GetDevicesByClientUid) {
                GetDevicesByClientUid getDevicesCommand = (GetDevicesByClientUid) command;
                handleGetDevicesByClientCommand(getDevicesCommand, key);
            } else if (command instanceof HealthCheckCommand) {
                HealthCheckCommand healthCommand = (HealthCheckCommand) command;
                handleHealthCheckCommand(healthCommand, key);
            } else {
                log.warn("Unknown device command type: {}", command.getClass().getCanonicalName());
            }

        } catch (Exception e) {
            log.error("Error processing device command: key={}", record.key(), e);
        }
    }

    private void handleGetDeviceByUidCommand(GetDeviceByUidCommand command, String key) {
        try {
            log.info("Processing GetDeviceByUidCommand for device UID: {}", command.getDeviceUid());

            DeviceBLM deviceBLM = deviceService.getDevice(command.getDeviceUid());
            DeviceDTO deviceDTO = deviceConverter.toDTO(deviceBLM);

            GetDeviceByUidResponse response = GetDeviceByUidResponse.success(
                    command.getCorrelationId(),
                    deviceDTO);

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.info("Successfully processed GetDeviceByUidCommand for device: {}", command.getDeviceUid());

        } catch (Exception e) {
            log.error("Error processing GetDeviceByUidCommand for device UID: {}", command.getDeviceUid(), e);

            GetDeviceByUidResponse response = GetDeviceByUidResponse.error(
                    command.getCorrelationId(),
                    e.getMessage());

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
        }
    }

    private void handleGetDevicesByClientCommand(GetDevicesByClientUid command, String key) {
        try {
            log.info("Processing GetDevicesByClientCommand for client UID: {}", command.getClientUid());

            List<DeviceBLM> devicesBLM = deviceService.getDevicesByClient(command.getClientUid());
            List<DeviceDTO> deviceDTOs = devicesBLM.stream()
                    .map(deviceConverter::toDTO)
                    .collect(Collectors.toList());

            GetDevicesByClientResponse response = GetDevicesByClientResponse.valid(
                    command.getCorrelationId(),
                    deviceDTOs);

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

            log.info("Successfully processed GetDevicesByClientCommand for client: {}, found {} devices",
                    command.getClientUid(), deviceDTOs.size());

        } catch (Exception e) {
            log.error("Error processing GetDevicesByClientCommand for client UID: {}", command.getClientUid(), e);

            GetDevicesByClientResponse response = GetDevicesByClientResponse.error(
                    command.getCorrelationId(),
                    e.getMessage());

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
        }
    }

    private void handleHealthCheckCommand(HealthCheckCommand command, String key) {
        try {
            log.info("Processing HealthCheckCommand");

            var healthStatus = deviceService.getHealthStatus();

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