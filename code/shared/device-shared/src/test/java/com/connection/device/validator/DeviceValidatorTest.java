package com.connection.device.validator;

import static com.connection.device.mother.DeviceObjectMother.createDeviceBLMWithNullFields;
import static com.connection.device.mother.DeviceObjectMother.createDeviceDTOWithInvalidUid;
import static com.connection.device.mother.DeviceObjectMother.createDeviceDTOWithLongDescription;
import static com.connection.device.mother.DeviceObjectMother.createDeviceDTOWithLongName;
import static com.connection.device.mother.DeviceObjectMother.createDeviceDTOWithNullUid;
import static com.connection.device.mother.DeviceObjectMother.createValidDeviceBLM;
import static com.connection.device.mother.DeviceObjectMother.createValidDeviceDALM;
import static com.connection.device.mother.DeviceObjectMother.createValidDeviceDTO;
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
import com.connection.device.model.DeviceBLM;
import com.connection.device.model.DeviceDALM;
import com.connection.device.model.DeviceDTO;

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
    @DisplayName("Validate valid DeviceDTO - Positive")
    void testValidateDeviceDTO_Positive() {

        DeviceDTO device = createValidDeviceDTO();

        assertThat(device).isNotNull();
        validator.validate(device);
    }

    @Test
    @Order(2)
    @DisplayName("Validate valid DeviceBLM - Positive")
    void testValidateDeviceBLM_Positive() {

        DeviceBLM device = createValidDeviceBLM();

        assertThat(device).isNotNull();
        validator.validate(device);
    }

    @Test
    @Order(3)
    @DisplayName("Validate valid DeviceDALM - Positive")
    void testValidateDeviceDALM_Positive() {

        DeviceDALM device = createValidDeviceDALM();

        assertThat(device).isNotNull();
        validator.validate(device);
    }

    // Negative tests - DeviceDTO
    @Test
    @Order(4)
    @DisplayName("Validate null DeviceDTO - Negative")
    void testValidateNullDeviceDTO_Negative() {

        DeviceDTO device = null;

        assertThatThrownBy(() -> validator.validate(device))
                .isInstanceOf(DeviceValidateException.class);
    }

    @Test
    @Order(5)
    @DisplayName("Validate DeviceDTO with null UID - Negative")
    void testValidateDeviceDTOWithNullUid_Negative() {

        DeviceDTO device = createDeviceDTOWithNullUid();

        assertThatThrownBy(() -> validator.validate(device))
                .isInstanceOf(DeviceValidateException.class);
    }

    @Test
    @Order(6)
    @DisplayName("Validate DeviceDTO with invalid UID - Negative")
    void testValidateDeviceDTOWithInvalidUid_Negative() {

        DeviceDTO device = createDeviceDTOWithInvalidUid();

        assertThatThrownBy(() -> validator.validate(device))
                .isInstanceOf(DeviceValidateException.class);
    }

    @Test
    @Order(7)
    @DisplayName("Validate DeviceDTO with long name - Negative")
    void testValidateDeviceDTOWithLongName_Negative() {

        DeviceDTO device = createDeviceDTOWithLongName();

        assertThatThrownBy(() -> validator.validate(device))
                .isInstanceOf(DeviceValidateException.class);
    }

    @Test
    @Order(8)
    @DisplayName("Validate DeviceDTO with long description - Negative")
    void testValidateDeviceDTOWithLongDescription_Negative() {

        DeviceDTO device = createDeviceDTOWithLongDescription();

        assertThatThrownBy(() -> validator.validate(device))
                .isInstanceOf(DeviceValidateException.class);
    }

    // Negative tests - DeviceBLM
    @Test
    @Order(9)
    @DisplayName("Validate null DeviceBLM - Negative")
    void testValidateNullDeviceBLM_Negative() {

        DeviceBLM device = null;

        assertThatThrownBy(() -> validator.validate(device))
                .isInstanceOf(DeviceValidateException.class);
    }

    @Test
    @Order(10)
    @DisplayName("Validate DeviceBLM with null fields - Negative")
    void testValidateDeviceBLMWithNullFields_Negative() {

        DeviceBLM device = createDeviceBLMWithNullFields();

        assertThatThrownBy(() -> validator.validate(device))
                .isInstanceOf(DeviceValidateException.class);
    }

    // Edge cases
    @Test
    @Order(11)
    @DisplayName("Validate DeviceDTO with empty name - Negative")
    void testValidateDeviceDTOWithEmptyName_Negative() {

        DeviceDTO device = DeviceDTO.builder()
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
    @DisplayName("Validate DeviceDTO with whitespace name - Negative")
    void testValidateDeviceDTOWithWhitespaceName_Negative() {

        DeviceDTO device = DeviceDTO.builder()
                .uid(UUID.randomUUID().toString())
                .clientUuid(UUID.randomUUID().toString())
                .deviceName("   ")
                .deviceDescription("Valid description")
                .build();

        assertThatThrownBy(() -> validator.validate(device))
                .isInstanceOf(DeviceValidateException.class);
    }
}