// FeignClientConfig.java
package com.service.bufferdevice.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.service.bufferdevice.client.FeignErrorDecoder;

@Configuration
@EnableFeignClients(basePackages = "com.service.bufferdevice.client")
public class FeignClientConfig {
    
    @Bean
    public FeignErrorDecoder feignErrorDecoder() {
        return new FeignErrorDecoder();
    }
}