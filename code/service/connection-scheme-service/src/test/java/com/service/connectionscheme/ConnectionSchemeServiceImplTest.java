package com.service.connectionscheme;

import static com.service.connectionscheme.mother.ConnectionSchemeObjectMother.BUFFER_UUID_1;
import static com.service.connectionscheme.mother.ConnectionSchemeObjectMother.BUFFER_UUID_2;
import static com.service.connectionscheme.mother.ConnectionSchemeObjectMother.BUFFER_UUID_3;
import static com.service.connectionscheme.mother.ConnectionSchemeObjectMother.CLIENT_UUID;
import static com.service.connectionscheme.mother.ConnectionSchemeObjectMother.SCHEME_JSON;
import static com.service.connectionscheme.mother.ConnectionSchemeObjectMother.SCHEME_UUID;
import static com.service.connectionscheme.mother.ConnectionSchemeObjectMother.createSchemeDTOWithDifferentClient;
import static com.service.connectionscheme.mother.ConnectionSchemeObjectMother.createSchemeDTOWithDifferentUid;
import static com.service.connectionscheme.mother.ConnectionSchemeObjectMother.createValidSchemeBLM;
import static com.service.connectionscheme.mother.ConnectionSchemeObjectMother.createValidSchemeDTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.connection.auth.events.responses.HealthCheckResponse;
import com.connection.scheme.converter.ConnectionSchemeConverter;
import com.connection.scheme.exception.ConnectionSchemeAlreadyExistsException;
import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.repository.ConnectionSchemeRepository;
import com.connection.scheme.validator.ConnectionSchemeValidator;
import com.connection.service.auth.AuthService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Connection Scheme Service Implementation Tests - Kafka Version")
class ApiConnectionSchemeServiceImplTest {

    @Mock
    private ConnectionSchemeRepository schemeRepository;

    @Mock
    private ConnectionSchemeConverter schemeConverter;

    @Mock
    private ConnectionSchemeValidator schemeValidator;

    @Mock
    private AuthService authClient;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ConnectionSchemeServiceImpl connectionSchemeService;

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
    @DisplayName("Create scheme - Positive")
    void shouldCreateSchemeWhenValidData() {
        // Arrange
        ConnectionSchemeBLM schemeBLM = createValidSchemeBLM();
        
        setupAuthentication(CLIENT_UUID);
        when(schemeRepository.exists(SCHEME_UUID)).thenReturn(false);

        // Act
        ConnectionSchemeBLM result = connectionSchemeService.createScheme(schemeBLM);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(SCHEME_UUID);
        assertThat(result.getUsedBuffers()).isEqualTo(Arrays.asList(BUFFER_UUID_1, BUFFER_UUID_2, BUFFER_UUID_3));
        assertThat(result.getBufferTransitions()).hasSize(2);
        verify(schemeRepository).add(schemeBLM);
    }

    @Test
    @DisplayName("Create scheme - Negative: Scheme already exists")
    void shouldThrowExceptionWhenSchemeAlreadyExists() {
        // Arrange
        ConnectionSchemeBLM schemeBLM = createValidSchemeBLM();

        setupAuthentication(CLIENT_UUID);
        
        when(schemeRepository.exists(SCHEME_UUID)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> connectionSchemeService.createScheme(schemeBLM))
            .isInstanceOf(ConnectionSchemeAlreadyExistsException.class);

        verify(schemeRepository, never()).add(any());
    }

