package com.service.device.auth;

import static com.service.device.auth.mother.DeviceTokenObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import com.connection.device.token.converter.DeviceAccessTokenConverter;
import com.connection.device.token.converter.DeviceTokenConverter;
import com.connection.device.token.exception.DeviceAccessTokenExistsException;
import com.connection.device.token.exception.DeviceAccessTokenNotFoundException;
import com.connection.device.token.exception.DeviceTokenAlreadyExistsException;
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

    private Duration deviceTokenDuration = Duration.ofSeconds(2592000); // 30 days
    private Duration deviceAccessTokenDuration = Duration.ofSeconds(3600); // 1 hour

    @InjectMocks
    private DeviceAuthServiceImpl deviceAuthService;

    @BeforeEach
    void setUp() {
        // Устанавливаем реальные Duration через рефлексию
        try {
            var deviceTokenDurationField = DeviceAuthServiceImpl.class.getDeclaredField("deviceTokenDuration");
            var deviceAccessTokenDurationField = DeviceAuthServiceImpl.class.getDeclaredField("deviceAccessTokenDuration");
            
            deviceTokenDurationField.setAccessible(true);
            deviceAccessTokenDurationField.setAccessible(true);
            
            deviceTokenDurationField.set(deviceAuthService, deviceTokenDuration);
            deviceAccessTokenDurationField.set(deviceAuthService, deviceAccessTokenDuration);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set Duration fields", e);
        }
    }

    @Test
    @DisplayName("Create device token - Positive")
    void shouldCreateDeviceTokenWhenValidDeviceUid() {
        // Arrange
        UUID deviceUid = UUID.randomUUID();
        String generatedToken = "generated.jwt.token";
        DeviceTokenDALM deviceTokenDALM = createValidDeviceTokenDALM();

        when(deviceTokenRepository.existsByDeviceUid(deviceUid)).thenReturn(false);
        when(deviceTokenGenerator.generateDeviceToken(any(UUID.class), any(Date.class), any(Date.class)))
            .thenReturn(generatedToken);
        when(deviceTokenConverter.toDALM(any(DeviceTokenBLM.class))).thenReturn(deviceTokenDALM);

        // Act
        DeviceTokenBLM result = deviceAuthService.createDeviceToken(deviceUid);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(generatedToken);
        verify(deviceTokenValidator).validate(any(DeviceTokenBLM.class));
        verify(deviceTokenRepository).add(deviceTokenDALM);
    }

    @Test
    @DisplayName("Create device token - Negative: Token already exists")
    void shouldThrowExceptionWhenDeviceTokenAlreadyExists() {
        // Arrange
        UUID deviceUid = UUID.randomUUID();

        when(deviceTokenRepository.existsByDeviceUid(deviceUid)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> deviceAuthService.createDeviceToken(deviceUid))
            .isInstanceOf(DeviceTokenAlreadyExistsException.class);
        verify(deviceTokenRepository, never()).add(any());
    }

    @Test
    @DisplayName("Get device token - Positive")
    void shouldGetDeviceTokenWhenValidDeviceUid() {
        // Arrange
        UUID deviceUid = UUID.randomUUID();
        DeviceTokenDALM deviceTokenDALM = createValidDeviceTokenDALM();
        DeviceTokenBLM deviceTokenBLM = createValidDeviceTokenBLM();

        when(deviceTokenRepository.findByDeviceUid(deviceUid)).thenReturn(deviceTokenDALM);
        when(deviceTokenConverter.toBLM(deviceTokenDALM)).thenReturn(deviceTokenBLM);

        // Act
        DeviceTokenBLM result = deviceAuthService.getDeviceToken(deviceUid);

        // Assert
        assertThat(result).isNotNull().isEqualTo(deviceTokenBLM);
        verify(deviceTokenValidator).validate(deviceTokenBLM);
    }

    @Test
    @DisplayName("Get device token - Negative: Token not found")
    void shouldThrowExceptionWhenDeviceTokenNotFound() {
        // Arrange
        UUID deviceUid = UUID.randomUUID();

        when(deviceTokenRepository.findByDeviceUid(deviceUid))
            .thenThrow(new DeviceTokenNotFoundException("Token not found"));

        // Act & Assert
        assertThatThrownBy(() -> deviceAuthService.getDeviceToken(deviceUid))
            .isInstanceOf(DeviceTokenNotFoundException.class);
    }

    @Test
    @DisplayName("Revoke device token - Positive")
    void shouldRevokeDeviceTokenWhenValidDeviceUid() {
        // Arrange
        UUID deviceUid = UUID.randomUUID();
        DeviceTokenDALM deviceTokenDALM = createValidDeviceTokenDALM();

        when(deviceTokenRepository.findByDeviceUid(deviceUid)).thenReturn(deviceTokenDALM);

        // Act
        deviceAuthService.revokeDeviceToken(deviceUid);

        // Assert
        verify(deviceTokenRepository).revokeByDeviceUid(deviceUid);
        verify(deviceAccessTokenRepository).revokeByDeviceTokenUid(deviceTokenDALM.getUid());
    }

    @Test
    @DisplayName("Create device access token - Positive")
    void shouldCreateDeviceAccessTokenWhenValidDeviceToken() {
        // Arrange
        DeviceTokenBLM deviceToken = createValidDeviceTokenBLM();
        String generatedAccessToken = "generated.access.jwt.token";
        DeviceAccessTokenDALM deviceAccessTokenDALM = createValidDeviceAccessTokenDALM();

        when(deviceAccessTokenRepository.hasDeviceAccessToken(deviceToken.getUid())).thenReturn(false);
        when(deviceAccessTokenGenerator.generateDeviceAccessToken(any(UUID.class), any(Date.class), any(Date.class)))
            .thenReturn(generatedAccessToken);
        when(deviceAccessTokenConverter.toDALM(any(DeviceAccessTokenBLM.class))).thenReturn(deviceAccessTokenDALM);

        // Act
        Pair<DeviceAccessTokenBLM, DeviceTokenBLM> result = deviceAuthService.createDeviceAccessToken(deviceToken);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFirst().getToken()).isEqualTo(generatedAccessToken);
        assertThat(result.getSecond()).isEqualTo(deviceToken);
        verify(deviceTokenValidator).validate(deviceToken);
        verify(deviceAccessTokenValidator).validate(any(DeviceAccessTokenBLM.class));
        verify(deviceAccessTokenRepository).add(deviceAccessTokenDALM);
    }

    @Test
    @DisplayName("Create device access token - Negative: Active access token exists")
    void shouldThrowExceptionWhenActiveAccessTokenExists() {
        // Arrange
        DeviceTokenBLM deviceToken = createValidDeviceTokenBLM();

        when(deviceAccessTokenRepository.hasDeviceAccessToken(deviceToken.getUid())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> deviceAuthService.createDeviceAccessToken(deviceToken))
            .isInstanceOf(DeviceAccessTokenExistsException.class);
        verify(deviceTokenValidator).validate(deviceToken);
        verify(deviceAccessTokenRepository, never()).add(any());
    }

    @Test
    @DisplayName("Refresh device access token - Positive")
    void shouldRefreshDeviceAccessTokenWhenValid() {
        // Arrange
        DeviceAccessTokenBLM oldAccessToken = createValidDeviceAccessTokenBLM();
        String newGeneratedToken = "new.generated.access.token";
        DeviceAccessTokenDALM newAccessTokenDALM = createValidDeviceAccessTokenDALM();

        when(deviceAccessTokenGenerator.generateDeviceAccessToken(any(UUID.class), any(Date.class), any(Date.class)))
            .thenReturn(newGeneratedToken);
        when(deviceAccessTokenConverter.toDALM(any(DeviceAccessTokenBLM.class))).thenReturn(newAccessTokenDALM);

        // Act
        DeviceAccessTokenBLM result = deviceAuthService.refreshDeviceAccessToken(oldAccessToken);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(newGeneratedToken);
        verify(deviceAccessTokenValidator).validate(oldAccessToken);
        verify(deviceAccessTokenValidator, times(2)).validate(any(DeviceAccessTokenBLM.class));
        verify(deviceAccessTokenRepository).revoke(oldAccessToken.getUid());
        verify(deviceAccessTokenRepository).add(newAccessTokenDALM);
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
    @DisplayName("Validate device token - Negative: Invalid token")
    void shouldThrowExceptionWhenDeviceTokenInvalid() {
        // Arrange
        DeviceTokenBLM invalidToken = createValidDeviceTokenBLM();
        invalidToken.setToken("");

        doThrow(new IllegalArgumentException("Invalid token"))
            .when(deviceTokenValidator).validate(invalidToken);

        // Act & Assert
        assertThatThrownBy(() -> deviceAuthService.validateDeviceToken(invalidToken))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid token");
    }

    @Test
    @DisplayName("Extract device UID from device token - Positive")
    void shouldReturnDeviceUidFromDeviceToken() {
        // Arrange
        DeviceTokenBLM deviceToken = createValidDeviceTokenBLM();

        // Act
        UUID result = deviceAuthService.extractDeviceUidFromToken(deviceToken);

        // Assert
        assertThat(result).isEqualTo(deviceToken.getDeviceUid());
        verify(deviceTokenValidator).validate(deviceToken);
    }

    @Test
    @DisplayName("Extract device UID from access token - Positive")
    void shouldReturnDeviceUidFromAccessToken() {
        // Arrange
        DeviceAccessTokenBLM accessToken = createValidDeviceAccessTokenBLM();
        DeviceTokenDALM deviceTokenDALM = createValidDeviceTokenDALM();

        when(deviceTokenRepository.findByUid(accessToken.getDeviceTokenUid())).thenReturn(deviceTokenDALM);

        // Act
        UUID result = deviceAuthService.extractDeviceUidFromAccessToken(accessToken);

        // Assert
        assertThat(result).isEqualTo(deviceTokenDALM.getDeviceUid());
        verify(deviceAccessTokenValidator).validate(accessToken);
    }

    @Test
    @DisplayName("Extract device UID from access token - Negative: Device token not found")
    void shouldThrowExceptionWhenDeviceTokenNotFoundForAccessToken() {
        // Arrange
        DeviceAccessTokenBLM accessToken = createValidDeviceAccessTokenBLM();

        when(deviceTokenRepository.findByUid(accessToken.getDeviceTokenUid()))
            .thenThrow(new DeviceTokenNotFoundException("Device token not found"));

        // Act & Assert
        assertThatThrownBy(() -> deviceAuthService.extractDeviceUidFromAccessToken(accessToken))
            .isInstanceOf(DeviceTokenNotFoundException.class);
        verify(deviceAccessTokenValidator).validate(accessToken);
    }
}