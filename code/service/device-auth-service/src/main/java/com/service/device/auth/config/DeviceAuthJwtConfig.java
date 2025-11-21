
package com.service.device.auth.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** . */
@Configuration
public class DeviceAuthJwtConfig {
    @Value("${DEVICE_JWT_KEY:${app.jwt.device.key:deviceJwtSecretKeyForAuthService123}}")
    private String jwtSecretString;

    @Value("DeviceToken")
    private String jwtSubjectString;

    @Value("${DEVICE_ACCESS_TOKEN_EXPIRATION:${app.jwt.device.access-token.expiration:3600}}")
    private long deviceAccessTokenExpiration;

    @Value("${DEVICE_TOKEN_EXPIRATION:${app.jwt.device.token.expiration:2592000}}") // 30 дней
    private long deviceTokenExpiration;

    private final MacAlgorithm jwtAlgorithmMacAlgorithm = Jwts.SIG.HS256;

    @Bean("deviceJwtSecretKey")
    SecretKey deviceJwtSecretKey() {
        return createSecretKeyFromString(jwtSecretString,
                jwtAlgorithmMacAlgorithm);
    }

    @Bean("deviceJwtSubject")
    String deviceJwtSubject() {
        return jwtSubjectString;
    }

    @Bean("deviceAccessTokenDuration")
    Duration deviceAccessTokenDuration() {
        return Duration.ofSeconds(deviceAccessTokenExpiration);
    }

    @Bean("deviceTokenDuration")
    Duration deviceTokenDuration() {
        return Duration.ofSeconds(deviceTokenExpiration);
    }

    @Bean("deviceJwtAlgorithm")
    MacAlgorithm deviceJwtAlgorithm() {
        return jwtAlgorithmMacAlgorithm;
    }

    private SecretKey createSecretKeyFromString(String secretString,
            MacAlgorithm algorithm) {
        byte[] keyBytes = secretString.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
