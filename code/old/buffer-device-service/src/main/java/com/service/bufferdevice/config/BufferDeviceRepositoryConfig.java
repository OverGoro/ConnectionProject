// BufferDeviceRepositoryConfig.java
package com.service.bufferdevice.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.connection.processing.buffer.bufferdevice.repository.BufferDeviceRepository;
import com.connection.processing.buffer.bufferdevice.repository.BufferDeviceRepositorySQLImpl;

@Configuration
public class BufferDeviceRepositoryConfig {
    @Bean("BufferDeviceRepository")
    BufferDeviceRepository bufferDeviceRepository(@Qualifier("BufferDeviceJdbcTemplate") NamedParameterJdbcTemplate template){
        return new BufferDeviceRepositorySQLImpl(template);
    }
}