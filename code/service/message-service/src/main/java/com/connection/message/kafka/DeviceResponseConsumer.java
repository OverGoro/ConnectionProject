// DeviceResponseConsumer.java
package com.connection.message.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.connection.common.events.CommandResponse;
import com.connection.device.events.responses.GetDeviceByUidResponse;
import com.connection.device.events.responses.GetDevicesByClientResponse;
import com.connection.device.events.responses.HealthCheckResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceResponseConsumer {

    private final TypedDeviceKafkaClient deviceKafkaClient;

    @KafkaListener(topics = "${app.kafka.topics.device-responses:device.responses}")
    public void handleDeviceResponse(ConsumerRecord<String, CommandResponse> record) {
        try {
            CommandResponse message = record.value();
            String correlationId = record.key();
            
            log.info("Received device response: correlationId={}", correlationId);
                
            if (message instanceof GetDeviceByUidResponse) {
                GetDeviceByUidResponse typedResponse = (GetDeviceByUidResponse) message;
                deviceKafkaClient.handleResponse(correlationId, typedResponse);
            } else if (message instanceof GetDevicesByClientResponse) {
                GetDevicesByClientResponse typedResponse = (GetDevicesByClientResponse) message;
                deviceKafkaClient.handleResponse(correlationId, typedResponse);
            } else if (message instanceof HealthCheckResponse) {
                HealthCheckResponse typedResponse = (HealthCheckResponse) message;
                deviceKafkaClient.handleResponse(correlationId, typedResponse);
            } else {
                log.warn("Unknown device response type for correlationId: {}", correlationId);
            }
            
        } catch (Exception e) {
            log.error("Error processing device response: correlationId={}", record.key(), e);
        }
    }
}