package com.connection.message.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.connection.buffer.events.responses.GetBufferByUidResponse;
import com.connection.buffer.events.responses.GetBuffersByClientResponse;
import com.connection.buffer.events.responses.GetBuffersByConnectionSchemeResponse;
import com.connection.buffer.events.responses.GetBuffersByDeviceResponse;
import com.connection.buffer.events.responses.HealthCheckResponse;
import com.connection.common.events.CommandResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BufferResponseConsumer {

    private final TypedBufferKafkaClient bufferKafkaClient;

    @KafkaListener(topics = "${app.kafka.topics.buffer-responses:buffer.responses}")
    public void handleBufferResponse(ConsumerRecord<String, CommandResponse> record) {
        try {
            CommandResponse message = record.value();
            String correlationId = record.key();
            
            log.info("Received buffer response: correlationId={}", correlationId);
                
            if (message instanceof GetBufferByUidResponse) {
                GetBufferByUidResponse typedResponse = (GetBufferByUidResponse) message;
                bufferKafkaClient.handleResponse(correlationId, typedResponse);
            } else if (message instanceof GetBuffersByClientResponse) {
                GetBuffersByClientResponse typedResponse = (GetBuffersByClientResponse) message;
                bufferKafkaClient.handleResponse(correlationId, typedResponse);
            } else if (message instanceof GetBuffersByDeviceResponse) {
                GetBuffersByDeviceResponse typedResponse = (GetBuffersByDeviceResponse) message;
                bufferKafkaClient.handleResponse(correlationId, typedResponse);
            } else if (message instanceof GetBuffersByConnectionSchemeResponse) {
                GetBuffersByConnectionSchemeResponse typedResponse = (GetBuffersByConnectionSchemeResponse) message;
                bufferKafkaClient.handleResponse(correlationId, typedResponse);
            } else if (message instanceof HealthCheckResponse) {
                HealthCheckResponse typedResponse = (HealthCheckResponse) message;
                bufferKafkaClient.handleResponse(correlationId, typedResponse);
            } else {
                log.warn("Unknown buffer response type for correlationId: {}", correlationId);
            }
            
        } catch (Exception e) {
            log.error("Error processing buffer response: correlationId={}", record.key(), e);
        }
    }
}