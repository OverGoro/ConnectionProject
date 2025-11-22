
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
import com.connection.scheme.model.ConnectionSchemeBlm;
import com.connection.scheme.model.ConnectionSchemeDalm;
import com.connection.scheme.model.ConnectionSchemeDto;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Connection Scheme Validator Tests")
class ConnectionSchemeValidatorTest {

    private ConnectionSchemeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ConnectionSchemeValidator();
    }

    @Test
    @DisplayName("Validate valid ConnectionSchemeDto - Positive")
    void testValidateConnectionSchemeDto_Positive() {
        ConnectionSchemeDto scheme = createValidConnectionSchemeDto();
        assertThat(scheme).isNotNull();
        validator.validate(scheme);
    }

    @Test
    @DisplayName("Validate valid ConnectionSchemeBlm - Positive")
    void testValidateConnectionSchemeBlm_Positive() {
        ConnectionSchemeBlm scheme = createValidConnectionSchemeBlm();
        assertThat(scheme).isNotNull();
        validator.validate(scheme);
    }

    @Test
    @DisplayName("Validate valid ConnectionSchemeDalm - Positive")
    void testValidateConnectionSchemeDalm_Positive() {
        ConnectionSchemeDalm scheme = createValidConnectionSchemeDalm();
        assertThat(scheme).isNotNull();
        validator.validate(scheme);
    }

    @Test
    @DisplayName("Validate null ConnectionSchemeDto - Negative")
    void testValidateNullConnectionSchemeDto_Negative() {
        ConnectionSchemeDto scheme = null;
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate ConnectionSchemeDto with null UID - Negative")
    void testValidateConnectionSchemeDtoWithNullUid_Negative() {
        ConnectionSchemeDto scheme = createConnectionSchemeDtoWithNullUid();
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate ConnectionSchemeDto with invalid UID - Negative")
    void testValidateConnectionSchemeDtoWithInvalidUid_Negative() {
        ConnectionSchemeDto scheme = createConnectionSchemeDtoWithInvalidUid();
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate ConnectionSchemeDto with empty JSON - Negative")
    void testValidateConnectionSchemeDtoWithEmptyJson_Negative() {
        ConnectionSchemeDto scheme = createConnectionSchemeDtoWithEmptyJson();
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate ConnectionSchemeDto with empty transitions - Negative")
    void testValidateConnectionSchemeDtoWithEmptyTransitions_Negative() {
        ConnectionSchemeDto scheme = ConnectionSchemeDto.builder()
                .uid(UUID.randomUUID().toString())
                .clientUid(UUID.randomUUID().toString())
                .schemeJson("{}") // Пустые transitions
                .build();
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate ConnectionSchemeDto with invalid JSON - Negative")
    void testValidateConnectionSchemeDtoWithInvalidJson_Negative() {
        ConnectionSchemeDto scheme = createConnectionSchemeDtoWithInvalidJson();
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate null ConnectionSchemeBlm - Negative")
    void testValidateNullConnectionSchemeBlm_Negative() {
        ConnectionSchemeBlm scheme = null;
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate ConnectionSchemeBlm with null fields - Negative")
    void testValidateConnectionSchemeBlmWithNullFields_Negative() {
        ConnectionSchemeBlm scheme = createConnectionSchemeBlmWithNullFields();
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate ConnectionSchemeBlm with null usedBuffers - Negative")
    void testValidateConnectionSchemeBlmWithNullUsedBuffers_Negative() {
        Map<UUID, List<UUID>> transitions = new HashMap<>();
        transitions.put(UUID.randomUUID(), Arrays.asList(UUID.randomUUID()));

        ConnectionSchemeBlm scheme = ConnectionSchemeBlm.builder()
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
    @DisplayName("Validate ConnectionSchemeBlm with null bufferTransitions - Negative")
    void testValidateConnectionSchemeBlmWithNullBufferTransitions_Negative() {
        ConnectionSchemeBlm scheme = ConnectionSchemeBlm.builder()
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
    @DisplayName("Validate ConnectionSchemeDto with null client UID - Negative")
    void testValidateConnectionSchemeDtoWithNullClientUid_Negative() {
        ConnectionSchemeDto scheme = ConnectionSchemeDto.builder()
                .uid(UUID.randomUUID().toString())
                .clientUid(null)
                .schemeJson("{\"usedBuffers\": [], \"bufferTransitions\": {}}")
                .build();
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }

    @Test
    @DisplayName("Validate ConnectionSchemeDto with whitespace JSON - Negative")
    void testValidateConnectionSchemeDtoWithWhitespaceJson_Negative() {
        ConnectionSchemeDto scheme = ConnectionSchemeDto.builder()
                .uid(UUID.randomUUID().toString())
                .clientUid(UUID.randomUUID().toString())
                .schemeJson("   ")
                .build();
        assertThatThrownBy(() -> validator.validate(scheme))
                .isInstanceOf(ConnectionSchemeValidateException.class);
    }
}