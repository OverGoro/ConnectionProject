package com.service.device.auth.config;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.connection.device.token.converter.DeviceTokenConverter;
import com.connection.device.token.generator.DeviceTokenGenerator;
import com.connection.device.token.validator.DeviceTokenValidator;

@Configuration
public class DeviceTokenUtilsConfig {

    @Bean("DeviceTokenValidator")
    DeviceTokenValidator deviceTokenValidator(){
        return new DeviceTokenValidator();
    }

    @Bean("DeviceTokenGenerator")
    DeviceTokenGenerator deviceTokenGenerator(
            @Qualifier("jwtSecretKey") SecretKey secretKey,
            @Qualifier("appName") String appNameString,
            @Qualifier("jwtSubject") String subjecString) {
        return new DeviceTokenGenerator(secretKey, appNameString, subjecString);
    }

    @Bean("DeviceTokenConverter")
    DeviceTokenConverter deviceTokenConverter(
            @Qualifier("DeviceTokenGenerator") DeviceTokenGenerator deviceTokenGenerator) {
        return new DeviceTokenConverter(deviceTokenGenerator);
    }
}
