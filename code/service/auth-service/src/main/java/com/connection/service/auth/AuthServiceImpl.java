package com.connection.service.auth;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.connection.client.model.ClientBLM;
import com.connection.client.repository.ClientRepository;
import com.connection.client.validator.ClientValidator;
import com.connection.token.converter.RefreshTokenConverter;
import com.connection.token.generator.AccessTokenGenerator;
import com.connection.token.generator.RefreshTokenGenerator;
import com.connection.token.model.AccessTokenBLM;
import com.connection.token.model.RefreshTokenBLM;
import com.connection.token.model.RefreshTokenDALM;
import com.connection.token.repository.RefreshTokenRepository;
import com.connection.token.validator.AccessTokenValidator;
import com.connection.token.validator.RefreshTokenValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
@EnableAutoConfiguration(exclude = {
        JpaRepositoriesAutoConfiguration.class
})
public class AuthServiceImpl implements AuthService {
    private final RefreshTokenConverter refreshTokenConverter;

    private final ClientValidator clientValidator;
    private final RefreshTokenValidator refreshTokenValidator;
    private final AccessTokenValidator accessTokenValidator;

    private final RefreshTokenGenerator refreshTokenGenerator;
    private final AccessTokenGenerator accessTokenGenerator;

    private final ClientRepository clientRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Qualifier("jwtAccessTokenExpiration")
    private final Duration jwtAccessTokenDuration;

    @Qualifier("jwtRefreshTokenExpiration")
    private final Duration jwtRefreshTokenDuration;

    @Override
    public Mono<Pair<AccessTokenBLM, RefreshTokenBLM>> authorizeByEmail(String email, String password) {
        // Валидация email
        clientValidator.validateEmail(email);

        return clientRepository.findByEmail(email)
                .flatMap(clientBLM -> {
                    // Проверка пароля
                    if (!clientBLM.getPassword().equals(password)) {
                        return Mono.error(new SecurityException("Invalid email or password"));
                    }

                    // Инициализация общих полей
                    UUID newClientUuid = clientBLM.getUid();
                    Date newCreatedAt = new Date();

                    // Инициализация нового refreshToken
                    UUID newRefreshUID = UUID.randomUUID();
                    Date newRefreshExpiresAt = Date.from(newCreatedAt.toInstant().plus(jwtRefreshTokenDuration));
                    String newRefreshTokenString = refreshTokenGenerator.generateRefreshToken(newRefreshUID, newClientUuid,
                            newCreatedAt, newRefreshExpiresAt);

                    RefreshTokenBLM newRefreshTokenBLM = new RefreshTokenBLM(newRefreshTokenString, newRefreshUID, newClientUuid,
                            newCreatedAt, newRefreshExpiresAt);

                    // Валидация refresh token
                    refreshTokenValidator.validate(newRefreshTokenBLM);

                    // Инициализация нового accessToken
                    Date newAccessExpiresAt = Date.from(newCreatedAt.toInstant().plus(jwtAccessTokenDuration));
                    String newAccessTokenString = accessTokenGenerator.generateAccessToken(newClientUuid, newCreatedAt,
                            newAccessExpiresAt);
                    AccessTokenBLM newAccessTokenBLM = new AccessTokenBLM(newAccessTokenString, newClientUuid, newCreatedAt,
                            newAccessExpiresAt);
                    accessTokenValidator.validate(newAccessTokenBLM);

                    // Добавление нового refreshToken в БД
                    RefreshTokenDALM newRefreshTokenDALM = refreshTokenConverter.toDALM(newRefreshTokenBLM);
                    
                    return refreshTokenRepository.add(newRefreshTokenDALM)
                            .thenReturn(Pair.of(newAccessTokenBLM, newRefreshTokenBLM));
                });
    }

    @Override
    public Mono<Void> register(ClientBLM clientBLM) {
        // Валидация клиента
        clientValidator.validate(clientBLM);
        
        // Регистрация клиента
        return clientRepository.add(clientBLM);
    }

    @Override
    public Mono<Pair<AccessTokenBLM, RefreshTokenBLM>> refresh(RefreshTokenBLM refreshTokenBLM) {
        log.info("Validating refresh token");
        
        // Валидация refresh token
        refreshTokenValidator.validate(refreshTokenBLM);
        log.info("Validated refresh token");

        // Инициализация общих полей
        UUID newClientUuid = refreshTokenBLM.getClientUID();
        Date newCreatedAt = new Date();

        // Инициализация нового refreshToken
        UUID newRefreshUID = refreshTokenBLM.getUid();
        Date newRefreshExpiresAt = Date.from(newCreatedAt.toInstant().plus(jwtRefreshTokenDuration));
        String newRefreshTokenString = refreshTokenGenerator.generateRefreshToken(newRefreshUID, newClientUuid,
                newCreatedAt, newRefreshExpiresAt);

        RefreshTokenBLM newRefreshTokenBLM = new RefreshTokenBLM(newRefreshTokenString, newRefreshUID, newClientUuid,
                newCreatedAt, newRefreshExpiresAt);
        
        log.info("Validating new refresh token");
        refreshTokenValidator.validate(newRefreshTokenBLM);
        log.info("Validated new refresh token");

        // Инициализация нового accessToken
        Date newAccessExpiresAt = Date.from(newCreatedAt.toInstant().plus(jwtAccessTokenDuration));
        String newAccessTokenString = accessTokenGenerator.generateAccessToken(newClientUuid, newCreatedAt,
                newAccessExpiresAt);
        log.info("Generated new access token");
        
        AccessTokenBLM newAccessTokenBLM = new AccessTokenBLM(newAccessTokenString, newClientUuid, newCreatedAt,
                newAccessExpiresAt);
        accessTokenValidator.validate(newAccessTokenBLM);
        log.info("Validated new access token");

        // Конвертация токенов и обновление в репозитории
        RefreshTokenDALM refreshTokenDALM = refreshTokenConverter.toDALM(refreshTokenBLM);
        log.info("Converted refresh token");
        
        RefreshTokenDALM newRefreshTokenDALM = refreshTokenConverter.toDALM(newRefreshTokenBLM);
        log.info("Converted new refresh token");

        // Обновление RefreshToken в репозитории и возврат новых токенов
        return refreshTokenRepository.updateToken(refreshTokenDALM, newRefreshTokenDALM)
                .thenReturn(Pair.of(newAccessTokenBLM, newRefreshTokenBLM))
                .doOnSuccess(tokens -> log.info("Updated by repo refresh token"));
    }

    @Override
    public Mono<Void> validateAccessToken(AccessTokenBLM accessTokenBLM) {
        return Mono.fromRunnable(() -> accessTokenValidator.validate(accessTokenBLM));
    }

    @Override
    public Mono<Void> validateRefreshToken(RefreshTokenBLM refreshTokenBLM) {
        return Mono.fromRunnable(() -> refreshTokenValidator.validate(refreshTokenBLM));
    }

    @Override
    public Mono<Map<String, Object>> getHealthStatus() {
        return Mono.fromCallable(() -> {
            Map<String, Object> map = new HashMap<>();
            map.put("status", "OK");
            map.put("service", "auth-service");
            map.put("timestamp", System.currentTimeMillis());
            return map;
        });
    }

    @Override
    public Mono<AccessTokenBLM> validateAccessToken(String token) {
        return Mono.fromCallable(() -> {
            AccessTokenBLM accessTokenBLM = accessTokenGenerator.getAccessTokenBLM(token);
            accessTokenValidator.validate(accessTokenBLM);
            return accessTokenBLM;
        });
    }
}