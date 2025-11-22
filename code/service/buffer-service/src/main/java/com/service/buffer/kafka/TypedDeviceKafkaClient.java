// package com.service.buffer.kafka;

// import java.util.List;
// import java.util.Map;
// import java.util.UUID;
// import java.util.concurrent.CompletableFuture;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.TimeUnit;

// import org.springframework.beans.factory.annotation.Autowired;
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

// import lombok.extern.slf4j.Slf4j;

// @Slf4j
// @Component
// public class TypedDeviceKafkaClient {

// @Autowired
// private KafkaTemplate<String, Object> kafkaTemplate;

// private final Map<String, PendingRequest<?>> pendingRequests = new ConcurrentHashMap<>();

// @Value("${app.kafka.topics.device-commands:device.commands}")
// private String deviceCommandsTopic;

// private final String instanceReplyTopic = "device.responses." + UUID.randomUUID().toString();

// private static class PendingRequest<T> {
// final CompletableFuture<T> future;
// final Class<T> responseType;

// PendingRequest(CompletableFuture<T> future, Class<T> responseType) {
// this.future = future;
// this.responseType = responseType;
// }
// }

// public CompletableFuture<GetDeviceByUidResponse>
//  getDeviceByUid(UUID deviceUid, String sourceService) {
// return sendRequest(
// GetDeviceByUidCommand.builder()
// .deviceUid(deviceUid)
// .sourceService(sourceService)
// .replyTopic(instanceReplyTopic) 
// .correlationId(DeviceEventUtils.generateCorrelationId())
// .build(),
// GetDeviceByUidResponse.class
// );
// }

// public CompletableFuture<GetDevicesByClientResponse> 
// getDevicesByClient(UUID clientUid, String sourceService) {
// return sendRequest(
// GetDevicesByClientUid.builder()
// .sourceService(sourceService)
// .clientUid(clientUid)
// .replyTopic(instanceReplyTopic) 
// .correlationId(DeviceEventUtils.generateCorrelationId())
// .build(),
// GetDevicesByClientResponse.class
// );
// }

// public CompletableFuture<HealthCheckResponse> healthCheck(String sourceService) {
// return sendRequest(
// HealthCheckCommand.builder()
// .sourceService(sourceService)
// .replyTopic(instanceReplyTopic) 
// .correlationId(DeviceEventUtils.generateCorrelationId())
// .build(),
// HealthCheckResponse.class
// );
// }


// public boolean deviceExistsAndBelongsToClient(UUID deviceUid, UUID clientUid) {
// try {
// GetDeviceByUidResponse response = getDeviceByUid(deviceUid, "buffer-service")
// .get(10, TimeUnit.SECONDS);
// return response.isSuccess() && response.getDeviceDto() != null && 
// response.getDeviceDto().getClientUuid().equals(clientUid.toString());
// } catch (Exception e) {
// log.error("Error checking device existence: {}", e.getMessage());
// return false;
// }
// }

// public List<UUID> getClientDeviceUids(UUID clientUid) {
// try {
// GetDevicesByClientResponse response = getDevicesByClient(clientUid, "buffer-service")
// .get(10, TimeUnit.SECONDS);

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

// private <T> CompletableFuture<T> sendRequest(Object command, Class<T> responseType) {
// String correlationId = extractCorrelationId(command);

// CompletableFuture<T> future = new CompletableFuture<>();
// pendingRequests.put(correlationId, new PendingRequest<>(future, responseType));


// future.orTimeout(30, TimeUnit.SECONDS).whenComplete((result, ex) -> {
// if (ex != null) {
// pendingRequests.remove(correlationId);
// log.warn("Device request timeout or error for correlationId: {}", correlationId);
// }
// });

// kafkaTemplate.send(deviceCommandsTopic, correlationId, command)
// .whenComplete((result, ex) -> {
// if (ex != null) {
// future.completeExceptionally(ex);
// pendingRequests.remove(correlationId);
// log.error("Failed to send device command: {}", ex.getMessage());
// } else {
// log.info("Device command sent successfully: correlationId={}, topic={}", 
// correlationId, deviceCommandsTopic);
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
// log.info("Device response: ", pendingRequest.toString());
// CompletableFuture<Object> future = (CompletableFuture<Object>) pendingRequest.future;
// future.complete(response);
// log.info("Device response handled successfully: correlationId={}", correlationId);
// } else {
// log.warn("Type mismatch for correlationId: {}. Expected: {}, Got: {}", 
// correlationId, pendingRequest.responseType, response.getClass());
// pendingRequest.future.completeExceptionally(
// new ClassCastException("Type mismatch in device response")
// );
// }
// } catch (Exception e) {
// pendingRequest.future.completeExceptionally(e);
// }
// } else {
// log.warn("Received device response for unknown correlationId: {}", correlationId);
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
// throw new IllegalArgumentException("Unsupported device command type: " + command.getClass());
// }
// }


// public String getInstanceReplyTopic() {
// log.info("Got teply topic: " + instanceReplyTopic);
// return instanceReplyTopic;
// }
// }