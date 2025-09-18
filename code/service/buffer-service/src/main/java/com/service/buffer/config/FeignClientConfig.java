// FeignClientConfig.java
package com.service.buffer.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.service.buffer.client.FeignErrorDecoder;

@Configuration
@EnableFeignClients(basePackages = "com.service.buffer.client")
public class FeignClientConfig {
    
    @Bean
    public FeignErrorDecoder feignErrorDecoder() {
        return new FeignErrorDecoder();
    }
}