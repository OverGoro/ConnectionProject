package com.service.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.service.auth.repository.token.access.AccessTokenRepository;
import com.service.auth.repository.token.access.AccessTokenRepositoryGenImpl;

@Configuration
public class ServiceConfig {
    @Bean
    AccessTokenRepository accessTokenRepository(){
        return new AccessTokenRepositoryGenImpl();
    }
}
