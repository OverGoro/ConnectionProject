package com.connection.message.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.connection.device.auth.events.responses.TokenValidationResponse;
import com.connection.common.events.CommandResponse;
import com.connection.device.auth.events.responses.DeviceUidResponse;
import com.connection.device.auth.events.responses.HealthCheckResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceAuthResponseConsumer {

    private final TypedDeviceAuthKafkaClient deviceAuthKafkaClient;

    @KafkaListener(topics = "${app.kafka.topics.device-auth-responses:device.auth.responses}")
    public void handleDeviceAuthResponse(ConsumerRecord<String, CommandResponse> record) {
        try {
            CommandResponse message = record.value();
            String correlationId = record.key();
            
            log.info("Received device auth response: correlationId={}", correlationId);
                
            if (message instanceof TokenValidationResponse) {
                TokenValidationResponse typedResponse = (TokenValidationResponse) message;
                deviceAuthKafkaClient.handleResponse(correlationId, typedResponse);
            } else if (message instanceof DeviceUidResponse) {
                DeviceUidResponse typedResponse = (DeviceUidResponse) message;
                deviceAuthKafkaClient.handleResponse(correlationId, typedResponse);
            } else if (message instanceof HealthCheckResponse) {
                HealthCheckResponse typedResponse = (HealthCheckResponse) message;
                deviceAuthKafkaClient.handleResponse(correlationId, typedResponse);
            } else {
                log.warn("Unknown device auth response type for correlationId: {}", correlationId);
            }
            
        } catch (Exception e) {
            log.error("Error processing device auth response: correlationId={}", record.key(), e);
        }
    }
}