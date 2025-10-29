package com.connection.service.auth.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.atomikos.jdbc.AtomikosDataSourceBean;

@Configuration
public class ClientJDBCConfig {
    
    @Value("${app.datasource.client.xa-data-source-class-name:org.postgresql.xa.PGXADataSource}")
    private String xaDataSourceClassName;

    @Value("${app.datasource.client.xa-properties.url}")
    private String jdbcUrl;

    @Value("${app.datasource.client.xa-properties.user}")
    private String username;

    @Value("${app.datasource.client.xa-properties.password}")
    private String password;

    @Value("${app.datasource.client.pool-size:5}")
    private int poolSize;

    @Value("${app.datasource.client.max-pool-size:10}")
    private int maxPoolSize;

    @Value("${app.datasource.client.min-pool-size:2}")
    private int minPoolSize;

    @Value("${app.datasource.client.borrow-connection-timeout:30000}")
    private int borrowConnectionTimeout;

    @Value("${app.datasource.client.max-idle-time:60}")
    private int maxIdleTime;

    @Value("${app.datasource.client.max-lifetime:120}")
    private int maxLifetime;

    @Value("${app.datasource.client.test-query:SELECT 1}")
    private String testQuery;

    @Value("${app.datasource.client.maintenance-interval:60}")
    private int maintenanceInterval;

    @Value("${app.datasource.client.unique-resource-name:clientXADataSource}")
    private String uniqueResourceName;

    @Bean("ClientDataSource")
    DataSource clientDataSource() {
        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();

        dataSource.setUniqueResourceName(uniqueResourceName);
        dataSource.setXaDataSourceClassName(xaDataSourceClassName);
        
        Properties xaProperties = new Properties();
        xaProperties.setProperty("url", jdbcUrl);
        xaProperties.setProperty("user", username);
        xaProperties.setProperty("password", password);

        dataSource.setXaProperties(xaProperties);

        dataSource.setPoolSize(poolSize);
        dataSource.setMaxPoolSize(maxPoolSize);
        dataSource.setMinPoolSize(minPoolSize);
        dataSource.setBorrowConnectionTimeout(borrowConnectionTimeout);
        dataSource.setMaxIdleTime(maxIdleTime);
        dataSource.setMaxLifetime(maxLifetime);

        dataSource.setTestQuery(testQuery);
        dataSource.setMaintenanceInterval(maintenanceInterval);

        return dataSource;
    }

    @Bean("ClientJdbcTemplate")
    NamedParameterJdbcTemplate clientNamedParameterJdbcTemplate(
            @Qualifier("ClientDataSource") DataSource clientDataSource) {
        return new NamedParameterJdbcTemplate(clientDataSource);
    }
}