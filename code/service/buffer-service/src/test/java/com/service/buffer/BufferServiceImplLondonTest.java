package com.service.buffer;

import static com.service.buffer.mother.BufferObjectMother.BUFFER_UUID;
import static com.service.buffer.mother.BufferObjectMother.CLIENT_UUID;
import static com.service.buffer.mother.BufferObjectMother.SCHEME_UUID;
import static com.service.buffer.mother.BufferObjectMother.createValidBufferBLM;
import static com.service.buffer.mother.BufferObjectMother.createValidBufferDALM;
import static com.service.buffer.mother.BufferObjectMother.createValidBufferDTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.connection.device.converter.DeviceConverter;
import com.connection.device.events.responses.GetDevicesByClientResponse;
import com.connection.device.model.DeviceBLM;
import com.connection.device.model.DeviceDTO;
import com.connection.processing.buffer.converter.BufferConverter;
import com.connection.processing.buffer.exception.BufferAlreadyExistsException;
import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferDALM;
import com.connection.processing.buffer.model.BufferDTO;
import com.connection.processing.buffer.repository.BufferRepository;
import com.connection.processing.buffer.validator.BufferValidator;
import com.service.buffer.kafka.TypedAuthKafkaClient;
import com.service.buffer.kafka.TypedConnectionSchemeKafkaClient;
import com.service.buffer.kafka.TypedDeviceKafkaClient;

@ExtendWith(MockitoExtension.class)
@DisplayName("Buffer Service Implementation Tests")
class BufferServiceImplLondonTest {

    @Mock
    private BufferRepository bufferRepository;

    @Mock
    private BufferConverter bufferConverter;

    @Mock
    private BufferValidator bufferValidator;

    @Mock
    private DeviceConverter deviceConverter;

    @Mock
    private TypedAuthKafkaClient authKafkaClient;

    @Mock
    private TypedDeviceKafkaClient deviceKafkaClient;

    @Mock
    private TypedConnectionSchemeKafkaClient connectionSchemeKafkaClient;

    @InjectMocks
    private BufferServiceImpl bufferService;

    private static final UUID DEVICE_UUID = UUID.fromString("523e4567-e89b-12d3-a456-426614174001");

    @BeforeEach
    void setUp() {
        // Инициализация может быть добавлена при необходимости
    }

    @Test
    @DisplayName("Create buffer - Positive")
    void shouldCreateBufferWhenValidData() {
        // Arrange
        BufferDTO bufferDTO = createValidBufferDTO();
        BufferBLM bufferBLM = createValidBufferBLM();
        BufferDALM bufferDALM = createValidBufferDALM();

        when(bufferConverter.toBLM(bufferDTO)).thenReturn(bufferBLM);
        when(bufferConverter.toDALM(bufferBLM)).thenReturn(bufferDALM);
        when(deviceKafkaClient.deviceExistsAndBelongsToClient(
                eq(bufferBLM.getDeviceUid()), eq(CLIENT_UUID))).thenReturn(true);
        when(bufferRepository.exists(BUFFER_UUID)).thenReturn(false);

        // Act
        BufferBLM result = bufferService.createBuffer(CLIENT_UUID, bufferDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(BUFFER_UUID);
        verify(bufferValidator).validate(bufferDTO);
        verify(deviceKafkaClient).deviceExistsAndBelongsToClient(bufferBLM.getDeviceUid(), CLIENT_UUID);
        verify(bufferRepository).add(bufferDALM);
    }

    @Test
    @DisplayName("Create buffer - Negative: Device doesn't belong to client")
    void shouldThrowExceptionWhenDeviceNotBelongsToClient() {
        // Arrange
        BufferDTO bufferDTO = createValidBufferDTO();
        BufferBLM bufferBLM = createValidBufferBLM();

        when(bufferConverter.toBLM(bufferDTO)).thenReturn(bufferBLM);
        when(deviceKafkaClient.deviceExistsAndBelongsToClient(
                eq(bufferBLM.getDeviceUid()), eq(CLIENT_UUID))).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> bufferService.createBuffer(CLIENT_UUID, bufferDTO))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Device doesn't exist or doesn't belong to the authenticated client");

        verify(bufferValidator).validate(bufferDTO);
        verify(bufferRepository, never()).add(any());
    }

