// DeviceAccessTokenUtilsConfig.java
package com.service.device.auth.config;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.connection.device.token.converter.DeviceAccessTokenConverter;
import com.connection.device.token.generator.DeviceAccessTokenGenerator;
import com.connection.device.token.validator.DeviceAccessTokenValidator;

@Configuration
public class DeviceAccessTokenUtilsConfig {

    @Bean
    DeviceAccessTokenValidator deviceAccessTokenValidator() {
        return new DeviceAccessTokenValidator();
    }

    @Bean
    DeviceAccessTokenGenerator deviceAccessTokenGenerator(
            SecretKey jwtSecretKey,
            @Qualifier("appName") String appNameString,
            @Qualifier("jwtSubject") String subjectString) {
        return new DeviceAccessTokenGenerator(jwtSecretKey, appNameString, subjectString);
    }

    @Bean
    DeviceAccessTokenConverter deviceAccessTokenConverter(DeviceAccessTokenGenerator deviceAccessTokenGenerator) {
        return new DeviceAccessTokenConverter(deviceAccessTokenGenerator);
    }
}