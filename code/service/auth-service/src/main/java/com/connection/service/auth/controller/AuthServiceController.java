package com.connection.service.auth.controller;

import com.connection.client.converter.ClientConverter;
import com.connection.client.model.ClientBlm;
import com.connection.client.model.ClientDto;
import com.connection.client.validator.ClientValidator;
import com.connection.service.auth.AuthService;
import com.connection.token.converter.AccessTokenConverter;
import com.connection.token.converter.RefreshTokenConverter;
import com.connection.token.exception.BaseTokenException;
import com.connection.token.model.AccessTokenBlm;
import com.connection.token.model.AccessTokenDto;
import com.connection.token.model.RefreshTokenBlm;
import com.connection.token.model.RefreshTokenDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** . */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth Service",
        description = "Authentication and Authorization APIs")
public class AuthServiceController {

    private final AccessTokenConverter accessTokenConverter;
    private final RefreshTokenConverter refreshTokenConverter;
    private final ClientConverter clientConverter;
    private final ClientValidator clientValidator;
    private final AuthService authService;

    /** . */

    @Operation(summary = "Register new client",
            description = "Register a new client in the system")
    @ApiResponse(responseCode = "200",
            description = "Client registered successfully",
            content = @Content(schema = @Schema(
                    implementation = RegistrationResponse.class)))
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Parameter(description = "Client data",
                    required = true) @RequestBody ClientDto clientDto) {

        log.info("Registration attempt for email: {}", clientDto.getEmail());

        clientValidator.validate(clientDto);

        ClientBlm clientBlm = clientConverter.toBlm(clientDto);
        authService.register(clientBlm);

        log.info("Client registered successfully: {}", clientDto.getUid());
        return ResponseEntity.ok(new RegistrationResponse(
                "User registered successfully", clientDto.getEmail()));
    }

    /** . */

    @Operation(summary = "Login by email",
            description = "Authenticate client using email and password")
    @ApiResponse(responseCode = "201", description = "Login successful",
            content = @Content(
                    schema = @Schema(implementation = LoginResponse.class)))
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginByEmail(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials", required = true,
                    content = @Content(schema = @Schema(
                    implementation = LoginRequest.class))) @RequestBody LoginRequest loginRequest) {

        log.info("Login attempt by email: {}", loginRequest.getEmail());

        Pair<AccessTokenBlm, RefreshTokenBlm> tokens =
                authService.authorizeByEmail(loginRequest.getEmail(),
                        loginRequest.getPassword());

        log.info("Login successful for email: {}", loginRequest.getEmail());

        return ResponseEntity.ok(new LoginResponse(tokens.getFirst().getToken(),
                tokens.getSecond().getToken(), tokens.getFirst().getExpiresAt(),
                tokens.getSecond().getExpiresAt(),
                tokens.getFirst().getClientUid()));
    }

    /** . */

    @Operation(summary = "Refresh tokens",
            description = "Get new access and refresh tokens using refresh token")
    @ApiResponse(responseCode = "201",
            description = "Tokens refreshed successfully",
            content = @Content(
                    schema = @Schema(implementation = LoginResponse.class)))
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@Parameter(
            description = "Refresh token request",
            required = true) @RequestBody RefreshTokenRequest refreshRequest) {
        try {
            log.info("Token refresh attempt");
            log.info(refreshRequest.getRefreshToken());
            RefreshTokenDto refreshTokenDto =
                    new RefreshTokenDto(refreshRequest.getRefreshToken());
            RefreshTokenBlm refreshTokenBlm =
                    refreshTokenConverter.toBlm(refreshTokenDto);

            log.info(refreshTokenBlm.getToken());
            log.info(refreshTokenBlm.getClientUid().toString());

            Pair<AccessTokenBlm, RefreshTokenBlm> newTokens =
                    authService.refresh(refreshTokenBlm);

            log.info("Token refresh successful");
            return ResponseEntity
                    .ok(new LoginResponse(newTokens.getFirst().getToken(),
                            newTokens.getSecond().getToken(),
                            newTokens.getFirst().getExpiresAt(),
                            newTokens.getSecond().getExpiresAt(),
                            newTokens.getFirst().getClientUid()));
        } catch (BaseTokenException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /** . */

    @Operation(summary = "Health check", description = "Check service health")
    @ApiResponse(responseCode = "200", description = "Service is healthy",
            content = @Content(
                    schema = @Schema(implementation = HealthResponse.class)))
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        log.info(
                "Health check: status: OK, service: auth-service, timestamp: {}",
                System.currentTimeMillis());

        return ResponseEntity.ok(new HealthResponse("OK", "auth-service",
                System.currentTimeMillis()));
    }

    /** . */

    @Operation(summary = "Validate access token",
            description = "Check if access token is valid")
    @ApiResponse(responseCode = "200", description = "Token is valid",
            content = @Content(schema = @Schema(
                    implementation = ValidationResponse.class)))
    @PostMapping("/validate/access")
    public ResponseEntity<ValidationResponse> validateAccessToken(
            @Parameter(description = "Access token to validate",
                    required = true) @RequestParam String accessToken) {
        try {
            log.info("Validating access token");
            AccessTokenDto accessTokenDto = new AccessTokenDto(accessToken);

            AccessTokenBlm accessTokenBlm =
                    accessTokenConverter.toBlm(accessTokenDto);
            authService.validateAccessToken(accessTokenBlm);

            return ResponseEntity.ok(new ValidationResponse("OK"));
        } catch (BaseTokenException e) {
            return ResponseEntity.badRequest().build();
        }

    }

    /** . */

    @Operation(summary = "Validate refresh token",
            description = "Check if refresh token is valid")
    @ApiResponse(responseCode = "200", description = "Token is valid",
            content = @Content(schema = @Schema(
                    implementation = ValidationResponse.class)))
    @PostMapping("/validate/refresh")
    public ResponseEntity<ValidationResponse> validateRefreshToken(
            @Parameter(description = "Refresh token to validate",
                    required = true) @RequestParam String refreshToken) {

        log.info("Validating refresh token");
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(refreshToken);

        RefreshTokenBlm refreshTokenBlm =
                refreshTokenConverter.toBlm(refreshTokenDto);
        authService.validateRefreshToken(refreshTokenBlm);

        return ResponseEntity.ok(new ValidationResponse("OK"));
    }
}
