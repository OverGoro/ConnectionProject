package com.service.buffer;

import static com.service.buffer.mother.BufferObjectMother.BUFFER_UUID;
import static com.service.buffer.mother.BufferObjectMother.CLIENT_UUID;
import static com.service.buffer.mother.BufferObjectMother.SCHEME_UUID;
import static com.service.buffer.mother.BufferObjectMother.createValidBufferBlm;
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

import com.connection.device.DeviceService;
import com.connection.device.converter.DeviceConverter;
import com.connection.device.model.DeviceBlm;
import com.connection.processing.buffer.converter.BufferConverter;
import com.connection.processing.buffer.exception.BufferAlreadyExistsException;
import com.connection.processing.buffer.model.BufferBlm;
import com.connection.processing.buffer.repository.BufferRepository;
import com.connection.processing.buffer.validator.BufferValidator;
import com.connection.scheme.model.ConnectionSchemeBlm;
import com.connection.service.auth.AuthService;
import com.service.connectionscheme.ConnectionSchemeService;

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
    private AuthService authClient;

    @Mock
    private DeviceService deviceClient;

    @Mock
    private ConnectionSchemeService connectionSchemeClient;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ApiBufferServiceImpl bufferService;

    private static final UUID DEVICE_UUID = UUID.fromString("523e4567-e89b-12d3-a456-426614174001");

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    private void setupAuthentication(UUID clientUid) {
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(clientUid, null, Collections.emptyList());
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    private void mockDeviceExistsAndBelongsToClient(UUID deviceUuid, UUID clientUuid, boolean exists) {
        DeviceBlm device = new DeviceBlm();
        device.setClientUuid(clientUuid);
        when(deviceClient.getDevice(deviceUuid)).thenReturn(device);
    }

    private void mockConnectionSchemeExistsAndBelongsToClient(UUID schemeUuid, UUID clientUuid, boolean exists) {
        when(connectionSchemeClient.schemeExists(schemeUuid)).thenReturn(exists);
        if (exists) {
            ConnectionSchemeBlm scheme = new ConnectionSchemeBlm();
            scheme.setClientUid(clientUuid);
            when(connectionSchemeClient.getSchemeByUid(schemeUuid)).thenReturn(scheme);
        }
    }

    @Test
    @DisplayName("Create buffer - Positive")
    void shouldCreateBufferWhenValidData() {
        // Arrange
        BufferBlm bufferBlm = createValidBufferBlm();

        mockDeviceExistsAndBelongsToClient(bufferBlm.getDeviceUid(), CLIENT_UUID, true);
        when(bufferRepository.exists(BUFFER_UUID)).thenReturn(false);

        setupAuthentication(CLIENT_UUID);

        // Act
        BufferBlm result = bufferService.createBuffer(bufferBlm);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(BUFFER_UUID);
        verify(bufferValidator).validate(bufferBlm);
        verify(bufferRepository).add(bufferBlm);
    }

    @Test
    @DisplayName("Create buffer - Negative: Device doesn't belong to client")
    void shouldThrowExceptionWhenDeviceNotBelongsToClient() {
        // Arrange
        BufferBlm bufferBlm = createValidBufferBlm();

        // Мокаем устройство, которое принадлежит другому клиенту
        DeviceBlm device = new DeviceBlm();
        device.setClientUuid(UUID.randomUUID()); // другой клиент
        when(deviceClient.getDevice(bufferBlm.getDeviceUid())).thenReturn(device);
        
        setupAuthentication(CLIENT_UUID);

        // Act & Assert
        assertThatThrownBy(() -> bufferService.createBuffer(bufferBlm))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Device doesn't exist or doesn't belong to the authenticated client");

        verify(bufferValidator).validate(bufferBlm);
        verify(bufferRepository, never()).add(any());
    }

    @Test
    @DisplayName("Create buffer - Negative: Buffer already exists")
    void shouldThrowExceptionWhenBufferAlreadyExists() {
        // Arrange
        BufferBlm bufferBlm = createValidBufferBlm();

        mockDeviceExistsAndBelongsToClient(bufferBlm.getDeviceUid(), CLIENT_UUID, true);
        when(bufferRepository.exists(BUFFER_UUID)).thenReturn(true);
        setupAuthentication(CLIENT_UUID);

        // Act & Assert
        assertThatThrownBy(() -> bufferService.createBuffer(bufferBlm))
            .isInstanceOf(BufferAlreadyExistsException.class);
        
        verify(bufferValidator).validate(bufferBlm);
        verify(bufferRepository, never()).add(any());
    }

    @Test
    @DisplayName("Get buffer - Positive")
    void shouldGetBufferWhenValidRequest() {
        // Arrange
        BufferBlm bufferBlm = createValidBufferBlm();

        when(bufferRepository.findByUid(BUFFER_UUID)).thenReturn(bufferBlm);
        mockDeviceExistsAndBelongsToClient(bufferBlm.getDeviceUid(), CLIENT_UUID, true);
        setupAuthentication(CLIENT_UUID);

        // Act
        BufferBlm result = bufferService.getBufferByUid(BUFFER_UUID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(BUFFER_UUID);
        verify(bufferRepository).findByUid(BUFFER_UUID);
    }

    @Test
    @DisplayName("Get buffers by connection scheme - Positive")
    void shouldGetBuffersByConnectionSchemeWhenValidRequest() {
        // Arrange
        BufferBlm bufferBlm = createValidBufferBlm();
        List<BufferBlm> buffersBlm = Collections.singletonList(bufferBlm);

        mockConnectionSchemeExistsAndBelongsToClient(SCHEME_UUID, CLIENT_UUID, true);
        when(bufferRepository.findByConnectionSchemeUid(SCHEME_UUID)).thenReturn(buffersBlm);
        setupAuthentication(CLIENT_UUID);

        // Act
        List<BufferBlm> result = bufferService.getBuffersByConnectionScheme(SCHEME_UUID);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getUid()).isEqualTo(BUFFER_UUID);
        verify(bufferRepository).findByConnectionSchemeUid(SCHEME_UUID);
    }

    @Test
    @DisplayName("Get buffers by connection scheme - Negative: Scheme doesn't belong to client")
    void shouldThrowExceptionWhenSchemeNotBelongsToClient() {
        // Arrange
        when(connectionSchemeClient.schemeExists(SCHEME_UUID)).thenReturn(true);
        
        // Схема принадлежит другому клиенту
        ConnectionSchemeBlm scheme = new ConnectionSchemeBlm();
        scheme.setClientUid(UUID.randomUUID());
        when(connectionSchemeClient.getSchemeByUid(SCHEME_UUID)).thenReturn(scheme);
        
        setupAuthentication(CLIENT_UUID);

        // Act & Assert
        assertThatThrownBy(() -> bufferService.getBuffersByConnectionScheme(SCHEME_UUID))
            .isInstanceOf(SecurityException.class);

        verify(bufferRepository, never()).findByConnectionSchemeUid(any());
    }

    @Test
    @DisplayName("Get buffers by device - Positive")
    void shouldGetBuffersByDeviceWhenValidRequest() {
        // Arrange
        BufferBlm bufferBlm = createValidBufferBlm();
        List<BufferBlm> buffersBlm = Collections.singletonList(bufferBlm);

        mockDeviceExistsAndBelongsToClient(DEVICE_UUID, CLIENT_UUID, true);
        when(bufferRepository.findByDeviceUid(DEVICE_UUID)).thenReturn(buffersBlm);
        setupAuthentication(CLIENT_UUID);

        // Act
        List<BufferBlm> result = bufferService.getBuffersByDevice(DEVICE_UUID);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getUid()).isEqualTo(BUFFER_UUID);
        verify(bufferRepository).findByDeviceUid(DEVICE_UUID);
    }

    @Test
    @DisplayName("Get buffers by device - Negative: Device doesn't belong to client")
    void shouldThrowExceptionWhenDeviceNotBelongsToClientForGetBuffers() {
        // Arrange
        // Мокаем устройство, которое принадлежит другому клиенту
        DeviceBlm device = new DeviceBlm();
        device.setClientUuid(UUID.randomUUID());
        when(deviceClient.getDevice(DEVICE_UUID)).thenReturn(device);
        
        setupAuthentication(CLIENT_UUID);

        // Act & Assert
        assertThatThrownBy(() -> bufferService.getBuffersByDevice(DEVICE_UUID))
            .isInstanceOf(SecurityException.class);

        verify(bufferRepository, never()).findByDeviceUid(any());
    }

    @Test
    @DisplayName("Update buffer - Positive")
    void shouldUpdateBufferWhenValidData() {
        // Arrange
        BufferBlm bufferBlm = createValidBufferBlm();
        BufferBlm existingBuffer = createValidBufferBlm();

        when(bufferRepository.findByUid(BUFFER_UUID)).thenReturn(existingBuffer);
        mockDeviceExistsAndBelongsToClient(bufferBlm.getDeviceUid(), CLIENT_UUID, true);
        setupAuthentication(CLIENT_UUID);

        // Act
        BufferBlm result = bufferService.updateBuffer(BUFFER_UUID, bufferBlm);

        // Assert
        assertThat(result).isNotNull();
        verify(bufferValidator).validate(bufferBlm);
        verify(bufferRepository).update(bufferBlm);
    }

    @Test
    @DisplayName("Delete buffer - Positive")
    void shouldDeleteBufferWhenValidRequest() {
        // Arrange
        BufferBlm existingBuffer = createValidBufferBlm();

        when(bufferRepository.findByUid(BUFFER_UUID)).thenReturn(existingBuffer);
        mockDeviceExistsAndBelongsToClient(existingBuffer.getDeviceUid(), CLIENT_UUID, true);
        setupAuthentication(CLIENT_UUID);

        // Act
        bufferService.deleteBuffer(BUFFER_UUID);

        // Assert
        verify(bufferRepository).delete(BUFFER_UUID);
    }

    @Test
    @DisplayName("Delete all buffers from connection scheme - Positive")
    void shouldDeleteAllBuffersFromConnectionSchemeWhenValidRequest() {
        // Arrange
        BufferBlm bufferBlm = createValidBufferBlm();
        List<BufferBlm> buffersBlm = Collections.singletonList(bufferBlm);

        mockConnectionSchemeExistsAndBelongsToClient(SCHEME_UUID, CLIENT_UUID, true);
        when(bufferRepository.findByConnectionSchemeUid(SCHEME_UUID)).thenReturn(buffersBlm);
        setupAuthentication(CLIENT_UUID);

        // Act
        bufferService.deleteAllBuffersFromConnectionScheme(SCHEME_UUID);

        // Assert
        verify(bufferRepository).findByConnectionSchemeUid(SCHEME_UUID);
        verify(bufferRepository).removeBufferFromConnectionScheme(BUFFER_UUID, SCHEME_UUID);
    }

    @Test
    @DisplayName("Delete buffer from connection scheme - Positive")
    void shouldDeleteBufferFromConnectionSchemeWhenValidRequest() {
        // Arrange
        BufferBlm bufferBlm = createValidBufferBlm();
        List<BufferBlm> buffersBlm = Collections.singletonList(bufferBlm);

        mockConnectionSchemeExistsAndBelongsToClient(SCHEME_UUID, CLIENT_UUID, true);
        when(bufferRepository.findByConnectionSchemeUid(SCHEME_UUID)).thenReturn(buffersBlm);
        setupAuthentication(CLIENT_UUID);

        // Act
        bufferService.deleteBufferFromConnectionScheme(SCHEME_UUID, BUFFER_UUID);

        // Assert
        verify(bufferRepository).findByConnectionSchemeUid(SCHEME_UUID);
        verify(bufferRepository).removeBufferFromConnectionScheme(BUFFER_UUID, SCHEME_UUID);
    }

    @Test
    @DisplayName("Buffer exists - Positive")
    void shouldReturnTrueWhenBufferExists() {
        // Arrange
        BufferBlm bufferBlm = createValidBufferBlm();

        when(bufferRepository.exists(BUFFER_UUID)).thenReturn(true);
        when(bufferRepository.findByUid(BUFFER_UUID)).thenReturn(bufferBlm);
        mockDeviceExistsAndBelongsToClient(bufferBlm.getDeviceUid(), CLIENT_UUID, true);
        setupAuthentication(CLIENT_UUID);

        // Act
        boolean result = bufferService.bufferExists(BUFFER_UUID);

        // Assert
        assertThat(result).isTrue();
        verify(bufferRepository).exists(BUFFER_UUID);
    }

    @Test
    @DisplayName("Buffer exists - Negative: Buffer not found")
    void shouldReturnFalseWhenBufferNotExists() {
        // Arrange
        when(bufferRepository.exists(BUFFER_UUID)).thenReturn(false);
        setupAuthentication(CLIENT_UUID);

        // Act
        boolean result = bufferService.bufferExists(BUFFER_UUID);

        // Assert
        assertThat(result).isFalse();
        verify(bufferRepository).exists(BUFFER_UUID);
    }

    @Test
    @DisplayName("Buffer exists - Negative: Device doesn't belong to client")
    void shouldReturnFalseWhenDeviceNotBelongsToClient() {
        // Arrange
        BufferBlm bufferBlm = createValidBufferBlm();

        when(bufferRepository.exists(BUFFER_UUID)).thenReturn(true);
        when(bufferRepository.findByUid(BUFFER_UUID)).thenReturn(bufferBlm);
        
        // Мокаем устройство, которое принадлежит другому клиенту
        DeviceBlm device = new DeviceBlm();
        device.setClientUuid(UUID.randomUUID());
        when(deviceClient.getDevice(bufferBlm.getDeviceUid())).thenReturn(device);
        
        setupAuthentication(CLIENT_UUID);

        // Act
        boolean result = bufferService.bufferExists(BUFFER_UUID);

        // Assert
        assertThat(result).isFalse();
        verify(bufferRepository).exists(BUFFER_UUID);
    }

    @Test
    @DisplayName("Update buffer - Negative: Cannot change buffer UID")
    void shouldThrowExceptionWhenTryingToChangeBufferUid() {
        // Arrange
        BufferBlm bufferBlm = createValidBufferBlm();
        BufferBlm existingBuffer = createValidBufferBlm();
        
        // Создаем Blm с другим UID
        BufferBlm otherBufferBlm = new BufferBlm(
            UUID.randomUUID(), // другой UID
            bufferBlm.getDeviceUid(),
            bufferBlm.getMaxMessagesNumber(),
            bufferBlm.getMaxMessageSize(),
            bufferBlm.getMessagePrototype()
        );

        when(bufferRepository.findByUid(BUFFER_UUID)).thenReturn(existingBuffer);
        mockDeviceExistsAndBelongsToClient(existingBuffer.getDeviceUid(), CLIENT_UUID, true);
        setupAuthentication(CLIENT_UUID);

        // Act & Assert
        assertThatThrownBy(() -> bufferService.updateBuffer(BUFFER_UUID, otherBufferBlm))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot change buffer UID");

        verify(bufferValidator).validate(otherBufferBlm);
        verify(bufferRepository, never()).update(any());
    }
}