// package com.connection.message.kafka;

// import org.apache.kafka.clients.consumer.ConsumerRecord;
// import org.springframework.boot.context.event.ApplicationReadyEvent;
// import org.springframework.context.ApplicationListener;
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
// import org.springframework.kafka.listener.MessageListenerContainer;
// import org.springframework.stereotype.Component;

// import com.connection.buffer.events.responses.GetBufferByUidResponse;
// import com.connection.buffer.events.responses.GetBuffersByClientResponse;
// import com.connection.buffer.events.responses.GetBuffersByConnectionSchemeResponse;
// import com.connection.buffer.events.responses.GetBuffersByDeviceResponse;
// import com.connection.buffer.events.responses.HealthCheckResponse;
// import com.connection.common.events.CommandResponse;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class BufferResponseConsumer implements ApplicationListener<ApplicationReadyEvent> {

//     private final TypedBufferKafkaClient bufferKafkaClient;
//     private final KafkaListenerEndpointRegistry registry;

//     @KafkaListener(id = "dynamicBufferListener", topics = "#{@typedBufferKafkaClient.getInstanceReplyTopic()}")
//     public void handleBufferResponse(ConsumerRecord<String, CommandResponse> record) {
//         try {
//             CommandResponse message = record.value();
//             String correlationId = record.key();
            
//             log.info("Received buffer response from instance topic: correlationId={}, topic={}", 
//                     correlationId, record.topic());
                
//             if (message instanceof GetBufferByUidResponse) {
//                 GetBufferByUidResponse typedResponse = (GetBufferByUidResponse) message;
//                 bufferKafkaClient.handleResponse(correlationId, typedResponse);
//             } else if (message instanceof GetBuffersByClientResponse) {
//                 GetBuffersByClientResponse typedResponse = (GetBuffersByClientResponse) message;
//                 bufferKafkaClient.handleResponse(correlationId, typedResponse);
//             } else if (message instanceof GetBuffersByDeviceResponse) {
//                 GetBuffersByDeviceResponse typedResponse = (GetBuffersByDeviceResponse) message;
//                 bufferKafkaClient.handleResponse(correlationId, typedResponse);
//             } else if (message instanceof GetBuffersByConnectionSchemeResponse) {
//                 GetBuffersByConnectionSchemeResponse typedResponse = (GetBuffersByConnectionSchemeResponse) message;
//                 bufferKafkaClient.handleResponse(correlationId, typedResponse);
//             } else if (message instanceof HealthCheckResponse) {
//                 HealthCheckResponse typedResponse = (HealthCheckResponse) message;
//                 bufferKafkaClient.handleResponse(correlationId, typedResponse);
//             } else {
//                 log.warn("Unknown buffer response type for correlationId: {}", correlationId);
//             }
            
//         } catch (Exception e) {
//             log.error("Error processing buffer response: correlationId={}", record.key(), e);
//         }
//     }

//     @Override
//     public void onApplicationEvent(ApplicationReadyEvent event) {
//         try {
//             MessageListenerContainer container = registry.getListenerContainer("dynamicBufferListener");
//             if (container != null && !container.isRunning()) {
//                 container.start();
//                 log.info("Dynamic buffer response listener started for topic: {}", 
//                         bufferKafkaClient.getInstanceReplyTopic());
//             }
//         } catch (Exception e) {
//             log.error("Failed to start dynamic buffer listener", e);
//         }
//     }
// }