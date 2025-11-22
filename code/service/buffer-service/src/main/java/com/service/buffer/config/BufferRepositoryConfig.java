
package com.service.buffer.config;

import com.connection.processing.buffer.repository.BufferRepository;
import com.connection.processing.buffer.repository.BufferRepositorySqlImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** . */
@Configuration
public class BufferRepositoryConfig {
    @Bean("BufferRepository")
    BufferRepository bufferRepository(
            @Qualifier("BufferJdbcTemplate") NamedParameterJdbcTemplate template) {
        return new BufferRepositorySqlImpl(template);
    }
}
