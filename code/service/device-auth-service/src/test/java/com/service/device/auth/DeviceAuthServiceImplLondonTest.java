package com.service.device.auth;

import static com.service.device.auth.mother.DeviceTokenObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.connection.device.model.DeviceDALM;
import com.connection.device.repository.DeviceRepository;
import com.connection.device.token.converter.DeviceAccessTokenConverter;
import com.connection.device.token.converter.DeviceTokenConverter;
import com.connection.device.token.exception.DeviceAccessTokenExistsException;
import com.connection.device.token.exception.DeviceAccessTokenNotFoundException;
import com.connection.device.token.exception.DeviceTokenNotFoundException;
import com.connection.device.token.generator.DeviceAccessTokenGenerator;
import com.connection.device.token.generator.DeviceTokenGenerator;
import com.connection.device.token.model.DeviceAccessTokenBLM;
import com.connection.device.token.model.DeviceAccessTokenDALM;
import com.connection.device.token.model.DeviceTokenBLM;
import com.connection.device.token.model.DeviceTokenDALM;
import com.connection.device.token.repository.DeviceAccessTokenRepository;
import com.connection.device.token.repository.DeviceTokenRepository;
import com.connection.device.token.validator.DeviceAccessTokenValidator;
import com.connection.device.token.validator.DeviceTokenValidator;

