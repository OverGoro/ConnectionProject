package com.service.device.auth.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.atomikos.jdbc.AtomikosDataSourceBean;

@Configuration
public class DeviceAccessTokenJDBCConfig {
    
    @Value("${app.datasource.device-access-token.xa-data-source-class-name:org.postgresql.xa.PGXADataSource}")
    private String xaDataSourceClassName;

    @Value("${app.datasource.device-access-token.xa-properties.url}")
    private String jdbcUrl;

    @Value("${app.datasource.device-access-token.xa-properties.user}")
    private String username;

    @Value("${app.datasource.device-access-token.xa-properties.password}")
    private String password;

    @Value("${app.datasource.device-access-token.pool-size:5}")
    private int poolSize;

    @Value("${app.datasource.device-access-token.max-pool-size:10}")
    private int maxPoolSize;

    @Value("${app.datasource.device-access-token.min-pool-size:2}")
    private int minPoolSize;

    @Value("${app.datasource.device-access-token.borrow-connection-timeout:30000}")
    private int borrowConnectionTimeout;

    @Value("${app.datasource.device-access-token.max-idle-time:60}")
    private int maxIdleTime;

    @Value("${app.datasource.device-access-token.max-lifetime:120}")
    private int maxLifetime;

    @Value("${app.datasource.device-access-token.test-query:SELECT 1}")
    private String testQuery;

    @Value("${app.datasource.device-access-token.maintenance-interval:60}")
    private int maintenanceInterval;

    @Value("${app.datasource.device-access-token.unique-resource-name:device-access-tokenXADataSource}")
    private String uniqueResourceName;

    @Bean("DeviceAccessTokenDataSource")
    DataSource deviceAccessTokentokenDataSource() {
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

    @Bean("DeviceAccessTokenJdbcTemplate")
    NamedParameterJdbcTemplate deviceAccessTokentokenNamedParameterJdbcTemplate(
            @Qualifier("DeviceAccessTokenDataSource") DataSource deviceAccessTokentokenDataSource) {
        return new NamedParameterJdbcTemplate(deviceAccessTokentokenDataSource);
    }
}