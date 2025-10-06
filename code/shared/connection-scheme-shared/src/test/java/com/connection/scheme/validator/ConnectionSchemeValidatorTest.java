// ConnectionSchemeValidatorTest.java
package com.connection.scheme.validator;

import static com.connection.scheme.mother.ConnectionSchemeObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.scheme.exception.ConnectionSchemeValidateException;
import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDALM;
import com.connection.scheme.model.ConnectionSchemeDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Connection Scheme Validator Tests")
class ConnectionSchemeValidatorTest {

    private ConnectionSchemeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ConnectionSchemeValidator();
    }

    @Test
    @DisplayName("Validate valid ConnectionSchemeDTO - Positive")
    void testValidateConnectionSchemeDTO_Positive() {
        ConnectionSchemeDTO scheme = createValidConnectionSchemeDTO();
        assertThat(scheme).isNotNull();
        validator.validate(scheme);
    }

    @Test
    @DisplayName("Validate valid ConnectionSchemeBLM - Positive")
    void testValidateConnectionSchemeBLM_Positive() {
        ConnectionSchemeBLM scheme = createValidConnectionSchemeBLM();
        assertThat(scheme).isNotNull();
        validator.validate(scheme);
    }

    @Test
    @DisplayName("Validate valid ConnectionSchemeDALM - Positive")
    void testValidateConnectionSchemeDALM_Positive() {
        ConnectionSchemeDALM scheme = createValidConnectionSchemeDALM();
        assertThat(scheme).isNotNull();
        validator.validate(scheme);
    }

    @Test
    @DisplayName("Validate null ConnectionSchemeDTO - Negative")
    void testValidateNullConnectionSchemeDTO_Negative() {
        ConnectionSchemeDTO scheme = null;
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate ConnectionSchemeDTO with null UID - Negative")
    void testValidateConnectionSchemeDTOWithNullUid_Negative() {
        ConnectionSchemeDTO scheme = createConnectionSchemeDTOWithNullUid();
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate ConnectionSchemeDTO with invalid UID - Negative")
    void testValidateConnectionSchemeDTOWithInvalidUid_Negative() {
        ConnectionSchemeDTO scheme = createConnectionSchemeDTOWithInvalidUid();
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate ConnectionSchemeDTO with empty JSON - Negative")
    void testValidateConnectionSchemeDTOWithEmptyJson_Negative() {
        ConnectionSchemeDTO scheme = createConnectionSchemeDTOWithEmptyJson();
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate ConnectionSchemeDTO with empty transitions - Negative")
    void testValidateConnectionSchemeDTOWithEmptyTransitions_Negative() {
        ConnectionSchemeDTO scheme = ConnectionSchemeDTO.builder()
                .uid(UUID.randomUUID().toString())
                .clientUid(UUID.randomUUID().toString())
                .schemeJson("{}") // Пустые transitions
                .build();
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate ConnectionSchemeDTO with invalid JSON - Negative")
    void testValidateConnectionSchemeDTOWithInvalidJson_Negative() {
        ConnectionSchemeDTO scheme = createConnectionSchemeDTOWithInvalidJson();
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate null ConnectionSchemeBLM - Negative")
    void testValidateNullConnectionSchemeBLM_Negative() {
        ConnectionSchemeBLM scheme = null;
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate ConnectionSchemeBLM with null fields - Negative")
    void testValidateConnectionSchemeBLMWithNullFields_Negative() {
        ConnectionSchemeBLM scheme = createConnectionSchemeBLMWithNullFields();
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate ConnectionSchemeBLM with null usedBuffers - Negative")
    void testValidateConnectionSchemeBLMWithNullUsedBuffers_Negative() {
        Map<UUID, List<UUID>> transitions = new HashMap<>();
        transitions.put(UUID.randomUUID(), Arrays.asList(UUID.randomUUID()));

        ConnectionSchemeBLM scheme = ConnectionSchemeBLM.builder()
                .uid(UUID.randomUUID())
                .clientUid(UUID.randomUUID())
                .schemeJson("{\"usedBuffers\": [], \"bufferTransitions\": {}}")
                .usedBuffers(null)
                .bufferTransitions(transitions)
                .build();

        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate ConnectionSchemeBLM with null bufferTransitions - Negative")
    void testValidateConnectionSchemeBLMWithNullBufferTransitions_Negative() {
        ConnectionSchemeBLM scheme = ConnectionSchemeBLM.builder()
                .uid(UUID.randomUUID())
                .clientUid(UUID.randomUUID())
                .schemeJson("{\"usedBuffers\": [], \"bufferTransitions\": {}}")
                .usedBuffers(Arrays.asList(UUID.randomUUID()))
                .bufferTransitions(null)
                .build();

        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate ConnectionSchemeDTO with null client UID - Negative")
    void testValidateConnectionSchemeDTOWithNullClientUid_Negative() {
        ConnectionSchemeDTO scheme = ConnectionSchemeDTO.builder()
                .uid(UUID.randomUUID().toString())
                .clientUid(null)
                .schemeJson("{\"usedBuffers\": [], \"bufferTransitions\": {}}")
                .build();
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate ConnectionSchemeDTO with whitespace JSON - Negative")
    void testValidateConnectionSchemeDTOWithWhitespaceJson_Negative() {
        ConnectionSchemeDTO scheme = ConnectionSchemeDTO.builder()
                .uid(UUID.randomUUID().toString())
                .clientUid(UUID.randomUUID().toString())
                .schemeJson("   ")
                .build();
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }
}