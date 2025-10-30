package com.connection.service.auth;

import static com.connection.service.auth.mother.AuthObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import com.connection.client.converter.ClientConverter;
import com.connection.client.model.ClientBLM;
import com.connection.client.validator.ClientValidator;
import com.connection.service.auth.AuthServiceImpl;
import com.connection.token.converter.RefreshTokenConverter;
import com.connection.token.generator.AccessTokenGenerator;
import com.connection.token.generator.RefreshTokenGenerator;
import com.connection.token.model.AccessTokenBLM;
import com.connection.token.model.RefreshTokenBLM;
import com.connection.token.model.RefreshTokenDALM;
import com.connection.token.repository.RefreshTokenRepository;
import com.connection.token.validator.AccessTokenValidator;
import com.connection.token.validator.RefreshTokenValidator;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service Implementation Tests - London Style")
class AuthServiceImplLondonTest {

    @Mock
    private RefreshTokenConverter refreshTokenConverter;

    @Mock
    private ClientConverter clientConverter;

    @Mock
    private ClientValidator clientValidator;

    @Mock
    private RefreshTokenValidator refreshTokenValidator;

    @Mock
    private AccessTokenValidator accessTokenValidator;

    @Mock
    private RefreshTokenGenerator refreshTokenGenerator;

    @Mock
    private AccessTokenGenerator accessTokenGenerator;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private com.connection.client.repository.ClientRepository clientRepository;

