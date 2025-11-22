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
import com.connection.device.token.model.DeviceTokenBlm;
import com.connection.device.token.model.DeviceTokenDalm;
import com.connection.device.token.model.DeviceTokenDto;

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
    @DisplayName("Convert Dalm to Blm - Positive")
    void testToBlmFromDalm_Positive() {
        DeviceTokenDalm dalM = createValidDeviceTokenDalm();
        DeviceTokenBlm expectedBlm = createValidDeviceTokenBlm();
        
        when(deviceTokenGenerator.generateDeviceToken(any(),any(), any(), any()))
                .thenReturn(expectedBlm.getToken());

        DeviceTokenBlm result = converter.toBlm(dalM);

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(dalM.getUid());
        assertThat(result.getDeviceUid()).isEqualTo(dalM.getDeviceUid());
        assertThat(result.getCreatedAt()).isEqualTo(dalM.getCreatedAt());
        assertThat(result.getExpiresAt()).isEqualTo(dalM.getExpiresAt());
    }

    @Test
    @DisplayName("Convert Dto to Blm - Positive")
    void testToBlmFromDto_Positive() {
        DeviceTokenDto dto = createValidDeviceTokenDto();
        DeviceTokenBlm expectedBlm = createValidDeviceTokenBlm();
        
        when(deviceTokenGenerator.getDeviceTokenBlm(dto.getToken()))
                .thenReturn(expectedBlm);

        DeviceTokenBlm result = converter.toBlm(dto);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(dto.getToken());
    }

    @Test
    @DisplayName("Convert Blm to Dto - Positive")
    void testToDtoFromBlm_Positive() {
        DeviceTokenBlm blm = createValidDeviceTokenBlm();

        DeviceTokenDto result = converter.toDto(blm);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(blm.getToken());
    }

    @Test
    @DisplayName("Convert Blm to Dalm - Positive")
    void testToDalmFromBlm_Positive() {
        DeviceTokenBlm blm = createValidDeviceTokenBlm();

        DeviceTokenDalm result = converter.toDalm(blm);

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getDeviceUid()).isEqualTo(blm.getDeviceUid());
        assertThat(result.getToken()).isEqualTo(blm.getToken());
        assertThat(result.getCreatedAt()).isEqualTo(blm.getCreatedAt());
        assertThat(result.getExpiresAt()).isEqualTo(blm.getExpiresAt());
    }

    @Test
    @DisplayName("Convert Dto to Blm with invalid token - Negative")
    void testToBlmFromDtoWithInvalidToken_Negative() {
        DeviceTokenDto dto = createValidDeviceTokenDto();
        
        when(deviceTokenGenerator.getDeviceTokenBlm(dto.getToken()))
                .thenThrow(new RuntimeException("Invalid JWT token"));

        assertThatThrownBy(() -> converter.toBlm(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid JWT token");
    }
}