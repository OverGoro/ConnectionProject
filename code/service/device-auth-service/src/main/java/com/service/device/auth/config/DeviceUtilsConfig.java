package com.service.device.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.connection.device.converter.DeviceConverter;
import com.connection.device.validator.DeviceValidator;


@Configuration
public class DeviceUtilsConfig {
    @Bean("DeviceConverter")
    DeviceConverter clientConverter(){
        return new DeviceConverter();
    }

    @Bean("DeviceValidator")
    DeviceValidator clientValidator(){
        return new DeviceValidator();
    }
}
