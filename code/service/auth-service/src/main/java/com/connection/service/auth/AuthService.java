package com.connection.service.auth;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.util.Pair;

import com.connection.client.model.ClientBLM;
import com.connection.token.model.AccessTokenBLM;
import com.connection.token.model.RefreshTokenBLM;

import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<Pair<AccessTokenBLM, RefreshTokenBLM>> authorizeByEmail(String email, String password);
    Mono<Void> register(ClientBLM clientBLM);
    Mono<Pair<AccessTokenBLM, RefreshTokenBLM>> refresh(RefreshTokenBLM refreshTokenBLM);
    Mono<Void> validateAccessToken(AccessTokenBLM accessTokenBLM);
    Mono<Void> validateRefreshToken(RefreshTokenBLM refreshTokenBLM);
    Mono<AccessTokenBLM> validateAccessToken(String token);
    Mono<Map<String, Object>> getHealthStatus();

    Mono<Void> deleteUserData(UUID clientUid);
}