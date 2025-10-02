// BufferKafkaConfig.java
package com.service.buffer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import com.connection.auth.events.AuthEventConstants;

@Configuration
public class BufferKafkaConfig {

    @Bean
    public NewTopic authResponsesTopic() {
        return TopicBuilder.name(AuthEventConstants.AUTH_RESPONSES_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}