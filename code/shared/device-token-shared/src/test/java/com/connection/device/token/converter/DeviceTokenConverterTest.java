package com.connection.device.token.converter;

import static com.connection.device.token.mother.DeviceTokenObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.connection.device.token.generator.DeviceTokenGenerator;
import com.connection.device.token.model.DeviceTokenBLM;
import com.connection.device.token.model.DeviceTokenDALM;
import com.connection.device.token.model.DeviceTokenDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Device Token Converter Tests")
class DeviceTokenConverterTest {

    @Mock
    private DeviceTokenGenerator deviceTokenGenerator;

    private DeviceTokenConverter converter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new DeviceTokenConverter(deviceTokenGenerator);
    }

    @Test
    @DisplayName("Convert DALM to BLM - Positive")
    void testToBLMFromDALM_Positive() {
        DeviceTokenDALM dalM = createValidDeviceTokenDALM();
        DeviceTokenBLM expectedBLM = createValidDeviceTokenBLM();
        
        when(deviceTokenGenerator.generateDeviceToken(any(),any(), any(), any()))
                .thenReturn(expectedBLM.getToken());

        DeviceTokenBLM result = converter.toBLM(dalM);

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(dalM.getUid());
        assertThat(result.getDeviceUid()).isEqualTo(dalM.getDeviceUid());
        assertThat(result.getCreatedAt()).isEqualTo(dalM.getCreatedAt());
        assertThat(result.getExpiresAt()).isEqualTo(dalM.getExpiresAt());
    }

    @Test
    @DisplayName("Convert DTO to BLM - Positive")
    void testToBLMFromDTO_Positive() {
        DeviceTokenDTO dto = createValidDeviceTokenDTO();
        DeviceTokenBLM expectedBLM = createValidDeviceTokenBLM();
        
        when(deviceTokenGenerator.getDeviceTokenBLM(dto.getToken()))
                .thenReturn(expectedBLM);

        DeviceTokenBLM result = converter.toBLM(dto);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(dto.getToken());
    }

    @Test
    @DisplayName("Convert BLM to DTO - Positive")
    void testToDTOFromBLM_Positive() {
        DeviceTokenBLM blm = createValidDeviceTokenBLM();

        DeviceTokenDTO result = converter.toDTO(blm);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(blm.getToken());
    }

    @Test
    @DisplayName("Convert BLM to DALM - Positive")
    void testToDALMFromBLM_Positive() {
        DeviceTokenBLM blm = createValidDeviceTokenBLM();

        DeviceTokenDALM result = converter.toDALM(blm);

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getDeviceUid()).isEqualTo(blm.getDeviceUid());
        assertThat(result.getToken()).isEqualTo(blm.getToken());
        assertThat(result.getCreatedAt()).isEqualTo(blm.getCreatedAt());
        assertThat(result.getExpiresAt()).isEqualTo(blm.getExpiresAt());
    }

    @Test
    @DisplayName("Convert DTO to BLM with invalid token - Negative")
    void testToBLMFromDTOWithInvalidToken_Negative() {
        DeviceTokenDTO dto = createValidDeviceTokenDTO();
        
        when(deviceTokenGenerator.getDeviceTokenBLM(dto.getToken()))
                .thenThrow(new RuntimeException("Invalid JWT token"));

        assertThatThrownBy(() -> converter.toBLM(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid JWT token");
    }
}