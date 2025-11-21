
package com.service.connectionscheme.config;

import com.connection.scheme.repository.ConnectionSchemeRepository;
import com.connection.scheme.repository.ConnectionSchemeRepositorySQLImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** . */
@Configuration
public class ConnectionSchemeRepositoryConfig {
    @Bean("ConnectionSchemeRepository")
    ConnectionSchemeRepository connectionSchemeRepository(
            @Qualifier("ConnectionSchemeJdbcTemplate") NamedParameterJdbcTemplate template) {
        return new ConnectionSchemeRepositorySQLImpl(template);
    }
}
