package com.service.auth.controller;

import java.util.UUID;

import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.connection.client.converter.ClientConverter;
import com.connection.client.model.ClientBLM;
import com.connection.client.model.ClientDTO;
import com.connection.client.validator.ClientValidator;
import com.connection.token.converter.AccessTokenConverter;
import com.connection.token.converter.RefreshTokenConverter;
import com.connection.token.model.AccessTokenBLM;
import com.connection.token.model.AccessTokenDTO;
import com.connection.token.model.RefreshTokenBLM;
import com.connection.token.model.RefreshTokenDTO;
import com.service.auth.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth-service")
public class AuthServiceController {
    private final AccessTokenConverter accessTokenConverter;
    private final RefreshTokenConverter refreshTokenConverter;
    private final ClientConverter clientConverter;

    private final ClientValidator clientValidator;

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody ClientDTO clientDTO) {
        log.info("Registration attempt for email: {}", clientDTO.getEmail());
        clientValidator.validate(clientDTO);

        ClientBLM clientBLM = clientConverter.toBLM(clientDTO);
        authService.register(clientBLM);

        log.info("Client registered successfully: {}", clientDTO.getEmail());
        return ResponseEntity.ok().body(new RegistrationResponse(
                "User registered successfully",
                clientDTO.getEmail()));

    }

    @PostMapping("/login/email")
    public ResponseEntity<?> loginByEmail(@RequestBody LoginRequest loginRequest) {
        log.info("Login attempt by email: {}", loginRequest.getEmail());

        Pair<AccessTokenBLM, RefreshTokenBLM> tokens = authService.authorizeByEmail(
                loginRequest.getEmail(), loginRequest.getPassword());

        log.info("Login successful for email: {}", loginRequest.getEmail());

        return ResponseEntity.ok().body(new LoginResponse(
                tokens.getFirst().getToken(),
                tokens.getSecond().getToken(),
                tokens.getFirst().getExpiresAt(),
                tokens.getSecond().getExpiresAt(),
                tokens.getFirst().getClientUID()));

    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshRequest) {
        log.info("Token refresh attempt");

        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO(refreshRequest.getRefreshToken());
        RefreshTokenBLM refreshTokenBLM = refreshTokenConverter.toBLM(refreshTokenDTO);

        Pair<AccessTokenBLM, RefreshTokenBLM> newTokens = authService.refresh(refreshTokenBLM);

        log.info("Token refresh successful");

        return ResponseEntity.ok().body(new LoginResponse(
                newTokens.getFirst().getToken(),
                newTokens.getSecond().getToken(),
                newTokens.getFirst().getExpiresAt(),
                newTokens.getSecond().getExpiresAt(),
                newTokens.getFirst().getClientUID()));

    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        log.info("Health check: \nstatus: OK\nservice: auth-service\ntimestamp: {}",
                System.currentTimeMillis());

        return ResponseEntity.ok().body(new HealthResponse(
                "OK",
                "auth-service",
                System.currentTimeMillis()));
    }

    @GetMapping("/validate/token/access")
    public ResponseEntity<?> validateAccessToken(@RequestParam String accessToken) {
        log.info("Validating access token");
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO(accessToken);

        AccessTokenBLM accessTokenBLM = accessTokenConverter.toBLM(accessTokenDTO);
        authService.validateAccessToken(accessTokenBLM);

        return ResponseEntity.ok().body(new ValidationResponse("OK"));
    }

    @GetMapping("/validate/token/refresh")
    public ResponseEntity<?> validateRefreshToken(@RequestParam String refreshToken) {
        log.info("Validating refresh token");
        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO(refreshToken);

        RefreshTokenBLM refreshTokenBLM = refreshTokenConverter.toBLM(refreshTokenDTO);
        authService.validateRefreshToken(refreshTokenBLM);

        return ResponseEntity.ok().body(new ValidationResponse("OK"));
    }

    @GetMapping("/extract/accessTokenClientUID")
    public ResponseEntity<UUID> getAccessTokenClientUID(@RequestParam String accessToken) {
        log.info("Extracting client UID from access token");
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO(accessToken);

        AccessTokenBLM accessTokenBLM = accessTokenConverter.toBLM(accessTokenDTO);
        authService.validateAccessToken(accessTokenBLM);

        return ResponseEntity.ok().body(accessTokenBLM.getClientUID());
    }

    @GetMapping("/extract/refreshTokenClientUID")
    public ResponseEntity<UUID> getRefreshTokenClientUID(@RequestParam String refreshToken) {
        log.info("Extracting client UID from refresh token");
        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO(refreshToken);

        RefreshTokenBLM refreshTokenBLM = refreshTokenConverter.toBLM(refreshTokenDTO);
        authService.validateRefreshToken(refreshTokenBLM);

        return ResponseEntity.ok().body(refreshTokenBLM.getClientUID());
    }
}