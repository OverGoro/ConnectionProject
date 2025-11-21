
// package com.service.buffer.kafka;

// import org.apache.kafka.clients.consumer.ConsumerRecord;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.kafka.core.KafkaTemplate;
// import org.springframework.stereotype.Component;

// import com.connection.buffer.events.commands.GetBufferByUidCommand;
// import com.connection.buffer.events.commands.GetBuffersByClientUidCommand;
// import com.connection.buffer.events.commands.GetBuffersByConnectionSchemeUidCommand;
// import com.connection.buffer.events.commands.GetBuffersByDeviceUidCommand;
// import com.connection.buffer.events.commands.HealthCheckCommand;
// import com.connection.buffer.events.responses.GetBufferByUidResponse;
// import com.connection.buffer.events.responses.GetBuffersByClientResponse;
// import com.connection.buffer.events.responses.GetBuffersByConnectionSchemeResponse;
// import com.connection.buffer.events.responses.GetBuffersByDeviceResponse;
// import com.connection.buffer.events.responses.HealthCheckResponse;
// import com.connection.common.events.Command;
// import com.service.buffer.BufferService;
// import com.connection.processing.buffer.converter.BufferConverter;
// import com.connection.processing.buffer.model.BufferBlm;
// import com.connection.processing.buffer.model.BufferDto;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// import java.util.List;
// import java.util.stream.Collectors;

// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class BufferCommandConsumer {

// @Qualifier("KafkaBufferService")
// private final BufferService bufferService;
// private final BufferConverter bufferConverter;
// private final KafkaTemplate<String, Object> kafkaTemplate;

// @KafkaListener(topics = "${app.kafka.topics.buffer-commands:buffer.commands}")
// public void handleBufferCommand(ConsumerRecord<String, Command> record) {
// try {
// Command command = record.value();
// String key = record.key();

// log.info("Received buffer command: {} with key: {}", command.getClass().getSimpleName(), key);

// if (command instanceof GetBufferByUidCommand) {
// GetBufferByUidCommand getBufferCommand = (GetBufferByUidCommand) command;
// handleGetBufferByUidCommand(getBufferCommand, key);
// } else if (command instanceof GetBuffersByClientUidCommand) {
// GetBuffersByClientUidCommand getBuffersCommand = (GetBuffersByClientUidCommand) command;
// handleGetBuffersByClientCommand(getBuffersCommand, key);
// } else if (command instanceof GetBuffersByDeviceUidCommand) {
// GetBuffersByDeviceUidCommand getBuffersCommand = (GetBuffersByDeviceUidCommand) command;
// handleGetBuffersByDeviceCommand(getBuffersCommand, key);
// } else if (command instanceof GetBuffersByConnectionSchemeUidCommand) {
// GetBuffersByConnectionSchemeUidCommand getBuffersCommand =
//  (GetBuffersByConnectionSchemeUidCommand) command;
// handleGetBuffersByConnectionSchemeCommand(getBuffersCommand, key);
// } else if (command instanceof HealthCheckCommand) {
// HealthCheckCommand healthCommand = (HealthCheckCommand) command;
// handleHealthCheckCommand(healthCommand, key);
// } else {
// log.warn("Unknown buffer command type: {}", command.getClass().getCanonicalName());
// }

// } catch (Exception e) {
// log.error("Error processing buffer command: key={}", record.key(), e);
// }
// }

// private void handleGetBufferByUidCommand(GetBufferByUidCommand command, String key) {
// try {
// log.info("Processing GetBufferByUidCommand for buffer UID: {}", command.getBufferUid());

// BufferBlm bufferBlm = bufferService.getBufferByUid(command.getBufferUid());
// BufferDto bufferDto = bufferConverter.toDto(bufferBlm);

// GetBufferByUidResponse response = GetBufferByUidResponse.success(
// command.getCorrelationId(),
// bufferDto);

// kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
// log.info("GetBufferByUid response sent to {}: correlationId={}",
// command.getReplyTopic(), command.getCorrelationId());

// } catch (Exception e) {
// log.error("Error processing GetBufferByUidCommand 
// for buffer UID: {}", command.getBufferUid(), e);