    @Test
    @DisplayName("Get scheme by UID - Positive")
    void shouldGetSchemeWhenValidRequest() {
        // Arrange
        ConnectionSchemeBLM schemeBLM = createValidSchemeBLM();

        setupAuthentication(CLIENT_UUID);
        when(schemeRepository.findByUid(SCHEME_UUID)).thenReturn(schemeBLM);
        

        // Act
        ConnectionSchemeBLM result = connectionSchemeService.getSchemeByUid(SCHEME_UUID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(SCHEME_UUID);
        assertThat(result.getUsedBuffers()).isEqualTo(Arrays.asList(BUFFER_UUID_1, BUFFER_UUID_2, BUFFER_UUID_3));
        assertThat(result.getBufferTransitions()).hasSize(2);
        verify(schemeRepository).findByUid(SCHEME_UUID);
    }

    @Test
    @DisplayName("Get scheme by UID - Negative: Scheme doesn't belong to client")
    void shouldThrowExceptionWhenSchemeNotBelongsToClient() {
        // Arrange
        UUID differentClientUuid = UUID.randomUUID();
        ConnectionSchemeBLM schemeBLM = ConnectionSchemeBLM.builder()
            .uid(SCHEME_UUID)
            .clientUid(differentClientUuid) // different client
            .schemeJson(SCHEME_JSON)
            .usedBuffers(Arrays.asList(BUFFER_UUID_1, BUFFER_UUID_2))
            .build();

        setupAuthentication(CLIENT_UUID);
        when(schemeRepository.findByUid(SCHEME_UUID)).thenReturn((schemeBLM));
        

        // Act & Assert
        assertThatThrownBy(() -> connectionSchemeService.getSchemeByUid(SCHEME_UUID))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("doesn't belong");

        verify(schemeRepository).findByUid(SCHEME_UUID);
    }

    @Test
    @DisplayName("Get schemes by client - Positive")
    void shouldGetSchemesByClientWhenValidRequest() {
        // Arrange
        ConnectionSchemeBLM schemeBLM = createValidSchemeBLM();
        ConnectionSchemeBLM expectedBLM = createValidSchemeBLM();
        List<ConnectionSchemeBLM> schemesBLM = Collections.singletonList(schemeBLM);

        when(schemeRepository.findByClientUid(CLIENT_UUID)).thenReturn(schemesBLM);

        // Act
        List<ConnectionSchemeBLM> result = connectionSchemeService.getSchemesByClient(CLIENT_UUID);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getUid()).isEqualTo(SCHEME_UUID);
        assertThat(result.get(0).getUsedBuffers()).isEqualTo(Arrays.asList(BUFFER_UUID_1, BUFFER_UUID_2, BUFFER_UUID_3));
        assertThat(result.get(0).getBufferTransitions()).hasSize(2);
        verify(schemeRepository).findByClientUid(CLIENT_UUID);
    }

