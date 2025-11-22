package com.connection.message.config;

import com.connection.message.repository.MessageRepository;
import com.connection.message.repository.MessageRepositorySqlImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** . */
@Configuration
public class MessageRepositoryConfig {

    @Bean("MessageRepository")
    MessageRepository messageRepository(
            @Qualifier("MessageJdbcTemplate") NamedParameterJdbcTemplate template) {
        return new MessageRepositorySqlImpl(template);
    }
}
