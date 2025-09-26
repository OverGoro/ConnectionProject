package com.service.device.auth.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.connection.device.token.repository.DeviceAccessTokenRepository;
import com.connection.device.token.repository.DeviceAccessTokenRepositorySQLImpl;

@Configuration
public class DeviceAccessTokenRepositoryConfig {
    @Bean("DeviceAccessTokenRepository")
    DeviceAccessTokenRepository deviceTokenRepository(@Qualifier("DeviceAccessTokenJdbcTemplate")NamedParameterJdbcTemplate template){
        return new DeviceAccessTokenRepositorySQLImpl(template);
    }
}
