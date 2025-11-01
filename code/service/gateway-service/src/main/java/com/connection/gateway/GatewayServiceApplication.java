// GatewayServiceApplication.java
package com.connection.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;


@SpringBootApplication
@ComponentScan(
    basePackages = {
        "com.connection.gateway",
        "com.connection.service.auth",
        "com.service.device.auth", 
        "com.connection.device",
        "com.connection.message",
        "com.service.buffer",
        "com.service.connectionscheme"
    },
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {                
                com.connection.service.auth.AuthServiceApplication.class,
                com.connection.device.DeviceServiceApplication.class,
                com.service.buffer.BufferServiceApplication.class,
                com.service.connectionscheme.ConnectionSchemeServiceApplication.class,
                com.service.device.auth.DeviceAuthServiceApplication.class,
                com.connection.message.MessageServiceApplication.class,
            }
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = {
                "com.connection.service.auth.config.kafka.*",
                "com.connection.device.config.kafka.*",
                "com.service.buffer.config.kafka.*",
                "com.service.connectionscheme.config.kafka.*",
                "com.service.device.auth.config.kafka.*",
                "com.connection.message.config.kafka.*",
            }
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = {
                "com.connection.service.auth.config.security.*",
                "com.connection.device.config.security.*",
                "com.service.buffer.config.security.*",
                "com.service.connectionscheme.config.security.*",
                "com.service.device.auth.config.security.*",
                "com.connection.message.config.security.*",
            }
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = {
                "com.connection.service.auth.config.spring.*",
                "com.connection.device.config.spring.*",
                "com.service.buffer.config.spring.*",
                "com.service.connectionscheme.config.spring.*",
                "com.service.device.auth.config.spring.*",
                "com.connection.message.config.spring.*",
            }
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = {
                "com.connection.service.auth.config.swagger.*",
                "com.connection.device.config.swagger.*",
                "com.service.buffer.config.swagger.*",
                "com.service.connectionscheme.config.swagger.*",
                "com.service.device.auth.config.swagger.*",
                "com.connection.message.config.swagger.*",
            }
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = {
                "com.connection.service.auth.config.transaction.*",
                "com.connection.device.config.transaction.*",
                "com.service.buffer.config.transaction.*",
                "com.service.connectionscheme.config.transaction.*",
                "com.service.device.auth.config.transaction.*",
                "com.connection.message.config.transaction.*",
            }
        ),
                @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = {
                "com.connection.service.auth.*Filter.*",
                "com.connection.device.*Filter.*",
                "com.service.buffer.*Filter.*",
                "com.service.connectionscheme.*Filter.*",
                "com.service.device.auth.*Filter.*",
                "com.connection.message.*Filter.*",
            }
        ),
        
    }
)
public class GatewayServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}