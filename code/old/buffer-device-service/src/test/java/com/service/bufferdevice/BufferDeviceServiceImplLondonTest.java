package com.service.bufferdevice;

import static com.service.bufferdevice.mother.BufferDeviceObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.connection.device.model.DeviceBLM;
import com.connection.processing.buffer.bufferdevice.converter.BufferDeviceConverter;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceBLM;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDALM;
import com.connection.processing.buffer.bufferdevice.model.BufferDeviceDTO;
import com.connection.processing.buffer.bufferdevice.repository.BufferDeviceRepository;
import com.connection.processing.buffer.bufferdevice.validator.BufferDeviceValidator;
import com.connection.processing.buffer.model.BufferBLM;
import com.service.bufferdevice.client.AuthServiceClient;
import com.service.bufferdevice.client.BufferServiceClient;
import com.service.bufferdevice.client.DeviceServiceClient;

@ExtendWith(MockitoExtension.class)
@DisplayName("Buffer Device Service Implementation Tests - London Style")
class BufferDeviceServiceImplLondonTest {

    @Mock
    private BufferDeviceRepository bufferDeviceRepository;

    @Mock
    private BufferDeviceConverter bufferDeviceConverter;

    @Mock
    private BufferDeviceValidator bufferDeviceValidator;

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private BufferServiceClient bufferServiceClient;

    @Mock
    private DeviceServiceClient deviceServiceClient;

    @InjectMocks
    private BufferDeviceServiceImpl bufferDeviceService;

    @BeforeEach
    void setUp() {
        // Убрали глобальные заглушки - создаем их только в тех тестах, где они нужны
    }

    @Test
    @DisplayName("Create buffer device binding - Positive")
    void shouldCreateBufferDeviceWhenValidData() {
        // Arrange
        BufferDeviceDTO bufferDeviceDTO = createValidBufferDeviceDTO();
        BufferDeviceBLM bufferDeviceBLM = createValidBufferDeviceBLM();
        BufferDeviceDALM bufferDeviceDALM = new BufferDeviceDALM(BUFFER_UUID, DEVICE_UUID);
        BufferBLM buffer = createValidBufferBLM();
        DeviceBLM device = createValidDeviceBLM();

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(bufferDeviceConverter.toBLM(bufferDeviceDTO)).thenReturn(bufferDeviceBLM);
        when(bufferDeviceConverter.toDALM(bufferDeviceBLM)).thenReturn(bufferDeviceDALM);
        when(bufferServiceClient.getBuffer(VALID_TOKEN, BUFFER_UUID)).thenReturn(buffer);
        when(deviceServiceClient.getDevice(VALID_TOKEN, DEVICE_UUID)).thenReturn(device);

        // Act
        BufferDeviceBLM result = bufferDeviceService.createBufferDevice(VALID_TOKEN, bufferDeviceDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getBufferUid()).isEqualTo(BUFFER_UUID);
        assertThat(result.getDeviceUid()).isEqualTo(DEVICE_UUID);
        verify(bufferDeviceValidator).validate(bufferDeviceDTO);
        verify(bufferDeviceRepository).add(bufferDeviceDALM);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Create buffer device binding - Negative: Token validation fails")
    void shouldThrowExceptionWhenTokenValidationFails() {
        // Arrange
        BufferDeviceDTO bufferDeviceDTO = createValidBufferDeviceDTO();
        
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED))
            .when(authServiceClient).validateAccessToken(INVALID_TOKEN);

        // Act & Assert
        assertThatThrownBy(() -> bufferDeviceService.createBufferDevice(INVALID_TOKEN, bufferDeviceDTO))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("401 UNAUTHORIZED");

