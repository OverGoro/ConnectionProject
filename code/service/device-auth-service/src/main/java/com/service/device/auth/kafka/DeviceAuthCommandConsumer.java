// // DeviceAuthCommandConsumer.java
// package com.service.device.auth.kafka;

// import java.util.UUID;

// import org.apache.kafka.clients.consumer.ConsumerRecord;
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.kafka.core.KafkaTemplate;
// import org.springframework.stereotype.Component;

// import com.connection.device.auth.events.commands.ExtractDeviceUidCommand;
// import com.connection.device.auth.events.commands.HealthCheckCommand;
// import com.connection.device.auth.events.commands.ValidateTokenCommand;
// import com.connection.device.auth.events.domain.TokenValidatedEvent;
// import com.connection.device.auth.events.responses.DeviceUidResponse;
// import com.connection.device.auth.events.responses.HealthCheckResponse;
// import com.connection.device.auth.events.responses.TokenValidationResponse;
// import com.connection.common.events.Command;
// import com.connection.device.token.converter.DeviceAccessTokenConverter;
// import com.connection.device.token.converter.DeviceTokenConverter;
// import com.connection.device.token.model.DeviceAccessTokenBLM;
// import com.connection.device.token.model.DeviceTokenBLM;
// import com.connection.device.token.util.TokenUtils;
// import com.service.device.auth.DeviceAuthService;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class DeviceAuthCommandConsumer {

//     private final DeviceAuthService deviceAuthService;
//     private final DeviceTokenConverter deviceTokenConverter;
//     private final DeviceAccessTokenConverter deviceAccessTokenConverter;
//     private final KafkaTemplate<String, Object> kafkaTemplate;

//     @KafkaListener(topics = "${app.kafka.topics.device-auth-commands:device.auth.commands}")
//     public void handleDeviceAuthCommand(ConsumerRecord<String, Command> record) {
//         try {
//             Command command = record.value();
//             String key = record.key();

//             if (command instanceof ValidateTokenCommand) {
//                 ValidateTokenCommand validateCommand = (ValidateTokenCommand) command;
//                 handleValidateTokenCommand(validateCommand, key);
//             } else if (command instanceof ExtractDeviceUidCommand) {
//                 ExtractDeviceUidCommand extractCommand = (ExtractDeviceUidCommand) command;
//                 handleExtractDeviceUidCommand(extractCommand, key);
//             } else if (command instanceof HealthCheckCommand) {
//                 HealthCheckCommand healthCommand = (HealthCheckCommand) command;
//                 handleHealthCheckCommand(healthCommand, key);
//             } else {
//                 log.warn("Unknown device auth command type: {}", command.getClass().getCanonicalName());
//             }

//         } catch (Exception e) {
//             log.error("Error processing device auth command: key={}", record.key(), e);
//         }
//     }

//     private void handleValidateTokenCommand(ValidateTokenCommand command, String key) {
//         try {
//             UUID deviceUid = null;
//             boolean isValid = false;
//             String tokenType = command.getTokenType().name();

//             if (command.getTokenType() == ValidateTokenCommand.TokenType.ACCESS) {
//                 // Валидация device access token (только JWT)
//                 DeviceAccessTokenBLM tokenBLM = deviceAccessTokenConverter.toBLM(
//                     new com.connection.device.token.model.DeviceAccessTokenDTO(command.getToken()));
//                 deviceAuthService.validateDeviceAccessToken(tokenBLM);
//                 deviceUid = TokenUtils.extractDeviceUidFromDeviceToken(tokenBLM.getToken());
//                 isValid = true;
//             } else {
//                 // Валидация device token (полная валидация)
//                 DeviceTokenBLM tokenBLM = deviceTokenConverter.toBLM(
//                     new com.connection.device.token.model.DeviceTokenDTO(command.getToken()));
//                 deviceAuthService.validateDeviceToken(tokenBLM);
//                 deviceUid = tokenBLM.getDeviceUid();
//                 isValid = true;
//             }

//             // Отправка события валидации
//             TokenValidatedEvent event = new TokenValidatedEvent(deviceUid, isValid, tokenType);
//             kafkaTemplate.send("device.auth.events", event);

//             // Отправка ответа
//             TokenValidationResponse response = TokenValidationResponse.valid(
//                 command.getCorrelationId(), deviceUid, tokenType);
//             kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

//             log.info("Device token validation successful: deviceUid={}, type={}", deviceUid, tokenType);

//         } catch (Exception e) {
//             TokenValidationResponse response = TokenValidationResponse.error(
//                 command.getCorrelationId(), e.getMessage());
//             kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
//             log.error("Device token validation failed: {}", e.getMessage());
//         }
//     }

//     private void handleExtractDeviceUidCommand(ExtractDeviceUidCommand command, String key) {
//         try {
//             UUID deviceUid = null;
//             String tokenType = command.getTokenType().name();

//             if (command.getTokenType() == ExtractDeviceUidCommand.TokenType.ACCESS) {
//                 DeviceAccessTokenBLM tokenBLM = deviceAccessTokenConverter.toBLM(
//                     new com.connection.device.token.model.DeviceAccessTokenDTO(command.getToken()));
//                 deviceAuthService.validateDeviceAccessToken(tokenBLM);
//                 deviceUid = TokenUtils.extractDeviceUidFromDeviceToken(tokenBLM.getToken());
//             } else {
//                 DeviceTokenBLM tokenBLM = deviceTokenConverter.toBLM(
//                     new com.connection.device.token.model.DeviceTokenDTO(command.getToken()));
//                 deviceAuthService.validateDeviceToken(tokenBLM);
//                 deviceUid = tokenBLM.getDeviceUid();
//             }

//             DeviceUidResponse response = DeviceUidResponse.success(
//                 command.getCorrelationId(), deviceUid, tokenType);
//             kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);

//             log.info("Device UID extracted: {}, type={}", deviceUid, tokenType);

//         } catch (Exception e) {
//             DeviceUidResponse response = DeviceUidResponse.error(
//                 command.getCorrelationId(), e.getMessage());
//             kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
//             log.error("Device UID extraction failed: {}", e.getMessage());
//         }
//     }

//     private void handleHealthCheckCommand(HealthCheckCommand command, String key) {
//         try {
//             java.util.Map<String, Object> healthStatus = java.util.Map.of(
//                 "status", "OK",
//                 "service", "device-auth-service",
//                 "timestamp", System.currentTimeMillis());

//             HealthCheckResponse response = HealthCheckResponse.success(
//                 command.getCorrelationId(), healthStatus);

//             kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
//             log.info("Device auth health check completed successfully");

//         } catch (Exception e) {
//             HealthCheckResponse response = HealthCheckResponse.error(
//                 command.getCorrelationId(), e.getMessage());
//             kafkaTemplate.send(command.getReplyTopic(), command.getCorrelationId(), response);
//             log.error("Device auth health check failed: {}", e.getMessage());
//         }
//     }
// }