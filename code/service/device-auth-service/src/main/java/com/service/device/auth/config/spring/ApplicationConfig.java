// ApplicationConfig.java
package com.service.device.auth.config.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    @Value("${spring.application.name:device-auth-service}")
    private String appNameString;

    @Bean
    String appName(){
        return appNameString;
    }
}