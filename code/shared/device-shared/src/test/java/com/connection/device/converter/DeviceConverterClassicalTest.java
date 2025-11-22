package com.connection.device.converter;

import static com.connection.device.mother.DeviceObjectMother.createValidDeviceBlm;
import static com.connection.device.mother.DeviceObjectMother.createValidDeviceDalm;
import static com.connection.device.mother.DeviceObjectMother.createValidDeviceDto;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.device.model.DeviceBlm;
import com.connection.device.model.DeviceDalm;
import com.connection.device.model.DeviceDto;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Device Converter Tests")
class DeviceConverterTest {

    private DeviceConverter converter;

    @BeforeEach
    void setUp() {
        converter = new DeviceConverter();
    }

    @Test
    @DisplayName("Convert Dalm to Blm - Positive")
    void testToBlmFromDalm_Positive() {

        DeviceDalm dalM = createValidDeviceDalm();

        DeviceBlm result = converter.toBlm(dalM);

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(dalM.getUid());
        assertThat(result.getClientUuid()).isEqualTo(dalM.getClientUuid());
        assertThat(result.getDeviceName()).isEqualTo(dalM.getDeviceName());
        assertThat(result.getDeviceDescription()).isEqualTo(dalM.getDeviceDescription());
    }

    @Test
    @DisplayName("Convert Dto to Blm - Positive")
    void testToBlmFromDto_Positive() {

        DeviceDto dto = createValidDeviceDto();

        DeviceBlm result = converter.toBlm(dto);

        assertThat(result).isNotNull();
        assertThat(result.getUid().toString()).isEqualTo(dto.getUid());
        assertThat(result.getClientUuid().toString()).isEqualTo(dto.getClientUuid());
        assertThat(result.getDeviceName()).isEqualTo(dto.getDeviceName());
        assertThat(result.getDeviceDescription()).isEqualTo(dto.getDeviceDescription());
    }

    @Test
    @DisplayName("Convert Blm to Dto - Positive")
    void testToDtoFromBlm_Positive() {

        DeviceBlm blm = createValidDeviceBlm();

        DeviceDto result = converter.toDto(blm);

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid().toString());
        assertThat(result.getClientUuid()).isEqualTo(blm.getClientUuid().toString());
        assertThat(result.getDeviceName()).isEqualTo(blm.getDeviceName());
        assertThat(result.getDeviceDescription()).isEqualTo(blm.getDeviceDescription());
    }

    @Test
    @DisplayName("Convert Blm to Dalm - Positive")
    void testToDalmFromBlm_Positive() {

        DeviceBlm blm = createValidDeviceBlm();

        DeviceDalm result = converter.toDalm(blm);

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(blm.getUid());
        assertThat(result.getClientUuid()).isEqualTo(blm.getClientUuid());
        assertThat(result.getDeviceName()).isEqualTo(blm.getDeviceName());
        assertThat(result.getDeviceDescription()).isEqualTo(blm.getDeviceDescription());
    }

    @Test
    @DisplayName("Round-trip conversion Dto -> Blm -> Dto")
    void testRoundTripDtoToBlmToDto() {

        DeviceDto original = createValidDeviceDto();

        DeviceBlm blm = converter.toBlm(original);
        DeviceDto result = converter.toDto(blm);

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getClientUuid()).isEqualTo(original.getClientUuid());
        assertThat(result.getDeviceName()).isEqualTo(original.getDeviceName());
        assertThat(result.getDeviceDescription()).isEqualTo(original.getDeviceDescription());
    }

    @Test
    @DisplayName("Round-trip conversion Dalm -> Blm -> Dalm")
    void testRoundTripDalmToBlmToDalm() {

        DeviceDalm original = createValidDeviceDalm();

        DeviceBlm blm = converter.toBlm(original);
        DeviceDalm result = converter.toDalm(blm);

        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(original.getUid());
        assertThat(result.getClientUuid()).isEqualTo(original.getClientUuid());
        assertThat(result.getDeviceName()).isEqualTo(original.getDeviceName());
        assertThat(result.getDeviceDescription()).isEqualTo(original.getDeviceDescription());
    }
}