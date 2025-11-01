// DatabaseConfig.java
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
public class DeviceAuthDatabaseConfig {
    
    // Device Token DataSource
    @Value("${app.datasource.device-token.xa-data-source-class-name:org.postgresql.xa.PGXADataSource}")
    private String deviceTokenXaDataSourceClassName;

    @Value("${app.datasource.device-token.xa-properties.url}")
    private String deviceTokenJdbcUrl;

    @Value("${app.datasource.device-token.xa-properties.user}")
    private String deviceTokenUsername;

    @Value("${app.datasource.device-token.xa-properties.password}")
    private String deviceTokenPassword;

    @Value("${app.datasource.device-token.unique-resource-name:deviceTokenXADataSource}")
    private String deviceTokenUniqueResourceName;

    // Device Access Token DataSource
    @Value("${app.datasource.device-access-token.xa-data-source-class-name:org.postgresql.xa.PGXADataSource}")
    private String deviceAccessTokenXaDataSourceClassName;

    @Value("${app.datasource.device-access-token.xa-properties.url}")
    private String deviceAccessTokenJdbcUrl;

    @Value("${app.datasource.device-access-token.xa-properties.user}")
    private String deviceAccessTokenUsername;

    @Value("${app.datasource.device-access-token.xa-properties.password}")
    private String deviceAccessTokenPassword;

    @Value("${app.datasource.device-access-token.unique-resource-name:deviceAccessTokenXADataSource}")
    private String deviceAccessTokenUniqueResourceName;

    @Bean("deviceTokenDataSource")
    DataSource deviceTokenDataSource() {
        return createDataSource(
            deviceTokenUniqueResourceName,
            deviceTokenXaDataSourceClassName,
            deviceTokenJdbcUrl,
            deviceTokenUsername,
            deviceTokenPassword
        );
    }

    @Bean("deviceAccessTokenDataSource")
    DataSource deviceAccessTokenDataSource() {
        return createDataSource(
            deviceAccessTokenUniqueResourceName,
            deviceAccessTokenXaDataSourceClassName,
            deviceAccessTokenJdbcUrl,
            deviceAccessTokenUsername,
            deviceAccessTokenPassword
        );
    }

    @Bean("deviceTokenJdbcTemplate")
    NamedParameterJdbcTemplate deviceTokenJdbcTemplate(
            @Qualifier("deviceTokenDataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean("deviceAccessTokenJdbcTemplate")
    NamedParameterJdbcTemplate deviceAccessTokenJdbcTemplate(
            @Qualifier("deviceAccessTokenDataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    private DataSource createDataSource(String uniqueResourceName, String xaDataSourceClassName, 
                                      String url, String username, String password) {
        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
        dataSource.setUniqueResourceName(uniqueResourceName);
        dataSource.setXaDataSourceClassName(xaDataSourceClassName);

        Properties xaProperties = new Properties();
        xaProperties.setProperty("url", url);
        xaProperties.setProperty("user", username);
        xaProperties.setProperty("password", password);

        dataSource.setXaProperties(xaProperties);
        dataSource.setPoolSize(5);
        dataSource.setMaxPoolSize(10);
        dataSource.setTestQuery("SELECT 1");

        return dataSource;
    }
}