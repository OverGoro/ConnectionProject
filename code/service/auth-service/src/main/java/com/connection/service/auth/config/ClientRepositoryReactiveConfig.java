package com.connection.service.auth.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.connection.client.converter.ClientConverter;
import com.connection.client.repository.ClientReactiveRepositoryImpl;
import com.connection.client.repository.ClientRepository;
import com.connection.client.repository.ClientRepositorySQLImpl;


import io.r2dbc.spi.ConnectionFactory;

@Configuration
@ConditionalOnProperty(name = "app.controller.mode", havingValue = "webflux")
public class ClientRepositoryReactiveConfig {
    @Bean("ClientRepository")
    ClientRepository clientWebFluxRepository(@Qualifier("clientConnectionFactory")ConnectionFactory connectionFactory,
                                            @Qualifier("ClientConverter") ClientConverter clientConverter){
        return new ClientReactiveRepositoryImpl(connectionFactory, clientConverter);
    }
}
