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

import com.connection.device.token.exception.DeviceTokenValidateException;
import com.connection.device.token.model.DeviceTokenBLM;
import com.connection.device.token.model.DeviceTokenDALM;
import com.connection.device.token.model.DeviceTokenDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Device Token Validator Tests")
class DeviceTokenValidatorTest {

    private DeviceTokenValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DeviceTokenValidator();
    }

    @Test
    @DisplayName("Validate valid DeviceTokenDTO - Positive")
    void testValidateDeviceTokenDTO_Positive() {
        DeviceTokenDTO deviceToken = createValidDeviceTokenDTO();
        assertThat(deviceToken).isNotNull();
        validator.validate(deviceToken);
    }

    @Test
    @DisplayName("Validate valid DeviceTokenBLM - Positive")
    void testValidateDeviceTokenBLM_Positive() {
        DeviceTokenBLM deviceToken = createValidDeviceTokenBLM();
        assertThat(deviceToken).isNotNull();
        validator.validate(deviceToken);
    }

    @Test
    @DisplayName("Validate valid DeviceTokenDALM - Positive")
    void testValidateDeviceTokenDALM_Positive() {
        DeviceTokenDALM deviceToken = createValidDeviceTokenDALM();
        assertThat(deviceToken).isNotNull();
        validator.validate(deviceToken);
    }

    @Test
    @DisplayName("Validate null DeviceTokenDTO - Negative")
    void testValidateNullDeviceTokenDTO_Negative() {
        DeviceTokenDTO deviceToken = null;
        assertThatThrownBy(() -> validator.validate(deviceToken))
                .isInstanceOf(DeviceTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate DeviceTokenDTO with empty token - Negative")
    void testValidateDeviceTokenDTOWithEmptyToken_Negative() {
        DeviceTokenDTO deviceToken = createDeviceTokenDTOWithEmptyToken();
        assertThatThrownBy(() -> validator.validate(deviceToken))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Validate DeviceTokenBLM with expired token - Negative")
    void testValidateDeviceTokenBLMWithExpiredToken_Negative() {
        DeviceTokenBLM deviceToken = createDeviceTokenBLMWithExpiredToken();
        assertThatThrownBy(() -> validator.validate(deviceToken))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Validate DeviceTokenBLM with future creation date - Negative")
    void testValidateDeviceTokenBLMWithFutureCreationDate_Negative() {
        DeviceTokenBLM deviceToken = createDeviceTokenBLMWithFutureCreationDate();
        assertThatThrownBy(() -> validator.validate(deviceToken))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Validate DeviceTokenDALM with null UID - Negative")
    void testValidateDeviceTokenDALMWithNullUid_Negative() {
        DeviceTokenDALM deviceToken = DeviceTokenDALM.builder()
                .uid(null)
                .deviceUid(UUID.randomUUID())
                .token("valid.token")
                .createdAt(new Date())
                .expiresAt(new Date(System.currentTimeMillis() + 3600000))
                .build();
        assertThatThrownBy(() -> validator.validate(deviceToken))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Validate DeviceTokenBLM with long token - Negative")
    void testValidateDeviceTokenBLMWithLongToken_Negative() {
        DeviceTokenBLM deviceToken = DeviceTokenBLM.builder()
                .token("a".repeat(513))
                .uid(UUID.randomUUID())
                .deviceUid(UUID.randomUUID())
                .createdAt(new Date())
                .expiresAt(new Date(System.currentTimeMillis() + 3600000))
                .build();
        assertThatThrownBy(() -> validator.validate(deviceToken))
                .isInstanceOf(RuntimeException.class);
    }
}