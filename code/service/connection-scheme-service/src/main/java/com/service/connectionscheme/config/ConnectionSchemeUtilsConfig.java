// ConnectionSchemeUtilsConfig.java
package com.service.connectionscheme.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.connection.scheme.converter.ConnectionSchemeConverter;
import com.connection.scheme.validator.ConnectionSchemeValidator;

@Configuration
@Primary
public class ConnectionSchemeUtilsConfig {
    @Bean("ConnectionSchemeConverter")
    ConnectionSchemeConverter connectionSchemeConverter(){
        return new ConnectionSchemeConverter();
    }

    @Bean("ConnectionSchemeValidator")
    ConnectionSchemeValidator connectionSchemeValidator(){
        return new ConnectionSchemeValidator();
    }
}