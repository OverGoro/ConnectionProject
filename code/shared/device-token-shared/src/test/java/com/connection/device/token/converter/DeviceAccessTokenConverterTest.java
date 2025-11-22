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
import com.connection.device.token.model.DeviceAccessTokenBlm;
import com.connection.device.token.model.DeviceAccessTokenDalm;
import com.connection.device.token.model.DeviceAccessTokenDto;

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
    // @DisplayName("Convert Dalm to Blm - Positive")
    // void testToBlmFromDalm_Positive() {
    //     DeviceAccessTokenDalm dalM = createValidDeviceAccessTokenDalm();
    //     DeviceAccessTokenBlm expectedBlm = createValidDeviceAccessTokenBlm();
        
    //     when(deviceAccessTokenGenerator.generateDeviceAccessToken(any(), any(), any()))
    //             .thenReturn(expectedBlm.getToken());

    //     DeviceAccessTokenBlm result = converter.toBlm(dalM);

    //     assertThat(result).isNotNull();
    //     assertThat(result.getUid()).isEqualTo(dalM.getUid());
    //     assertThat(result.getDeviceTokenUid()).isEqualTo(dalM.getDeviceTokenUid());
    //     assertThat(result.getCreatedAt()).isEqualTo(dalM.getCreatedAt());
    //     assertThat(result.getExpiresAt()).isEqualTo(dalM.getExpiresAt());
    // }

    @Test
    @DisplayName("Convert Dto to Blm - Positive")
    void testToBlmFromDto_Positive() {
        DeviceAccessTokenDto dto = createValidDeviceAccessTokenDto();
        DeviceAccessTokenBlm expectedBlm = createValidDeviceAccessTokenBlm();
        
        when(deviceAccessTokenGenerator.getDeviceAccessTokenBlm(dto.getToken()))
                .thenReturn(expectedBlm);

        DeviceAccessTokenBlm result = converter.toBlm(dto);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(dto.getToken());
    }

    @Test
    @DisplayName("Convert Blm to Dto - Positive")
    void testToDtoFromBlm_Positive() {
        DeviceAccessTokenBlm blm = createValidDeviceAccessTokenBlm();

        DeviceAccessTokenDto result = converter.toDto(blm);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(blm.getToken());
    }

    @Test
    @DisplayName("Convert Blm to Dalm - Positive")
    void testToDalmFromBlm_Positive() {
        DeviceAccessTokenBlm blm = createValidDeviceAccessTokenBlm();

        DeviceAccessTokenDalm result = converter.toDalm(blm);

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getDeviceTokenUid()).isEqualTo(blm.getDeviceTokenUid());
        assertThat(result.getToken()).isEqualTo(blm.getToken());
        assertThat(result.getCreatedAt()).isEqualTo(blm.getCreatedAt());
        assertThat(result.getExpiresAt()).isEqualTo(blm.getExpiresAt());
    }
}