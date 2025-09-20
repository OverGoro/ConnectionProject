package com.connection.device;

import static com.connection.device.mother.DeviceObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.connection.device.client.AuthServiceClient;
import com.connection.device.converter.DeviceConverter;
import com.connection.device.exception.DeviceAlreadyExistsException;
import com.connection.device.model.DeviceBLM;
import com.connection.device.model.DeviceDALM;
import com.connection.device.model.DeviceDTO;
import com.connection.device.repository.DeviceRepository;
import com.connection.device.validator.DeviceValidator;

@ExtendWith(MockitoExtension.class)
@DisplayName("Device Service Implementation Tests - London Style")
class DeviceServiceImplLondonTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceConverter deviceConverter;

    @Mock
    private DeviceValidator deviceValidator;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    @BeforeEach
    void setUp() {

    }

    @Test
    @DisplayName("Create device - Positive")
    void shouldCreateDeviceWhenValidData() {

        DeviceDTO deviceDTO = createValidDeviceDTO();
        DeviceBLM deviceBLM = createValidDeviceBLM();
        DeviceDALM deviceDALM = createValidDeviceDALM();

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(deviceConverter.toBLM(deviceDTO)).thenReturn(deviceBLM);
        when(deviceConverter.toDALM(deviceBLM)).thenReturn(deviceDALM);
        when(deviceRepository.existsByClientAndName(CLIENT_UUID, "Test Device")).thenReturn(false);

        DeviceBLM result = deviceService.createDevice(VALID_TOKEN, deviceDTO);

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(DEVICE_UUID);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(deviceValidator).validate(deviceDTO);
        verify(deviceRepository).add(deviceDALM);
    }

    @Test
    @DisplayName("Create device - Negative: Token validation fails")
    void shouldThrowExceptionWhenTokenValidationFails() {

        DeviceDTO deviceDTO = createValidDeviceDTO();

        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED))
                .when(authServiceClient).validateAccessToken(INVALID_TOKEN);

        assertThatThrownBy(() -> deviceService.createDevice(INVALID_TOKEN, deviceDTO))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401 UNAUTHORIZED");

        verify(deviceValidator, never()).validate(any(DeviceDTO.class));
        verify(deviceRepository, never()).add(any());
        verify(authServiceClient, never()).getAccessTokenClientUID(any());
    }

    @Test
    @DisplayName("Create device - Negative: Client UID mismatch")
    void shouldThrowExceptionWhenClientUidMismatch() {

        DeviceDTO deviceDTO = createValidDeviceDTO();
        UUID differentClientUuid = UUID.randomUUID();
        DeviceBLM deviceBLM = new DeviceBLM(
                DEVICE_UUID,
                differentClientUuid,
                "Test Device",
                "Test Description");

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(deviceConverter.toBLM(deviceDTO)).thenReturn(deviceBLM);

        assertThatThrownBy(() -> deviceService.createDevice(VALID_TOKEN, deviceDTO))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Client UID from token doesn't match");

        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(deviceValidator).validate(deviceDTO);
        verify(deviceRepository, never()).add(any());
    }

    @Test
    @DisplayName("Create device - Negative: Device already exists")
    void shouldThrowExceptionWhenDeviceAlreadyExists() {

        DeviceDTO deviceDTO = createValidDeviceDTO();
        DeviceBLM deviceBLM = createValidDeviceBLM();

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(deviceConverter.toBLM(deviceDTO)).thenReturn(deviceBLM);
        when(deviceRepository.existsByClientAndName(CLIENT_UUID, "Test Device")).thenReturn(true);

        assertThatThrownBy(() -> deviceService.createDevice(VALID_TOKEN, deviceDTO))
                .isInstanceOf(DeviceAlreadyExistsException.class);

        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(deviceValidator).validate(deviceDTO);
        verify(deviceRepository, never()).add(any());
    }

    @Test
    @DisplayName("Get device - Positive")
    void shouldGetDeviceWhenValidRequest() {

        DeviceDALM deviceDALM = createValidDeviceDALM();
        DeviceBLM expectedBLM = createValidDeviceBLM();

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(deviceRepository.findByUid(DEVICE_UUID)).thenReturn(deviceDALM);
        when(deviceConverter.toBLM(deviceDALM)).thenReturn(expectedBLM);

        DeviceBLM result = deviceService.getDevice(VALID_TOKEN, DEVICE_UUID);

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(DEVICE_UUID);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(deviceRepository).findByUid(DEVICE_UUID);
    }

    @Test
    @DisplayName("Get device - Negative: Device doesn't belong to client")
    void shouldThrowExceptionWhenDeviceNotBelongsToClient() {

        UUID differentClientUuid = UUID.randomUUID();
        DeviceDALM deviceDALM = new DeviceDALM(
                DEVICE_UUID,
                differentClientUuid,
                "Test Device",
                "Test Description");

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(deviceRepository.findByUid(DEVICE_UUID)).thenReturn(deviceDALM);

        assertThatThrownBy(() -> deviceService.getDevice(VALID_TOKEN, DEVICE_UUID))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("doesn't belong");

        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(deviceRepository).findByUid(DEVICE_UUID);
        verify(deviceConverter, never()).toBLM(any(DeviceDTO.class));
        verify(deviceConverter, never()).toBLM(any(DeviceDALM.class));
    }

    @Test
    @DisplayName("Get devices by client - Positive")
    void shouldGetDevicesByClientWhenValidRequest() {

        DeviceDALM deviceDALM = createValidDeviceDALM();
        DeviceBLM expectedBLM = createValidDeviceBLM();
        List<DeviceDALM> devicesDALM = Collections.singletonList(deviceDALM);

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(deviceRepository.findByClientUuid(CLIENT_UUID)).thenReturn(devicesDALM);
        when(deviceConverter.toBLM(deviceDALM)).thenReturn(expectedBLM);

        List<DeviceBLM> result = deviceService.getDevicesByClient(VALID_TOKEN);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getUid()).isEqualTo(DEVICE_UUID);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(deviceRepository).findByClientUuid(CLIENT_UUID);
    }

    @Test
    @DisplayName("Update device - Positive")
    void shouldUpdateDeviceWhenValidData() {

        DeviceDTO deviceDTO = createValidDeviceDTO();
        DeviceBLM deviceBLM = createValidDeviceBLM();
        DeviceDALM deviceDALM = createValidDeviceDALM();
        DeviceDALM existingDevice = createValidDeviceDALM();

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(deviceRepository.findByUid(DEVICE_UUID)).thenReturn(existingDevice);
        when(deviceConverter.toBLM(deviceDTO)).thenReturn(deviceBLM);
        when(deviceConverter.toDALM(deviceBLM)).thenReturn(deviceDALM);

        DeviceBLM result = deviceService.updateDevice(VALID_TOKEN, DEVICE_UUID, deviceDTO);

        assertThat(result).isNotNull();
        verify(authServiceClient, times(1)).validateAccessToken(VALID_TOKEN);
        verify(deviceValidator).validate(deviceDTO);
        verify(deviceRepository).update(deviceDALM);
    }

    @Test
    @DisplayName("Delete device - Positive")
    void shouldDeleteDeviceWhenValidRequest() {

        DeviceDALM existingDevice = createValidDeviceDALM();

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(deviceRepository.findByUid(DEVICE_UUID)).thenReturn(existingDevice);

        deviceService.deleteDevice(VALID_TOKEN, DEVICE_UUID);

        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(deviceRepository).delete(DEVICE_UUID);
    }

    @Test
    @DisplayName("Device exists - Positive")
    void shouldReturnTrueWhenDeviceExists() {

        when(deviceRepository.exists(DEVICE_UUID)).thenReturn(true);

        boolean result = deviceService.deviceExists(VALID_TOKEN, DEVICE_UUID);

        assertThat(result).isTrue();
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(deviceRepository).exists(DEVICE_UUID);
    }

    @Test
    @DisplayName("Device exists - Negative: Device not found")
    void shouldReturnFalseWhenDeviceNotExists() {

        when(deviceRepository.exists(DEVICE_UUID)).thenReturn(false);

        boolean result = deviceService.deviceExists(VALID_TOKEN, DEVICE_UUID);

        assertThat(result).isFalse();
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(deviceRepository).exists(DEVICE_UUID);
    }

    @Test
    @DisplayName("Health check - Positive")
    void shouldReturnHealthStatus() {

        Map<String, Object> authHealth = Map.of("status", "OK");
        when(authServiceClient.healthCheck()).thenReturn(authHealth);

        Map<String, Object> result = deviceService.getHealthStatus();

        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("OK");
        assertThat(result.get("service")).isEqualTo("device-service");
        assertThat(result.get("auth-service")).isEqualTo(authHealth);
        verify(authServiceClient).healthCheck();
    }

    @Test
    @DisplayName("Health check - Negative: Auth service down")
    void shouldHandleAuthServiceDownInHealthCheck() {

        when(authServiceClient.healthCheck())
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR));

        Map<String, Object> result = deviceService.getHealthStatus();

        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("OK");
        assertThat(result.get("service")).isEqualTo("device-service");
        assertThat(result.get("auth-service")).isNotNull();
        verify(authServiceClient).healthCheck();
    }
}