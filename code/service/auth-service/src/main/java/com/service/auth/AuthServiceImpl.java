package com.service.auth;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

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

@Slf4j
@RequiredArgsConstructor
@Service
@EnableAutoConfiguration(exclude = {
        JpaRepositoriesAutoConfiguration.class
})
@EnableTransactionManagement
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
    @Transactional("atomicosTransactionManager")
    public Pair<AccessTokenBLM, RefreshTokenBLM> authorizeByEmail(String email, String password) {
        clientValidator.validateEmail(email);

        ClientBLM clientBLM = clientRepository.findByEmail(email);

        if (!clientBLM.getPassword().equals(password)) {
            throw new SecurityException("Invalid email or password");
        }

        // Инициалаизация общих полей
        UUID newClientUuid = clientBLM.getUid();
        Date newCreatedAt = new Date();

        // Инициализация нового refreshToken
        UUID newRefreshUID = UUID.randomUUID();

        Date newRefreshExpiresAt = Date.from(newCreatedAt.toInstant().plus(jwtRefreshTokenDuration));
        String newRefreshTokenString = refreshTokenGenerator.generateRefreshToken(newRefreshUID, newClientUuid,
                newCreatedAt, newRefreshExpiresAt);

        RefreshTokenBLM newRefreshTokenBLM = new RefreshTokenBLM(newRefreshTokenString, newRefreshUID, newClientUuid,
                newCreatedAt, newRefreshExpiresAt);

        refreshTokenValidator.validate(newRefreshTokenBLM);

        // Инициалищация нового accessToken
        Date newAccessExpiresAt = Date.from(newCreatedAt.toInstant().plus(jwtAccessTokenDuration));
        String newAcessTokenString = accessTokenGenerator.generateAccessToken(newClientUuid, newCreatedAt,
                newAccessExpiresAt);
        AccessTokenBLM newAccessTokenBLM = new AccessTokenBLM(newAcessTokenString, newClientUuid, newCreatedAt,
                newAccessExpiresAt);
        accessTokenValidator.validate(newAccessTokenBLM);

        // Добавление нового refreshToken в БД
        RefreshTokenDALM newRefreshTokenDALM = refreshTokenConverter.toDALM(newRefreshTokenBLM);
        refreshTokenRepository.add(newRefreshTokenDALM);

        // Возварты пары токенов
        return Pair.of(newAccessTokenBLM, newRefreshTokenBLM);

    }

    @Override
    @Transactional("atomicosTransactionManager")
    public void register(ClientBLM clientBLM) {
        clientValidator.validate(clientBLM);
        clientRepository.add(clientBLM);
    }

    @Override
    @Transactional("atomicosTransactionManager")
    public Pair<AccessTokenBLM, RefreshTokenBLM> refresh(
            RefreshTokenBLM refreshTokenBLM) {

        log.info("Validating refresh token");
        refreshTokenValidator.validate(refreshTokenBLM);
        log.info("Validated refresh token");

        // Инициалаизация общих полей
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

        // Инициалищация нового accessToken
        Date newAccessExpiresAt = Date.from(newCreatedAt.toInstant().plus(jwtAccessTokenDuration));
        String newAcessTokenString = accessTokenGenerator.generateAccessToken(newClientUuid, newCreatedAt,
                newAccessExpiresAt);
        log.info("Generated new access token");
        AccessTokenBLM newAccessTokenBLM = new AccessTokenBLM(newAcessTokenString, newClientUuid, newCreatedAt,
                newAccessExpiresAt);
        accessTokenValidator.validate(newAccessTokenBLM);
        log.info("Validated new access token");

        // Обновление RefreshToken в репозитории
        RefreshTokenDALM refreshTokenDALM = refreshTokenConverter.toDALM(refreshTokenBLM);
        log.info("Converted refresh token");
        RefreshTokenDALM newRefreshTokenDALM = refreshTokenConverter.toDALM(newRefreshTokenBLM);
        log.info("Converted new refresh token");
        refreshTokenRepository.updateToken(refreshTokenDALM, newRefreshTokenDALM);
        log.info("Updated by repo refresh token");
        // Возвращат новых токенов
        return Pair.of(newAccessTokenBLM, newRefreshTokenBLM);
    }

    @Override
    public void validateAccessToken(AccessTokenBLM accessTokenBLM) {
        accessTokenValidator.validate(accessTokenBLM);
    }

    @Override
    public void validateRefreshToken(RefreshTokenBLM refreshTokenBLM) {
        refreshTokenValidator.validate(refreshTokenBLM);
    }
}
