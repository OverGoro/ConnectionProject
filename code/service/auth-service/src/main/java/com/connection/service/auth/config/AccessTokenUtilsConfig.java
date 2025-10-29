package com.connection.service.auth.config;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.connection.token.converter.AccessTokenConverter;
import com.connection.token.generator.AccessTokenGenerator;
import com.connection.token.validator.AccessTokenValidator;
@Configuration
public class AccessTokenUtilsConfig {
    @Bean("AccessTokenValidator")
    AccessTokenValidator accessTokenValidator() {
        return new AccessTokenValidator();
    }

    @Bean("AccessTokenGenerator")
    AccessTokenGenerator accessTokenGenerator(
            @Qualifier("jwtSecretKey") SecretKey secretKey,
            @Qualifier("appName") String appNameString,
            @Qualifier("jwtSubject") String subjecString) {
        return new AccessTokenGenerator(secretKey, appNameString, subjecString);
    }

    @Bean("AccessTokenConverter")
    AccessTokenConverter accessTokenConverter(
            @Qualifier("AccessTokenGenerator") AccessTokenGenerator accessTokenGenerator) {
        return new AccessTokenConverter(accessTokenGenerator);
    }

}
