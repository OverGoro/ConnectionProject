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
    @Bean("DeviceAccessTokenValidator")
    DeviceAccessTokenValidator deviceAccessTokenValidator() {
        return new DeviceAccessTokenValidator();
    }

    @Bean("DeviceAccessTokenGenerator")
    DeviceAccessTokenGenerator deviceAccessTokenGenerator(
            @Qualifier("jwtSecretKey") SecretKey secretKey,
            @Qualifier("appName") String appNameString,
            @Qualifier("jwtSubject") String subjecString) {
        return new DeviceAccessTokenGenerator(secretKey, appNameString, subjecString);
    }

    @Bean("DeviceAccessTokenConverter")
    DeviceAccessTokenConverter deviceAccessTokenConverter(
            @Qualifier("DeviceAccessTokenGenerator") DeviceAccessTokenGenerator deviceAccessTokenGenerator) {
        return new DeviceAccessTokenConverter(deviceAccessTokenGenerator);
    }

}
