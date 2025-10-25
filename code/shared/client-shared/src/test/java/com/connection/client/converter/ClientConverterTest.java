package com.connection.client.converter;

import static com.connection.client.mother.ClientObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.client.model.ClientBLM;
import com.connection.client.model.ClientDALM;
import com.connection.client.model.ClientDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Client Converter Tests")
class ClientConverterTest {

    private ClientConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ClientConverter();
    }

    @Test
    @DisplayName("Convert DTO to BLM - Positive")
    void testToBLMFromDTO_Positive() {
        ClientDTO dto = createValidClientDTO();
        ClientBLM result = converter.toBLM(dto);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(dto.getUid());
        assertThat(result.getBirthDate()).isEqualTo(dto.getBirthDate());
        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
        assertThat(result.getPassword()).isEqualTo(dto.getPassword());
        assertThat(result.getUsername()).isEqualTo(dto.getUsername());
    }

    @Test
    @DisplayName("Convert DALM to BLM - Positive")
    void testToBLMFromDALM_Positive() {
        ClientDALM dalM = createValidClientDALM();
        ClientBLM result = converter.toBLM(dalM);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(dalM.getUid());
        assertThat(result.getBirthDate()).isEqualTo(dalM.getBirthDate());
        assertThat(result.getEmail()).isEqualTo(dalM.getEmail());
        assertThat(result.getPassword()).isEqualTo(dalM.getPassword());
        assertThat(result.getUsername()).isEqualTo(dalM.getUsername());
    }

    @Test
    @DisplayName("Convert BLM to DTO - Positive")
    void testToDTOFromBLM_Positive() {
        ClientBLM blm = createValidClientBLM();
        ClientDTO result = converter.toDTO(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getBirthDate()).isEqualTo(blm.getBirthDate());
        assertThat(result.getEmail()).isEqualTo(blm.getEmail());
        assertThat(result.getPassword()).isEqualTo(blm.getPassword());
        assertThat(result.getUsername()).isEqualTo(blm.getUsername());
    }

    @Test
    @DisplayName("Convert BLM to DALM - Positive")
    void testToDALMFromBLM_Positive() {
        ClientBLM blm = createValidClientBLM();
        ClientDALM result = converter.toDALM(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getBirthDate()).isEqualTo(blm.getBirthDate());
        assertThat(result.getEmail()).isEqualTo(blm.getEmail());
        assertThat(result.getPassword()).isEqualTo(blm.getPassword());
        assertThat(result.getUsername()).isEqualTo(blm.getUsername());
    }

    @Test
    @DisplayName("Round-trip conversion DTO -> BLM -> DTO")
    void testRoundTripDTOToBLMToDTO() {
        ClientDTO original = createValidClientDTO();
        ClientBLM blm = converter.toBLM(original);
        ClientDTO result = converter.toDTO(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getBirthDate()).isEqualTo(original.getBirthDate());
        assertThat(result.getEmail()).isEqualTo(original.getEmail());
        assertThat(result.getPassword()).isEqualTo(original.getPassword());
        assertThat(result.getUsername()).isEqualTo(original.getUsername());
    }

    @Test
    @DisplayName("Round-trip conversion DALM -> BLM -> DALM")
    void testRoundTripDALMToBLMToDALM() {
        ClientDALM original = createValidClientDALM();
        ClientBLM blm = converter.toBLM(original);
        ClientDALM result = converter.toDALM(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getBirthDate()).isEqualTo(original.getBirthDate());
        assertThat(result.getEmail()).isEqualTo(original.getEmail());
        assertThat(result.getPassword()).isEqualTo(original.getPassword());
        assertThat(result.getUsername()).isEqualTo(original.getUsername());
    }
}