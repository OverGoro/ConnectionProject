package com.connection.device.config;

import com.connection.device.repository.DeviceRepository;
import com.connection.device.repository.DeviceRepositorySqlImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** . */
@Configuration
public class DeviceRepositoryConfig {
    @Bean("DeviceRepository")
    DeviceRepository deviceRepository(
            @Qualifier("DeviceJdbcTemplate") NamedParameterJdbcTemplate template) {
        return new DeviceRepositorySqlImpl(template);
    }
}
