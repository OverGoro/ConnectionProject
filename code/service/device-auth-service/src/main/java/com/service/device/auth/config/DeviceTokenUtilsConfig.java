// DeviceTokenUtilsConfig.java
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

    @Bean
    DeviceTokenValidator deviceTokenValidator(){
        return new DeviceTokenValidator();
    }

    @Bean
    DeviceTokenGenerator deviceTokenGenerator(
            SecretKey jwtSecretKey,
            @Qualifier("appName") String appNameString,
            @Qualifier("jwtSubject") String subjectString) {
        return new DeviceTokenGenerator(jwtSecretKey, appNameString, subjectString);
    }

    @Bean
    DeviceTokenConverter deviceTokenConverter(DeviceTokenGenerator deviceTokenGenerator) {
        return new DeviceTokenConverter(deviceTokenGenerator);
    }
}