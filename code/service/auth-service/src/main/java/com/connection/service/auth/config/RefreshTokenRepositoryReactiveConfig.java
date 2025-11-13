package com.connection.service.auth.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;


import com.connection.token.repository.RefreshTokenReactiveRepositoryImpl;
import com.connection.token.repository.RefreshTokenRepository;
import com.connection.token.repository.RefreshTokenRepositorySQLImpl;

import io.r2dbc.spi.ConnectionFactory;

@Configuration
@ConditionalOnProperty(name = "app.controller.mode", havingValue = "webflux")
public class RefreshTokenRepositoryReactiveConfig {

    @Bean("RefreshTokenRepository")
        RefreshTokenRepository refreshTokenRepository(@Qualifier("refreshTokenConnectionFactory")ConnectionFactory connectionFactory){
        return new RefreshTokenReactiveRepositoryImpl(connectionFactory);
    }

}
