package com.service.connectionscheme;

import static com.service.connectionscheme.mother.ConnectionSchemeObjectMother.*;
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

import com.connection.scheme.converter.ConnectionSchemeConverter;
import com.connection.scheme.exception.ConnectionSchemeAlreadyExistsException;
import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDALM;
import com.connection.scheme.model.ConnectionSchemeDTO;
import com.connection.scheme.repository.ConnectionSchemeRepository;
import com.connection.scheme.validator.ConnectionSchemeValidator;
import com.service.connectionscheme.client.AuthServiceClient;

@ExtendWith(MockitoExtension.class)
@DisplayName("Connection Scheme Service Implementation Tests")
class ConnectionSchemeServiceImplLondonTest {

    @Mock
    private ConnectionSchemeRepository schemeRepository;

    @Mock
    private ConnectionSchemeConverter schemeConverter;

    @Mock
    private ConnectionSchemeValidator schemeValidator;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private ConnectionSchemeServiceImpl connectionSchemeService;

    @BeforeEach
    void setUp() {
        // Пустой метод - заглушки создаем только в тех тестах, где они нужны
    }

    @Test
    @DisplayName("Create scheme - Positive")
    void shouldCreateSchemeWhenValidData() {
        // Arrange
        ConnectionSchemeDTO schemeDTO = createValidSchemeDTO();
        ConnectionSchemeBLM schemeBLM = createValidSchemeBLM();
        ConnectionSchemeDALM schemeDALM = createValidSchemeDALM();

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(schemeConverter.toBLM(schemeDTO)).thenReturn(schemeBLM);
        when(schemeConverter.toDALM(schemeBLM)).thenReturn(schemeDALM);
        when(schemeRepository.exists(SCHEME_UUID)).thenReturn(false);

        // Act
        ConnectionSchemeBLM result = connectionSchemeService.createScheme(VALID_TOKEN, schemeDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(SCHEME_UUID);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(schemeValidator).validate(schemeDTO);
        verify(schemeRepository).add(schemeDALM);
    }

    @Test
    @DisplayName("Create scheme - Negative: Token validation fails")
    void shouldThrowExceptionWhenTokenValidationFails() {
        // Arrange
        ConnectionSchemeDTO schemeDTO = createValidSchemeDTO();
        
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED))
            .when(authServiceClient).validateAccessToken(INVALID_TOKEN);

        // Act & Assert
        assertThatThrownBy(() -> connectionSchemeService.createScheme(INVALID_TOKEN, schemeDTO))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("401 UNAUTHORIZED");

