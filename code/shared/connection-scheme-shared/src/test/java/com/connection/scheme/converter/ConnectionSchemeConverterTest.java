// ConnectionSchemeConverterTest.java
package com.connection.scheme.converter;

import static com.connection.scheme.mother.ConnectionSchemeObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDALM;
import com.connection.scheme.model.ConnectionSchemeDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Connection Scheme Converter Tests")
class ConnectionSchemeConverterTest {

    private ConnectionSchemeConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ConnectionSchemeConverter();
    }

    @Test
    @DisplayName("Convert DALM to BLM - Positive")
    void testToBLMFromDALM_Positive() {
        ConnectionSchemeDALM dalM = createValidConnectionSchemeDALM();
        ConnectionSchemeBLM result = converter.toBLM(dalM);
        
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(dalM.getUid());
        assertThat(result.getClientUid()).isEqualTo(dalM.getClientUid());
        assertThat(result.getSchemeJson()).isEqualTo(dalM.getSchemeJson());
        assertThat(result.getUsedBuffers()).isEqualTo(dalM.getUsedBuffers());
        assertThat(result.getBufferTransitions()).isNotEmpty();
        assertThat(result.getBufferTransitions()).hasSize(2);
    }

    @Test
    @DisplayName("Convert DTO to BLM - Positive")
    void testToBLMFromDTO_Positive() {
        ConnectionSchemeDTO dto = createValidConnectionSchemeDTO();
        ConnectionSchemeBLM result = converter.toBLM(dto);
        
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
    @DisplayName("Convert BLM to DTO - Positive")
    void testToDTOFromBLM_Positive() {
        ConnectionSchemeBLM blm = createValidConnectionSchemeBLM();
        ConnectionSchemeDTO result = converter.toDTO(blm);
        
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid().toString());
        assertThat(result.getClientUid()).isEqualTo(blm.getClientUid().toString());
        assertThat(result.getSchemeJson()).isEqualTo(blm.getSchemeJson());
    }

    @Test
    @DisplayName("Convert BLM to DALM - Positive")
    void testToDALMFromBLM_Positive() {
        ConnectionSchemeBLM blm = createValidConnectionSchemeBLM();
        ConnectionSchemeDALM result = converter.toDALM(blm);
        
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getClientUid()).isEqualTo(blm.getClientUid());
        assertThat(result.getSchemeJson()).isEqualTo(blm.getSchemeJson());
        assertThat(result.getUsedBuffers()).isEqualTo(blm.getUsedBuffers());
    }

    @Test
    @DisplayName("Round-trip conversion DTO -> BLM -> DTO")
    void testRoundTripDTOToBLMToDTO() {
        ConnectionSchemeDTO original = createValidConnectionSchemeDTO();
        ConnectionSchemeBLM blm = converter.toBLM(original);
        ConnectionSchemeDTO result = converter.toDTO(blm);
        
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getClientUid()).isEqualTo(original.getClientUid());
        assertThat(result.getSchemeJson()).isEqualTo(original.getSchemeJson());
    }

    @Test
    @DisplayName("Round-trip conversion DALM -> BLM -> DALM")
    void testRoundTripDALMToBLMToDALM() {
        ConnectionSchemeDALM original = createValidConnectionSchemeDALM();
        ConnectionSchemeBLM blm = converter.toBLM(original);
        ConnectionSchemeDALM result = converter.toDALM(blm);
        
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getClientUid()).isEqualTo(original.getClientUid());
        assertThat(result.getSchemeJson()).isEqualTo(original.getSchemeJson());
        assertThat(result.getUsedBuffers()).isEqualTo(original.getUsedBuffers());
    }

    @Test
    @DisplayName("Convert with empty used buffers")
    void testConvertWithEmptyUsedBuffers() {
        ConnectionSchemeDALM dalM = createConnectionSchemeDALMWithUsedBuffers(Arrays.asList());
        ConnectionSchemeBLM result = converter.toBLM(dalM);
        
        assertThat(result).isNotNull();
        assertThat(result.getUsedBuffers()).isEmpty();
    }

    @Test
    @DisplayName("Convert with null used buffers")
    void testConvertWithNullUsedBuffers() {
        ConnectionSchemeDALM dalM = createConnectionSchemeDALMWithUsedBuffers(null);
        ConnectionSchemeBLM result = converter.toBLM(dalM);
        
        assertThat(result).isNotNull();
        assertThat(result.getUsedBuffers()).isNull();
    }
}