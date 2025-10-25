package com.connection.device.config;

import java.util.Properties;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.atomikos.jdbc.AtomikosDataSourceBean;

@Configuration
public class DeviceJDBCConfig {
    
    @Value("${app.datasource.device.xa-data-source-class-name:org.postgresql.xa.PGXADataSource}")
    private String xaDataSourceClassName;

    @Value("${app.datasource.device.xa-properties.url}")
    private String jdbcUrl;

    @Value("${app.datasource.device.xa-properties.user}")
    private String username;

    @Value("${app.datasource.device.xa-properties.password}")
    private String password;

    @Value("${app.datasource.device.unique-resource-name:deviceXADataSource}")
    private String uniqueResourceName;

    @Bean("DeviceDataSource")
    DataSource deviceDataSource() {
        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
        dataSource.setUniqueResourceName(uniqueResourceName);
        dataSource.setXaDataSourceClassName(xaDataSourceClassName);
        
        Properties xaProperties = new Properties();
        xaProperties.setProperty("url", jdbcUrl);
        xaProperties.setProperty("user", username);
        xaProperties.setProperty("password", password);

        dataSource.setXaProperties(xaProperties);
        dataSource.setPoolSize(5);
        dataSource.setTestQuery("SELECT 1");

        return dataSource;
    }

    @Bean("DeviceJdbcTemplate")
    NamedParameterJdbcTemplate deviceNamedParameterJdbcTemplate(
            @Qualifier("DeviceDataSource") DataSource deviceDataSource) {
        return new NamedParameterJdbcTemplate(deviceDataSource);
    }
}