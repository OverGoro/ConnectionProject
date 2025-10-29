package com.connection.service.auth.config;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.connection.token.converter.RefreshTokenConverter;
import com.connection.token.generator.RefreshTokenGenerator;
import com.connection.token.validator.RefreshTokenValidator;

@Configuration
public class RefreshTokenUtilsConfig {

    @Bean("RefreshTokenValidator")
    RefreshTokenValidator refreshTokenValidator(){
        return new RefreshTokenValidator();
    }

    @Bean("RefreshTokenGenerator")
    RefreshTokenGenerator refreshTokenGenerator(
            @Qualifier("jwtSecretKey") SecretKey secretKey,
            @Qualifier("appName") String appNameString,
            @Qualifier("jwtSubject") String subjecString) {
        return new RefreshTokenGenerator(secretKey, appNameString, subjecString);
    }

    @Bean("RefreshTokenConverter")
    RefreshTokenConverter refreshTokenConverter(
            @Qualifier("RefreshTokenGenerator") RefreshTokenGenerator refreshTokenGenerator) {
        return new RefreshTokenConverter(refreshTokenGenerator);
    }
}
