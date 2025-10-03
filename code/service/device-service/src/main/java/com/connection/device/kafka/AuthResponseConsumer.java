package com.connection.device.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.connection.auth.events.responses.TokenValidationResponse;
import com.connection.common.events.CommandResponse;
import com.connection.auth.events.responses.ClientUidResponse;
import com.connection.auth.events.responses.HealthCheckResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthResponseConsumer {

    private final TypedAuthKafkaClient authKafkaClient;

    @KafkaListener(topics = "${app.kafka.topics.auth-responses:auth.responses}")
    public void handleAuthResponse(ConsumerRecord<String, CommandResponse> record) {
        try {
            CommandResponse message = record.value();
            String correlationId = record.key();
            
            log.info("Received auth response: correlationId={}", correlationId);
                
            if (message instanceof TokenValidationResponse) {
                TokenValidationResponse typedResponse = (TokenValidationResponse) message;
                authKafkaClient.handleResponse(correlationId, typedResponse);
            } else if (message instanceof ClientUidResponse) {
                ClientUidResponse typedResponse = (ClientUidResponse) message;
                authKafkaClient.handleResponse(correlationId, typedResponse);
            } else if (message instanceof HealthCheckResponse) {
                HealthCheckResponse typedResponse = (HealthCheckResponse) message;
                authKafkaClient.handleResponse(correlationId, typedResponse);
            } else {
                log.warn("Unknown response type for correlationId: {}", correlationId);
            }
            
        } catch (Exception e) {
            log.error("Error processing auth response: correlationId={}", record.key(), e);
        }
    }
}