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
public class DeviceTokenJDBCConfig {
    
    @Value("${app.datasource.device-token.xa-data-source-class-name:org.postgresql.xa.PGXADataSource}")
    private String xaDataSourceClassName;

    @Value("${app.datasource.device-token.xa-properties.url}")
    private String jdbcUrl;

    @Value("${app.datasource.device-token.xa-properties.user}")
    private String username;

    @Value("${app.datasource.device-token.xa-properties.password}")
    private String password;

    @Value("${app.datasource.device-token.pool-size:5}")
    private int poolSize;

    @Value("${app.datasource.device-token.max-pool-size:10}")
    private int maxPoolSize;

    @Value("${app.datasource.device-token.min-pool-size:2}")
    private int minPoolSize;

    @Value("${app.datasource.device-token.borrow-connection-timeout:30000}")
    private int borrowConnectionTimeout;

    @Value("${app.datasource.device-token.max-idle-time:60}")
    private int maxIdleTime;

    @Value("${app.datasource.device-token.max-lifetime:120}")
    private int maxLifetime;

    @Value("${app.datasource.device-token.test-query:SELECT 1}")
    private String testQuery;

    @Value("${app.datasource.device-token.maintenance-interval:60}")
    private int maintenanceInterval;

    @Value("${app.datasource.device-token.unique-resource-name:deviceTokenXADataSource}")
    private String uniqueResourceName;

    @Bean("DeviceTokenDataSource")
    DataSource deviceTokenDataSource() {
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

    @Bean("DeviceTokenJdbcTemplate")
    NamedParameterJdbcTemplate deviceTokenNamedParameterJdbcTemplate(
            @Qualifier("DeviceTokenDataSource") DataSource deviceTokenDataSource) {
        return new NamedParameterJdbcTemplate(deviceTokenDataSource);
    }
}