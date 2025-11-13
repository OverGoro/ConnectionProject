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
@ConditionalOnProperty(name = "app.controller.mode", havingValue = "mvc", matchIfMissing = true)
public class RefreshTokenRepositoryConfig {
    @Bean("RefreshTokenRepository")
    RefreshTokenRepository refreshTokenRepository(@Qualifier("RefreshTokenJdbcTemplate")NamedParameterJdbcTemplate template){
        return new RefreshTokenRepositorySQLImpl(template);
    }

}
