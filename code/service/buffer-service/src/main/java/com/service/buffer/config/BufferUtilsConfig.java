// BufferUtilsConfig.java
package com.service.buffer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.connection.processing.buffer.converter.BufferConverter;
import com.connection.processing.buffer.validator.BufferValidator;

@Configuration
public class BufferUtilsConfig {
    @Bean("BufferConverter")
    BufferConverter bufferConverter(){
        return new BufferConverter();
    }

    @Bean("BufferValidator")
    BufferValidator bufferValidator(){
        return new BufferValidator();
    }
}