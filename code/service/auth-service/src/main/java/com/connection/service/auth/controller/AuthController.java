package com.connection.service.auth.controller;

import org.springframework.http.ResponseEntity;

import com.connection.client.model.ClientDTO;
import com.connection.service.auth.controller.model.HealthResponse;
import com.connection.service.auth.controller.model.LoginRequest;
import com.connection.service.auth.controller.model.LoginResponse;
import com.connection.service.auth.controller.model.RefreshTokenRequest;
import com.connection.service.auth.controller.model.RegistrationResponse;
import com.connection.service.auth.controller.model.ValidationResponse;

import reactor.core.publisher.Mono;

public interface AuthController {
    Mono<ResponseEntity<RegistrationResponse>> register(ClientDTO clientDTO);
    Mono<ResponseEntity<LoginResponse>> loginByEmail(LoginRequest loginRequest);
    Mono<ResponseEntity<LoginResponse>> refreshToken(RefreshTokenRequest refreshRequest);
    Mono<ResponseEntity<HealthResponse>> healthCheck();
    Mono<ResponseEntity<ValidationResponse>> validateAccessToken(String accessToken);
    Mono<ResponseEntity<ValidationResponse>> validateRefreshToken(String refreshToken);
}