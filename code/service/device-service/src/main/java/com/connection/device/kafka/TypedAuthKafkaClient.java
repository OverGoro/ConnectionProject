// package com.connection.device.kafka;

// import com.connection.auth.events.AuthEventConstants;
// import com.connection.auth.events.AuthEventUtils;
// import com.connection.auth.events.commands.ExtractClientUidCommand;
// import com.connection.auth.events.commands.HealthCheckCommand;
// import com.connection.auth.events.commands.ValidateTokenCommand;
// import com.connection.auth.events.responses.ClientUidResponse;
// import com.connection.auth.events.responses.HealthCheckResponse;
// import com.connection.auth.events.responses.TokenValidationResponse;
// import java.util.Date;
// import java.util.Map;
// import java.util.UUID;
// import java.util.concurrent.CompletableFuture;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.TimeUnit;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.kafka.core.KafkaTemplate;
// import org.springframework.stereotype.Component;

// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class TypedAuthKafkaClient {

// private final KafkaTemplate<String, Object> kafkaTemplate;

// private final Map<String, PendingRequest<?>> pendingRequests = new ConcurrentHashMap<>();

// private final String instanceReplyTopic = "auth.responses." + UUID.randomUUID().toString();

// private static class PendingRequest<T> {
// final CompletableFuture<T> future;
// final Class<T> responseType;

// PendingRequest(CompletableFuture<T> future, Class<T> responseType) {
// this.future = future;
// this.responseType = responseType;
// }
// }

// public CompletableFuture<TokenValidationResponse>
//  validateToken(String token, String sourceService) {
// return sendRequest(
// ValidateTokenCommand.builder()
// .token(token)
// .tokenType(ValidateTokenCommand.TokenType.ACCESS)
// .sourceService(sourceService)
// .replyTopic(instanceReplyTopic)
// .correlationId(AuthEventUtils.generateCorrelationId())
// .build(),
// TokenValidationResponse.class);
// }

// public CompletableFuture<ClientUidResponse> getClientUid(String token, String sourceService) {
// return sendRequest(
// ExtractClientUidCommand.builder()
// .token(token)
// .tokenType(ExtractClientUidCommand.TokenType.ACCESS)
// .sourceService(sourceService)
// .replyTopic(instanceReplyTopic)
// .correlationId(AuthEventUtils.generateCorrelationId())
// .build(),
// ClientUidResponse.class);
// }

// public CompletableFuture<HealthCheckResponse> healthCheck(String sourceService) {
// return sendRequest(
// HealthCheckCommand.builder()
// .eventId(UUID.randomUUID().toString())
// .sourceService(sourceService)
// .timestamp(new Date().toInstant())
// .replyTopic(instanceReplyTopic)
// .correlationId(AuthEventUtils.generateCorrelationId())
// .commandType(AuthEventConstants.COMMAND_HEALTH_CHECK)
// .build(),
// HealthCheckResponse.class);
// }

// private <T> CompletableFuture<T> sendRequest(Object command, Class<T> responseType) {
// String correlationId;

// if (command instanceof ValidateTokenCommand) {
// correlationId = ((ValidateTokenCommand) command).getCorrelationId();
// } else if (command instanceof ExtractClientUidCommand) {
// correlationId = ((ExtractClientUidCommand) command).getCorrelationId();
// } else if (command instanceof HealthCheckCommand) {
// correlationId = ((HealthCheckCommand) command).getCorrelationId();
// } else {
// throw new IllegalArgumentException("Unsupported command type: " + command.getClass());
// }

// CompletableFuture<T> future = new CompletableFuture<>();
// pendingRequests.put(correlationId, new PendingRequest<>(future, responseType));

// future.orTimeout(30, TimeUnit.SECONDS).whenComplete((result, ex) -> {
// if (ex != null) {
// pendingRequests.remove(correlationId);
// log.warn("Request timeout or error for correlationId: {}", correlationId);
// }
// });

// kafkaTemplate.send(AuthEventConstants.AUTH_COMMANDS_TOPIC, correlationId, command)
// .whenComplete((result, ex) -> {
// if (ex != null) {
// future.completeExceptionally(ex);
// pendingRequests.remove(correlationId);
// log.error("Failed to send command: {}", ex.getMessage());
// } else {
// log.info("Command sent successfully: correlationId={}, topic={}",
// correlationId, AuthEventConstants.AUTH_COMMANDS_TOPIC);
// }
// });

// return future;
// }

// @SuppressWarnings("unchecked")
// public void handleResponse(String correlationId, Object response) {
// PendingRequest<?> pendingRequest = pendingRequests.remove(correlationId);
// if (pendingRequest != null) {
// try {
// if (pendingRequest.responseType.isInstance(response)) {
// CompletableFuture<Object> future = (CompletableFuture<Object>) pendingRequest.future;
// future.complete(response);
// log.info("Response handled successfully: correlationId={}", correlationId);
// } else {
// log.warn("Type mismatch for correlationId: {}. Expected: {}, Got: {}",
// correlationId, pendingRequest.responseType, response.getClass());
// pendingRequest.future.completeExceptionally(
// new ClassCastException("Type mismatch in response"));
// }
// } catch (Exception e) {
// pendingRequest.future.completeExceptionally(e);
// log.error("Error handling response: correlationId={}", correlationId, e);
// }
// } else {
// log.warn("Received response for unknown correlationId: {}", correlationId);
// }
// }

// public String getInstanceReplyTopic() {
// return instanceReplyTopic;
// }
// }