    // Используем реальные Duration вместо моков
    private Duration jwtAccessTokenDuration = Duration.ofSeconds(600); // 10 minutes
    private Duration jwtRefreshTokenDuration = Duration.ofSeconds(86400); // 24 hours

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        // Устанавливаем реальные Duration через рефлексию, так как они final в
        // AuthServiceImpl
        try {
            var accessTokenField = AuthServiceImpl.class.getDeclaredField("jwtAccessTokenDuration");
            var refreshTokenField = AuthServiceImpl.class.getDeclaredField("jwtRefreshTokenDuration");

            accessTokenField.setAccessible(true);
            refreshTokenField.setAccessible(true);

            accessTokenField.set(authService, jwtAccessTokenDuration);
            refreshTokenField.set(authService, jwtRefreshTokenDuration);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set Duration fields", e);
        }
    }

    @Test
    @DisplayName("Authorize by email - Positive")
    void shouldAuthorizeByEmailWhenValidCredentials() {
        // Arrange
        ClientBLM clientBLM = createValidClientBLM();
        AccessTokenBLM accessTokenBLM = createValidAccessTokenBLM();
        RefreshTokenBLM refreshTokenBLM = createValidRefreshTokenBLM();
        RefreshTokenDALM refreshTokenDALM = new RefreshTokenDALM();

        when(clientRepository.findByEmail(VALID_EMAIL)).thenReturn(clientBLM);
        when(refreshTokenGenerator.generateRefreshToken(any(), any(), any(), any()))
                .thenReturn(refreshTokenBLM.getToken());
        when(accessTokenGenerator.generateAccessToken(any(), any(), any()))
                .thenReturn(accessTokenBLM.getToken());
        when(refreshTokenConverter.toDALM(any(RefreshTokenBLM.class))).thenReturn(refreshTokenDALM);

        // Act
        Pair<AccessTokenBLM, RefreshTokenBLM> result = authService.authorizeByEmail(VALID_EMAIL, VALID_PASSWORD);

        // Assert
        assertThat(result).isNull();
        assertThat(result.getFirst()).isNotNull();
        assertThat(result.getSecond()).isNotNull();
        verify(clientValidator).validateEmail(VALID_EMAIL);
        verify(refreshTokenValidator).validate(any(RefreshTokenBLM.class));
        verify(accessTokenValidator).validate(any(AccessTokenBLM.class));
        verify(refreshTokenRepository).add(refreshTokenDALM);
    }

    @Test
    @DisplayName("Authorize by email - Negative: Email validation fails")
    void shouldThrowExceptionWhenEmailValidationFails() {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid email"))
                .when(clientValidator).validateEmail("invalid-email");

        // Act & Assert
        assertThatThrownBy(() -> authService.authorizeByEmail("invalid-email", VALID_PASSWORD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email");

        verify(clientRepository, never()).findByEmail(any());
        verify(refreshTokenRepository, never()).add(any());
    }

    @Test
    @DisplayName("Register client - Positive")
    void shouldRegisterClientWhenValidData() {
        // Arrange
        ClientBLM clientBLM = createValidClientBLM();


        // Act
        authService.register(clientBLM);

        // Assert
        verify(clientValidator).validate(clientBLM);
        verify(clientRepository).add(clientBLM);
    }

    @Test
    @DisplayName("Register client - Negative: Client validation fails")
    void shouldThrowExceptionWhenClientValidationFails() {
        // Arrange
        ClientBLM invalidClient = createValidClientBLM();

        doThrow(new IllegalArgumentException("Invalid client data"))
                .when(clientValidator).validate(invalidClient);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(invalidClient))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid client data");

        verify(clientRepository, never()).add(any());
    }

    @Test
    @DisplayName("Refresh tokens - Positive")
    void shouldRefreshTokensWhenValidRefreshToken() {
        // Arrange
        RefreshTokenBLM oldRefreshToken = createValidRefreshTokenBLM();
        RefreshTokenBLM newRefreshToken = createValidRefreshTokenBLM();
        AccessTokenBLM newAccessToken = createValidAccessTokenBLM();
        RefreshTokenDALM oldRefreshTokenDALM = new RefreshTokenDALM();
        RefreshTokenDALM newRefreshTokenDALM = new RefreshTokenDALM();

        when(refreshTokenGenerator.generateRefreshToken(any(), any(), any(), any()))
                .thenReturn(newRefreshToken.getToken());
        when(accessTokenGenerator.generateAccessToken(any(), any(), any()))
                .thenReturn(newAccessToken.getToken());
        when(refreshTokenConverter.toDALM(any(RefreshTokenBLM.class)))
                .thenReturn(oldRefreshTokenDALM) // для первого вызова
                .thenReturn(newRefreshTokenDALM); // для второго вызова
        // Act
        Pair<AccessTokenBLM, RefreshTokenBLM> result = authService.refresh(oldRefreshToken);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFirst()).isNotNull();
        assertThat(result.getSecond()).isNotNull();
    }

    @Test
    @DisplayName("Refresh tokens - Negative: Invalid refresh token")
    void shouldThrowExceptionWhenRefreshTokenInvalid() {
        // Arrange
        RefreshTokenBLM invalidRefreshToken = createExpiredRefreshTokenBLM();

        doThrow(new IllegalArgumentException("Expired refresh token"))
                .when(refreshTokenValidator).validate(invalidRefreshToken);

        // Act & Assert
        assertThatThrownBy(() -> authService.refresh(invalidRefreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expired refresh token");

        verify(refreshTokenRepository, never()).updateToken(any(), any());
    }

    @Test
    @DisplayName("Validate access token - Positive")
    void shouldValidateAccessTokenWhenValid() {
        // Arrange
        AccessTokenBLM accessToken = createValidAccessTokenBLM();

        // Act
        authService.validateAccessToken(accessToken);

        // Assert
        verify(accessTokenValidator).validate(accessToken);
    }

    @Test
    @DisplayName("Validate access token - Negative: Invalid token")
    void shouldThrowExceptionWhenAccessTokenInvalid() {
        // Arrange
        AccessTokenBLM invalidAccessToken = createValidAccessTokenBLM();

        doThrow(new IllegalArgumentException("Expired access token"))
                .when(accessTokenValidator).validate(invalidAccessToken);

        // Act & Assert
        assertThatThrownBy(() -> authService.validateAccessToken(invalidAccessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expired access token");
    }

    @Test
    @DisplayName("Validate refresh token - Positive")
    void shouldValidateRefreshTokenWhenValid() {
        // Arrange
        RefreshTokenBLM refreshToken = createValidRefreshTokenBLM();

        // Act
        authService.validateRefreshToken(refreshToken);

        // Assert
        verify(refreshTokenValidator).validate(refreshToken);
    }
}
