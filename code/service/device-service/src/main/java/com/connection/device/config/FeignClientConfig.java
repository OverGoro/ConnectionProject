package com.connection.device.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.connection.device.client.FeignErrorDecoder;

@Configuration
@EnableFeignClients(basePackages = "com.service.device.client")
public class FeignClientConfig {
    
    @Bean
    public FeignErrorDecoder feignErrorDecoder() {
        return new FeignErrorDecoder();
    }
}