// BufferUtilsConfig.java
package com.service.buffer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.connection.device.converter.DeviceConverter;
import com.connection.processing.buffer.converter.BufferConverter;
import com.connection.processing.buffer.validator.BufferValidator;

@Configuration
public class BufferUtilsConfig {
    @Bean("BufferConverter")
    @Primary
    BufferConverter bufferConverter(){
        return new BufferConverter();
    }

    @Bean("BufferValidator")
    @Primary
    BufferValidator bufferValidator(){
        return new BufferValidator();
    }

    @Bean
    DeviceConverter deviceConverter(){
        return new DeviceConverter();
    }
}