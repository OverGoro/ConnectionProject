package com.service.auth;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
        try {
            log.info("Registration attempt for email: {}", clientDTO.getEmail());
            clientValidator.validate(clientDTO);

            ClientBLM clientBLM = clientConverter.toBLM(clientDTO);

            authService.register(clientBLM);

            log.info("Client registered successfully: {}", clientDTO.getEmail());
            return ResponseEntity.ok().body(Map.of(
                    "message", "User registered successfully",
                    "email", clientDTO.getEmail()));

        } catch (Exception e) {
            log.error("Registration failed for email: {}", clientDTO.getEmail(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Registration failed",
                    "message", e.getMessage()));
        }
    }

    @PostMapping("/login/email")
    public ResponseEntity<?> loginByEmail(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            log.info("Login attempt by email: {}", email);

            // Вызываем сервис авторизации
            Pair<AccessTokenBLM, RefreshTokenBLM> tokens = authService.authorizeByEmail(email, password);

            log.info("Login successful for email: {}", email);

            return ResponseEntity.ok().body(Map.of(
                    "accessToken", tokens.getFirst().getToken(),
                    "refreshToken", tokens.getSecond().getToken(),
                    "accessTokenExpiresAt", tokens.getFirst().getExpiresAt(),
                    "refreshTokenExpiresAt", tokens.getSecond().getExpiresAt(),
                    "clientUid", tokens.getFirst().getClientUID()));

        } catch (Exception e) {
            log.error("Login failed for email: {}", credentials.get("email"), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Login failed",
                    "message", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> tokenRequest) {
        try {
            String refreshToken = tokenRequest.get("refreshToken");

            log.info("Token refresh attempt");

            RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO(refreshToken);
            RefreshTokenBLM refreshTokenBLM = refreshTokenConverter.toBLM(refreshTokenDTO);

            // Вызываем сервис обновления токенов
            Pair<AccessTokenBLM, RefreshTokenBLM> newTokens = authService.refresh(refreshTokenBLM);

            log.info("Token refresh successful");

            return ResponseEntity.ok().body(Map.of(
                    "accessToken", newTokens.getFirst().getToken(),
                    "refreshToken", newTokens.getSecond().getToken(),
                    "accessTokenExpiresAt", newTokens.getFirst().getExpiresAt(),
                    "refreshTokenExpiresAt", newTokens.getSecond().getExpiresAt()));

        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Token refresh failed",
                    "message", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        log.info("Heakth check: " + "\nstatus: OK" + "\nservice: auth-service" + "\ntimestamp: "
                + System.currentTimeMillis());

        ResponseEntity<?> responseEntity = ResponseEntity.ok().body(Map.of(
                "status", "OK",
                "service", "auth-service",
                "timestamp", System.currentTimeMillis()));
        return responseEntity;
    }

    @GetMapping("/validate/token/access")
    public ResponseEntity<?> validateAccessToken(String accessToken) {
        log.info("Validating access token");
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO(accessToken);

        AccessTokenBLM accessTokenBLM = accessTokenConverter.toBLM(accessTokenDTO);
        authService.validateAccessToken(accessTokenBLM);

        ResponseEntity<?> responseEntity = ResponseEntity.ok().body(Map.of("status", "OK"));
        return responseEntity;
    }

    @GetMapping("/validate/token/refresh")
    public ResponseEntity<?> validateRefreshToken(String refreshToken) {
        log.info("Validating refresh token");
        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO(refreshToken);

        RefreshTokenBLM refreshTokenBLM = refreshTokenConverter.toBLM(refreshTokenDTO);
        authService.validateRefreshToken(refreshTokenBLM);

        ResponseEntity<?> responseEntity = ResponseEntity.ok().body(Map.of("status", "OK"));
        return responseEntity;
    }

    @GetMapping("/extract/accessTokenClientUID")
    public ResponseEntity<UUID> getAccessTokenClientUID(String accessToken) {
        log.info("Validating access token");
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO(accessToken);

        AccessTokenBLM accessTokenBLM = accessTokenConverter.toBLM(accessTokenDTO);
        authService.validateAccessToken(accessTokenBLM);

        ResponseEntity<UUID> responseEntity = ResponseEntity.ok().body(accessTokenBLM.getClientUID());
        return responseEntity;
    }

    @GetMapping("/extract/refreshTokenClientUID")
    public ResponseEntity<UUID> getRefreshTokenClientUID(String refreshToken) {
        log.info("Validating refresh token");
        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO(refreshToken);

        RefreshTokenBLM refreshTokenBLM = refreshTokenConverter.toBLM(refreshTokenDTO);
        authService.validateRefreshToken(refreshTokenBLM);

        ResponseEntity<UUID> responseEntity = ResponseEntity.ok().body(refreshTokenBLM.getClientUID());
        return responseEntity;
    }
}