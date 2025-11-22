package com.connection.device;

import static com.connection.device.mother.DeviceObjectMother.CLIENT_UUID;
import static com.connection.device.mother.DeviceObjectMother.DEVICE_UUID;
import static com.connection.device.mother.DeviceObjectMother.createValidDeviceBlm;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.connection.auth.events.responses.HealthCheckResponse;

import com.connection.device.exception.DeviceAlreadyExistsException;
import com.connection.device.model.DeviceBlm;
import com.connection.device.repository.DeviceRepository;
import com.connection.device.validator.DeviceValidator;
import com.connection.service.auth.AuthService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Device Service Implementation Tests - Kafka Version")
class DeviceServiceImplTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceValidator deviceValidator;

    @Mock
    private AuthService authKafkaClient;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private DeviceServiceApiImpl deviceService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    private void setupAuthentication(UUID clientUid) {
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(clientUid, null, Collections.emptyList());
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    @DisplayName("Create device - Positive")
    void shouldCreateDeviceWhenValidData() {
        // Arrange
        DeviceBlm deviceBlm = createValidDeviceBlm();
        
        setupAuthentication(CLIENT_UUID);
        when(deviceRepository.existsByClientAndName(CLIENT_UUID, "Test Device")).thenReturn(false);

        // Act
        DeviceBlm result = deviceService.createDevice(deviceBlm);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(DEVICE_UUID);
        verify(deviceValidator).validate(deviceBlm);
        verify(deviceRepository).add(deviceBlm);
    }

    @Test
    @DisplayName("Create device - Negative: Client UID mismatch")
    void shouldThrowExceptionWhenClientUidMismatch() {
        // Arrange
        UUID differentClientUuid = UUID.randomUUID();
        DeviceBlm deviceBlm = new DeviceBlm(
                DEVICE_UUID,
                differentClientUuid,
                "Test Device",
                "Test Description");

        setupAuthentication(CLIENT_UUID);

        // Act & Assert
        assertThatThrownBy(() -> deviceService.createDevice(deviceBlm))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Client UID from token doesn't match device client UID");

        verify(deviceValidator).validate(deviceBlm);
        verify(deviceRepository, never()).add(any());
    }

    @Test
    @DisplayName("Create device - Negative: Device already exists")
    void shouldThrowExceptionWhenDeviceAlreadyExists() {
        // Arrange
        DeviceBlm deviceBlm = createValidDeviceBlm();
        
        setupAuthentication(CLIENT_UUID);
        when(deviceRepository.existsByClientAndName(CLIENT_UUID, "Test Device")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> deviceService.createDevice(deviceBlm))
                .isInstanceOf(DeviceAlreadyExistsException.class);

        verify(deviceValidator).validate(deviceBlm);
        verify(deviceRepository, never()).add(any());
    }

    @Test
    @DisplayName("Get device - Positive")
    void shouldGetDeviceWhenValidRequest() {
        // Arrange
        DeviceBlm deviceBlm = createValidDeviceBlm();

        setupAuthentication(CLIENT_UUID);
        when(deviceRepository.findByUid(DEVICE_UUID)).thenReturn(deviceBlm);

        // Act
        DeviceBlm result = deviceService.getDevice(DEVICE_UUID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(DEVICE_UUID);
        verify(deviceRepository).findByUid(DEVICE_UUID);
    }

    @Test
    @DisplayName("Get device - Negative: Device doesn't belong to client")
    void shouldThrowExceptionWhenDeviceNotBelongsToClient() {
        // Arrange
        UUID differentClientUuid = UUID.randomUUID();
        DeviceBlm deviceBlm = new DeviceBlm(
                DEVICE_UUID,
                differentClientUuid,
                "Test Device",
                "Test Description");

        setupAuthentication(CLIENT_UUID);
        when(deviceRepository.findByUid(DEVICE_UUID)).thenReturn(deviceBlm);

        // Act & Assert
        assertThatThrownBy(() -> deviceService.getDevice(DEVICE_UUID))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("doesn't belong");

        verify(deviceRepository).findByUid(DEVICE_UUID);
    }

    @Test
    @DisplayName("Get devices by client - Positive")
    void shouldGetDevicesByClientWhenValidRequest() {
        // Arrange
        DeviceBlm deviceBlm = createValidDeviceBlm();
        List<DeviceBlm> devicesBlm = Collections.singletonList(deviceBlm);

        setupAuthentication(CLIENT_UUID);
        when(deviceRepository.findByClientUuid(CLIENT_UUID)).thenReturn(devicesBlm);

        // Act
        List<DeviceBlm> result = deviceService.getDevicesByClient(CLIENT_UUID);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getUid()).isEqualTo(DEVICE_UUID);
        verify(deviceRepository).findByClientUuid(CLIENT_UUID);
    }

    @Test
    @DisplayName("Update device - Positive")
    void shouldUpdateDeviceWhenValidData() {
        // Arrange
        DeviceBlm deviceBlm = createValidDeviceBlm();
        DeviceBlm existingDevice = createValidDeviceBlm();

        setupAuthentication(CLIENT_UUID);
        when(deviceRepository.findByUid(DEVICE_UUID)).thenReturn(existingDevice);


        // Act
        DeviceBlm result = deviceService.updateDevice(deviceBlm);

        // Assert
        assertThat(result).isNotNull();
        verify(deviceValidator).validate(deviceBlm);
        verify(deviceRepository).update(deviceBlm);
    }

    @Test
    @DisplayName("Delete device - Positive")
    void shouldDeleteDeviceWhenValidRequest() {
        // Arrange
        DeviceBlm existingDevice = createValidDeviceBlm();

        setupAuthentication(CLIENT_UUID);
        when(deviceRepository.findByUid(DEVICE_UUID)).thenReturn(existingDevice);

        // Act
        deviceService.deleteDevice(DEVICE_UUID);

        // Assert
        verify(deviceRepository).delete(DEVICE_UUID);
    }

    @Test
    @DisplayName("Device exists - Positive")
    void shouldReturnTrueWhenDeviceExists() {
        // Arrange
        when(deviceRepository.exists(DEVICE_UUID)).thenReturn(true);

        // Act
        boolean result = deviceService.deviceExists(DEVICE_UUID);

        // Assert
        assertThat(result).isTrue();
        verify(deviceRepository).exists(DEVICE_UUID);
    }

    @Test
    @DisplayName("Device exists - Negative: Device not found")
    void shouldReturnFalseWhenDeviceNotExists() {
        // Arrange
        when(deviceRepository.exists(DEVICE_UUID)).thenReturn(false);

        // Act
        boolean result = deviceService.deviceExists(DEVICE_UUID);

        // Assert
        assertThat(result).isFalse();
        verify(deviceRepository).exists(DEVICE_UUID);
    }

    @Test
    @DisplayName("Health check - Positive")
    void shouldReturnHealthStatus() throws Exception {
        // Arrange
        Map<String, Object> authHealth = Map.of("status", "OK");
        HealthCheckResponse healthResponse = HealthCheckResponse.success("correlation-id", authHealth);
        
        when(authKafkaClient.getHealthStatus())
            .thenReturn(Map.of("status", "OK"));

        // Act
        Map<String, Object> result = deviceService.getHealthStatus();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("OK");
        assertThat(result.get("service")).isEqualTo("device-service");
        assertThat(result.get("auth-service")).isEqualTo(authHealth);
    }
}