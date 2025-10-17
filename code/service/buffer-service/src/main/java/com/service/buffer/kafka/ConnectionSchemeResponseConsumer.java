package com.service.buffer.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import com.connection.common.events.CommandResponse;
import com.connection.scheme.events.responses.GetConnectionSchemeByUidResponse;
import com.connection.scheme.events.responses.HealthCheckResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionSchemeResponseConsumer implements ApplicationListener<ApplicationReadyEvent> {

    private final TypedConnectionSchemeKafkaClient connectionSchemeKafkaClient;
    private final KafkaListenerEndpointRegistry registry;

    @KafkaListener(id = "dynamicConnectionSchemeListener", 
                   topics = "#{@typedConnectionSchemeKafkaClient.getInstanceReplyTopic()}")
    public void handleConnectionSchemeResponse(ConsumerRecord<String, CommandResponse> record) {
        try {
            CommandResponse message = record.value();
            String correlationId = record.key();
            
            log.info("Received connection scheme response from instance topic: correlationId={}, topic={}", 
                    correlationId, record.topic());
                
            if (message instanceof GetConnectionSchemeByUidResponse) {
                GetConnectionSchemeByUidResponse typedResponse = (GetConnectionSchemeByUidResponse) message;
                connectionSchemeKafkaClient.handleResponse(correlationId, typedResponse);
            } else if (message instanceof HealthCheckResponse) {
                HealthCheckResponse typedResponse = (HealthCheckResponse) message;
                connectionSchemeKafkaClient.handleResponse(correlationId, typedResponse);
            } else {
                log.warn("Unknown connection scheme response type for correlationId: {}", correlationId);
            }
            
        } catch (Exception e) {
            log.error("Error processing connection scheme response: correlationId={}", record.key(), e);
        }
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            MessageListenerContainer container = registry.getListenerContainer("dynamicConnectionSchemeListener");
            if (container != null && !container.isRunning()) {
                container.start();
                log.info("Dynamic connection scheme response listener started for topic: {}", 
                        connectionSchemeKafkaClient.getInstanceReplyTopic());
            }
        } catch (Exception e) {
            log.error("Failed to start dynamic connection scheme listener", e);
        }
    }
}