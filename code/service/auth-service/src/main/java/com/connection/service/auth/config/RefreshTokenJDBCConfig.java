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
public class RefreshTokenJDBCConfig {
    
    @Value("${app.datasource.refresh-token.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Value("${app.datasource.refresh-token.jdbc-url}")
    private String jdbcUrl;

    @Value("${app.datasource.refresh-token.username}")
    private String username;

    @Value("${app.datasource.refresh-token.password}")
    private String password;

    @Value("${app.datasource.refresh-token.maximum-pool-size:10}")
    private int maximumPoolSize;

    @Value("${app.datasource.refresh-token.minimum-idle:2}")
    private int minimumIdle;

    @Value("${app.datasource.refresh-token.connection-timeout:30000}")
    private int connectionTimeout;

    @Value("${app.datasource.refresh-token.idle-timeout:60000}")
    private int idleTimeout;

    @Value("${app.datasource.refresh-token.max-lifetime:120000}")
    private int maxLifetime;

    @Value("${app.datasource.refresh-token.connection-test-query:SELECT 1}")
    private String connectionTestQuery;

    @Value("${app.datasource.refresh-token.pool-name:refresh-token-ds}")
    private String poolName;

    @Value("${app.datasource.refresh-token.leak-detection-threshold:60000}")
    private long leakDetectionThreshold;

    @Bean("RefreshTokenDataSource")
    DataSource refreshTokenDataSource() {
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
        dataSource.setLeakDetectionThreshold(leakDetectionThreshold);
        dataSource.setInitializationFailTimeout(1);

        return dataSource;
    }

    @Bean("RefreshTokenJdbcTemplate")
    NamedParameterJdbcTemplate refreshTokenNamedParameterJdbcTemplate(
            @Qualifier("RefreshTokenDataSource") DataSource refreshTokenDataSource) {
        return new NamedParameterJdbcTemplate(refreshTokenDataSource);
    }
}