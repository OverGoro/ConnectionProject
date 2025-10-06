package com.service.buffer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.connection.device.converter.DeviceConverter;

@Configuration
public class DeviceUtilsConfig {
    @Bean
    DeviceConverter deviceConverter(){
        return new DeviceConverter();
    }
}
