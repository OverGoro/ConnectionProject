package com.connection.device.token.validator;

import static com.connection.device.token.mother.DeviceTokenObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.device.token.exception.DeviceAccessTokenValidateException;
import com.connection.device.token.model.DeviceAccessTokenBLM;
import com.connection.device.token.model.DeviceAccessTokenDALM;
import com.connection.device.token.model.DeviceAccessTokenDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Device Access Token Validator Tests")
class DeviceAccessTokenValidatorTest {

    private DeviceAccessTokenValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DeviceAccessTokenValidator();
    }

    @Test
    @DisplayName("Validate valid DeviceAccessTokenDTO - Positive")
    void testValidateDeviceAccessTokenDTO_Positive() {
        DeviceAccessTokenDTO deviceToken = createValidDeviceAccessTokenDTO();
        assertThat(deviceToken).isNotNull();
        validator.validate(deviceToken);
    }

    @Test
    @DisplayName("Validate valid DeviceAccessTokenBLM - Positive")
    void testValidateDeviceAccessTokenBLM_Positive() {
        DeviceAccessTokenBLM deviceToken = createValidDeviceAccessTokenBLM();
        assertThat(deviceToken).isNotNull();
        validator.validate(deviceToken);
    }

    @Test
    @DisplayName("Validate valid DeviceAccessTokenDALM - Positive")
    void testValidateDeviceAccessTokenDALM_Positive() {
        DeviceAccessTokenDALM deviceToken = createValidDeviceAccessTokenDALM();
        assertThat(deviceToken).isNotNull();
        validator.validate(deviceToken);
    }

    @Test
    @DisplayName("Validate null DeviceAccessTokenDTO - Negative")
    void testValidateNullDeviceAccessTokenDTO_Negative() {
        DeviceAccessTokenDTO deviceToken = null;
        assertThatThrownBy(() -> validator.validate(deviceToken))
                .isInstanceOf(DeviceAccessTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate DeviceAccessTokenDTO with long token - Negative")
    void testValidateDeviceAccessTokenDTOWithLongToken_Negative() {
        DeviceAccessTokenDTO deviceToken = createDeviceAccessTokenDTOWithLongToken();
        assertThatThrownBy(() -> validator.validate(deviceToken))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Validate DeviceAccessTokenBLM with invalid dates - Negative")
    void testValidateDeviceAccessTokenBLMWithInvalidDates_Negative() {
        DeviceAccessTokenBLM deviceToken = createDeviceAccessTokenBLMWithInvalidDates();
        assertThatThrownBy(() -> validator.validate(deviceToken))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Validate DeviceAccessTokenDALM with null device token UID - Negative")
    void testValidateDeviceAccessTokenDALMWithNullDeviceTokenUid_Negative() {
        DeviceAccessTokenDALM deviceToken = DeviceAccessTokenDALM.builder()
                .uid(UUID.randomUUID())
                .deviceTokenUid(null)
                .token("valid.token")
                .createdAt(new Date())
                .expiresAt(new Date(System.currentTimeMillis() + 3600000))
                .build();
        assertThatThrownBy(() -> validator.validate(deviceToken))
                .isInstanceOf(RuntimeException.class);
    }
}