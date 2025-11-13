package com.connection.service.auth.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ConditionalOnProperty(name = "app.controller.mode", havingValue = "mvc", matchIfMissing = true)
public class ClientJDBCConfig {
    
    @Value("${app.datasource.client.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Value("${app.datasource.client.jdbc-url}")
    private String jdbcUrl;

    @Value("${app.datasource.client.username}")
    private String username;

    @Value("${app.datasource.client.password}")
    private String password;

    @Value("${app.datasource.client.maximum-pool-size:10}")
    private int maximumPoolSize;

    @Value("${app.datasource.client.minimum-idle:2}")
    private int minimumIdle;

    @Value("${app.datasource.client.connection-timeout:30000}")
    private int connectionTimeout;

    @Value("${app.datasource.client.idle-timeout:600000}")
    private int idleTimeout;

    @Value("${app.datasource.client.max-lifetime:1200000}")
    private int maxLifetime;

    @Value("${app.datasource.client.connection-test-query:SELECT 1}")
    private String connectionTestQuery;

    @Value("${app.datasource.client.pool-name:client-ds}")
    private String poolName;

    @Bean("ClientDataSource")
    DataSource clientDataSource() {
        HikariDataSource dataSource = new HikariDataSource();

        // Базовые настройки подключения
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        // Настройки пула
        dataSource.setPoolName(poolName);
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(minimumIdle);
        dataSource.setConnectionTimeout(connectionTimeout);
        dataSource.setIdleTimeout(idleTimeout);
        dataSource.setMaxLifetime(maxLifetime);
        
        // Настройки валидации
        dataSource.setConnectionTestQuery(connectionTestQuery);
        dataSource.setValidationTimeout(5000);

        // Дополнительные настройки
        dataSource.setLeakDetectionThreshold(60000);
        dataSource.setInitializationFailTimeout(1);

        return dataSource;
    }

    @Bean("ClientJdbcTemplate")
    NamedParameterJdbcTemplate clientNamedParameterJdbcTemplate(
            @Qualifier("ClientDataSource") DataSource clientDataSource) {
        return new NamedParameterJdbcTemplate(clientDataSource);
    }
}