package com.connection.device.converter;

import static com.connection.device.mother.DeviceObjectMother.createValidDeviceBLM;
import static com.connection.device.mother.DeviceObjectMother.createValidDeviceDALM;
import static com.connection.device.mother.DeviceObjectMother.createValidDeviceDTO;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.device.model.DeviceBLM;
import com.connection.device.model.DeviceDALM;
import com.connection.device.model.DeviceDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Device Converter Tests")
class DeviceConverterTest {

    private DeviceConverter converter;

    @BeforeEach
    void setUp() {
        converter = new DeviceConverter();
    }

    @Test
    @DisplayName("Convert DALM to BLM - Positive")
    void testToBLMFromDALM_Positive() {

        DeviceDALM dalM = createValidDeviceDALM();

        DeviceBLM result = converter.toBLM(dalM);

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(dalM.getUid());
        assertThat(result.getClientUuid()).isEqualTo(dalM.getClientUuid());
        assertThat(result.getDeviceName()).isEqualTo(dalM.getDeviceName());
        assertThat(result.getDeviceDescription()).isEqualTo(dalM.getDeviceDescription());
    }

    @Test
    @DisplayName("Convert DTO to BLM - Positive")
    void testToBLMFromDTO_Positive() {

        DeviceDTO dto = createValidDeviceDTO();

        DeviceBLM result = converter.toBLM(dto);

        assertThat(result).isNotNull();
        assertThat(result.getUid().toString()).isEqualTo(dto.getUid());
        assertThat(result.getClientUuid().toString()).isEqualTo(dto.getClientUuid());
        assertThat(result.getDeviceName()).isEqualTo(dto.getDeviceName());
        assertThat(result.getDeviceDescription()).isEqualTo(dto.getDeviceDescription());
    }

    @Test
    @DisplayName("Convert BLM to DTO - Positive")
    void testToDTOFromBLM_Positive() {

        DeviceBLM blm = createValidDeviceBLM();

        DeviceDTO result = converter.toDTO(blm);

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid().toString());
        assertThat(result.getClientUuid()).isEqualTo(blm.getClientUuid().toString());
        assertThat(result.getDeviceName()).isEqualTo(blm.getDeviceName());
        assertThat(result.getDeviceDescription()).isEqualTo(blm.getDeviceDescription());
    }

    @Test
    @DisplayName("Convert BLM to DALM - Positive")
    void testToDALMFromBLM_Positive() {

        DeviceBLM blm = createValidDeviceBLM();

        DeviceDALM result = converter.toDALM(blm);

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getClientUuid()).isEqualTo(blm.getClientUuid());
        assertThat(result.getDeviceName()).isEqualTo(blm.getDeviceName());
        assertThat(result.getDeviceDescription()).isEqualTo(blm.getDeviceDescription());
    }

    @Test
    @DisplayName("Round-trip conversion DTO -> BLM -> DTO")
    void testRoundTripDTOToBLMToDTO() {

        DeviceDTO original = createValidDeviceDTO();

        DeviceBLM blm = converter.toBLM(original);
        DeviceDTO result = converter.toDTO(blm);

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getClientUuid()).isEqualTo(original.getClientUuid());
        assertThat(result.getDeviceName()).isEqualTo(original.getDeviceName());
        assertThat(result.getDeviceDescription()).isEqualTo(original.getDeviceDescription());
    }

    @Test
    @DisplayName("Round-trip conversion DALM -> BLM -> DALM")
    void testRoundTripDALMToBLMToDALM() {

        DeviceDALM original = createValidDeviceDALM();

        DeviceBLM blm = converter.toBLM(original);
        DeviceDALM result = converter.toDALM(blm);

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getClientUuid()).isEqualTo(original.getClientUuid());
        assertThat(result.getDeviceName()).isEqualTo(original.getDeviceName());
        assertThat(result.getDeviceDescription()).isEqualTo(original.getDeviceDescription());
    }
}