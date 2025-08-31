package com.service.auth.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;

@Configuration
public class JwtConfig {
    @Value("${JWT_KEY:${jwt.key:zbhcWhLNkuwmbgJBdLKQU5tEArWPrWyMHrenwYT7e9c}}")
    private String jwtSecretString;

    @Value("AuthToken")
    private String jwtSubjectString;

    @Value("${ACCESS_TOKEN_EXPIRATION:${jwt.access-token.expiration:600}}")
    private long accessTokenExpiration;
    
    @Value("${REFRESH_TOKEN_EXPIRATION:${jwt.refresh-token.expiration:86400}}")
    private long refreshTokenExpiration;

    private final MacAlgorithm jwtAlgorithmMacAlgorithm = Jwts.SIG.HS256;

    @Bean
    @Qualifier("jwtSecretKey")
    SecretKey jwtSecretKey() {
        return createSecretKeyFromString(jwtSecretString, jwtAlgorithmMacAlgorithm);
    }

    @Bean
    @Qualifier("jwtSubject")
    String jwtSubject(){
        return jwtSecretString;
    }

    @Bean
    @Qualifier("jwtAccessTokenExpiration")
    Duration jwtAccessTokenDuration(){
        return Duration.ofSeconds(accessTokenExpiration);
    }

    @Bean
    @Qualifier("jwtRefreshTokenExpiration")
    Duration jwtRefreshTokenDuration(){
        return Duration.ofSeconds(refreshTokenExpiration);
    }
    
    @Bean 
    @Qualifier("jwtAlghorithm")
    MacAlgorithm jwtAlgorithmMacAlgorithm(){
        return jwtAlgorithmMacAlgorithm;
    }

    private SecretKey createSecretKeyFromString(String secretString, MacAlgorithm algorithm) {
        byte[] keyBytes = secretString.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }


}
