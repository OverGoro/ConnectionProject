// ConnectionSchemeRepositoryConfig.java
package com.service.connectionscheme.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.connection.scheme.repository.ConnectionSchemeRepository;
import com.connection.scheme.repository.ConnectionSchemeRepositorySQLImpl;

@Configuration
public class ConnectionSchemeRepositoryConfig {
    @Bean("ConnectionSchemeRepository")
    ConnectionSchemeRepository connectionSchemeRepository(@Qualifier("ConnectionSchemeJdbcTemplate") NamedParameterJdbcTemplate template){
        return new ConnectionSchemeRepositorySQLImpl(template);
    }
}