package com.connection.service.auth.config;

import com.connection.client.repository.ClientRepository;
import com.connection.client.repository.ClientRepositorySqlImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** . */
@Configuration
public class ClientRepositoryConfig {
    @Bean("ClientRepository")
    ClientRepository clientRepository(
            @Qualifier("ClientJdbcTemplate") NamedParameterJdbcTemplate template) {
        return new ClientRepositorySqlImpl(template);
    }
}
