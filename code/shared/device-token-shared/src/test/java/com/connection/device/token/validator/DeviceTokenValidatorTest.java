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
import com.connection.device.token.model.DeviceTokenBlm;
import com.connection.device.token.model.DeviceTokenDalm;
import com.connection.device.token.model.DeviceTokenDto;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Device Token Validator Tests")
class DeviceTokenValidatorTest {

    private DeviceTokenValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DeviceTokenValidator();
    }

    @Test
    @DisplayName("Validate valid DeviceTokenDto - Positive")
    void testValidateDeviceTokenDto_Positive() {
        DeviceTokenDto deviceToken = createValidDeviceTokenDto();
        assertThat(deviceToken).isNotNull();
        validator.validate(deviceToken);
    }

    @Test
    @DisplayName("Validate valid DeviceTokenBlm - Positive")
    void testValidateDeviceTokenBlm_Positive() {
        DeviceTokenBlm deviceToken = createValidDeviceTokenBlm();
        assertThat(deviceToken).isNotNull();
        validator.validate(deviceToken);
    }

    @Test
    @DisplayName("Validate valid DeviceTokenDalm - Positive")
    void testValidateDeviceTokenDalm_Positive() {
        DeviceTokenDalm deviceToken = createValidDeviceTokenDalm();
        assertThat(deviceToken).isNotNull();
        validator.validate(deviceToken);
    }

    @Test
    @DisplayName("Validate null DeviceTokenDto - Negative")
    void testValidateNullDeviceTokenDto_Negative() {
        DeviceTokenDto deviceToken = null;
        assertThatThrownBy(() -> validator.validate(deviceToken))
                .isInstanceOf(DeviceTokenValidateException.class);
    }

    @Test
    @DisplayName("Validate DeviceTokenDto with empty token - Negative")
    void testValidateDeviceTokenDtoWithEmptyToken_Negative() {
        DeviceTokenDto deviceToken = createDeviceTokenDtoWithEmptyToken();
        assertThatThrownBy(() -> validator.validate(deviceToken))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Validate DeviceTokenBlm with expired token - Negative")
    void testValidateDeviceTokenBlmWithExpiredToken_Negative() {
        DeviceTokenBlm deviceToken = createDeviceTokenBlmWithExpiredToken();
        assertThatThrownBy(() -> validator.validate(deviceToken))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Validate DeviceTokenBlm with future creation date - Negative")
    void testValidateDeviceTokenBlmWithFutureCreationDate_Negative() {
        DeviceTokenBlm deviceToken = createDeviceTokenBlmWithFutureCreationDate();
        assertThatThrownBy(() -> validator.validate(deviceToken))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Validate DeviceTokenDalm with null UID - Negative")
    void testValidateDeviceTokenDalmWithNullUid_Negative() {
        DeviceTokenDalm deviceToken = DeviceTokenDalm.builder()
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
    @DisplayName("Validate DeviceTokenBlm with long token - Negative")
    void testValidateDeviceTokenBlmWithLongToken_Negative() {
        DeviceTokenBlm deviceToken = DeviceTokenBlm.builder()
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