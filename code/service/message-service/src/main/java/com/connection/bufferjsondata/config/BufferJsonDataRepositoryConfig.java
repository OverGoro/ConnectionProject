// BufferJsonDataRepositoryConfig.java
package com.connection.bufferjsondata.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.connection.processing.buffer.objects.json.repository.BufferJsonDataRepository;
import com.connection.processing.buffer.objects.json.repository.BufferJsonDataRepositorySQLImpl;

@Configuration
public class BufferJsonDataRepositoryConfig {
    @Bean("BufferJsonDataRepository")
    BufferJsonDataRepository bufferJsonDataRepository(@Qualifier("BufferJsonDataJdbcTemplate") NamedParameterJdbcTemplate template){
        return new BufferJsonDataRepositorySQLImpl(template);
    }
}