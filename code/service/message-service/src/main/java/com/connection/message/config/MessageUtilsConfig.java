package com.connection.message.config;

import com.connection.device.converter.DeviceConverter;
import com.connection.message.converter.MessageConverter;
import com.connection.message.validator.MessageValidator;
import com.connection.processing.buffer.converter.BufferConverter;
import com.connection.scheme.converter.ConnectionSchemeConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/** . */
@Configuration
public class MessageUtilsConfig {

    @Bean("MessageConverter")
    @Primary
    MessageConverter messageConverter() {
        return new MessageConverter();
    }

    @Bean("MessageValidator")
    @Primary
    MessageValidator messageValidator() {
        return new MessageValidator();
    }

    @Bean("BufferConverter")
    BufferConverter bufferConverter() {
        return new BufferConverter();
    }

    @Bean("ConnectionSchemeConverter")
    ConnectionSchemeConverter connectionSchemeConverter() {
        return new ConnectionSchemeConverter();
    }

    @Bean("DeviceConverter")
    DeviceConverter deviceConverter() {
        return new DeviceConverter();
    }
}
