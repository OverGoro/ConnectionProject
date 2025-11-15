package com.connection.service.auth.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;

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

    @Bean
    @Primary
    public ConnectionFactory clientConnectionFactory() {
        PostgresqlConnectionConfiguration config = PostgresqlConnectionConfiguration.builder()
                .host(extractHost(clientJdbcUrl))
                .port(extractPort(clientJdbcUrl))
                .database(extractDatabase(clientJdbcUrl))
                .username(clientUsername)
                .password(clientPassword)
                .build();
                
        return new PostgresqlConnectionFactory(config);
    }
    // @Bean("clientConnectionFactory")
    // public PostgresqlConnectionFactory clientConnectionFactory() {
    //     PostgresqlConnectionConfiguration config = PostgresqlConnectionConfiguration.builder()
    //             .host(extractHost(clientJdbcUrl))
    //             .port(extractPort(clientJdbcUrl))
    //             .database(extractDatabase(clientJdbcUrl))
    //             .username(clientUsername)
    //             .password(clientPassword)
    //             .build();
                
    //     return new PostgresqlConnectionFactory(config);
    // }


    // @Bean("refreshTokenConnectionFactory")
    // public PostgresqlConnectionFactory refreshTokenConnectionFactory() {
    //     PostgresqlConnectionConfiguration config = PostgresqlConnectionConfiguration.builder()
    //             .host(extractHost(refreshTokenJdbcUrl))
    //             .port(extractPort(refreshTokenJdbcUrl))
    //             .database(extractDatabase(refreshTokenJdbcUrl))
    //             .username(refreshTokenUsername)
    //             .password(refreshTokenPassword)
    //             .build();
                
    //     return new PostgresqlConnectionFactory(config);
    // }

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

    @Bean("clientConnectionPool")
    public ConnectionPool clientConnectionPool(
            ConnectionFactory connectionFactory) {
        
        ConnectionPoolConfiguration poolConfig = ConnectionPoolConfiguration.builder(connectionFactory)
                .maxIdleTime(Duration.ofMinutes(30))
                .maxSize(50) // Увеличьте максимальный размер пула
                .maxAcquireTime(Duration.ofSeconds(30)) // Увеличьте время ожидания соединения
                .maxCreateConnectionTime(Duration.ofSeconds(10))
                .initialSize(10) // Увеличьте начальный размер
                .acquireRetry(3) // Добавьте повторные попытки
                .build();
                
        return new ConnectionPool(poolConfig);
    }

    @Bean("refreshTokenConnectionPool")
    public ConnectionPool refreshTokenConnectionPool(
            ConnectionFactory connectionFactory) {
        
        ConnectionPoolConfiguration poolConfig = ConnectionPoolConfiguration.builder(connectionFactory)
                .maxIdleTime(Duration.ofMinutes(30))
                .maxSize(50) // Увеличьте максимальный размер пула
                .maxAcquireTime(Duration.ofSeconds(30))
                .maxCreateConnectionTime(Duration.ofSeconds(10))
                .initialSize(10)
                .acquireRetry(3)
                .build();
                
        return new ConnectionPool(poolConfig);
    }
}