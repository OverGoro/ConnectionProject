package com.connection.service.auth;

import com.connection.client.model.ClientBlm;
import com.connection.client.repository.ClientRepository;
import com.connection.client.validator.ClientValidator;
import com.connection.token.converter.RefreshTokenConverter;
import com.connection.token.generator.AccessTokenGenerator;
import com.connection.token.generator.RefreshTokenGenerator;
import com.connection.token.model.AccessTokenBlm;
import com.connection.token.model.RefreshTokenBlm;
import com.connection.token.model.RefreshTokenDalm;
import com.connection.token.repository.RefreshTokenRepository;
import com.connection.token.validator.AccessTokenValidator;
import com.connection.token.validator.RefreshTokenValidator;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/** . */
@Slf4j
@RequiredArgsConstructor
@Service
@EnableAutoConfiguration(exclude = {JpaRepositoriesAutoConfiguration.class})
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
    public Pair<AccessTokenBlm, RefreshTokenBlm> authorizeByEmail(String email,
            String password) {
        clientValidator.validateEmail(email);

        ClientBlm clientBlm = clientRepository.findByEmail(email);

        if (!clientBlm.getPassword().equals(password)) {
            throw new SecurityException("Invalid email or password");
        }

        // Инициалаизация общих полей
        UUID newClientUuid = clientBlm.getUid();
        Date newCreatedAt = new Date();

        // Инициализация нового refreshToken
        UUID newRefreshUid = UUID.randomUUID();

        Date newRefreshExpiresAt = Date
                .from(newCreatedAt.toInstant().plus(jwtRefreshTokenDuration));
        String newRefreshTokenString =
                refreshTokenGenerator.generateRefreshToken(newRefreshUid,
                        newClientUuid, newCreatedAt, newRefreshExpiresAt);

        RefreshTokenBlm newRefreshTokenBlm =
                new RefreshTokenBlm(newRefreshTokenString, newRefreshUid,
                        newClientUuid, newCreatedAt, newRefreshExpiresAt);

        refreshTokenValidator.validate(newRefreshTokenBlm);

        // Инициалищация нового accessToken
        Date newAccessExpiresAt = Date
                .from(newCreatedAt.toInstant().plus(jwtAccessTokenDuration));
        String newAcessTokenString = accessTokenGenerator.generateAccessToken(
                newClientUuid, newCreatedAt, newAccessExpiresAt);
        AccessTokenBlm newAccessTokenBlm =
                new AccessTokenBlm(newAcessTokenString, newClientUuid,
                        newCreatedAt, newAccessExpiresAt);
        accessTokenValidator.validate(newAccessTokenBlm);

        // Добавление нового refreshToken в БД
        RefreshTokenDalm newRefreshTokenDalm =
                refreshTokenConverter.toDalm(newRefreshTokenBlm);
        refreshTokenRepository.add(newRefreshTokenDalm);

        // Возварты пары токенов
        return Pair.of(newAccessTokenBlm, newRefreshTokenBlm);

    }

    @Override
    @Transactional("atomicosTransactionManager")
    public void register(ClientBlm clientBlm) {
        clientValidator.validate(clientBlm);
        clientRepository.add(clientBlm);
    }

    @Override
    @Transactional("atomicosTransactionManager")
    public Pair<AccessTokenBlm, RefreshTokenBlm> refresh(
            RefreshTokenBlm refreshTokenBlm) {

        log.info("Validating refresh token");
        refreshTokenValidator.validate(refreshTokenBlm);
        log.info("Validated refresh token");

        // Инициалаизация общих полей
        UUID newClientUuid = refreshTokenBlm.getClientUID();
        Date newCreatedAt = new Date();

        // Инициализация нового refreshToken
        UUID newRefreshUid = refreshTokenBlm.getUid();
        Date newRefreshExpiresAt = Date
                .from(newCreatedAt.toInstant().plus(jwtRefreshTokenDuration));
        String newRefreshTokenString =
                refreshTokenGenerator.generateRefreshToken(newRefreshUid,
                        newClientUuid, newCreatedAt, newRefreshExpiresAt);

        RefreshTokenBlm newRefreshTokenBlm =
                new RefreshTokenBlm(newRefreshTokenString, newRefreshUid,
                        newClientUuid, newCreatedAt, newRefreshExpiresAt);
        log.info("Validating new refresh token");
        refreshTokenValidator.validate(newRefreshTokenBlm);
        log.info("Validated new refresh token");

        // Инициалищация нового accessToken
        Date newAccessExpiresAt = Date
                .from(newCreatedAt.toInstant().plus(jwtAccessTokenDuration));
        String newAcessTokenString = accessTokenGenerator.generateAccessToken(
                newClientUuid, newCreatedAt, newAccessExpiresAt);
        log.info("Generated new access token");
        AccessTokenBlm newAccessTokenBlm =
                new AccessTokenBlm(newAcessTokenString, newClientUuid,
                        newCreatedAt, newAccessExpiresAt);
        accessTokenValidator.validate(newAccessTokenBlm);
        log.info("Validated new access token");

        // Обновление RefreshToken в репозитории
        RefreshTokenDalm refreshTokenDalm =
                refreshTokenConverter.toDalm(refreshTokenBlm);
        log.info("Converted refresh token");
        RefreshTokenDalm newRefreshTokenDalm =
                refreshTokenConverter.toDalm(newRefreshTokenBlm);
        log.info("Converted new refresh token");
        refreshTokenRepository.updateToken(refreshTokenDalm,
                newRefreshTokenDalm);
        log.info("Updated by repo refresh token");
        // Возвращат новых токенов
        return Pair.of(newAccessTokenBlm, newRefreshTokenBlm);
    }

    @Override
    public void validateAccessToken(AccessTokenBlm accessTokenBlm) {
        accessTokenValidator.validate(accessTokenBlm);
    }
    
    @Override
    public AccessTokenBlm validateAccessToken(String token) {
        AccessTokenBlm accessTokenBlm =
                accessTokenGenerator.getAccessTokenBlm(token);
        accessTokenValidator.validate(accessTokenBlm);
        return accessTokenBlm;
    }

    @Override
    public void validateRefreshToken(RefreshTokenBlm refreshTokenBlm) {
        refreshTokenValidator.validate(refreshTokenBlm);
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> map = new HashMap<>();
        map.put("status", "OK");
        return map;
    }
}
