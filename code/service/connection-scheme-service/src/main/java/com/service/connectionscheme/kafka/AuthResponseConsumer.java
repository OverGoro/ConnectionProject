package com.service.connectionscheme.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
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
public class AuthResponseConsumer implements ApplicationListener<ApplicationReadyEvent> {

    private final TypedAuthKafkaClient authKafkaClient;
    private final KafkaListenerEndpointRegistry registry;

    @KafkaListener(id = "dynamicAuthListener", topics = "#{@typedAuthKafkaClient.getInstanceReplyTopic()}")
    public void handleAuthResponse(ConsumerRecord<String, CommandResponse> record) {
        try {
            CommandResponse message = record.value();
            String correlationId = record.key();
            
            log.info("Received auth response from instance topic: correlationId={}, topic={}", 
                    correlationId, record.topic());
                
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

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            MessageListenerContainer container = registry.getListenerContainer("dynamicAuthListener");
            if (container != null && !container.isRunning()) {
                container.start();
                log.info("Dynamic auth response listener started for topic: {}", 
                        authKafkaClient.getInstanceReplyTopic());
            }
        } catch (Exception e) {
            log.error("Failed to start dynamic auth listener", e);
        }
    }
}