package com.connection.message.config;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** . */
@Configuration
public class MessageJdbcConfig {

    @Value("${app.datasource.message.xa-data-source-class-name:org.postgresql.xa.PGXADataSource}")
    private String xaDataSourceClassName;

    @Value("${app.datasource.message.xa-properties.url}")
    private String jdbcUrl;

    @Value("${app.datasource.message.xa-properties.user}")
    private String username;

    @Value("${app.datasource.message.xa-properties.password}")
    private String password;

    @Value("${app.datasource.message.unique-resource-name:messageXADataSource}")
    private String uniqueResourceName;

    @Bean("MessageDataSource")
    DataSource messageDataSource() {
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

    @Bean("MessageJdbcTemplate")
    NamedParameterJdbcTemplate messageNamedParameterJdbcTemplate(
            @Qualifier("MessageDataSource") DataSource messageDataSource) {
        return new NamedParameterJdbcTemplate(messageDataSource);
    }
}
