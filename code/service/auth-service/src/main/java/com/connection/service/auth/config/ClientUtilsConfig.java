package com.connection.service.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.connection.client.converter.ClientConverter;
import com.connection.client.validator.ClientValidator;


@Configuration
public class ClientUtilsConfig {
    @Bean("ClientConverter")
    ClientConverter clientConverter(){
        return new ClientConverter();
    }

    @Bean("ClientValidator")
    ClientValidator clientValidator(){
        return new ClientValidator();
    }
}
