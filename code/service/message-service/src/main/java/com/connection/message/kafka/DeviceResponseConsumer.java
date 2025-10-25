package com.connection.message.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
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
public class DeviceResponseConsumer implements ApplicationListener<ApplicationReadyEvent> {

    private final TypedDeviceKafkaClient deviceKafkaClient;
    private final KafkaListenerEndpointRegistry registry;

    @KafkaListener(id = "dynamicDeviceListener", 
                   topics = "#{@typedDeviceKafkaClient.getInstanceReplyTopic()}")
    public void handleDeviceResponse(ConsumerRecord<String, CommandResponse> record) {
        try {
            CommandResponse message = record.value();
            String correlationId = record.key();
            
            log.info("Received device response from instance topic: correlationId={}, topic={}", 
                    correlationId, record.topic());
                
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

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            MessageListenerContainer container = registry.getListenerContainer("dynamicDeviceListener");
            if (container != null && !container.isRunning()) {
                container.start();
                log.info("Dynamic device response listener started for topic: {}", 
                        deviceKafkaClient.getInstanceReplyTopic());
            }
        } catch (Exception e) {
            log.error("Failed to start dynamic device listener", e);
        }
    }
}