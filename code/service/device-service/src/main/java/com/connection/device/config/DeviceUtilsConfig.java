package com.connection.device.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.connection.device.converter.DeviceConverter;
import com.connection.device.validator.DeviceValidator;

@Configuration
public class DeviceUtilsConfig {
    @Bean("DeviceConverter")
    DeviceConverter deviceConverter(){
        return new DeviceConverter();
    }

    @Bean("DeviceValidator")
    DeviceValidator deviceValidator(){
        return new DeviceValidator();
    }
}