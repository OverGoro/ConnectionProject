package com.service.connectionscheme.config.kafka;
// package com.service.connectionscheme.config;

// import java.util.HashMap;
// import java.util.Map;

// import org.apache.kafka.clients.admin.NewTopic;
// import org.apache.kafka.clients.consumer.ConsumerConfig;
// import org.apache.kafka.clients.producer.ProducerConfig;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
// import org.springframework.kafka.config.TopicBuilder;
// import org.springframework.kafka.core.ConsumerFactory;
// import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
// import org.springframework.kafka.core.DefaultKafkaProducerFactory;
// import org.springframework.kafka.core.KafkaTemplate;
// import org.springframework.kafka.core.ProducerFactory;
// import org.springframework.kafka.listener.DefaultErrorHandler;
// import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
// import org.springframework.kafka.support.serializer.JsonDeserializer;
// import org.springframework.kafka.support.serializer.JsonSerializer;
// import org.springframework.util.backoff.FixedBackOff;

// import com.connection.auth.events.AuthEventConstants;
// import com.connection.scheme.events.ConnectionSchemeEventConstants;

// @Configuration
// public class ConnectionSchemeKafkaConfig {
// @Value("${spring.kafka.bootstrap-servers:localhost:29092}")
// private String bootstrapServers;

// @Bean
// public NewTopic authResponsesTopic() {
// return TopicBuilder.name(AuthEventConstants.AUTH_RESPONSES_TOPIC)
// .partitions(3)
// .replicas(1)
// .build();
// }

// // Конфигурация топиков для connection-scheme-service
// @Bean
// public NewTopic connectionSchemeCommandsTopic() {
// return TopicBuilder.name(ConnectionSchemeEventConstants.CONNECTION_SCHEME_COMMANDS_TOPIC)
// .partitions(3)
// .replicas(1)
// .build();
// }

// @Bean
// public NewTopic connectionSchemeResponsesTopic() {
// return TopicBuilder.name(ConnectionSchemeEventConstants.CONNECTION_SCHEME_RESPONSES_TOPIC)
// .partitions(3)
// .replicas(1)
// .build();
// }

// @Bean
// public NewTopic connectionSchemeEventsTopic() {
// return TopicBuilder.name(ConnectionSchemeEventConstants.CONNECTION_SCHEME_EVENTS_TOPIC)
// .partitions(3)
// .replicas(1)
// .build();
// }

// // Конфигурация для Consumer с обработкой ошибок десериализации
// @Bean
// public ConsumerFactory<String, Object> consumerFactory() {
// Map<String, Object> configProps = new HashMap<>();
// configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
// configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "connection-scheme-service-group");
// configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
// configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
// configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

// // Конфигурация для ErrorHandlingDeserializer
// configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS,
// org.apache.kafka.common.serialization.StringDeserializer.class);
// configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

// // Конфигурация для JsonDeserializer
// configProps.put(JsonDeserializer.TRUSTED_PACKAGES, 
// "com.connection.auth.events.commands," +
// "com.connection.scheme.events.commands," +
// "com.connection.scheme.events.responses," +
// "com.connection.common.events," +
// "com.connection.device.events.commands," +
// "com.connection.auth.events.responses");
// configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);
// configProps.put(JsonDeserializer.REMOVE_TYPE_INFO_HEADERS, false);
// configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.connection.common.events.Command");

// return new DefaultKafkaConsumerFactory<>(configProps);
// }

// @Bean
// public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
// ConcurrentKafkaListenerContainerFactory<String, Object>
//  factory = new ConcurrentKafkaListenerContainerFactory<>();
// factory.setConsumerFactory(consumerFactory());

// // Обработка ошибок десериализации - пропускаем некорректные сообщения
// DefaultErrorHandler errorHandler = new DefaultErrorHandler(
// (record, exception) -> {
// org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("KafkaErrorHandler");
// logger.error("Skipping invalid message - Topic: 
// {}, Partition: {}, Offset: {}, Key: {}, Error: {}",
// record.topic(),
// record.partition(),
// record.offset(),
// record.key(),
// exception.getMessage());
// },
// new FixedBackOff(0L, 0L));

// factory.setCommonErrorHandler(errorHandler);

// return factory;
// }

// // Конфигурация для Producer
// @Bean
// public ProducerFactory<String, Object> producerFactory() {
// Map<String, Object> configProps = new HashMap<>();
// configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
// configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
// org.apache.kafka.common.serialization.StringSerializer.class);
// configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
// configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
// return new DefaultKafkaProducerFactory<>(configProps);
// }

// @Bean
// public KafkaTemplate<String, Object> kafkaTemplate() {
// return new KafkaTemplate<>(producerFactory());
// }
// }