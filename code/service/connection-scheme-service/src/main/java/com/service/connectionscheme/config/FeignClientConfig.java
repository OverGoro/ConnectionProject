// FeignClientConfig.java
package com.service.connectionscheme.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.service.connectionscheme.client.FeignErrorDecoder;

@Configuration
@EnableFeignClients(basePackages = "com.service.connectionscheme.client")
public class FeignClientConfig {
    
    @Bean
    public FeignErrorDecoder feignErrorDecoder() {
        return new FeignErrorDecoder();
    }
}