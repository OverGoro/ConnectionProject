package com.connection.message.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.connection.message.repository.MessageRepository;
import com.connection.message.repository.MessageRepositorySQLImpl;

@Configuration
public class MessageRepositoryConfig {
    
    @Bean("MessageRepository")
    MessageRepository messageRepository(@Qualifier("MessageJdbcTemplate") NamedParameterJdbcTemplate template){
        return new MessageRepositorySQLImpl(template);
    }
}