        verify(schemeValidator, never()).validate(any(ConnectionSchemeDTO.class));
        verify(schemeRepository, never()).add(any());
        verify(authServiceClient, never()).getAccessTokenClientUID(any());
    }

    @Test
    @DisplayName("Create scheme - Negative: Client UID mismatch")
    void shouldThrowExceptionWhenClientUidMismatch() {
        // Arrange
        ConnectionSchemeDTO schemeDTO = createSchemeDTOWithDifferentClient();
        UUID differentClientUuid = UUID.randomUUID();
        ConnectionSchemeBLM schemeBLM = new ConnectionSchemeBLM(
            SCHEME_UUID,
            differentClientUuid, // different client
            SCHEME_JSON
        );

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(schemeConverter.toBLM(schemeDTO)).thenReturn(schemeBLM);

        // Act & Assert
        assertThatThrownBy(() -> connectionSchemeService.createScheme(VALID_TOKEN, schemeDTO))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Client UID from token doesn't match");

        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(schemeValidator).validate(schemeDTO);
        verify(schemeRepository, never()).add(any());
    }

    @Test
    @DisplayName("Create scheme - Negative: Scheme already exists")
    void shouldThrowExceptionWhenSchemeAlreadyExists() {
        // Arrange
        ConnectionSchemeDTO schemeDTO = createValidSchemeDTO();
        ConnectionSchemeBLM schemeBLM = createValidSchemeBLM();

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(schemeConverter.toBLM(schemeDTO)).thenReturn(schemeBLM);
        when(schemeRepository.exists(SCHEME_UUID)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> connectionSchemeService.createScheme(VALID_TOKEN, schemeDTO))
            .isInstanceOf(ConnectionSchemeAlreadyExistsException.class);

        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(schemeValidator).validate(schemeDTO);
        verify(schemeRepository, never()).add(any());
    }

    @Test
    @DisplayName("Get scheme - Positive")
    void shouldGetSchemeWhenValidRequest() {
        // Arrange
        ConnectionSchemeDALM schemeDALM = createValidSchemeDALM();
        ConnectionSchemeBLM expectedBLM = createValidSchemeBLM();

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(schemeRepository.findByUid(SCHEME_UUID)).thenReturn(schemeDALM);
        when(schemeConverter.toBLM(schemeDALM)).thenReturn(expectedBLM);

        // Act
        ConnectionSchemeBLM result = connectionSchemeService.getScheme(VALID_TOKEN, SCHEME_UUID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(SCHEME_UUID);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(schemeRepository).findByUid(SCHEME_UUID);
    }

    @Test
    @DisplayName("Get scheme - Negative: Scheme doesn't belong to client")
    void shouldThrowExceptionWhenSchemeNotBelongsToClient() {
        // Arrange
        UUID differentClientUuid = UUID.randomUUID();
        ConnectionSchemeDALM schemeDALM = new ConnectionSchemeDALM(
            SCHEME_UUID,
            differentClientUuid, // different client
            SCHEME_JSON
        );

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(schemeRepository.findByUid(SCHEME_UUID)).thenReturn(schemeDALM);

        // Act & Assert
        assertThatThrownBy(() -> connectionSchemeService.getScheme(VALID_TOKEN, SCHEME_UUID))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("doesn't belong");

        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(schemeRepository).findByUid(SCHEME_UUID);
        verify(schemeConverter, never()).toBLM(any(ConnectionSchemeDALM.class));
    }

    @Test
    @DisplayName("Get schemes by client - Positive")
    void shouldGetSchemesByClientWhenValidRequest() {
        // Arrange
        ConnectionSchemeDALM schemeDALM = createValidSchemeDALM();
        ConnectionSchemeBLM expectedBLM = createValidSchemeBLM();
        List<ConnectionSchemeDALM> schemesDALM = Collections.singletonList(schemeDALM);

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(schemeRepository.findByClientUid(CLIENT_UUID)).thenReturn(schemesDALM);
        when(schemeConverter.toBLM(schemeDALM)).thenReturn(expectedBLM);

        // Act
        List<ConnectionSchemeBLM> result = connectionSchemeService.getSchemesByClient(VALID_TOKEN);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getUid()).isEqualTo(SCHEME_UUID);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(schemeRepository).findByClientUid(CLIENT_UUID);
    }

    @Test
    @DisplayName("Update scheme - Positive")
    void shouldUpdateSchemeWhenValidData() {
        // Arrange
        ConnectionSchemeDTO schemeDTO = createValidSchemeDTO();
        ConnectionSchemeBLM schemeBLM = createValidSchemeBLM();
        ConnectionSchemeDALM schemeDALM = createValidSchemeDALM();
        ConnectionSchemeDALM existingScheme = createValidSchemeDALM();

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(schemeRepository.findByUid(SCHEME_UUID)).thenReturn(existingScheme);
        when(schemeConverter.toBLM(schemeDTO)).thenReturn(schemeBLM);
        when(schemeConverter.toDALM(schemeBLM)).thenReturn(schemeDALM);

        // Act
        ConnectionSchemeBLM result = connectionSchemeService.updateScheme(VALID_TOKEN, SCHEME_UUID, schemeDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(schemeValidator).validate(schemeDTO);
        verify(schemeRepository).update(schemeDALM);
    }

    @Test
    @DisplayName("Update scheme - Negative: UID change attempt")
    void shouldThrowExceptionWhenTryingToChangeUid() {
        // Arrange
        ConnectionSchemeDTO schemeDTO = createSchemeDTOWithDifferentUid();
        ConnectionSchemeBLM schemeBLM = new ConnectionSchemeBLM(
            UUID.randomUUID(), // different UID
            CLIENT_UUID,
            SCHEME_JSON
        );
        ConnectionSchemeDALM existingScheme = createValidSchemeDALM();

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(schemeRepository.findByUid(SCHEME_UUID)).thenReturn(existingScheme);
        when(schemeConverter.toBLM(schemeDTO)).thenReturn(schemeBLM);

        // Act & Assert
        assertThatThrownBy(() -> connectionSchemeService.updateScheme(VALID_TOKEN, SCHEME_UUID, schemeDTO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot change scheme UID");

        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(schemeValidator).validate(schemeDTO);
        verify(schemeRepository, never()).update(any());
    }

    @Test
    @DisplayName("Delete scheme - Positive")
    void shouldDeleteSchemeWhenValidRequest() {
        // Arrange
        ConnectionSchemeDALM existingScheme = createValidSchemeDALM();

        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
        when(schemeRepository.findByUid(SCHEME_UUID)).thenReturn(existingScheme);

        // Act
        connectionSchemeService.deleteScheme(VALID_TOKEN, SCHEME_UUID);

        // Assert
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(schemeRepository).delete(SCHEME_UUID);
    }

    @Test
    @DisplayName("Scheme exists - Positive")
    void shouldReturnTrueWhenSchemeExists() {
        // Arrange
        when(schemeRepository.exists(SCHEME_UUID)).thenReturn(true);

        // Act
        boolean result = connectionSchemeService.schemeExists(VALID_TOKEN, SCHEME_UUID);

        // Assert
        assertThat(result).isTrue();
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(schemeRepository).exists(SCHEME_UUID);
    }

    @Test
    @DisplayName("Scheme exists - Negative: Scheme not found")
    void shouldReturnFalseWhenSchemeNotExists() {
        // Arrange
        when(schemeRepository.exists(SCHEME_UUID)).thenReturn(false);

        // Act
        boolean result = connectionSchemeService.schemeExists(VALID_TOKEN, SCHEME_UUID);

        // Assert
        assertThat(result).isFalse();
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(schemeRepository).exists(SCHEME_UUID);
    }

    @Test
    @DisplayName("Health check - Positive")
    void shouldReturnHealthStatus() {
        // Arrange
        Map<String, Object> authHealth = Map.of("status", "OK");
        when(authServiceClient.healthCheck()).thenReturn(authHealth);

        // Act
        Map<String, Object> result = connectionSchemeService.getHealthStatus();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("OK");
        assertThat(result.get("service")).isEqualTo("connection-scheme-service");
        assertThat(result.get("auth-service: ")).isEqualTo(authHealth);
        verify(authServiceClient).healthCheck();
    }

    @Test
    @DisplayName("Health check - Negative: Auth service down")
    void shouldHandleAuthServiceDownInHealthCheck() {
        // Arrange
        when(authServiceClient.healthCheck())
            .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR));

        // Act
        Map<String, Object> result = connectionSchemeService.getHealthStatus();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("OK");
        assertThat(result.get("service")).isEqualTo("connection-scheme-service");
        assertThat(result.get("auth-service: ").toString().contains("DOWN"));
        verify(authServiceClient).healthCheck();
    }
}