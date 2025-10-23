package com.connection.device.token.converter;

import static com.connection.device.token.mother.DeviceTokenObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.connection.device.token.generator.DeviceAccessTokenGenerator;
import com.connection.device.token.model.DeviceAccessTokenBLM;
import com.connection.device.token.model.DeviceAccessTokenDALM;
import com.connection.device.token.model.DeviceAccessTokenDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Device Access Token Converter Tests")
class DeviceAccessTokenConverterTest {

    @Mock
    private DeviceAccessTokenGenerator deviceAccessTokenGenerator;

    private DeviceAccessTokenConverter converter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new DeviceAccessTokenConverter(deviceAccessTokenGenerator);
    }

    // @Test
    // @DisplayName("Convert DALM to BLM - Positive")
    // void testToBLMFromDALM_Positive() {
    //     DeviceAccessTokenDALM dalM = createValidDeviceAccessTokenDALM();
    //     DeviceAccessTokenBLM expectedBLM = createValidDeviceAccessTokenBLM();
        
    //     when(deviceAccessTokenGenerator.generateDeviceAccessToken(any(), any(), any()))
    //             .thenReturn(expectedBLM.getToken());

    //     DeviceAccessTokenBLM result = converter.toBLM(dalM);

    //     assertThat(result).isNotNull();
    //     assertThat(result.getUid()).isEqualTo(dalM.getUid());
    //     assertThat(result.getDeviceTokenUid()).isEqualTo(dalM.getDeviceTokenUid());
    //     assertThat(result.getCreatedAt()).isEqualTo(dalM.getCreatedAt());
    //     assertThat(result.getExpiresAt()).isEqualTo(dalM.getExpiresAt());
    // }

    @Test
    @DisplayName("Convert DTO to BLM - Positive")
    void testToBLMFromDTO_Positive() {
        DeviceAccessTokenDTO dto = createValidDeviceAccessTokenDTO();
        DeviceAccessTokenBLM expectedBLM = createValidDeviceAccessTokenBLM();
        
        when(deviceAccessTokenGenerator.getDeviceAccessTokenBLM(dto.getToken()))
                .thenReturn(expectedBLM);

        DeviceAccessTokenBLM result = converter.toBLM(dto);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(dto.getToken());
    }

    @Test
    @DisplayName("Convert BLM to DTO - Positive")
    void testToDTOFromBLM_Positive() {
        DeviceAccessTokenBLM blm = createValidDeviceAccessTokenBLM();

        DeviceAccessTokenDTO result = converter.toDTO(blm);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(blm.getToken());
    }

    @Test
    @DisplayName("Convert BLM to DALM - Positive")
    void testToDALMFromBLM_Positive() {
        DeviceAccessTokenBLM blm = createValidDeviceAccessTokenBLM();

        DeviceAccessTokenDALM result = converter.toDALM(blm);

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getDeviceTokenUid()).isEqualTo(blm.getDeviceTokenUid());
        assertThat(result.getToken()).isEqualTo(blm.getToken());
        assertThat(result.getCreatedAt()).isEqualTo(blm.getCreatedAt());
        assertThat(result.getExpiresAt()).isEqualTo(blm.getExpiresAt());
    }
}