@ExtendWith(MockitoExtension.class)
@DisplayName("Device Auth Service Implementation Tests - London Style")
class DeviceAuthServiceImplLondonTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Mock
    private DeviceAccessTokenRepository deviceAccessTokenRepository;

    @Mock
    private DeviceTokenConverter deviceTokenConverter;

    @Mock
    private DeviceAccessTokenConverter deviceAccessTokenConverter;

    @Mock
    private DeviceTokenValidator deviceTokenValidator;

    @Mock
    private DeviceAccessTokenValidator deviceAccessTokenValidator;

    @Mock
    private DeviceTokenGenerator deviceTokenGenerator;

    @Mock
    private DeviceAccessTokenGenerator deviceAccessTokenGenerator;

    // Используем реальные Duration вместо моков
    private Duration jwtAccessTokenDuration = Duration.ofSeconds(3600); // 1 hour
    private Duration jwtRefreshTokenDuration = Duration.ofSeconds(86400); // 24 hours

    @InjectMocks
    private DeviceAuthServiceImpl deviceAuthService;

    @BeforeEach
    void setUp() {
        // Устанавливаем реальные Duration через рефлексию
        try {
            var accessTokenField = DeviceAuthServiceImpl.class.getDeclaredField("jwtAccessTokenDuration");
            var refreshTokenField = DeviceAuthServiceImpl.class.getDeclaredField("jwtRefreshTokenDuration");
            
            accessTokenField.setAccessible(true);
            refreshTokenField.setAccessible(true);
            
            accessTokenField.set(deviceAuthService, jwtAccessTokenDuration);
            refreshTokenField.set(deviceAuthService, jwtRefreshTokenDuration);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set Duration fields", e);
        }
    }

    @Test
    @DisplayName("Authorize by token - Positive")
    void shouldAuthorizeByTokenWhenValidDeviceToken() {
        // Arrange
        DeviceTokenBLM deviceToken = createValidDeviceTokenBLM();
        DeviceDALM device = createValidDeviceDALM();
        DeviceAccessTokenBLM deviceAccessTokenBLM = createValidDeviceAccessTokenBLM();
        DeviceAccessTokenDALM deviceAccessTokenDALM = createValidDeviceAccessTokenDALM();

        when(deviceRepository.findByUid(deviceToken.getDeviceUid())).thenReturn(device);
        when(deviceAccessTokenGenerator.generateDeviceAccessToken(any(), any(), any()))
            .thenReturn(deviceAccessTokenBLM.getToken());
        when(deviceAccessTokenGenerator.getDeviceAccessTokenBLM(deviceAccessTokenBLM.getToken()))
            .thenReturn(deviceAccessTokenBLM);
        when(deviceAccessTokenConverter.toDALM(deviceAccessTokenBLM)).thenReturn(deviceAccessTokenDALM);
        when(deviceAccessTokenRepository.findByDeviceTokenUid(deviceToken.getUid()))
            .thenThrow(new DeviceTokenNotFoundException("Not found"));

        // Act
        DeviceAccessTokenBLM result = deviceAuthService.authorizeByToken(deviceToken);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(VALID_DEVICE_ACCESS_TOKEN_STRING);
        verify(deviceTokenValidator).validate(deviceToken);
        verify(deviceAccessTokenRepository).add(deviceAccessTokenDALM);
    }

    @Test
    @DisplayName("Authorize by token - Negative: Active access token exists")
    void shouldThrowExceptionWhenActiveAccessTokenExists() {
        // Arrange
        DeviceTokenBLM deviceToken = createValidDeviceTokenBLM();
        DeviceDALM device = createValidDeviceDALM();
        DeviceAccessTokenDALM existingToken = createValidDeviceAccessTokenDALM();

        when(deviceRepository.findByUid(deviceToken.getDeviceUid())).thenReturn(device);
        when(deviceAccessTokenRepository.findByDeviceTokenUid(deviceToken.getUid())).thenReturn(existingToken);

        // Act & Assert
        assertThatThrownBy(() -> deviceAuthService.authorizeByToken(deviceToken))
            .isInstanceOf(DeviceAccessTokenExistsException.class);
        verify(deviceTokenValidator).validate(deviceToken);
        verify(deviceAccessTokenRepository, never()).add(any());
    }

    @Test
    @DisplayName("Authorize by token - Negative: Device token validation fails")
    void shouldThrowExceptionWhenDeviceTokenValidationFails() {
        // Arrange
        DeviceTokenBLM invalidDeviceToken = createValidDeviceTokenBLM();
        invalidDeviceToken.setToken("");

        doThrow(new IllegalArgumentException("Invalid token"))
            .when(deviceTokenValidator).validate(invalidDeviceToken);

        // Act & Assert
        assertThatThrownBy(() -> deviceAuthService.authorizeByToken(invalidDeviceToken))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid token");

        verify(deviceRepository, never()).findByUid(any());
        verify(deviceAccessTokenRepository, never()).add(any());
    }

    @Test
    @DisplayName("Validate device access token - Positive")
    void shouldValidateDeviceAccessTokenWhenValid() {
        // Arrange
        DeviceAccessTokenBLM deviceAccessToken = createValidDeviceAccessTokenBLM();

        // Act
        deviceAuthService.validateDeviceAccessToken(deviceAccessToken);

        // Assert
        verify(deviceAccessTokenValidator).validate(deviceAccessToken);
    }

    @Test
    @DisplayName("Validate device access token - Negative: Invalid token")
    void shouldThrowExceptionWhenDeviceAccessTokenInvalid() {
        // Arrange
        DeviceAccessTokenBLM invalidToken = createExpiredDeviceAccessTokenBLM();

        doThrow(new IllegalArgumentException("Expired token"))
            .when(deviceAccessTokenValidator).validate(invalidToken);

        // Act & Assert
        assertThatThrownBy(() -> deviceAuthService.validateDeviceAccessToken(invalidToken))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Expired token");
    }

    @Test
    @DisplayName("Validate device token - Positive")
    void shouldValidateDeviceTokenWhenValid() {
        // Arrange
        DeviceTokenBLM deviceToken = createValidDeviceTokenBLM();

        // Act
        deviceAuthService.validateDeviceToken(deviceToken);

        // Assert
        verify(deviceTokenValidator).validate(deviceToken);
    }

    @Test
    @DisplayName("Get device UID from access token - Positive")
    void shouldReturnDeviceUidFromAccessToken() {
        // Arrange
        DeviceAccessTokenBLM accessToken = createValidDeviceAccessTokenBLM();
        DeviceAccessTokenDALM accessTokenDALM = createValidDeviceAccessTokenDALM();
        DeviceTokenDALM deviceTokenDALM = createValidDeviceTokenDALM();

        when(deviceAccessTokenRepository.findByToken(accessToken.getToken())).thenReturn(accessTokenDALM);
        when(deviceTokenRepository.findByUid(accessTokenDALM.getDeviceTokenUid())).thenReturn(deviceTokenDALM);

        // Act
        UUID result = deviceAuthService.getDeviceUid(accessToken);

        // Assert
        assertThat(result).isEqualTo(DEVICE_UID);
        verify(deviceAccessTokenValidator).validate(accessToken);
    }

    @Test
    @DisplayName("Get device UID from access token - Negative: Token not found")
    void shouldThrowExceptionWhenAccessTokenNotFound() {
        // Arrange
        DeviceAccessTokenBLM accessToken = createValidDeviceAccessTokenBLM();

        when(deviceAccessTokenRepository.findByToken(accessToken.getToken()))
            .thenThrow(new DeviceAccessTokenNotFoundException("Token not found"));

        // Act & Assert
        assertThatThrownBy(() -> deviceAuthService.getDeviceUid(accessToken))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Failed to extract device UID from token");

        verify(deviceAccessTokenValidator).validate(accessToken);
    }

    @Test
    @DisplayName("Get device UID from device token - Positive")
    void shouldReturnDeviceUidFromDeviceToken() {
        // Arrange
        DeviceTokenBLM deviceToken = createValidDeviceTokenBLM();
        DeviceTokenDALM deviceTokenDALM = createValidDeviceTokenDALM();

        when(deviceTokenRepository.findByToken(deviceToken.getToken())).thenReturn(deviceTokenDALM);

        // Act
        UUID result = deviceAuthService.getDeviceUid(deviceToken);

        // Assert
        assertThat(result).isEqualTo(DEVICE_UID);
        verify(deviceTokenValidator).validate(deviceToken);
    }

    @Test
    @DisplayName("Get device UID from device token - Negative: Token not found")
    void shouldThrowExceptionWhenDeviceTokenNotFound() {
        // Arrange
        DeviceTokenBLM deviceToken = createValidDeviceTokenBLM();

        when(deviceTokenRepository.findByToken(deviceToken.getToken()))
            .thenThrow(new DeviceTokenNotFoundException("Token not found"));

        // Act & Assert
        assertThatThrownBy(() -> deviceAuthService.getDeviceUid(deviceToken))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Failed to extract device UID from token");

        verify(deviceTokenValidator).validate(deviceToken);
    }
}