package com.service.auth.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.connection.client.repository.ClientRepository;
import com.connection.client.repository.ClientRepositorySQLImpl;

@Configuration
public class ClientRepositoryConfig {
    @Bean("ClientRepository")
    ClientRepository clientRepository(@Qualifier("ClientJdbcTemplate")NamedParameterJdbcTemplate template){
        return new ClientRepositorySQLImpl(template);
    }
}
