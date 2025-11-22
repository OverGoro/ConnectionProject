// // TypedDeviceKafkaClient.java
// package com.connection.message.kafka;

// import java.util.List;
// import java.util.Map;
// import java.util.UUID;
// import java.util.concurrent.CompletableFuture;
// import java.util.concurrent.ConcurrentHashMap;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.kafka.core.KafkaTemplate;
// import org.springframework.stereotype.Component;

// import com.connection.device.events.DeviceEventUtils;
// import com.connection.device.events.commands.GetDeviceByUidCommand;
// import com.connection.device.events.commands.GetDevicesByClientUid;
// import com.connection.device.events.commands.HealthCheckCommand;
// import com.connection.device.events.responses.GetDeviceByUidResponse;
// import com.connection.device.events.responses.GetDevicesByClientResponse;
// import com.connection.device.events.responses.HealthCheckResponse;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class TypedDeviceKafkaClient {

// private final KafkaTemplate<String, Object> kafkaTemplate;
// private final Map<String, PendingRequest<?>> pendingRequests =
// new ConcurrentHashMap<>();
// private final String instanceReplyTopic =
// "device.responses." + UUID.randomUUID().toString();
// @Value("${app.kafka.topics.auth-commands:auth.commands}")
// String authcommands;
// @Value("${app.kafka.topics.auth-responses:auth.responses}")
// String authresponses;
// @Value("${app.kafka.topics.device-auth-commands:device.auth.commands}")
// String deviceauthcommands;
// @Value("${app.kafka.topics.device-auth-responses:device.auth.responses}")
// String deviceauthresponses;
// @Value("${app.kafka.topics.device-commands:device.commands}")
// String devicecommands;
// @Value("${app.kafka.topics.device-responses:device.responses}")
// String deviceresponses;
// @Value("${app.kafka.topics.connection-scheme-commands:connection-scheme.commands}")
// String connectionschemecommands;
// @Value("${app.kafka.topics.connection-scheme-responses:connection-scheme.responses}")
// String connectionschemeresponses;
// @Value("${app.kafka.topics.buffer-commands:buffer.commands}")
// String buffercommands;
// @Value("${app.kafka.topics.buffer-responses:buffer.responses}")
// String bufferresponses;
// @Value("${app.kafka.topics.message-commands:message.commands}")
// String messagecommands;
// @Value("${app.kafka.topics.message-responses:message.responses}")
// String messageresponses;
// @Value("${app.kafka.topics.message-events:message.events}")
// String messageevents;

// private static class PendingRequest<T> {
// final CompletableFuture<T> future;
// final Class<T> responseType;

// PendingRequest(CompletableFuture<T> future, Class<T> responseType) {
// this.future = future;
// this.responseType = responseType;
// }
// }

// public CompletableFuture<GetDeviceByUidResponse> getDeviceByUid(
// UUID deviceUid, String sourceService) {
// return sendRequest(GetDeviceByUidCommand.builder().deviceUid(deviceUid)
// .sourceService(sourceService).replyTopic(instanceReplyTopic)
// .correlationId(DeviceEventUtils.generateCorrelationId())
// .build(), GetDeviceByUidResponse.class);
// }

// public CompletableFuture<GetDevicesByClientResponse> getDevicesByClient(
// UUID clientUid, String sourceService) {
// return sendRequest(GetDevicesByClientUid.builder().clientUid(clientUid)
// .sourceService(sourceService).replyTopic(instanceReplyTopic)
// .correlationId(DeviceEventUtils.generateCorrelationId())
// .build(), GetDevicesByClientResponse.class);
// }

// public CompletableFuture<HealthCheckResponse> healthCheck(
// String sourceService) {
// return sendRequest(HealthCheckCommand.builder()
// .sourceService(sourceService).replyTopic(instanceReplyTopic)
// .correlationId(DeviceEventUtils.generateCorrelationId())
// .build(), HealthCheckResponse.class);
// }

