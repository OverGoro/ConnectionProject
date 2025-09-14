package com.service.auth.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.connection.token.repository.RefreshTokenRepository;
import com.connection.token.repository.RefreshTokenRepositorySQLImpl;

@Configuration
public class RefreshTokenRepositoryConfig {
    @Bean("RefreshTokenRepository")
    RefreshTokenRepository refreshTokenRepository(@Qualifier("RefreshTokenJdbcTemplate")NamedParameterJdbcTemplate template){
        return new RefreshTokenRepositorySQLImpl(template);
    }
}
