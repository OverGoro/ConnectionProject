
package com.connection.scheme.converter;

import static com.connection.scheme.mother.ConnectionSchemeObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.scheme.model.ConnectionSchemeBlm;
import com.connection.scheme.model.ConnectionSchemeDalm;
import com.connection.scheme.model.ConnectionSchemeDto;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Connection Scheme Converter Tests")
class ConnectionSchemeConverterTest {

    private ConnectionSchemeConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ConnectionSchemeConverter();
    }

    @Test
    @DisplayName("Convert Dalm to Blm - Positive")
    void testToBlmFromDalm_Positive() {
        ConnectionSchemeDalm dalM = createValidConnectionSchemeDalm();
        ConnectionSchemeBlm result = converter.toBlm(dalM);
        
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(dalM.getUid());
        assertThat(result.getClientUid()).isEqualTo(dalM.getClientUid());
        assertThat(result.getSchemeJson()).isEqualTo(dalM.getSchemeJson());
        assertThat(result.getUsedBuffers()).isEqualTo(dalM.getUsedBuffers());
        assertThat(result.getBufferTransitions()).isNotEmpty();
        assertThat(result.getBufferTransitions()).hasSize(2);
    }

    @Test
    @DisplayName("Convert Dto to Blm - Positive")
    void testToBlmFromDto_Positive() {
        ConnectionSchemeDto dto = createValidConnectionSchemeDto();
        ConnectionSchemeBlm result = converter.toBlm(dto);
        
        assertThat(result).isNotNull();
        assertThat(result.getUid().toString()).isEqualTo(dto.getUid());
        assertThat(result.getClientUid().toString()).isEqualTo(dto.getClientUid());
        assertThat(result.getSchemeJson()).isEqualTo(dto.getSchemeJson());
        assertThat(result.getUsedBuffers()).isNotEmpty();
        assertThat(result.getUsedBuffers()).hasSize(3);
        assertThat(result.getBufferTransitions()).isNotEmpty();
        assertThat(result.getBufferTransitions()).hasSize(2);
    }

    @Test
    @DisplayName("Convert Blm to Dto - Positive")
    void testToDtoFromBlm_Positive() {
        ConnectionSchemeBlm blm = createValidConnectionSchemeBlm();
        ConnectionSchemeDto result = converter.toDto(blm);
        
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid().toString());
        assertThat(result.getClientUid()).isEqualTo(blm.getClientUid().toString());
        assertThat(result.getSchemeJson()).isEqualTo(blm.getSchemeJson());
    }

    @Test
    @DisplayName("Convert Blm to Dalm - Positive")
    void testToDalmFromBlm_Positive() {
        ConnectionSchemeBlm blm = createValidConnectionSchemeBlm();
        ConnectionSchemeDalm result = converter.toDalm(blm);
        
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getClientUid()).isEqualTo(blm.getClientUid());
        assertThat(result.getSchemeJson()).isEqualTo(blm.getSchemeJson());
        assertThat(result.getUsedBuffers()).isEqualTo(blm.getUsedBuffers());
    }

    @Test
    @DisplayName("Round-trip conversion Dto -> Blm -> Dto")
    void testRoundTripDtoToBlmToDto() {
        ConnectionSchemeDto original = createValidConnectionSchemeDto();
        ConnectionSchemeBlm blm = converter.toBlm(original);
        ConnectionSchemeDto result = converter.toDto(blm);
        
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getClientUid()).isEqualTo(original.getClientUid());
        assertThat(result.getSchemeJson()).isEqualTo(original.getSchemeJson());
    }

    @Test
    @DisplayName("Round-trip conversion Dalm -> Blm -> Dalm")
    void testRoundTripDalmToBlmToDalm() {
        ConnectionSchemeDalm original = createValidConnectionSchemeDalm();
        ConnectionSchemeBlm blm = converter.toBlm(original);
        ConnectionSchemeDalm result = converter.toDalm(blm);
        
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getClientUid()).isEqualTo(original.getClientUid());
        assertThat(result.getSchemeJson()).isEqualTo(original.getSchemeJson());
        assertThat(result.getUsedBuffers()).isEqualTo(original.getUsedBuffers());
    }

    @Test
    @DisplayName("Convert with empty used buffers")
    void testConvertWithEmptyUsedBuffers() {
        ConnectionSchemeDalm dalM = createConnectionSchemeDalmWithUsedBuffers(Arrays.asList());
        ConnectionSchemeBlm result = converter.toBlm(dalM);
        
        assertThat(result).isNotNull();
        assertThat(result.getUsedBuffers()).isEmpty();
    }

    @Test
    @DisplayName("Convert with null used buffers")
    void testConvertWithNullUsedBuffers() {
        ConnectionSchemeDalm dalM = createConnectionSchemeDalmWithUsedBuffers(null);
        ConnectionSchemeBlm result = converter.toBlm(dalM);
        
        assertThat(result).isNotNull();
        assertThat(result.getUsedBuffers()).isNull();
    }
}