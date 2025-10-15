// ConnectionSchemeResponseConsumer.java
package com.connection.message.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.connection.common.events.CommandResponse;
import com.connection.scheme.events.responses.GetConnectionSchemeByUidResponse;
import com.connection.scheme.events.responses.HealthCheckResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionSchemeResponseConsumer {

    private final TypedConnectionSchemeKafkaClient connectionSchemeKafkaClient;

    @KafkaListener(topics = "${app.kafka.topics.connection-scheme-responses:connection-scheme.responses}")
    public void handleConnectionSchemeResponse(ConsumerRecord<String, CommandResponse> record) {
        try {
            CommandResponse message = record.value();
            String correlationId = record.key();
            
            log.info("Received connection scheme response: correlationId={}", correlationId);
                
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
}