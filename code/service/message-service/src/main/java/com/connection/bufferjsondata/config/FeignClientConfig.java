// FeignClientConfig.java
package com.connection.bufferjsondata.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.connection.bufferjsondata.client.FeignErrorDecoder;

@Configuration
@EnableFeignClients(basePackages = "com.service.bufferjsondata.client")
public class FeignClientConfig {
    
    @Bean
    public FeignErrorDecoder feignErrorDecoder() {
        return new FeignErrorDecoder();
    }
}