// GetBufferByUidResponse response = GetBufferByUidResponse.error(
// command.getCorrelationId(),
// e.getMessage());

// kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
// }
// }

// private void handleGetBuffersByClientCommand(GetBuffersByClientUidCommand command, String key) {
// try {
// log.info("Processing GetBuffersByClientCommand for client UID: {}", command.getClientUid());

// List<BufferBlm> buffersBlm = bufferService.getBuffersByClient(command.getClientUid());
// List<BufferDto> bufferDtos = buffersBlm.stream()
// .map(bufferConverter::toDto)
// .collect(Collectors.toList());

// GetBuffersByClientResponse response = GetBuffersByClientResponse.success(
// command.getCorrelationId(),
// bufferDtos);

// kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

// log.info("GetBufferByClient response sent to {}: correlationId={}",
// command.getReplyTopic(), command.getCorrelationId());

// } catch (Exception e) {
// log.error("Error processing GetBuffersByClientCommand for client UID: {}",
//  command.getClientUid(), e);

// GetBuffersByClientResponse response = GetBuffersByClientResponse.error(
// command.getCorrelationId(),
// e.getMessage());

// kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
// }
// }

// private void handleGetBuffersByDeviceCommand(GetBuffersByDeviceUidCommand command, String key) {
// try {
// log.info("Processing GetBuffersByDeviceCommand for device UID: {}", command.getDeviceUid());

// List<BufferBlm> buffersBlm = bufferService.getBuffersByDevice(command.getDeviceUid());
// List<BufferDto> bufferDtos = buffersBlm.stream()
// .map(bufferConverter::toDto)
// .collect(Collectors.toList());

// GetBuffersByDeviceResponse response = GetBuffersByDeviceResponse.success(
// command.getCorrelationId(),
// bufferDtos);

// kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

// log.info("GetBufferByDevice response sent to {}: correlationId={}",
// command.getReplyTopic(), command.getCorrelationId());

// } catch (Exception e) {
// log.error("Error processing GetBuffersByDeviceCommand for device UID: {}",
//  command.getDeviceUid(), e);

// GetBuffersByDeviceResponse response = GetBuffersByDeviceResponse.error(
// command.getCorrelationId(),
// e.getMessage());

// kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
// }
// }

// private void handleGetBuffersByConnectionSchemeCommand
// (GetBuffersByConnectionSchemeUidCommand command, String key) {
// try {
// log.info("Processing GetBuffersByConnectionSchemeCommand for connection scheme UID: {}",
// command.getConnectionSchemeUid());

// List<BufferBlm> buffersBlm =
//  bufferService.getBuffersByConnectionScheme(command.getConnectionSchemeUid());
// List<BufferDto> bufferDtos = buffersBlm.stream()
// .map(bufferConverter::toDto)
// .collect(Collectors.toList());

// GetBuffersByConnectionSchemeResponse response = GetBuffersByConnectionSchemeResponse.success(
// command.getCorrelationId(),
// bufferDtos);

// kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

// log.info("GetBufferByConnectionScheme response sent to {}: correlationId={}",
// command.getReplyTopic(), command.getCorrelationId());

// } catch (Exception e) {
// log.error("Error processing GetBuffersByConnectionSchemeCommand for connection scheme UID: {}",
// command.getConnectionSchemeUid(), e);

// GetBuffersByConnectionSchemeResponse response = GetBuffersByConnectionSchemeResponse.error(
// command.getCorrelationId(),
// e.getMessage());

// kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
// }
// }

// private void handleHealthCheckCommand(HealthCheckCommand command, String key) {
// try {
// log.info("Processing HealthCheckCommand");

// var healthStatus = bufferService.getHealthStatus();

// HealthCheckResponse response = HealthCheckResponse.success(
// command.getCorrelationId(),
// healthStatus);

// kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

// log.info("HealthCheck response sent to {}: correlationId={}",
// command.getReplyTopic(), command.getCorrelationId());

// } catch (Exception e) {
// log.error("Error processing HealthCheckCommand", e);

// HealthCheckResponse response = HealthCheckResponse.error(
// command.getCorrelationId(),
// e.getMessage());

// kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
// }
// }
// }