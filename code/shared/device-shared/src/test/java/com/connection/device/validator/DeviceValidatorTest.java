package com.connection.device.validator;

import static com.connection.device.mother.DeviceObjectMother.createDeviceBlmWithNullFields;
import static com.connection.device.mother.DeviceObjectMother.createDeviceDtoWithInvalidUid;
import static com.connection.device.mother.DeviceObjectMother.createDeviceDtoWithLongDescription;
import static com.connection.device.mother.DeviceObjectMother.createDeviceDtoWithLongName;
import static com.connection.device.mother.DeviceObjectMother.createDeviceDtoWithNullUid;
import static com.connection.device.mother.DeviceObjectMother.createValidDeviceBlm;
import static com.connection.device.mother.DeviceObjectMother.createValidDeviceDalm;
import static com.connection.device.mother.DeviceObjectMother.createValidDeviceDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.device.exception.DeviceValidateException;
import com.connection.device.model.DeviceBlm;
import com.connection.device.model.DeviceDalm;
import com.connection.device.model.DeviceDto;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Device Validator Tests")
class DeviceValidatorTest {

    private DeviceValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DeviceValidator();
    }

    // Positive tests
    @Test
    @Order(1)
    @DisplayName("Validate valid DeviceDto - Positive")
    void testValidateDeviceDto_Positive() {

        DeviceDto device = createValidDeviceDto();

        assertThat(device).isNotNull();
        validator.validate(device);
    }

    @Test
    @Order(2)
    @DisplayName("Validate valid DeviceBlm - Positive")
    void testValidateDeviceBlm_Positive() {

        DeviceBlm device = createValidDeviceBlm();

        assertThat(device).isNotNull();
        validator.validate(device);
    }

    @Test
    @Order(3)
    @DisplayName("Validate valid DeviceDalm - Positive")
    void testValidateDeviceDalm_Positive() {

        DeviceDalm device = createValidDeviceDalm();

        assertThat(device).isNotNull();
        validator.validate(device);
    }

    // Negative tests - DeviceDto
    @Test
    @Order(4)
    @DisplayName("Validate null DeviceDto - Negative")
    void testValidateNullDeviceDto_Negative() {

        DeviceDto device = null;

        assertThatThrownBy(() -> validator.validate(device))
                .isInstanceOf(DeviceValidateException.class);
    }

    @Test
    @Order(5)
    @DisplayName("Validate DeviceDto with null UID - Negative")
    void testValidateDeviceDtoWithNullUid_Negative() {

        DeviceDto device = createDeviceDtoWithNullUid();

        assertThatThrownBy(() -> validator.validate(device))
                .isInstanceOf(DeviceValidateException.class);
    }

    @Test
    @Order(6)
    @DisplayName("Validate DeviceDto with invalid UID - Negative")
    void testValidateDeviceDtoWithInvalidUid_Negative() {

        DeviceDto device = createDeviceDtoWithInvalidUid();

        assertThatThrownBy(() -> validator.validate(device))
                .isInstanceOf(DeviceValidateException.class);
    }

    @Test
    @Order(7)
    @DisplayName("Validate DeviceDto with long name - Negative")
    void testValidateDeviceDtoWithLongName_Negative() {

        DeviceDto device = createDeviceDtoWithLongName();

        assertThatThrownBy(() -> validator.validate(device))
                .isInstanceOf(DeviceValidateException.class);
    }

    @Test
    @Order(8)
    @DisplayName("Validate DeviceDto with long description - Negative")
    void testValidateDeviceDtoWithLongDescription_Negative() {

        DeviceDto device = createDeviceDtoWithLongDescription();

        assertThatThrownBy(() -> validator.validate(device))
                .isInstanceOf(DeviceValidateException.class);
    }

    // Negative tests - DeviceBlm
    @Test
    @Order(9)
    @DisplayName("Validate null DeviceBlm - Negative")
    void testValidateNullDeviceBlm_Negative() {

        DeviceBlm device = null;

        assertThatThrownBy(() -> validator.validate(device))
                .isInstanceOf(DeviceValidateException.class);
    }

    @Test
    @Order(10)
    @DisplayName("Validate DeviceBlm with null fields - Negative")
    void testValidateDeviceBlmWithNullFields_Negative() {

        DeviceBlm device = createDeviceBlmWithNullFields();

        assertThatThrownBy(() -> validator.validate(device))
                .isInstanceOf(DeviceValidateException.class);
    }

    // Edge cases
    @Test
    @Order(11)
    @DisplayName("Validate DeviceDto with empty name - Negative")
    void testValidateDeviceDtoWithEmptyName_Negative() {

        DeviceDto device = DeviceDto.builder()
                .uid(UUID.randomUUID().toString())
                .clientUuid(UUID.randomUUID().toString())
                .deviceName("")
                .deviceDescription("Valid description")
                .build();

        assertThatThrownBy(() -> validator.validate(device))
                .isInstanceOf(DeviceValidateException.class);
    }

    @Test
    @Order(12)
    @DisplayName("Validate DeviceDto with whitespace name - Negative")
    void testValidateDeviceDtoWithWhitespaceName_Negative() {

        DeviceDto device = DeviceDto.builder()
                .uid(UUID.randomUUID().toString())
                .clientUuid(UUID.randomUUID().toString())
                .deviceName("   ")
                .deviceDescription("Valid description")
                .build();

        assertThatThrownBy(() -> validator.validate(device))
                .isInstanceOf(DeviceValidateException.class);
    }
}