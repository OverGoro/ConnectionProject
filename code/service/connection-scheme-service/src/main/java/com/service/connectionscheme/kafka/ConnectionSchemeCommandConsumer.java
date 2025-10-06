package com.service.connectionscheme.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.connection.scheme.events.commands.GetConnectionSchemeByUidCommand;
import com.connection.scheme.events.commands.GetConnectionSchemesByClientUid;
import com.connection.scheme.events.commands.HealthCheckCommand;
import com.connection.scheme.events.responses.GetConnectionSchemeByUidResponse;
import com.connection.scheme.events.responses.GetConnectionSchemesByClientResponse;
import com.connection.scheme.events.responses.HealthCheckResponse;
import com.connection.common.events.Command;
import com.service.connectionscheme.ConnectionSchemeService;
import com.connection.scheme.converter.ConnectionSchemeConverter;
import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionSchemeCommandConsumer {

    private final ConnectionSchemeService connectionSchemeService;
    private final ConnectionSchemeConverter connectionSchemeConverter;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "${app.kafka.topics.connection-scheme-commands:connection-scheme.commands}")
    public void handleConnectionSchemeCommand(ConsumerRecord<String, Command> record) {
        try {
            Command command = record.value();
            String key = record.key();

            log.info("Received connection scheme command: {} with key: {}", command.getClass().getSimpleName(), key);

            if (command instanceof GetConnectionSchemeByUidCommand) {
                GetConnectionSchemeByUidCommand getSchemeCommand = (GetConnectionSchemeByUidCommand) command;
                handleGetConnectionSchemeByUidCommand(getSchemeCommand, key);
            } else if (command instanceof GetConnectionSchemesByClientUid) {
                GetConnectionSchemesByClientUid getSchemesCommand = (GetConnectionSchemesByClientUid) command;
                handleGetConnectionSchemesByClientCommand(getSchemesCommand, key);
            } else if (command instanceof HealthCheckCommand) {
                HealthCheckCommand healthCommand = (HealthCheckCommand) command;
                handleHealthCheckCommand(healthCommand, key);
            } else {
                log.warn("Unknown connection scheme command type: {}", command.getClass().getCanonicalName());
            }

        } catch (Exception e) {
            log.error("Error processing connection scheme command: key={}", record.key(), e);
        }
    }

    private void handleGetConnectionSchemeByUidCommand(GetConnectionSchemeByUidCommand command, String key) {
        try {
            log.info("Processing GetConnectionSchemeByUidCommand for scheme UID: {}", command.getConnectionSchemeUid());

            ConnectionSchemeBLM schemeBLM = connectionSchemeService.getSchemeByUid(command.getClientUid(), command.getConnectionSchemeUid());
            ConnectionSchemeDTO schemeDTO = connectionSchemeConverter.toDTO(schemeBLM);

            GetConnectionSchemeByUidResponse response = GetConnectionSchemeByUidResponse.success(
                    command.getCorrelationId(),
                    schemeDTO);

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
            log.info("Successfully processed GetConnectionSchemeByUidCommand for scheme: {}", command.getConnectionSchemeUid());

        } catch (Exception e) {
            log.error("Error processing GetConnectionSchemeByUidCommand for scheme UID: {}", command.getConnectionSchemeUid(), e);

            GetConnectionSchemeByUidResponse response = GetConnectionSchemeByUidResponse.error(
                    command.getCorrelationId(),
                    e.getMessage());

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
        }
    }

    private void handleGetConnectionSchemesByClientCommand(GetConnectionSchemesByClientUid command, String key) {
        try {
            log.info("Processing GetConnectionSchemesByClientCommand for client UID: {}", command.getClientUid());

            List<ConnectionSchemeBLM> schemesBLM = connectionSchemeService.getSchemesByClient(command.getClientUid());
            List<ConnectionSchemeDTO> schemeDTOs = schemesBLM.stream()
                    .map(connectionSchemeConverter::toDTO)
                    .collect(Collectors.toList());

            GetConnectionSchemesByClientResponse response = GetConnectionSchemesByClientResponse.valid(
                    command.getCorrelationId(),
                    schemeDTOs);

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

            log.info("Successfully processed GetConnectionSchemesByClientCommand for client: {}, found {} schemes",
                    command.getClientUid(), schemeDTOs.size());

        } catch (Exception e) {
            log.error("Error processing GetConnectionSchemesByClientCommand for client UID: {}", command.getClientUid(), e);

            GetConnectionSchemesByClientResponse response = GetConnectionSchemesByClientResponse.error(
                    command.getCorrelationId(),
                    e.getMessage());

            kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
        }
    }

    private void handleHealthCheckCommand(HealthCheckCommand command, String key) {
        try {
            log.info("Processing HealthCheckCommand");

            var healthStatus = connectionSchemeService.getHealthStatus();

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