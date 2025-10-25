// BufferDeviceJDBCConfig.java
package com.service.bufferdevice.config;

import java.util.Properties;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.atomikos.jdbc.AtomikosDataSourceBean;

@Configuration
public class BufferDeviceJDBCConfig {
    
    @Value("${app.datasource.buffer-device.xa-data-source-class-name:org.postgresql.xa.PGXADataSource}")
    private String xaDataSourceClassName;

    @Value("${app.datasource.buffer-device.xa-properties.url}")
    private String jdbcUrl;

    @Value("${app.datasource.buffer-device.xa-properties.user}")
    private String username;

    @Value("${app.datasource.buffer-device.xa-properties.password}")
    private String password;

    @Value("${app.datasource.buffer-device.unique-resource-name:bufferDeviceXADataSource}")
    private String uniqueResourceName;

    @Bean("BufferDeviceDataSource")
    DataSource bufferDeviceDataSource() {
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

    @Bean("BufferDeviceJdbcTemplate")
    NamedParameterJdbcTemplate bufferDeviceNamedParameterJdbcTemplate(
            @Qualifier("BufferDeviceDataSource") DataSource bufferDeviceDataSource) {
        return new NamedParameterJdbcTemplate(bufferDeviceDataSource);
    }
}