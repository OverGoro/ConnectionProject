package com.connection.service.auth.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;

import java.time.Duration;

@Configuration
public class R2DBCConfig {

    @Value("${app.datasource.client.jdbc-url}")
    private String clientJdbcUrl;
    
    @Value("${app.datasource.client.username}")
    private String clientUsername;
    
    @Value("${app.datasource.client.password}")
    private String clientPassword;
    
    @Value("${app.datasource.refresh-token.jdbc-url}")
    private String refreshTokenJdbcUrl;
    
    @Value("${app.datasource.refresh-token.username}")
    private String refreshTokenUsername;
    
    @Value("${app.datasource.refresh-token.password}")
    private String refreshTokenPassword;

    @Bean("clientConnectionFactory")
    public PostgresqlConnectionFactory clientConnectionFactory() {
        PostgresqlConnectionConfiguration config = PostgresqlConnectionConfiguration.builder()
                .host(extractHost(clientJdbcUrl))
                .port(extractPort(clientJdbcUrl))
                .database(extractDatabase(clientJdbcUrl))
                .username(clientUsername)
                .password(clientPassword)
                .build();
                
        return new PostgresqlConnectionFactory(config);
    }

    @Bean("clientConnectionPool")
    public ConnectionPool clientConnectionPool(
            @Qualifier("clientConnectionFactory") PostgresqlConnectionFactory connectionFactory) {
        
        ConnectionPoolConfiguration poolConfig = ConnectionPoolConfiguration.builder(connectionFactory)
                .maxIdleTime(Duration.ofMinutes(30))
                .maxSize(20)
                .maxCreateConnectionTime(Duration.ofSeconds(10))
                .initialSize(5)
                .build();
                
        return new ConnectionPool(poolConfig);
    }

    @Bean("refreshTokenConnectionFactory")
    public PostgresqlConnectionFactory refreshTokenConnectionFactory() {
        PostgresqlConnectionConfiguration config = PostgresqlConnectionConfiguration.builder()
                .host(extractHost(refreshTokenJdbcUrl))
                .port(extractPort(refreshTokenJdbcUrl))
                .database(extractDatabase(refreshTokenJdbcUrl))
                .username(refreshTokenUsername)
                .password(refreshTokenPassword)
                .build();
                
        return new PostgresqlConnectionFactory(config);
    }

    @Bean("refreshTokenConnectionPool")
    public ConnectionPool refreshTokenConnectionPool(
            @Qualifier("refreshTokenConnectionFactory") PostgresqlConnectionFactory connectionFactory) {
        
        ConnectionPoolConfiguration poolConfig = ConnectionPoolConfiguration.builder(connectionFactory)
                .maxIdleTime(Duration.ofMinutes(30))
                .maxSize(20)
                .maxCreateConnectionTime(Duration.ofSeconds(10))
                .initialSize(5)
                .build();
                
        return new ConnectionPool(poolConfig);
    }

    private String extractHost(String jdbcUrl) {
        // jdbc:postgresql://localhost:5432/database
        String[] parts = jdbcUrl.split("://")[1].split(":")[0].split("/");
        return parts[0];
    }

    private int extractPort(String jdbcUrl) {
        String[] parts = jdbcUrl.split("://")[1].split(":")[1].split("/");
        return Integer.parseInt(parts[0]);
    }

    private String extractDatabase(String jdbcUrl) {
        String[] parts = jdbcUrl.split("://")[1].split("/");
        return parts[1];
    }
}