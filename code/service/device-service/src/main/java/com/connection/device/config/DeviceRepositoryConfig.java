package com.connection.device.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.connection.device.repository.DeviceRepository;
import com.connection.device.repository.DeviceRepositorySQLImpl;

@Configuration
public class DeviceRepositoryConfig {
    @Bean("DeviceRepository")
    DeviceRepository deviceRepository(@Qualifier("DeviceJdbcTemplate") NamedParameterJdbcTemplate template){
        return new DeviceRepositorySQLImpl(template);
    }
}