        verify(bufferDeviceValidator, never()).validate(any(BufferDeviceBLM.class));
        verify(bufferDeviceValidator, never()).validate(any(BufferDeviceDALM.class));
        verify(bufferDeviceRepository, never()).add(any());
        verify(authServiceClient, never()).getAccessTokenClientUID(any());
    }

    @Test
    @DisplayName("Create buffer device binding - Negative: Buffer doesn't belong to client")
    void shouldThrowExceptionWhenBufferNotBelongsToClient() {
        // Arrange
        BufferDeviceDTO bufferDeviceDTO = createValidBufferDeviceDTO();
        BufferDeviceBLM bufferDeviceBLM = createValidBufferDeviceBLM();
        BufferBLM buffer = createBufferWithDifferentClient(); // Different client
        DeviceBLM device = createValidDeviceBLM();

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(bufferDeviceConverter.toBLM(bufferDeviceDTO)).thenReturn(bufferDeviceBLM);
        when(bufferServiceClient.getBuffer(VALID_TOKEN, BUFFER_UUID)).thenReturn(buffer);
        when(deviceServiceClient.getDevice(VALID_TOKEN, DEVICE_UUID)).thenReturn(device);

        // Act & Assert
        assertThatThrownBy(() -> bufferDeviceService.createBufferDevice(VALID_TOKEN, bufferDeviceDTO))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Buffer doesn't belong");

        verify(bufferDeviceValidator).validate(bufferDeviceDTO);
        verify(bufferDeviceRepository, never()).add(any());
    }

    @Test
    @DisplayName("Create buffer device binding - Negative: Device doesn't belong to client")
    void shouldThrowExceptionWhenDeviceNotBelongsToClient() {
        // Arrange
        BufferDeviceDTO bufferDeviceDTO = createValidBufferDeviceDTO();
        BufferDeviceBLM bufferDeviceBLM = createValidBufferDeviceBLM();
        BufferBLM buffer = createValidBufferBLM();
        DeviceBLM device = createDeviceWithDifferentClient(); // Different client

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(bufferDeviceConverter.toBLM(bufferDeviceDTO)).thenReturn(bufferDeviceBLM);
        when(bufferServiceClient.getBuffer(VALID_TOKEN, BUFFER_UUID)).thenReturn(buffer);
        when(deviceServiceClient.getDevice(VALID_TOKEN, DEVICE_UUID)).thenReturn(device);

        // Act & Assert
        assertThatThrownBy(() -> bufferDeviceService.createBufferDevice(VALID_TOKEN, bufferDeviceDTO))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Device doesn't belong");

        verify(bufferDeviceValidator).validate(bufferDeviceDTO);
        verify(bufferDeviceRepository, never()).add(any());
    }

    @Test
    @DisplayName("Delete buffer device binding - Positive")
    void shouldDeleteBufferDeviceWhenValidRequest() {
        // Arrange
        BufferBLM buffer = createValidBufferBLM();
        DeviceBLM device = createValidDeviceBLM();

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(bufferServiceClient.getBuffer(VALID_TOKEN, BUFFER_UUID)).thenReturn(buffer);
        when(deviceServiceClient.getDevice(VALID_TOKEN, DEVICE_UUID)).thenReturn(device);

        // Act
        bufferDeviceService.deleteBufferDevice(VALID_TOKEN, BUFFER_UUID, DEVICE_UUID);

        // Assert
        verify(bufferDeviceRepository).delete(any(BufferDeviceDALM.class));
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Get buffer devices by buffer - Positive")
    void shouldGetBufferDevicesByBufferWhenValidRequest() {
        // Arrange
        BufferBLM buffer = createValidBufferBLM();
        List<UUID> deviceUids = List.of(DEVICE_UUID, OTHER_DEVICE_UUID);

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(bufferServiceClient.getBuffer(VALID_TOKEN, BUFFER_UUID)).thenReturn(buffer);
        when(bufferDeviceRepository.findDeviceUidsByBufferUid(BUFFER_UUID)).thenReturn(deviceUids);

        // Act
        List<BufferDeviceBLM> result = bufferDeviceService.getBufferDevicesByBuffer(VALID_TOKEN, BUFFER_UUID);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getBufferUid()).isEqualTo(BUFFER_UUID);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Get buffer devices by device - Positive")
    void shouldGetBufferDevicesByDeviceWhenValidRequest() {
        // Arrange
        DeviceBLM device = createValidDeviceBLM();
        List<UUID> bufferUids = List.of(BUFFER_UUID, OTHER_BUFFER_UUID);

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(deviceServiceClient.getDevice(VALID_TOKEN, DEVICE_UUID)).thenReturn(device);
        when(bufferDeviceRepository.findBufferUidsByDeviceUid(DEVICE_UUID)).thenReturn(bufferUids);

        // Act
        List<BufferDeviceBLM> result = bufferDeviceService.getBufferDevicesByDevice(VALID_TOKEN, DEVICE_UUID);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDeviceUid()).isEqualTo(DEVICE_UUID);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Add devices to buffer - Positive")
    void shouldAddDevicesToBufferWhenValidRequest() {
        // Arrange
        BufferBLM buffer = createValidBufferBLM();
        DeviceBLM device1 = createValidDeviceBLM();
        DeviceBLM device2 = createValidDeviceBLM();
        List<UUID> deviceUids = createDeviceUidList();

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(bufferServiceClient.getBuffer(VALID_TOKEN, BUFFER_UUID)).thenReturn(buffer);
        when(deviceServiceClient.getDevice(VALID_TOKEN, DEVICE_UUID)).thenReturn(device1);
        when(deviceServiceClient.getDevice(VALID_TOKEN, OTHER_DEVICE_UUID)).thenReturn(device2);

        // Act
        bufferDeviceService.addDevicesToBuffer(VALID_TOKEN, BUFFER_UUID, deviceUids);

        // Assert
        verify(bufferDeviceRepository).addDevicesToBuffer(BUFFER_UUID, deviceUids);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Add buffers to device - Positive")
    void shouldAddBuffersToDeviceWhenValidRequest() {
        // Arrange
        DeviceBLM device = createValidDeviceBLM();
        BufferBLM buffer1 = createValidBufferBLM();
        BufferBLM buffer2 = createValidBufferBLM();
        List<UUID> bufferUids = createBufferUidList();

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(deviceServiceClient.getDevice(VALID_TOKEN, DEVICE_UUID)).thenReturn(device);
        when(bufferServiceClient.getBuffer(VALID_TOKEN, BUFFER_UUID)).thenReturn(buffer1);
        when(bufferServiceClient.getBuffer(VALID_TOKEN, OTHER_BUFFER_UUID)).thenReturn(buffer2);

        // Act
        bufferDeviceService.addBuffersToDevice(VALID_TOKEN, DEVICE_UUID, bufferUids);

        // Assert
        verify(bufferDeviceRepository).addBuffersToDevice(DEVICE_UUID, bufferUids);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Buffer device exists - Positive")
    void shouldReturnTrueWhenBufferDeviceExists() {
        // Arrange
        when(bufferDeviceRepository.exists(BUFFER_UUID, DEVICE_UUID)).thenReturn(true);

        // Act
        boolean result = bufferDeviceService.bufferDeviceExists(VALID_TOKEN, BUFFER_UUID, DEVICE_UUID);

        // Assert
        assertThat(result).isTrue();
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Buffer device exists - Negative")
    void shouldReturnFalseWhenBufferDeviceNotExists() {
        // Arrange
        when(bufferDeviceRepository.exists(BUFFER_UUID, DEVICE_UUID)).thenReturn(false);

        // Act
        boolean result = bufferDeviceService.bufferDeviceExists(VALID_TOKEN, BUFFER_UUID, DEVICE_UUID);

        // Assert
        assertThat(result).isFalse();
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Health check - Positive")
    void shouldReturnHealthStatus() {
        // Arrange
        Map<String, Object> authHealth = createHealthResponse();
        Map<String, Object> bufferHealth = createHealthResponse();
        Map<String, Object> deviceHealth = createHealthResponse();

        when(authServiceClient.healthCheck()).thenReturn(authHealth);
        when(bufferServiceClient.healthCheck()).thenReturn(bufferHealth);
        when(deviceServiceClient.healthCheck()).thenReturn(deviceHealth);

        // Act
        Map<String, Object> result = bufferDeviceService.getHealthStatus();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("OK");
        assertThat(result.get("service")).isEqualTo("buffer-device-service");
        verify(authServiceClient).healthCheck();
        verify(bufferServiceClient).healthCheck();
        verify(deviceServiceClient).healthCheck();
    }

    @Test
    @DisplayName("Health check - Negative: External service down")
    void shouldHandleExternalServiceDownInHealthCheck() {
        // Arrange
        Map<String, Object> authHealth = createHealthResponse();
        Map<String, Object> bufferHealth = createHealthResponse();

        when(authServiceClient.healthCheck()).thenReturn(authHealth);
        when(bufferServiceClient.healthCheck()).thenReturn(bufferHealth);
        when(deviceServiceClient.healthCheck())
            .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Device service down"));

        // Act
        Map<String, Object> result = bufferDeviceService.getHealthStatus();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("OK");
        assertThat(result.get("service")).isEqualTo("buffer-device-service");
    }

    @Test
    @DisplayName("Delete all buffer devices for buffer - Positive")
    void shouldDeleteAllBufferDevicesForBuffer() {
        // Arrange
        BufferBLM buffer = createValidBufferBLM();

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        
        when(bufferServiceClient.getBuffer(VALID_TOKEN, BUFFER_UUID)).thenReturn(buffer);

        // Act
        bufferDeviceService.deleteAllBufferDevicesForBuffer(VALID_TOKEN, BUFFER_UUID);

        // Assert
        verify(bufferDeviceRepository).deleteAllByBufferUid(BUFFER_UUID);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Delete all buffer devices for device - Positive")
    void shouldDeleteAllBufferDevicesForDevice() {
        // Arrange
        DeviceBLM device = createValidDeviceBLM();

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(deviceServiceClient.getDevice(VALID_TOKEN, DEVICE_UUID)).thenReturn(device);

        // Act
        bufferDeviceService.deleteAllBufferDevicesForDevice(VALID_TOKEN, DEVICE_UUID);

        // Assert
        verify(bufferDeviceRepository).deleteAllByDeviceUid(DEVICE_UUID);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
    }
}