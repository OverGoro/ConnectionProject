// BufferDeviceUtilsConfig.java
package com.service.bufferdevice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.connection.processing.buffer.bufferdevice.converter.BufferDeviceConverter;
import com.connection.processing.buffer.bufferdevice.validator.BufferDeviceValidator;

@Configuration
public class BufferDeviceUtilsConfig {
    @Bean("BufferDeviceConverter")
    BufferDeviceConverter bufferDeviceConverter(){
        return new BufferDeviceConverter();
    }

    @Bean("BufferDeviceValidator")
    BufferDeviceValidator bufferDeviceValidator(){
        return new BufferDeviceValidator();
    }
}