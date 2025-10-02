package com.service.buffer;

import static com.service.buffer.mother.BufferObjectMother.BUFFER_UUID;
import static com.service.buffer.mother.BufferObjectMother.CLIENT_UUID;
import static com.service.buffer.mother.BufferObjectMother.INVALID_TOKEN;
import static com.service.buffer.mother.BufferObjectMother.SCHEME_UUID;
import static com.service.buffer.mother.BufferObjectMother.VALID_TOKEN;
import static com.service.buffer.mother.BufferObjectMother.createValidBufferBLM;
import static com.service.buffer.mother.BufferObjectMother.createValidBufferDALM;
import static com.service.buffer.mother.BufferObjectMother.createValidBufferDTO;
import static com.service.buffer.mother.BufferObjectMother.createValidConnectionSchemeBLM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

import com.connection.auth.events.responses.ClientUidResponse;
import com.connection.auth.events.responses.HealthCheckResponse;
import com.connection.auth.events.responses.TokenValidationResponse;
import com.connection.processing.buffer.converter.BufferConverter;
import com.connection.processing.buffer.exception.BufferAlreadyExistsException;
import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferDALM;
import com.connection.processing.buffer.model.BufferDTO;
import com.connection.processing.buffer.repository.BufferRepository;
import com.connection.processing.buffer.validator.BufferValidator;
import com.connection.scheme.model.ConnectionSchemeBLM;
import com.service.buffer.client.ConnectionSchemeServiceClient;
import com.service.buffer.kafka.TypedAuthKafkaClient;

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
    private TypedAuthKafkaClient authKafkaClient; // Заменяем Feign client на Kafka client

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

        // Mock Kafka responses
        TokenValidationResponse validationResponse = TokenValidationResponse.valid(
            "correlation-id", CLIENT_UUID, "ACCESS"
        );
        ClientUidResponse clientUidResponse = ClientUidResponse.success(
            "correlation-id", CLIENT_UUID, "ACCESS"
        );

        when(authKafkaClient.validateToken(VALID_TOKEN, "buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(validationResponse));
        when(authKafkaClient.getClientUid(VALID_TOKEN, "buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(clientUidResponse));
        
        when(bufferConverter.toBLM(bufferDTO)).thenReturn(bufferBLM);
        when(bufferConverter.toDALM(bufferBLM)).thenReturn(bufferDALM);
        when(connectionSchemeServiceClient.getScheme(VALID_TOKEN, SCHEME_UUID)).thenReturn(connectionScheme);
        when(bufferRepository.exists(BUFFER_UUID)).thenReturn(false);

        // Act
        BufferBLM result = bufferService.createBuffer(VALID_TOKEN, bufferDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(BUFFER_UUID);
        verify(authKafkaClient).validateToken(VALID_TOKEN, "buffer-service");
        verify(authKafkaClient).getClientUid(VALID_TOKEN, "buffer-service");
        verify(bufferValidator).validate(bufferDTO);
        verify(connectionSchemeServiceClient).getScheme(VALID_TOKEN, SCHEME_UUID);
        verify(bufferRepository).add(bufferDALM);
    }

    @Test
    @DisplayName("Create buffer - Negative: Token validation fails")
    void shouldThrowExceptionWhenTokenValidationFails() {
        // Arrange
        BufferDTO bufferDTO = createValidBufferDTO();
        
        TokenValidationResponse validationResponse = TokenValidationResponse.error(
            "correlation-id", "Token validation failed"
        );

        when(authKafkaClient.validateToken(INVALID_TOKEN, "buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(validationResponse));

        // Act & Assert
        assertThatThrownBy(() -> bufferService.createBuffer(INVALID_TOKEN, bufferDTO))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Token validation failed");

        verify(bufferValidator, never()).validate(any(BufferDTO.class));
        verify(connectionSchemeServiceClient, never()).getScheme(anyString(), any());
        verify(bufferRepository, never()).add(any());
    }

    @Test
    @DisplayName("Create buffer - Negative: Connection scheme doesn't belong to client")
    void shouldThrowExceptionWhenConnectionSchemeNotBelongsToClient() {
        // Arrange
        BufferDTO bufferDTO = createValidBufferDTO();
        BufferBLM bufferBLM = createValidBufferBLM();
        ConnectionSchemeBLM differentClientScheme = createValidConnectionSchemeBLM(UUID.randomUUID());

        TokenValidationResponse validationResponse = TokenValidationResponse.valid(
            "correlation-id", CLIENT_UUID, "ACCESS"
        );
        ClientUidResponse clientUidResponse = ClientUidResponse.success(
            "correlation-id", CLIENT_UUID, "ACCESS"
        );

        when(authKafkaClient.validateToken(VALID_TOKEN, "buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(validationResponse));
        when(authKafkaClient.getClientUid(VALID_TOKEN, "buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(clientUidResponse));
        
        when(bufferConverter.toBLM(bufferDTO)).thenReturn(bufferBLM);
        when(connectionSchemeServiceClient.getScheme(VALID_TOKEN, SCHEME_UUID)).thenReturn(differentClientScheme);

        // Act & Assert
        assertThatThrownBy(() -> bufferService.createBuffer(VALID_TOKEN, bufferDTO))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("doesn't belong");

        verify(authKafkaClient).validateToken(VALID_TOKEN, "buffer-service");
        verify(authKafkaClient).getClientUid(VALID_TOKEN, "buffer-service");
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

        TokenValidationResponse validationResponse = TokenValidationResponse.valid(
            "correlation-id", CLIENT_UUID, "ACCESS"
        );
        ClientUidResponse clientUidResponse = ClientUidResponse.success(
            "correlation-id", CLIENT_UUID, "ACCESS"
        );

        when(authKafkaClient.validateToken(VALID_TOKEN, "buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(validationResponse));
        when(authKafkaClient.getClientUid(VALID_TOKEN, "buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(clientUidResponse));
        
        when(bufferConverter.toBLM(bufferDTO)).thenReturn(bufferBLM);
        when(connectionSchemeServiceClient.getScheme(VALID_TOKEN, SCHEME_UUID)).thenReturn(connectionScheme);
        when(bufferRepository.exists(BUFFER_UUID)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> bufferService.createBuffer(VALID_TOKEN, bufferDTO))
            .isInstanceOf(BufferAlreadyExistsException.class);
        
        verify(authKafkaClient).validateToken(VALID_TOKEN, "buffer-service");
        verify(authKafkaClient).getClientUid(VALID_TOKEN, "buffer-service");
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

        TokenValidationResponse validationResponse = TokenValidationResponse.valid(
            "correlation-id", CLIENT_UUID, "ACCESS"
        );
        ClientUidResponse clientUidResponse = ClientUidResponse.success(
            "correlation-id", CLIENT_UUID, "ACCESS"
        );

        when(authKafkaClient.validateToken(VALID_TOKEN, "buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(validationResponse));
        when(authKafkaClient.getClientUid(VALID_TOKEN, "buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(clientUidResponse));

        when(bufferRepository.findByUid(BUFFER_UUID)).thenReturn(bufferDALM);
        when(bufferConverter.toBLM(bufferDALM)).thenReturn(bufferBLM);
        when(connectionSchemeServiceClient.getScheme(VALID_TOKEN, SCHEME_UUID)).thenReturn(connectionScheme);

        // Act
        BufferBLM result = bufferService.getBuffer(VALID_TOKEN, BUFFER_UUID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(BUFFER_UUID);
        verify(authKafkaClient).validateToken(VALID_TOKEN, "buffer-service");
        verify(authKafkaClient).getClientUid(VALID_TOKEN, "buffer-service");
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

        TokenValidationResponse validationResponse = TokenValidationResponse.valid(
            "correlation-id", CLIENT_UUID, "ACCESS"
        );
        ClientUidResponse clientUidResponse = ClientUidResponse.success(
            "correlation-id", CLIENT_UUID, "ACCESS"
        );

        when(authKafkaClient.validateToken(VALID_TOKEN, "buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(validationResponse));
        when(authKafkaClient.getClientUid(VALID_TOKEN, "buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(clientUidResponse));
        
        when(connectionSchemeServiceClient.getScheme(VALID_TOKEN, SCHEME_UUID)).thenReturn(connectionScheme);
        when(bufferRepository.findByConnectionSchemeUid(SCHEME_UUID)).thenReturn(buffersDALM);
        when(bufferConverter.toBLM(bufferDALM)).thenReturn(bufferBLM);

        // Act
        List<BufferBLM> result = bufferService.getBuffersByConnectionScheme(VALID_TOKEN, SCHEME_UUID);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getUid()).isEqualTo(BUFFER_UUID);
        verify(authKafkaClient).validateToken(VALID_TOKEN, "buffer-service");
        verify(authKafkaClient).getClientUid(VALID_TOKEN, "buffer-service");
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

    TokenValidationResponse validationResponse = TokenValidationResponse.valid(
        "correlation-id", CLIENT_UUID, "ACCESS"
    );
    ClientUidResponse clientUidResponse = ClientUidResponse.success(
        "correlation-id", CLIENT_UUID, "ACCESS"
    );

    when(authKafkaClient.validateToken(VALID_TOKEN, "buffer-service"))
        .thenReturn(CompletableFuture.completedFuture(validationResponse));
    when(authKafkaClient.getClientUid(VALID_TOKEN, "buffer-service"))
        .thenReturn(CompletableFuture.completedFuture(clientUidResponse));

    when(connectionSchemeServiceClient.getSchemesByClient(VALID_TOKEN)).thenReturn(connectionSchemes);
    when(bufferRepository.findByConnectionSchemeUid(SCHEME_UUID)).thenReturn(buffersDALM);
    when(bufferConverter.toBLM(bufferDALM)).thenReturn(bufferBLM);

    // Act
    List<BufferBLM> result = bufferService.getBuffersByClient(VALID_TOKEN);

    // Assert
    assertThat(result).isNotEmpty();
    assertThat(result.get(0).getUid()).isEqualTo(BUFFER_UUID);
    verify(authKafkaClient).validateToken(VALID_TOKEN, "buffer-service");
    verify(authKafkaClient).getClientUid(VALID_TOKEN, "buffer-service");
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

        TokenValidationResponse validationResponse = TokenValidationResponse.valid(
            "correlation-id", CLIENT_UUID, "ACCESS"
        );
        ClientUidResponse clientUidResponse = ClientUidResponse.success(
            "correlation-id", CLIENT_UUID, "ACCESS"
        );

        when(authKafkaClient.validateToken(VALID_TOKEN, "buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(validationResponse));
        when(authKafkaClient.getClientUid(VALID_TOKEN, "buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(clientUidResponse));

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
        BufferDALM existingBuffer = createValidBufferDALM();
        ConnectionSchemeBLM connectionScheme = createValidConnectionSchemeBLM();

        TokenValidationResponse validationResponse = TokenValidationResponse.valid(
            "correlation-id", CLIENT_UUID, "ACCESS"
        );
        ClientUidResponse clientUidResponse = ClientUidResponse.success(
            "correlation-id", CLIENT_UUID, "ACCESS"
        );

        when(authKafkaClient.validateToken(VALID_TOKEN, "buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(validationResponse));
        when(authKafkaClient.getClientUid(VALID_TOKEN, "buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(clientUidResponse));

        when(bufferRepository.findByUid(BUFFER_UUID)).thenReturn(existingBuffer);
        when(connectionSchemeServiceClient.getScheme(VALID_TOKEN, SCHEME_UUID)).thenReturn(connectionScheme);

        // Act
        bufferService.deleteBuffer(VALID_TOKEN, BUFFER_UUID);

        // Assert
        verify(authKafkaClient).validateToken(VALID_TOKEN, "buffer-service");
        verify(authKafkaClient).getClientUid(VALID_TOKEN, "buffer-service");
        verify(bufferRepository).delete(BUFFER_UUID);
    }

    @Test
    @DisplayName("Delete buffers by connection scheme - Positive")
    void shouldDeleteBuffersByConnectionSchemeWhenValidRequest() {
        // Arrange
        ConnectionSchemeBLM connectionScheme = createValidConnectionSchemeBLM();

        TokenValidationResponse validationResponse = TokenValidationResponse.valid(
            "correlation-id", CLIENT_UUID, "ACCESS"
        );
        ClientUidResponse clientUidResponse = ClientUidResponse.success(
            "correlation-id", CLIENT_UUID, "ACCESS"
        );

        when(authKafkaClient.validateToken(VALID_TOKEN, "buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(validationResponse));
        when(authKafkaClient.getClientUid(VALID_TOKEN, "buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(clientUidResponse));
        
        when(connectionSchemeServiceClient.getScheme(VALID_TOKEN, SCHEME_UUID)).thenReturn(connectionScheme);

        // Act
        bufferService.deleteBuffersByConnectionScheme(VALID_TOKEN, SCHEME_UUID);

        // Assert
        verify(authKafkaClient).validateToken(VALID_TOKEN, "buffer-service");
        verify(authKafkaClient).getClientUid(VALID_TOKEN, "buffer-service");
        verify(bufferRepository).deleteByConnectionSchemeUid(SCHEME_UUID);
    }

    @Test
    @DisplayName("Buffer exists - Positive")
    void shouldReturnTrueWhenBufferExists() {
        // Arrange
        TokenValidationResponse validationResponse = TokenValidationResponse.valid(
            "correlation-id", CLIENT_UUID, "ACCESS"
        );

        when(authKafkaClient.validateToken(VALID_TOKEN, "buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(validationResponse));
        when(bufferRepository.exists(BUFFER_UUID)).thenReturn(true);

        // Act
        boolean result = bufferService.bufferExists(VALID_TOKEN, BUFFER_UUID);

        // Assert
        assertThat(result).isTrue();
        verify(authKafkaClient).validateToken(VALID_TOKEN, "buffer-service");
        verify(bufferRepository).exists(BUFFER_UUID);
    }

    @Test
    @DisplayName("Health check - Positive")
    void shouldReturnHealthStatus() {
        // Arrange
        Map<String, Object> healthStatus = Map.of("status", "OK", "service", "auth-service");
        HealthCheckResponse healthResponse = HealthCheckResponse.success("correlation-id", healthStatus);

        when(authKafkaClient.healthCheck("buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(healthResponse));

        // Act
        Map<String, Object> result = bufferService.getHealthStatus();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("OK");
        assertThat(result.get("service")).isEqualTo("buffer-service");
        assertThat(result.get("auth-service")).isEqualTo(healthStatus);
        verify(authKafkaClient).healthCheck("buffer-service");
    }

    @Test
    @DisplayName("Health check - Negative: Auth service unavailable")
    void shouldReturnDegradedStatusWhenAuthServiceUnavailable() {
        // Arrange
        HealthCheckResponse healthResponse = HealthCheckResponse.error("correlation-id", "Service unavailable");

        when(authKafkaClient.healthCheck("buffer-service"))
            .thenReturn(CompletableFuture.completedFuture(healthResponse));

        // Act
        Map<String, Object> result = bufferService.getHealthStatus();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("OK");
        assertThat(result.get("auth-service")).isEqualTo("UNAVAILABLE");
        verify(authKafkaClient).healthCheck("buffer-service");
    }

    @Test
    @DisplayName("Kafka timeout - Negative")
    void shouldThrowExceptionWhenKafkaTimeout() {
        // Arrange
        BufferDTO bufferDTO = createValidBufferDTO();

        CompletableFuture<TokenValidationResponse> timeoutFuture = new CompletableFuture<>();
        // Simulate timeout by not completing the future

        when(authKafkaClient.validateToken(VALID_TOKEN, "buffer-service"))
            .thenReturn(timeoutFuture);

        // Act & Assert
        assertThatThrownBy(() -> bufferService.createBuffer(VALID_TOKEN, bufferDTO))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Authentication failed");
    }
}