// // Вспомогательные методы для удобства
// public boolean deviceExists(UUID deviceUid) {
// try {
// GetDeviceByUidResponse response =
// getDeviceByUid(deviceUid, "buffer-service").get(10,
// java.util.concurrent.TimeUnit.SECONDS);
// return response.isSuccess() && response.getDeviceDto() != null;
// } catch (Exception e) {
// log.error("Error checking device existence: {}", e.getMessage());
// return false;
// }
// }

// // В TypedDeviceKafkaClient добавим метод:
// public boolean deviceExistsAndBelongsToClient(UUID deviceUid,
// UUID clientUid) {
// try {
// GetDeviceByUidResponse response =
// getDeviceByUid(deviceUid, "message-service").get(10,
// java.util.concurrent.TimeUnit.SECONDS);
// return response.isSuccess() && response.getDeviceDto() != null
// && response.getDeviceDto().getClientUuid()
// .equals(clientUid.toString());
// } catch (Exception e) {
// log.error("Error checking device existence: {}", e.getMessage());
// return false;
// }
// }

// public List<UUID> getClientDeviceUids(UUID clientUid) {
// try {
// GetDevicesByClientResponse response =
// getDevicesByClient(clientUid, "buffer-service").get(10,
// java.util.concurrent.TimeUnit.SECONDS);

// if (response.isSuccess() && response.getDeviceDtos() != null) {
// return response.getDeviceDtos().stream()
// .map(device -> UUID.fromString(device.getUid()))
// .collect(java.util.stream.Collectors.toList());
// }
// return List.of();
// } catch (Exception e) {
// log.error("Error getting client devices: {}", e.getMessage());
// return List.of();
// }
// }

// private <T> CompletableFuture<T> sendRequest(Object command,
// Class<T> responseType) {
// String correlationId = extractCorrelationId(command);

// CompletableFuture<T> future = new CompletableFuture<>();
// pendingRequests.put(correlationId,
// new PendingRequest<>(future, responseType));

// kafkaTemplate.send(devicecommands, correlationId, command)
// .whenComplete((result, ex) -> {
// if (ex != null) {
// future.completeExceptionally(ex);
// pendingRequests.remove(correlationId);
// log.error("Failed to send device command: {}",
// ex.getMessage());
// }
// });

// return future;
// }

// @SuppressWarnings("unchecked")
// public void handleResponse(String correlationId, Object response) {
// PendingRequest<?> pendingRequest =
// pendingRequests.remove(correlationId);
// if (pendingRequest != null) {
// try {
// if (pendingRequest.responseType.isInstance(response)) {
// CompletableFuture<Object> future =
// (CompletableFuture<Object>) pendingRequest.future;
// future.complete(response);
// } else {
// log.warn(
// "Type mismatch for correlationId: {}. Expected: {}, Got: {}",
// correlationId, pendingRequest.responseType,
// response.getClass());
// pendingRequest.future
// .completeExceptionally(new ClassCastException(
// "Type mismatch in device response"));
// }
// } catch (Exception e) {
// pendingRequest.future.completeExceptionally(e);
// }
// } else {
// log.warn("Received device response for unknown correlationId: {}",
// correlationId);
// }
// }

// private String extractCorrelationId(Object command) {
// if (command instanceof GetDeviceByUidCommand) {
// return ((GetDeviceByUidCommand) command).getCorrelationId();
// } else if (command instanceof GetDevicesByClientUid) {
// return ((GetDevicesByClientUid) command).getCorrelationId();
// } else if (command instanceof HealthCheckCommand) {
// return ((HealthCheckCommand) command).getCorrelationId();
// } else {
// throw new IllegalArgumentException(
// "Unsupported device command type: " + command.getClass());
// }
// }

// public String getInstanceReplyTopic() {
// return instanceReplyTopic;
// }
// }
