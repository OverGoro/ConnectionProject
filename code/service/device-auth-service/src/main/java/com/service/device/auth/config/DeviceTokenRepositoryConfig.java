package com.service.device.auth.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.connection.device.token.repository.DeviceTokenRepository;
import com.connection.device.token.repository.DeviceTokenRepositorySQLImpl;

@Configuration
public class DeviceTokenRepositoryConfig {
    @Bean("DeviceTokenRepository")
    DeviceTokenRepository deviceTokenRepository(@Qualifier("DeviceTokenJdbcTemplate")NamedParameterJdbcTemplate template){
        return new DeviceTokenRepositorySQLImpl(template);
    }
}