    @Test
    @DisplayName("Update scheme - Positive")
    void shouldUpdateSchemeWhenValidData() {
        // Arrange
        ConnectionSchemeBLM schemeBLM = createValidSchemeBLM();
        ConnectionSchemeBLM existingScheme = createValidSchemeBLM();

        setupAuthentication(CLIENT_UUID);
        when(schemeRepository.findByUid(SCHEME_UUID)).thenReturn(existingScheme);
        

        // Act
        ConnectionSchemeBLM result = connectionSchemeService.updateScheme(SCHEME_UUID, schemeBLM);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsedBuffers()).isEqualTo(Arrays.asList(BUFFER_UUID_1, BUFFER_UUID_2, BUFFER_UUID_3));
        assertThat(result.getBufferTransitions()).hasSize(2);
        verify(schemeValidator).validate(schemeBLM);
        verify(schemeRepository).update(schemeBLM);
    }

    @Test
    @DisplayName("Update scheme - Negative: UID change attempt")
    void shouldThrowExceptionWhenTryingToChangeUid() {
        // Arrange
        Map<UUID, List<UUID>> bufferTransitions = new HashMap<>();
        bufferTransitions.put(BUFFER_UUID_1, Arrays.asList(BUFFER_UUID_2));
        
        ConnectionSchemeBLM schemeBLM = ConnectionSchemeBLM.builder()
            .uid(UUID.randomUUID()) // different UID
            .clientUid(CLIENT_UUID)
            .schemeJson(SCHEME_JSON)
            .usedBuffers(Arrays.asList(BUFFER_UUID_1, BUFFER_UUID_2))
            .bufferTransitions(bufferTransitions)
            .build();
            
        ConnectionSchemeBLM existingScheme = createValidSchemeBLM();

        setupAuthentication(CLIENT_UUID);
        when(schemeRepository.findByUid(SCHEME_UUID)).thenReturn(existingScheme);
        

        // Act & Assert
        assertThatThrownBy(() -> connectionSchemeService.updateScheme(SCHEME_UUID, schemeBLM))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot change scheme UID");

        verify(schemeRepository, never()).update(any());
    }

    @Test
    @DisplayName("Update scheme - Negative: Client UID change attempt")
    void shouldThrowExceptionWhenTryingToChangeClientUid() {
        // Arrange
        
        Map<UUID, List<UUID>> bufferTransitions = new HashMap<>();
        bufferTransitions.put(BUFFER_UUID_1, Arrays.asList(BUFFER_UUID_2));
        
        ConnectionSchemeBLM schemeBLM = ConnectionSchemeBLM.builder()
            .uid(SCHEME_UUID)
            .clientUid(UUID.randomUUID()) // different client UID
            .schemeJson(SCHEME_JSON)
            .usedBuffers(Arrays.asList(BUFFER_UUID_1, BUFFER_UUID_2))
            .bufferTransitions(bufferTransitions)
            .build();
            
        ConnectionSchemeBLM existingScheme = createValidSchemeBLM();

        setupAuthentication(CLIENT_UUID);
        when(schemeRepository.findByUid(SCHEME_UUID)).thenReturn(existingScheme);
        

        // Act & Assert
        assertThatThrownBy(() -> connectionSchemeService.updateScheme(SCHEME_UUID, schemeBLM))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Client UID from token doesn't match");

        verify(schemeRepository, never()).update(any());
    }

    @Test
    @DisplayName("Delete scheme - Positive")
    void shouldDeleteSchemeWhenValidRequest() {
        // Arrange
        ConnectionSchemeBLM existingScheme = createValidSchemeBLM();

        setupAuthentication(CLIENT_UUID);
        when(schemeRepository.findByUid(SCHEME_UUID)).thenReturn(existingScheme);
        

        // Act
        connectionSchemeService.deleteScheme(SCHEME_UUID);

        // Assert
        verify(schemeRepository).delete(SCHEME_UUID);
    }

    @Test
    @DisplayName("Scheme exists - Positive")
    void shouldReturnTrueWhenSchemeExists() {
        // Arrange
        when(schemeRepository.exists(SCHEME_UUID)).thenReturn(true);
        

        // Act
        boolean result = connectionSchemeService.schemeExists(SCHEME_UUID);

        // Assert
        assertThat(result).isTrue();
        verify(schemeRepository).exists(SCHEME_UUID);
    }

    @Test
    @DisplayName("Scheme exists - Negative: Scheme not found")
    void shouldReturnFalseWhenSchemeNotExists() {
        // Arrange
        when(schemeRepository.exists(SCHEME_UUID)).thenReturn(false);
        

        // Act
        boolean result = connectionSchemeService.schemeExists(SCHEME_UUID);

        // Assert
        assertThat(result).isFalse();
        verify(schemeRepository).exists(SCHEME_UUID);
    }

    @Test
    @DisplayName("Health check - Positive")
    void shouldReturnHealthStatus() throws Exception {
        // Arrange
        Map<String, Object> authHealth = Map.of("status", "OK");
        HealthCheckResponse healthResponse = HealthCheckResponse.success("correlation-id", authHealth);
        
        when(authClient.getHealthStatus())
            .thenReturn(Map.of("status", "OK"));

        // Act
        Map<String, Object> result = connectionSchemeService.getHealthStatus();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("OK");
        assertThat(result.get("service")).isEqualTo("connection-scheme-service");
        assertThat(result.get("auth-service")).isEqualTo(authHealth);
        verify(authClient).getHealthStatus();
    }
}