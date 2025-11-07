package com.connection.gateway.config.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;

@Configuration
public class StrictDatabaseHealthCheckConfig {

    @Autowired
    private ApplicationContext context;

    @Bean
    @Order(Integer.MIN_VALUE) // Самый высокий приоритет
    public CommandLineRunner strictDatabaseHealthChecker() {
        return args -> {
            System.out.println("=== STRICT DATABASE HEALTH CHECK ===");
            
            boolean allHealthy = true;
            
            // Получаем все DataSource бины
            Map<String, DataSource> dataSources = context.getBeansOfType(DataSource.class);
            
            if (dataSources.isEmpty()) {
                System.err.println("✗ No DataSource beans found in context");
                System.exit(1);
            }
            
            for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
                String beanName = entry.getKey();
                DataSource dataSource = entry.getValue();
                
                if (!testDataSourceConnection(beanName, dataSource)) {
                    allHealthy = false;
                }
            }
            
            if (!allHealthy) {
                System.err.println("=== DATABASE CONNECTION FAILED - SHUTTING DOWN APPLICATION ===");
                System.exit(1);
            } else {
                System.out.println("=== ALL DATABASE CONNECTIONS SUCCESSFUL ===");
            }
        };
    }
    
    private boolean testDataSourceConnection(String beanName, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            statement.setQueryTimeout(5); // 5 секунд таймаут
            boolean hasResult = statement.execute("SELECT 1");
            
            if (hasResult) {
                System.out.println("✓ Database connection successful: " + beanName);
                return true;
            } else {
                System.err.println("✗ Database test query failed for: " + beanName);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("✗ Database connection failed for: " + beanName);
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }
}