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
import com.connection.device.token.model.DeviceAccessTokenBlm;
import com.connection.device.token.model.DeviceAccessTokenDalm;
import com.connection.device.token.model.DeviceAccessTokenDto;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Device Access Token Validator Tests")
class DeviceAccessTokenValidatorTest {

    private DeviceAccessTokenValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DeviceAccessTokenValidator();
    }

    @Test
    @DisplayName("Validate valid DeviceAccessTokenDto - Positive")
    void testValidateDeviceAccessTokenDto_Positive() {
        DeviceAccessTokenDto deviceToken = createValidDeviceAccessTokenDto();
        assertThat(deviceToken).isNotNull();
        validator.validate(deviceToken);
    }

    @Test
    @DisplayName("Validate valid DeviceAccessTokenBlm - Positive")
    void testValidateDeviceAccessTokenBlm_Positive() {
        DeviceAccessTokenBlm deviceToken = createValidDeviceAccessTokenBlm();
        assertThat(deviceToken).isNotNull();
        validator.validate(deviceToken);
    }

    @Test
    @DisplayName("Validate valid DeviceAccessTokenDalm - Positive")
    void testValidateDeviceAccessTokenDalm_Positive() {
        DeviceAccessTokenDalm deviceToken = createValidDeviceAccessTokenDalm();
        assertThat(deviceToken).isNotNull();
        validator.validate(deviceToken);
    }

    @Test
    @DisplayName("Validate null DeviceAccessTokenDto - Negative")
    void testValidateNullDeviceAccessTokenDto_Negative() {
        DeviceAccessTokenDto deviceToken = null;
        assertThatThrownBy(() -> validator.validate(deviceToken))
                .isInstanceOf(DeviceAccessTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate DeviceAccessTokenDto with long token - Negative")
    void testValidateDeviceAccessTokenDtoWithLongToken_Negative() {
        DeviceAccessTokenDto deviceToken = createDeviceAccessTokenDtoWithLongToken();
        assertThatThrownBy(() -> validator.validate(deviceToken))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Validate DeviceAccessTokenBlm with invalid dates - Negative")
    void testValidateDeviceAccessTokenBlmWithInvalidDates_Negative() {
        DeviceAccessTokenBlm deviceToken = createDeviceAccessTokenBlmWithInvalidDates();
        assertThatThrownBy(() -> validator.validate(deviceToken))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Validate DeviceAccessTokenDalm with null device token UID - Negative")
    void testValidateDeviceAccessTokenDalmWithNullDeviceTokenUid_Negative() {
        DeviceAccessTokenDalm deviceToken = DeviceAccessTokenDalm.builder()
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