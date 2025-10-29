package com.connection.service.auth.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;

@Configuration
public class JwtConfig {
    @Value("${JWT_KEY:${app.jwt.key:zbhcWhLNkuwmbgJBdLKQU5tEArWPrWyMHrenwYT7e9c}}")
    private String jwtSecretString;

    @Value("AuthToken")
    private String jwtSubjectString;

    @Value("${ACCESS_TOKEN_EXPIRATION:${app.jwt.access-token.expiration:600}}")
    private long accessTokenExpiration;
    
    @Value("${REFRESH_TOKEN_EXPIRATION:${app.jwt.refresh-token.expiration:86400}}")
    private long refreshTokenExpiration;

    private final MacAlgorithm jwtAlgorithmMacAlgorithm = Jwts.SIG.HS256;

    @Bean("jwtSecretKey")
    SecretKey jwtSecretKey() {
        return createSecretKeyFromString(jwtSecretString, jwtAlgorithmMacAlgorithm);
    }

    @Bean("jwtSubject")
    String jwtSubject(){
        return jwtSecretString;
    }

    @Bean("jwtAccessTokenExpiration")
    Duration jwtAccessTokenDuration(){
        return Duration.ofSeconds(accessTokenExpiration);
    }

    @Bean("jwtRefreshTokenExpiration")
    Duration jwtRefreshTokenDuration(){
        return Duration.ofSeconds(refreshTokenExpiration);
    }
    
    @Bean("jwtAlghorithm")
    MacAlgorithm jwtAlgorithmMacAlgorithm(){
        return jwtAlgorithmMacAlgorithm;
    }

    private SecretKey createSecretKeyFromString(String secretString, MacAlgorithm algorithm) {
        byte[] keyBytes = secretString.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }


}
