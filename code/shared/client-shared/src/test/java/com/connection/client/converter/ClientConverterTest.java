package com.connection.client.converter;

import static com.connection.client.mother.ClientObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.client.model.ClientBlm;
import com.connection.client.model.ClientDalm;
import com.connection.client.model.ClientDto;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Client Converter Tests")
class ClientConverterTest {

    private ClientConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ClientConverter();
    }

    @Test
    @DisplayName("Convert Dto to Blm - Positive")
    void testToBlmFromDto_Positive() {
        ClientDto dto = createValidClientDto();
        ClientBlm result = converter.toBlm(dto);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(dto.getUid());
        assertThat(result.getBirthDate()).isEqualTo(dto.getBirthDate());
        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
        assertThat(result.getPassword()).isEqualTo(dto.getPassword());
        assertThat(result.getUsername()).isEqualTo(dto.getUsername());
    }

    @Test
    @DisplayName("Convert Dalm to Blm - Positive")
    void testToBlmFromDalm_Positive() {
        ClientDalm dalM = createValidClientDalm();
        ClientBlm result = converter.toBlm(dalM);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(dalM.getUid());
        assertThat(result.getBirthDate()).isEqualTo(dalM.getBirthDate());
        assertThat(result.getEmail()).isEqualTo(dalM.getEmail());
        assertThat(result.getPassword()).isEqualTo(dalM.getPassword());
        assertThat(result.getUsername()).isEqualTo(dalM.getUsername());
    }

    @Test
    @DisplayName("Convert Blm to Dto - Positive")
    void testToDtoFromBlm_Positive() {
        ClientBlm blm = createValidClientBlm();
        ClientDto result = converter.toDto(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getBirthDate()).isEqualTo(blm.getBirthDate());
        assertThat(result.getEmail()).isEqualTo(blm.getEmail());
        assertThat(result.getPassword()).isEqualTo(blm.getPassword());
        assertThat(result.getUsername()).isEqualTo(blm.getUsername());
    }

    @Test
    @DisplayName("Convert Blm to Dalm - Positive")
    void testToDalmFromBlm_Positive() {
        ClientBlm blm = createValidClientBlm();
        ClientDalm result = converter.toDalm(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getBirthDate()).isEqualTo(blm.getBirthDate());
        assertThat(result.getEmail()).isEqualTo(blm.getEmail());
        assertThat(result.getPassword()).isEqualTo(blm.getPassword());
        assertThat(result.getUsername()).isEqualTo(blm.getUsername());
    }

    @Test
    @DisplayName("Round-trip conversion Dto -> Blm -> Dto")
    void testRoundTripDtoToBlmToDto() {
        ClientDto original = createValidClientDto();
        ClientBlm blm = converter.toBlm(original);
        ClientDto result = converter.toDto(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getBirthDate()).isEqualTo(original.getBirthDate());
        assertThat(result.getEmail()).isEqualTo(original.getEmail());
        assertThat(result.getPassword()).isEqualTo(original.getPassword());
        assertThat(result.getUsername()).isEqualTo(original.getUsername());
    }

    @Test
    @DisplayName("Round-trip conversion Dalm -> Blm -> Dalm")
    void testRoundTripDalmToBlmToDalm() {
        ClientDalm original = createValidClientDalm();
        ClientBlm blm = converter.toBlm(original);
        ClientDalm result = converter.toDalm(blm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getBirthDate()).isEqualTo(original.getBirthDate());
        assertThat(result.getEmail()).isEqualTo(original.getEmail());
        assertThat(result.getPassword()).isEqualTo(original.getPassword());
        assertThat(result.getUsername()).isEqualTo(original.getUsername());
    }
}