package com.connection.service.auth;

import static com.connection.service.auth.mother.AuthObjectMother.VALID_EMAIL;
import static com.connection.service.auth.mother.AuthObjectMother.VALID_PASSWORD;
import static com.connection.service.auth.mother.AuthObjectMother.createExpiredRefreshTokenBlm;
import static com.connection.service.auth.mother.AuthObjectMother.createValidAccessTokenBlm;
import static com.connection.service.auth.mother.AuthObjectMother.createValidClientBlm;
import static com.connection.service.auth.mother.AuthObjectMother.createValidRefreshTokenBlm;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.connection.client.converter.ClientConverter;
import com.connection.client.model.ClientBlm;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

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
            var accessTokenField = AuthServiceImpl.class
                    .getDeclaredField("jwtAccessTokenDuration");
            var refreshTokenField = AuthServiceImpl.class
                    .getDeclaredField("jwtRefreshTokenDuration");

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
        ClientBlm clientBlm = createValidClientBlm();
        AccessTokenBlm accessTokenBlm = createValidAccessTokenBlm();
        RefreshTokenBlm refreshTokenBlm = createValidRefreshTokenBlm();
        RefreshTokenDalm refreshTokenDalm = new RefreshTokenDalm();

        when(clientRepository.findByEmail(VALID_EMAIL)).thenReturn(clientBlm);
        when(refreshTokenGenerator.generateRefreshToken(any(), any(), any(),
                any())).thenReturn(refreshTokenBlm.getToken());
        when(accessTokenGenerator.generateAccessToken(any(), any(), any()))
                .thenReturn(accessTokenBlm.getToken());
        when(refreshTokenConverter.toDalm(any(RefreshTokenBlm.class)))
                .thenReturn(refreshTokenDalm);

        // Act
        Pair<AccessTokenBlm, RefreshTokenBlm> result =
                authService.authorizeByEmail(VALID_EMAIL, VALID_PASSWORD);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFirst()).isNotNull();
        assertThat(result.getSecond()).isNotNull();
        verify(clientValidator).validateEmail(VALID_EMAIL);
        verify(refreshTokenValidator).validate(any(RefreshTokenBlm.class));
        verify(accessTokenValidator).validate(any(AccessTokenBlm.class));
        verify(refreshTokenRepository).add(refreshTokenDalm);
    }

    @Test
    @DisplayName("Authorize by email - Negative: Email validation fails")
    void shouldThrowExceptionWhenEmailValidationFails() {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid email"))
                .when(clientValidator).validateEmail("invalid-email");

        // Act & Assert
        assertThatThrownBy(() -> authService.authorizeByEmail("invalid-email",
                VALID_PASSWORD)).isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Invalid email");

        verify(clientRepository, never()).findByEmail(any());
        verify(refreshTokenRepository, never()).add(any());
    }

    @Test
    @DisplayName("Register client - Positive")
    void shouldRegisterClientWhenValidData() {
        // Arrange
        ClientBlm clientBlm = createValidClientBlm();


        // Act
        authService.register(clientBlm);

        // Assert
        verify(clientValidator).validate(clientBlm);
        verify(clientRepository).add(clientBlm);
    }

    @Test
    @DisplayName("Register client - Negative: Client validation fails")
    void shouldThrowExceptionWhenClientValidationFails() {
        // Arrange
        ClientBlm invalidClient = createValidClientBlm();

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
        RefreshTokenBlm oldRefreshToken = createValidRefreshTokenBlm();
        RefreshTokenBlm newRefreshToken = createValidRefreshTokenBlm();
        AccessTokenBlm newAccessToken = createValidAccessTokenBlm();
        RefreshTokenDalm oldRefreshTokenDalm = new RefreshTokenDalm();
        RefreshTokenDalm newRefreshTokenDalm = new RefreshTokenDalm();

        when(refreshTokenGenerator.generateRefreshToken(any(), any(), any(),
                any())).thenReturn(newRefreshToken.getToken());
        when(accessTokenGenerator.generateAccessToken(any(), any(), any()))
                .thenReturn(newAccessToken.getToken());
        when(refreshTokenConverter.toDalm(any(RefreshTokenBlm.class)))
                .thenReturn(oldRefreshTokenDalm) // для первого вызова
                .thenReturn(newRefreshTokenDalm); // для второго вызова
        // Act
        Pair<AccessTokenBlm, RefreshTokenBlm> result =
                authService.refresh(oldRefreshToken);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFirst()).isNotNull();
        assertThat(result.getSecond()).isNotNull();
    }

    @Test
    @DisplayName("Refresh tokens - Negative: Invalid refresh token")
    void shouldThrowExceptionWhenRefreshTokenInvalid() {
        // Arrange
        RefreshTokenBlm invalidRefreshToken = createExpiredRefreshTokenBlm();

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
        AccessTokenBlm accessToken = createValidAccessTokenBlm();

        // Act
        authService.validateAccessToken(accessToken);

        // Assert
        verify(accessTokenValidator).validate(accessToken);
    }

    @Test
    @DisplayName("Validate access token - Negative: Invalid token")
    void shouldThrowExceptionWhenAccessTokenInvalid() {
        // Arrange
        AccessTokenBlm invalidAccessToken = createValidAccessTokenBlm();

        doThrow(new IllegalArgumentException("Expired access token"))
                .when(accessTokenValidator).validate(invalidAccessToken);

        // Act & Assert
        assertThatThrownBy(
                () -> authService.validateAccessToken(invalidAccessToken))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Expired access token");
    }

    @Test
    @DisplayName("Validate refresh token - Positive")
    void shouldValidateRefreshTokenWhenValid() {
        // Arrange
        RefreshTokenBlm refreshToken = createValidRefreshTokenBlm();

        // Act
        authService.validateRefreshToken(refreshToken);

        // Assert
        verify(refreshTokenValidator).validate(refreshToken);
    }
}
