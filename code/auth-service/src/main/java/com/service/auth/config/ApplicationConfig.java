package com.service.auth.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    @Value("${spring.application.name:auth-service}")
    private String appNameString;

    @Bean
    @Qualifier("appName")
    String appName(){
        return appNameString;
    }
}
