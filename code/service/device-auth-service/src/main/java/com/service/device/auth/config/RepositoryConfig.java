
package com.service.device.auth.config;

import com.connection.device.token.generator.DeviceAccessTokenGenerator;
import com.connection.device.token.generator.DeviceTokenGenerator;
import com.connection.device.token.repository.DeviceAccessTokenRepository;
import com.connection.device.token.repository.DeviceAccessTokenRepositorySqlImpl;
import com.connection.device.token.repository.DeviceTokenRepository;
import com.connection.device.token.repository.DeviceTokenRepositorySqlImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** . */
@Configuration
public class RepositoryConfig {

    @Autowired
    DeviceTokenGenerator generator;
    @Autowired
    DeviceAccessTokenGenerator accessGenerator;

    @Bean
    DeviceTokenRepository deviceTokenRepository(
            @Qualifier("deviceTokenJdbcTemplate") NamedParameterJdbcTemplate template) {
        return new DeviceTokenRepositorySqlImpl(template, generator);
    }

    @Bean
    DeviceAccessTokenRepository deviceAccessTokenRepository(
            @Qualifier("deviceAccessTokenJdbcTemplate") NamedParameterJdbcTemplate template,
            DeviceTokenRepository deviceTokenRepository) {
        return new DeviceAccessTokenRepositorySqlImpl(template,
                accessGenerator);
    }
}