    @Test
    @DisplayName("Create buffer - Negative: Buffer already exists")
    void shouldThrowExceptionWhenBufferAlreadyExists() {
        // Arrange
        BufferDTO bufferDTO = createValidBufferDTO();
        BufferBLM bufferBLM = createValidBufferBLM();

        when(bufferConverter.toBLM(bufferDTO)).thenReturn(bufferBLM);
        when(deviceKafkaClient.deviceExistsAndBelongsToClient(
                eq(bufferBLM.getDeviceUid()), eq(CLIENT_UUID))).thenReturn(true);
        when(bufferRepository.exists(BUFFER_UUID)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> bufferService.createBuffer(CLIENT_UUID, bufferDTO))
            .isInstanceOf(BufferAlreadyExistsException.class);
        
        verify(bufferValidator).validate(bufferDTO);
        verify(bufferRepository, never()).add(any());
    }

    @Test
    @DisplayName("Get buffer - Positive")
    void shouldGetBufferWhenValidRequest() {
        // Arrange
        BufferDALM bufferDALM = createValidBufferDALM();
        BufferBLM bufferBLM = createValidBufferBLM();

        when(bufferRepository.findByUid(BUFFER_UUID)).thenReturn(bufferDALM);
        when(bufferConverter.toBLM(bufferDALM)).thenReturn(bufferBLM);
        when(deviceKafkaClient.deviceExistsAndBelongsToClient(
                eq(bufferDALM.getDeviceUid()), eq(CLIENT_UUID))).thenReturn(true);

        // Act
        BufferBLM result = bufferService.getBufferByUid(CLIENT_UUID, BUFFER_UUID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(BUFFER_UUID);
        verify(bufferRepository).findByUid(BUFFER_UUID);
        verify(deviceKafkaClient).deviceExistsAndBelongsToClient(bufferDALM.getDeviceUid(), CLIENT_UUID);
    }

    @Test
    @DisplayName("Get buffers by connection scheme - Positive")
    void shouldGetBuffersByConnectionSchemeWhenValidRequest() {
        // Arrange
        BufferDALM bufferDALM = createValidBufferDALM();
        BufferBLM bufferBLM = createValidBufferBLM();
        List<BufferDALM> buffersDALM = Collections.singletonList(bufferDALM);

        when(connectionSchemeKafkaClient.connectionSchemeExistsAndBelongsToClient(
                eq(SCHEME_UUID), eq(CLIENT_UUID))).thenReturn(true);
        when(bufferRepository.findByConnectionSchemeUid(SCHEME_UUID)).thenReturn(buffersDALM);
        when(bufferConverter.toBLM(bufferDALM)).thenReturn(bufferBLM);

        // Act
        List<BufferBLM> result = bufferService.getBuffersByConnectionScheme(CLIENT_UUID, SCHEME_UUID);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getUid()).isEqualTo(BUFFER_UUID);
        verify(connectionSchemeKafkaClient).connectionSchemeExistsAndBelongsToClient(SCHEME_UUID, CLIENT_UUID);
        verify(bufferRepository).findByConnectionSchemeUid(SCHEME_UUID);
    }

