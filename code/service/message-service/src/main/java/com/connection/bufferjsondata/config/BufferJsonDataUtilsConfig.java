// BufferJsonDataUtilsConfig.java
package com.connection.bufferjsondata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.connection.processing.buffer.objects.json.converter.BufferJsonDataConverter;
import com.connection.processing.buffer.objects.json.validator.BufferJsonDataValidator;

@Configuration
public class BufferJsonDataUtilsConfig {
    @Bean("BufferJsonDataConverter")
    BufferJsonDataConverter bufferJsonDataConverter(){
        return new BufferJsonDataConverter();
    }

    @Bean("BufferJsonDataValidator")
    BufferJsonDataValidator bufferJsonDataValidator(){
        return new BufferJsonDataValidator();
    }
}