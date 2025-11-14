package com.connection.service.auth.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.connection.client.converter.ClientConverter;
import com.connection.client.model.ClientBLM;
import com.connection.client.model.ClientDTO;
import com.connection.client.validator.ClientValidator;
import com.connection.service.auth.AuthService;
import com.connection.service.auth.controller.model.HealthResponse;
import com.connection.service.auth.controller.model.LoginRequest;
import com.connection.service.auth.controller.model.LoginResponse;
import com.connection.service.auth.controller.model.RefreshTokenRequest;
import com.connection.service.auth.controller.model.RegistrationResponse;
import com.connection.service.auth.controller.model.ValidationResponse;
import com.connection.token.converter.AccessTokenConverter;
import com.connection.token.converter.RefreshTokenConverter;
import com.connection.token.exception.BaseTokenException;
import com.connection.token.model.AccessTokenBLM;
import com.connection.token.model.AccessTokenDTO;
import com.connection.token.model.RefreshTokenBLM;
import com.connection.token.model.RefreshTokenDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.controller.mode", havingValue = "webflux", matchIfMissing = true)
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth Service", description = "Authentication and Authorization APIs (WebFlux)")
public class AuthServiceWebFluxController implements AuthController {

    private final AccessTokenConverter accessTokenConverter;
    private final RefreshTokenConverter refreshTokenConverter;
    private final ClientConverter clientConverter;
    private final ClientValidator clientValidator;
    private final AuthService authService;

    @Operation(summary = "Register new client", description = "Register a new client in the system")
    @ApiResponse(responseCode = "200", description = "Client registered successfully", content = @Content(schema = @Schema(implementation = RegistrationResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid client data")
    @PostMapping("/register")
    public Mono<ResponseEntity<RegistrationResponse>> register(
            @Parameter(description = "Client data", required = true) @RequestBody ClientDTO clientDTO) {
        
        log.info("Registration attempt for email: {}", clientDTO.getEmail());

        return Mono.fromCallable(() -> {
            clientValidator.validate(clientDTO);
            return clientConverter.toBLM(clientDTO);
        })
        .flatMap(authService::register)
        .then(Mono.fromCallable(() -> {
            log.info("Client registered successfully: {}", clientDTO.getUid());
            return ResponseEntity.ok(new RegistrationResponse(
                    "User registered successfully",
                    clientDTO.getEmail()));
        }))
        .onErrorResume(throwable -> {
            log.error("Registration failed for email: {}", clientDTO.getEmail(), throwable);
            return Mono.just(ResponseEntity.badRequest().build());
        });
    }

    @Operation(summary = "Login by email", description = "Authenticate client using email and password")
    @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid credentials")
    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> loginByEmail(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Login credentials", required = true, content = @Content(schema = @Schema(implementation = LoginRequest.class))) @RequestBody LoginRequest loginRequest) {
        
        log.info("Login attempt by email: {}", loginRequest.getEmail());

        return authService.authorizeByEmail(loginRequest.getEmail(), loginRequest.getPassword())
                .map(tokens -> {
                    log.info("Login successful for email: {}", loginRequest.getEmail());
                    return ResponseEntity.ok(new LoginResponse(
                            tokens.getFirst().getToken(),
                            tokens.getSecond().getToken(),
                            tokens.getFirst().getExpiresAt(),
                            tokens.getSecond().getExpiresAt(),
                            tokens.getFirst().getClientUID()));
                })
                .onErrorResume(throwable -> {
                    log.error("Login failed for email: {}", loginRequest.getEmail(), throwable);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @Operation(summary = "Refresh tokens", description = "Get new access and refresh tokens using refresh token")
    @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid refresh token")
    @PostMapping("/refresh")
    public Mono<ResponseEntity<LoginResponse>> refreshToken(
            @Parameter(description = "Refresh token request", required = true) @RequestBody RefreshTokenRequest refreshRequest) {
        
        log.info("Token refresh attempt");

        return Mono.fromCallable(() -> {
            RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO(refreshRequest.getRefreshToken());
            return refreshTokenConverter.toBLM(refreshTokenDTO);
        })
        .flatMap(authService::refresh)
        .map(newTokens -> {
            log.info("Token refresh successful");
            return ResponseEntity.ok(new LoginResponse(
                    newTokens.getFirst().getToken(),
                    newTokens.getSecond().getToken(),
                    newTokens.getFirst().getExpiresAt(),
                    newTokens.getSecond().getExpiresAt(),
                    newTokens.getFirst().getClientUID()));
        })
        .onErrorResume(BaseTokenException.class, e -> {
            log.error("Token refresh failed", e);
            return Mono.just(ResponseEntity.badRequest().build());
        });
    }

    @Operation(summary = "Health check", description = "Check service health")
    @ApiResponse(responseCode = "200", description = "Service is healthy", content = @Content(schema = @Schema(implementation = HealthResponse.class)))
    @GetMapping("/health")
    public Mono<ResponseEntity<HealthResponse>> healthCheck() {
        return authService.getHealthStatus()
                .map(healthMap -> {
                    log.info("Health check: status: OK, service: auth-service, timestamp: {}",
                            System.currentTimeMillis());
                    return ResponseEntity.ok(new HealthResponse(
                            "OK",
                            "auth-service",
                            System.currentTimeMillis()));
                });
    }

    @Operation(summary = "Validate access token", description = "Check if access token is valid")
    @ApiResponse(responseCode = "200", description = "Token is valid", content = @Content(schema = @Schema(implementation = ValidationResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid access token")
    @PostMapping("/validate/access")
    public Mono<ResponseEntity<ValidationResponse>> validateAccessToken(
            @Parameter(description = "Access token to validate", required = true) @RequestParam String accessToken) {
        
        log.info("Validating access token");

        return authService.validateAccessToken(accessToken)
                .map(accessTokenBLM -> {
                    log.info("Access token validation successful for client: {}", accessTokenBLM.getClientUID());
                    return ResponseEntity.ok(new ValidationResponse("OK"));
                })
                .onErrorResume(BaseTokenException.class, e -> {
                    log.error("Access token validation failed", e);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @Operation(summary = "Validate refresh token", description = "Check if refresh token is valid")
    @ApiResponse(responseCode = "200", description = "Token is valid", content = @Content(schema = @Schema(implementation = ValidationResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid refresh token")
    @PostMapping("/validate/refresh")
    public Mono<ResponseEntity<ValidationResponse>> validateRefreshToken(
            @Parameter(description = "Refresh token to validate", required = true) @RequestParam String refreshToken) {
        
        log.info("Validating refresh token");

        return Mono.fromCallable(() -> {
            RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO(refreshToken);
            return refreshTokenConverter.toBLM(refreshTokenDTO);
        })
        .flatMap(authService::validateRefreshToken)
        .then(Mono.fromCallable(() -> {
            log.info("Refresh token validation successful");
            return ResponseEntity.ok(new ValidationResponse("OK"));
        }))
        .onErrorResume(BaseTokenException.class, e -> {
            log.error("Refresh token validation failed", e);
            return Mono.just(ResponseEntity.badRequest().build());
        });
    }
}