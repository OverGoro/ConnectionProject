package com.connection.device;

import static com.connection.device.mother.DeviceObjectMother.CLIENT_UUID;
import static com.connection.device.mother.DeviceObjectMother.DEVICE_UUID;
import static com.connection.device.mother.DeviceObjectMother.createValidDeviceBLM;
import static com.connection.device.mother.DeviceObjectMother.createValidDeviceDALM;
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
import com.connection.device.converter.DeviceConverter;
import com.connection.device.exception.DeviceAlreadyExistsException;
import com.connection.device.kafka.TypedAuthKafkaClient;
import com.connection.device.model.DeviceBLM;
import com.connection.device.model.DeviceDALM;
import com.connection.device.repository.DeviceRepository;
import com.connection.device.validator.DeviceValidator;

@ExtendWith(MockitoExtension.class)
@DisplayName("Device Service Implementation Tests - Kafka Version")
class DeviceServiceImplTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceConverter deviceConverter;

    @Mock
    private DeviceValidator deviceValidator;

    @Mock
    private TypedAuthKafkaClient authKafkaClient;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private DeviceServiceImpl deviceService;

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
        DeviceBLM deviceBLM = createValidDeviceBLM();
        DeviceDALM deviceDALM = createValidDeviceDALM();
        
        // setupAuthentication(CLIENT_UUID);
        when(deviceConverter.toDALM(deviceBLM)).thenReturn(deviceDALM);
        when(deviceRepository.existsByClientAndName(CLIENT_UUID, "Test Device")).thenReturn(false);

        // Act
        DeviceBLM result = deviceService.createDevice(CLIENT_UUID, deviceBLM);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(DEVICE_UUID);
        verify(deviceValidator).validate(deviceBLM);
        verify(deviceRepository).add(deviceDALM);
    }

    @Test
    @DisplayName("Create device - Negative: Client UID mismatch")
    void shouldThrowExceptionWhenClientUidMismatch() {
        // Arrange
        UUID differentClientUuid = UUID.randomUUID();
        DeviceBLM deviceBLM = new DeviceBLM(
                DEVICE_UUID,
                differentClientUuid,
                "Test Device",
                "Test Description");

        // setupAuthentication(CLIENT_UUID);

        // Act & Assert
        assertThatThrownBy(() -> deviceService.createDevice(CLIENT_UUID, deviceBLM))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Client UID from token doesn't match device client UID");

        verify(deviceValidator).validate(deviceBLM);
        verify(deviceRepository, never()).add(any());
    }

    @Test
    @DisplayName("Create device - Negative: Device already exists")
    void shouldThrowExceptionWhenDeviceAlreadyExists() {
        // Arrange
        DeviceBLM deviceBLM = createValidDeviceBLM();
        
        // setupAuthentication(CLIENT_UUID);
        when(deviceRepository.existsByClientAndName(CLIENT_UUID, "Test Device")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> deviceService.createDevice(CLIENT_UUID, deviceBLM))
                .isInstanceOf(DeviceAlreadyExistsException.class);

        verify(deviceValidator).validate(deviceBLM);
        verify(deviceRepository, never()).add(any());
    }

    @Test
    @DisplayName("Get device - Positive")
    void shouldGetDeviceWhenValidRequest() {
        // Arrange
        DeviceDALM deviceDALM = createValidDeviceDALM();
        DeviceBLM expectedBLM = createValidDeviceBLM();

        //setupAuthentication(CLIENT_UUID);
        when(deviceRepository.findByUid(DEVICE_UUID)).thenReturn(deviceDALM);
        when(deviceConverter.toBLM(deviceDALM)).thenReturn(expectedBLM);

        // Act
        DeviceBLM result = deviceService.getDevice(CLIENT_UUID, DEVICE_UUID);

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
        DeviceDALM deviceDALM = new DeviceDALM(
                DEVICE_UUID,
                differentClientUuid,
                "Test Device",
                "Test Description");

        //setupAuthentication(CLIENT_UUID);
        when(deviceRepository.findByUid(DEVICE_UUID)).thenReturn(deviceDALM);

        // Act & Assert
        assertThatThrownBy(() -> deviceService.getDevice(CLIENT_UUID, DEVICE_UUID))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("doesn't belong");

        verify(deviceRepository).findByUid(DEVICE_UUID);
        verify(deviceConverter, never()).toBLM(any(DeviceDALM.class));
    }

    @Test
    @DisplayName("Get devices by client - Positive")
    void shouldGetDevicesByClientWhenValidRequest() {
        // Arrange
        DeviceDALM deviceDALM = createValidDeviceDALM();
        DeviceBLM expectedBLM = createValidDeviceBLM();
        List<DeviceDALM> devicesDALM = Collections.singletonList(deviceDALM);

        //setupAuthentication(CLIENT_UUID);
        when(deviceRepository.findByClientUuid(CLIENT_UUID)).thenReturn(devicesDALM);
        when(deviceConverter.toBLM(deviceDALM)).thenReturn(expectedBLM);

        // Act
        List<DeviceBLM> result = deviceService.getDevicesByClient(CLIENT_UUID);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getUid()).isEqualTo(DEVICE_UUID);
        verify(deviceRepository).findByClientUuid(CLIENT_UUID);
    }

    @Test
    @DisplayName("Update device - Positive")
    void shouldUpdateDeviceWhenValidData() {
        // Arrange
        DeviceBLM deviceBLM = createValidDeviceBLM();
        DeviceDALM deviceDALM = createValidDeviceDALM();
        DeviceDALM existingDevice = createValidDeviceDALM();

        //setupAuthentication(CLIENT_UUID);
        when(deviceRepository.findByUid(DEVICE_UUID)).thenReturn(existingDevice);
        when(deviceConverter.toDALM(deviceBLM)).thenReturn(deviceDALM);

        // Act
        DeviceBLM result = deviceService.updateDevice(CLIENT_UUID, deviceBLM);

        // Assert
        assertThat(result).isNotNull();
        verify(deviceValidator).validate(deviceBLM);
        verify(deviceRepository).update(deviceDALM);
    }

    @Test
    @DisplayName("Delete device - Positive")
    void shouldDeleteDeviceWhenValidRequest() {
        // Arrange
        DeviceDALM existingDevice = createValidDeviceDALM();

        //setupAuthentication(CLIENT_UUID);
        when(deviceRepository.findByUid(DEVICE_UUID)).thenReturn(existingDevice);

        // Act
        deviceService.deleteDevice(CLIENT_UUID, DEVICE_UUID);

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
        
        when(authKafkaClient.healthCheck("device-service"))
            .thenReturn(CompletableFuture.completedFuture(healthResponse));

        // Act
        Map<String, Object> result = deviceService.getHealthStatus();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("OK");
        assertThat(result.get("service")).isEqualTo("device-service");
        assertThat(result.get("auth-service")).isEqualTo(authHealth);
        verify(authKafkaClient).healthCheck("device-service");
    }

    @Test
    @DisplayName("Health check - Negative: Auth service timeout")
    void shouldHandleAuthServiceTimeoutInHealthCheck() throws Exception {
        // Arrange
        CompletableFuture<HealthCheckResponse> timeoutFuture = new CompletableFuture<>();
        timeoutFuture.completeExceptionally(new java.util.concurrent.TimeoutException("Timeout"));
        
        when(authKafkaClient.healthCheck("device-service"))
            .thenReturn(timeoutFuture);

        // Act
        Map<String, Object> result = deviceService.getHealthStatus();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("DEGRADED");
        assertThat(result.get("auth-service")).isEqualTo("UNAVAILABLE");
        verify(authKafkaClient).healthCheck("device-service");
    }

    @Test
    @DisplayName("Health check - Negative: Auth service error")
    void shouldHandleAuthServiceErrorInHealthCheck() throws Exception {
        // Arrange
        HealthCheckResponse errorResponse = HealthCheckResponse.error("correlation-id", "Service unavailable");
        
        when(authKafkaClient.healthCheck("device-service"))
            .thenReturn(CompletableFuture.completedFuture(errorResponse));

        // Act
        Map<String, Object> result = deviceService.getHealthStatus();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("OK");
        assertThat(result.get("auth-service")).isEqualTo("UNAVAILABLE");
        verify(authKafkaClient).healthCheck("device-service");
    }
}