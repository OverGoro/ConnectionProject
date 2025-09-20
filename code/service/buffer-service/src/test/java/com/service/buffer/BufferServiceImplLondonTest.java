package com.service.buffer;

import static com.service.buffer.mother.BufferObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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

import com.connection.processing.buffer.converter.BufferConverter;
import com.connection.processing.buffer.exception.BufferAlreadyExistsException;
import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferDALM;
import com.connection.processing.buffer.model.BufferDTO;
import com.connection.processing.buffer.repository.BufferRepository;
import com.connection.processing.buffer.validator.BufferValidator;
import com.connection.scheme.model.ConnectionSchemeBLM;
import com.service.buffer.client.AuthServiceClient;
import com.service.buffer.client.ConnectionSchemeServiceClient;

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
    private AuthServiceClient authServiceClient;

    @Mock
    private ConnectionSchemeServiceClient connectionSchemeServiceClient;

    @InjectMocks
    private BufferServiceImpl bufferService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Create buffer - Positive")
    void shouldCreateBufferWhenValidData() {
        // Arrange
        BufferDTO bufferDTO = createValidBufferDTO();
        BufferBLM bufferBLM = createValidBufferBLM();
        BufferDALM bufferDALM = createValidBufferDALM();
        ConnectionSchemeBLM connectionScheme = createValidConnectionSchemeBLM();

        when(bufferConverter.toBLM(bufferDTO)).thenReturn(bufferBLM);
        when(bufferConverter.toDALM(bufferBLM)).thenReturn(bufferDALM);
        when(connectionSchemeServiceClient.getScheme(VALID_TOKEN, SCHEME_UUID)).thenReturn(connectionScheme);
        when(bufferRepository.exists(BUFFER_UUID)).thenReturn(false);
        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);


        // Act
        BufferBLM result = bufferService.createBuffer(VALID_TOKEN, bufferDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(BUFFER_UUID);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(bufferValidator).validate(bufferDTO);
        verify(connectionSchemeServiceClient).getScheme(VALID_TOKEN, SCHEME_UUID);
        verify(bufferRepository).add(bufferDALM);
    }

    @Test
    @DisplayName("Create buffer - Negative: Token validation fails")
    void shouldThrowExceptionWhenTokenValidationFails() {
        // Arrange
        BufferDTO bufferDTO = createValidBufferDTO();
        
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED))
            .when(authServiceClient).validateAccessToken(INVALID_TOKEN);

        // Act & Assert
        assertThatThrownBy(() -> bufferService.createBuffer(INVALID_TOKEN, bufferDTO))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("401 UNAUTHORIZED");

        verify(bufferValidator, never()).validate(any(BufferBLM.class));
        verify(connectionSchemeServiceClient, never()).getScheme(any(), any());
        verify(bufferRepository, never()).add(any());
    }

    @Test
    @DisplayName("Create buffer - Negative: Connection scheme doesn't belong to client")
    void shouldThrowExceptionWhenConnectionSchemeNotBelongsToClient() {
        // Arrange
        BufferDTO bufferDTO = createValidBufferDTO();
        BufferBLM bufferBLM = createValidBufferBLM();
        ConnectionSchemeBLM differentClientScheme = new ConnectionSchemeBLM(
            SCHEME_UUID,
            UUID.randomUUID(), // different client
            "{\"scheme\": \"test\"}"
        );

        when(bufferConverter.toBLM(bufferDTO)).thenReturn(bufferBLM);
        when(connectionSchemeServiceClient.getScheme(VALID_TOKEN, SCHEME_UUID)).thenReturn(differentClientScheme);
        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);

        // Act & Assert
        assertThatThrownBy(() -> bufferService.createBuffer(VALID_TOKEN, bufferDTO))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("doesn't belong");

        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(bufferValidator).validate(bufferDTO);
        verify(bufferRepository, never()).add(any());
    }

    @Test
    @DisplayName("Create buffer - Negative: Buffer already exists")
    void shouldThrowExceptionWhenBufferAlreadyExists() {
        // Arrange
        BufferDTO bufferDTO = createValidBufferDTO();
        BufferBLM bufferBLM = createValidBufferBLM();
        ConnectionSchemeBLM connectionScheme = createValidConnectionSchemeBLM();

        when(bufferConverter.toBLM(bufferDTO)).thenReturn(bufferBLM);
        when(connectionSchemeServiceClient.getScheme(VALID_TOKEN, SCHEME_UUID)).thenReturn(connectionScheme);
        when(bufferRepository.exists(BUFFER_UUID)).thenReturn(true);
        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);

        // Act & Assert
        assertThatThrownBy(() -> bufferService.createBuffer(VALID_TOKEN, bufferDTO))
            .isInstanceOf(BufferAlreadyExistsException.class);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(bufferValidator).validate(bufferDTO);
        verify(bufferRepository, never()).add(any());
    }

    @Test
    @DisplayName("Get buffer - Positive")
    void shouldGetBufferWhenValidRequest() {
        // Arrange
        BufferDALM bufferDALM = createValidBufferDALM();
        BufferBLM bufferBLM = createValidBufferBLM();
        ConnectionSchemeBLM connectionScheme = createValidConnectionSchemeBLM();
        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);

        when(bufferRepository.findByUid(BUFFER_UUID)).thenReturn(bufferDALM);
        when(bufferConverter.toBLM(bufferDALM)).thenReturn(bufferBLM);
        when(connectionSchemeServiceClient.getScheme(VALID_TOKEN, SCHEME_UUID)).thenReturn(connectionScheme);

        // Act
        BufferBLM result = bufferService.getBuffer(VALID_TOKEN, BUFFER_UUID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(BUFFER_UUID);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(connectionSchemeServiceClient).getScheme(VALID_TOKEN, SCHEME_UUID);
        verify(bufferRepository).findByUid(BUFFER_UUID);
    }

    @Test
    @DisplayName("Get buffers by connection scheme - Positive")
    void shouldGetBuffersByConnectionSchemeWhenValidRequest() {
        // Arrange
        BufferDALM bufferDALM = createValidBufferDALM();
        BufferBLM bufferBLM = createValidBufferBLM();
        ConnectionSchemeBLM connectionScheme = createValidConnectionSchemeBLM();
        List<BufferDALM> buffersDALM = Collections.singletonList(bufferDALM);

                when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(connectionSchemeServiceClient.getScheme(VALID_TOKEN, SCHEME_UUID)).thenReturn(connectionScheme);
        when(bufferRepository.findByConnectionSchemeUid(SCHEME_UUID)).thenReturn(buffersDALM);
        when(bufferConverter.toBLM(bufferDALM)).thenReturn(bufferBLM);

        // Act
        List<BufferBLM> result = bufferService.getBuffersByConnectionScheme(VALID_TOKEN, SCHEME_UUID);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getUid()).isEqualTo(BUFFER_UUID);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(connectionSchemeServiceClient).getScheme(VALID_TOKEN, SCHEME_UUID);
        verify(bufferRepository).findByConnectionSchemeUid(SCHEME_UUID);
    }

    @Test
    @DisplayName("Get buffers by client - Positive")
    void shouldGetBuffersByClientWhenValidRequest() {
        // Arrange
        ConnectionSchemeBLM connectionScheme = createValidConnectionSchemeBLM();
        List<ConnectionSchemeBLM> connectionSchemes = Collections.singletonList(connectionScheme);
        BufferDALM bufferDALM = createValidBufferDALM();
        BufferBLM bufferBLM = createValidBufferBLM();
        List<BufferDALM> buffersDALM = Collections.singletonList(bufferDALM);

        when(connectionSchemeServiceClient.getSchemesByClient(VALID_TOKEN)).thenReturn(connectionSchemes);
        when(bufferRepository.findByConnectionSchemeUid(SCHEME_UUID)).thenReturn(buffersDALM);
        when(bufferConverter.toBLM(bufferDALM)).thenReturn(bufferBLM);

        // Act
        List<BufferBLM> result = bufferService.getBuffersByClient(VALID_TOKEN);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getUid()).isEqualTo(BUFFER_UUID);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(connectionSchemeServiceClient).getSchemesByClient(VALID_TOKEN);
        verify(bufferRepository).findByConnectionSchemeUid(SCHEME_UUID);
    }

    @Test
    @DisplayName("Update buffer - Positive")
    void shouldUpdateBufferWhenValidData() {
        // Arrange
        BufferDTO bufferDTO = createValidBufferDTO();
        BufferBLM bufferBLM = createValidBufferBLM();
        BufferDALM bufferDALM = createValidBufferDALM();
        BufferDALM existingBuffer = createValidBufferDALM();
        ConnectionSchemeBLM connectionScheme = createValidConnectionSchemeBLM();


                when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(bufferRepository.findByUid(BUFFER_UUID)).thenReturn(existingBuffer);
        when(connectionSchemeServiceClient.getScheme(VALID_TOKEN, SCHEME_UUID)).thenReturn(connectionScheme);
        when(bufferConverter.toBLM(bufferDTO)).thenReturn(bufferBLM);
        when(bufferConverter.toDALM(bufferBLM)).thenReturn(bufferDALM);

        // Act
        BufferBLM result = bufferService.updateBuffer(VALID_TOKEN, BUFFER_UUID, bufferDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(bufferValidator).validate(bufferDTO);
        verify(bufferRepository).update(bufferDALM);
    }

    @Test
    @DisplayName("Delete buffer - Positive")
    void shouldDeleteBufferWhenValidRequest() {
        // Arrange

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);

        BufferDALM existingBuffer = createValidBufferDALM();
        ConnectionSchemeBLM connectionScheme = createValidConnectionSchemeBLM();

        when(bufferRepository.findByUid(BUFFER_UUID)).thenReturn(existingBuffer);
        when(connectionSchemeServiceClient.getScheme(VALID_TOKEN, SCHEME_UUID)).thenReturn(connectionScheme);

        // Act
        bufferService.deleteBuffer(VALID_TOKEN, BUFFER_UUID);

        // Assert
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(bufferRepository).delete(BUFFER_UUID);
    }

    @Test
    @DisplayName("Delete buffers by connection scheme - Positive")
    void shouldDeleteBuffersByConnectionSchemeWhenValidRequest() {
        // Arrange
        ConnectionSchemeBLM connectionScheme = createValidConnectionSchemeBLM();
        
        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(connectionSchemeServiceClient.getScheme(VALID_TOKEN, SCHEME_UUID)).thenReturn(connectionScheme);

        // Act
        bufferService.deleteBuffersByConnectionScheme(VALID_TOKEN, SCHEME_UUID);

        // Assert
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(bufferRepository).deleteByConnectionSchemeUid(SCHEME_UUID);
    }

    @Test
    @DisplayName("Buffer exists - Positive")
    void shouldReturnTrueWhenBufferExists() {
        // Arrange
        when(bufferRepository.exists(BUFFER_UUID)).thenReturn(true);

        // Act
        boolean result = bufferService.bufferExists(VALID_TOKEN, BUFFER_UUID);

        // Assert
        assertThat(result).isTrue();
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(bufferRepository).exists(BUFFER_UUID);
    }

    @Test
    @DisplayName("Health check - Positive")
    void shouldReturnHealthStatus() {
        // Arrange
        Map<String, Object> authHealth = Map.of("status", "OK");
        Map<String, Object> schemeHealth = Map.of("status", "OK");

        when(authServiceClient.healthCheck()).thenReturn(authHealth);
        when(connectionSchemeServiceClient.healthCheck()).thenReturn(schemeHealth);

        // Act
        Map<String, Object> result = bufferService.getHealthStatus();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("OK");
        assertThat(result.get("service")).isEqualTo("buffer-service");
        assertThat(result.get("auth-service")).isEqualTo(authHealth);
        assertThat(result.get("connection-scheme-service")).isEqualTo(schemeHealth);
        verify(authServiceClient).healthCheck();
        verify(connectionSchemeServiceClient).healthCheck();
    }
}