    @Test
    @DisplayName("Get buffers by client - Positive")
    void shouldGetBuffersByClientWhenValidRequest() {
        // Arrange
        // Создаем DeviceDTO с правильной структурой на основе декомпилированного кода
        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setUid(DEVICE_UUID.toString());
        deviceDTO.setClientUuid(CLIENT_UUID.toString());
        deviceDTO.setDeviceName("Test Device");
        deviceDTO.setDeviceDescription("Test Device Description");
        
        // Создаем DeviceBLM с правильным конструктором
        DeviceBLM deviceBLM = new DeviceBLM(DEVICE_UUID, CLIENT_UUID, "Test Device", "Test Device Description");
        List<DeviceDTO> deviceDTOs = Collections.singletonList(deviceDTO);
        
        BufferDALM bufferDALM = createValidBufferDALM();
        BufferBLM bufferBLM = createValidBufferBLM();
        List<BufferDALM> buffersDALM = Collections.singletonList(bufferDALM);

        GetDevicesByClientResponse devicesResponse = GetDevicesByClientResponse.valid(
            "correlation-id", deviceDTOs
        );

        when(deviceKafkaClient.getDevicesByClient(eq(CLIENT_UUID), eq("buffer-service")))
            .thenReturn(CompletableFuture.completedFuture(devicesResponse));
        when(deviceConverter.toBLM(deviceDTO)).thenReturn(deviceBLM);
        when(deviceKafkaClient.deviceExistsAndBelongsToClient(eq(DEVICE_UUID), eq(CLIENT_UUID))).thenReturn(true);
        when(bufferRepository.findByDeviceUid(DEVICE_UUID)).thenReturn(buffersDALM);
        when(bufferConverter.toBLM(bufferDALM)).thenReturn(bufferBLM);

        // Act
        List<BufferBLM> result = bufferService.getBuffersByClient(CLIENT_UUID);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getUid()).isEqualTo(BUFFER_UUID);
        verify(deviceKafkaClient).getDevicesByClient(CLIENT_UUID, "buffer-service");
        verify(bufferRepository).findByDeviceUid(DEVICE_UUID);
    }

    @Test
    @DisplayName("Get buffers by device - Positive")
    void shouldGetBuffersByDeviceWhenValidRequest() {
        // Arrange
        BufferDALM bufferDALM = createValidBufferDALM();
        BufferBLM bufferBLM = createValidBufferBLM();
        List<BufferDALM> buffersDALM = Collections.singletonList(bufferDALM);

        when(deviceKafkaClient.deviceExistsAndBelongsToClient(eq(DEVICE_UUID), eq(CLIENT_UUID))).thenReturn(true);
        when(bufferRepository.findByDeviceUid(DEVICE_UUID)).thenReturn(buffersDALM);
        when(bufferConverter.toBLM(bufferDALM)).thenReturn(bufferBLM);

        // Act
        List<BufferBLM> result = bufferService.getBuffersByDevice(CLIENT_UUID, DEVICE_UUID);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getUid()).isEqualTo(BUFFER_UUID);
        verify(deviceKafkaClient).deviceExistsAndBelongsToClient(DEVICE_UUID, CLIENT_UUID);
        verify(bufferRepository).findByDeviceUid(DEVICE_UUID);
    }

    @Test
    @DisplayName("Update buffer - Positive")
    void shouldUpdateBufferWhenValidData() {
        // Arrange
        BufferDTO bufferDTO = createValidBufferDTO();
        BufferBLM bufferBLM = createValidBufferBLM();
        BufferDALM bufferDALM = createValidBufferDALM();
        BufferDALM existingBuffer = createValidBufferDALM();

        when(bufferRepository.findByUid(BUFFER_UUID)).thenReturn(existingBuffer);
        when(bufferConverter.toBLM(bufferDTO)).thenReturn(bufferBLM);
        when(deviceKafkaClient.deviceExistsAndBelongsToClient(
                eq(bufferBLM.getDeviceUid()), eq(CLIENT_UUID))).thenReturn(true);
        when(bufferConverter.toDALM(bufferBLM)).thenReturn(bufferDALM);

        // Act
        BufferBLM result = bufferService.updateBuffer(CLIENT_UUID, BUFFER_UUID, bufferDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(bufferValidator).validate(bufferDTO);
        verify(bufferRepository).update(bufferDALM);
    }

    @Test
    @DisplayName("Delete buffer - Positive")
    void shouldDeleteBufferWhenValidRequest() {
        // Arrange
        BufferDALM existingBuffer = createValidBufferDALM();

        when(bufferRepository.findByUid(BUFFER_UUID)).thenReturn(existingBuffer);
        when(deviceKafkaClient.deviceExistsAndBelongsToClient(
                eq(existingBuffer.getDeviceUid()), eq(CLIENT_UUID))).thenReturn(true);

        // Act
        bufferService.deleteBuffer(CLIENT_UUID, BUFFER_UUID);

        // Assert
        verify(bufferRepository).delete(BUFFER_UUID);
    }

    @Test
    @DisplayName("Delete all buffers from connection scheme - Positive")
    void shouldDeleteAllBuffersFromConnectionSchemeWhenValidRequest() {
        // Arrange
        BufferDALM bufferDALM = createValidBufferDALM();
        BufferBLM bufferBLM = createValidBufferBLM();
        List<BufferDALM> buffersDALM = Collections.singletonList(bufferDALM);

        when(connectionSchemeKafkaClient.connectionSchemeExistsAndBelongsToClient(
                eq(SCHEME_UUID), eq(CLIENT_UUID))).thenReturn(true);
        when(bufferRepository.findByConnectionSchemeUid(SCHEME_UUID)).thenReturn(buffersDALM);
        when(bufferConverter.toBLM(bufferDALM)).thenReturn(bufferBLM);

        // Act
        bufferService.deleteAllBuffersFromConnectionScheme(CLIENT_UUID, SCHEME_UUID);

        // Assert
        verify(bufferRepository).findByConnectionSchemeUid(SCHEME_UUID);
        verify(bufferRepository).removeBufferFromConnectionScheme(BUFFER_UUID, SCHEME_UUID);
    }

    @Test
    @DisplayName("Delete buffer from connection scheme - Positive")
    void shouldDeleteBufferFromConnectionSchemeWhenValidRequest() {
        // Arrange
        BufferDALM bufferDALM = createValidBufferDALM();
        BufferBLM bufferBLM = createValidBufferBLM();
        List<BufferDALM> buffersDALM = Collections.singletonList(bufferDALM);

        when(connectionSchemeKafkaClient.connectionSchemeExistsAndBelongsToClient(
                eq(SCHEME_UUID), eq(CLIENT_UUID))).thenReturn(true);
        when(bufferRepository.findByConnectionSchemeUid(SCHEME_UUID)).thenReturn(buffersDALM);
        when(bufferConverter.toBLM(bufferDALM)).thenReturn(bufferBLM);

        // Act
        bufferService.deleteBufferFromConnectionScheme(CLIENT_UUID, SCHEME_UUID, BUFFER_UUID);

        // Assert
        verify(bufferRepository).findByConnectionSchemeUid(SCHEME_UUID);
        verify(bufferRepository).removeBufferFromConnectionScheme(BUFFER_UUID, SCHEME_UUID);
    }

    @Test
    @DisplayName("Buffer exists - Positive")
    void shouldReturnTrueWhenBufferExists() {
        // Arrange
        BufferDALM bufferDALM = createValidBufferDALM();

        when(bufferRepository.exists(BUFFER_UUID)).thenReturn(true);
        when(bufferRepository.findByUid(BUFFER_UUID)).thenReturn(bufferDALM);
        when(deviceKafkaClient.deviceExistsAndBelongsToClient(
                eq(bufferDALM.getDeviceUid()), eq(CLIENT_UUID))).thenReturn(true);

        // Act
        boolean result = bufferService.bufferExists(CLIENT_UUID, BUFFER_UUID);

        // Assert
        assertThat(result).isTrue();
        verify(bufferRepository).exists(BUFFER_UUID);
        verify(deviceKafkaClient).deviceExistsAndBelongsToClient(bufferDALM.getDeviceUid(), CLIENT_UUID);
    }

    @Test
    @DisplayName("Buffer exists - Negative: Buffer not found")
    void shouldReturnFalseWhenBufferNotExists() {
        // Arrange
        when(bufferRepository.exists(BUFFER_UUID)).thenReturn(false);

        // Act
        boolean result = bufferService.bufferExists(CLIENT_UUID, BUFFER_UUID);

        // Assert
        assertThat(result).isFalse();
        verify(bufferRepository).exists(BUFFER_UUID);
    }

    @Test
    @DisplayName("Buffer exists - Negative: Device doesn't belong to client")
    void shouldReturnFalseWhenDeviceNotBelongsToClient() {
        // Arrange
        BufferDALM bufferDALM = createValidBufferDALM();

        when(bufferRepository.exists(BUFFER_UUID)).thenReturn(true);
        when(bufferRepository.findByUid(BUFFER_UUID)).thenReturn(bufferDALM);
        when(deviceKafkaClient.deviceExistsAndBelongsToClient(
                eq(bufferDALM.getDeviceUid()), eq(CLIENT_UUID))).thenReturn(false);

        // Act
        boolean result = bufferService.bufferExists(CLIENT_UUID, BUFFER_UUID);

        // Assert
        assertThat(result).isFalse();
        verify(bufferRepository).exists(BUFFER_UUID);
        verify(deviceKafkaClient).deviceExistsAndBelongsToClient(bufferDALM.getDeviceUid(), CLIENT_UUID);
    }

    @Test
    @DisplayName("Update buffer - Negative: Cannot change buffer UID")
    void shouldThrowExceptionWhenTryingToChangeBufferUid() {
        // Arrange
        BufferDTO bufferDTO = createValidBufferDTO();
        BufferDALM existingBuffer = createValidBufferDALM();
        // Создаем BLM с другим UID
        BufferBLM bufferBLM = new BufferBLM(
            UUID.randomUUID(), // другой UID
            SCHEME_UUID,
            1000,
            1024,
            "message prototype"
        );

        when(bufferRepository.findByUid(BUFFER_UUID)).thenReturn(existingBuffer);
        when(deviceKafkaClient.deviceExistsAndBelongsToClient(
                eq(existingBuffer.getDeviceUid()), eq(CLIENT_UUID))).thenReturn(true);
        when(bufferConverter.toBLM(bufferDTO)).thenReturn(bufferBLM);

        // Act & Assert
        assertThatThrownBy(() -> bufferService.updateBuffer(CLIENT_UUID, BUFFER_UUID, bufferDTO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot change buffer UID");

        verify(bufferValidator).validate(bufferDTO);
        verify(bufferRepository, never()).update(any());
    }

    @Test
    @DisplayName("Get buffers by client - Negative: Device service returns error")
    void shouldReturnEmptyListWhenDeviceServiceFails() throws Exception {
        // Arrange
        GetDevicesByClientResponse devicesResponse = GetDevicesByClientResponse.error(
            "correlation-id", "Service unavailable"
        );

        when(deviceKafkaClient.getDevicesByClient(eq(CLIENT_UUID), eq("buffer-service")))
            .thenReturn(CompletableFuture.completedFuture(devicesResponse));

        // Act
        List<BufferBLM> result = bufferService.getBuffersByClient(CLIENT_UUID);

        // Assert
        assertThat(result).isEmpty();
        verify(deviceKafkaClient).getDevicesByClient(CLIENT_UUID, "buffer-service");
    }

    @Test
    @DisplayName("Get buffers by client - Negative: Device service timeout")
    void shouldThrowExceptionWhenDeviceServiceTimeout() throws Exception {
        // Arrange
        CompletableFuture<GetDevicesByClientResponse> timeoutFuture = new CompletableFuture<>();
        timeoutFuture.completeExceptionally(new java.util.concurrent.TimeoutException("Timeout"));

        when(deviceKafkaClient.getDevicesByClient(eq(CLIENT_UUID), eq("buffer-service")))
            .thenReturn(timeoutFuture);

        // Act & Assert
        assertThatThrownBy(() -> bufferService.getBuffersByClient(CLIENT_UUID))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Device service error");

        verify(deviceKafkaClient).getDevicesByClient(CLIENT_UUID, "buffer-service");
    }
}