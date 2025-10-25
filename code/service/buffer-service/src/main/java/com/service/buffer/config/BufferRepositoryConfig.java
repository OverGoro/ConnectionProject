// BufferRepositoryConfig.java
package com.service.buffer.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.connection.processing.buffer.repository.BufferRepository;
import com.connection.processing.buffer.repository.BufferRepositorySQLImpl;

@Configuration
public class BufferRepositoryConfig {
    @Bean("BufferRepository")
    BufferRepository bufferRepository(@Qualifier("BufferJdbcTemplate") NamedParameterJdbcTemplate template){
        return new BufferRepositorySQLImpl(template);
    }
}