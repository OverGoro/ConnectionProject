package com.connection.bufferjsondata;

import static com.connection.bufferjsondata.mother.BufferJsonDataObjectMother.BUFFER_UUID;
import static com.connection.bufferjsondata.mother.BufferJsonDataObjectMother.CLIENT_UUID;
import static com.connection.bufferjsondata.mother.BufferJsonDataObjectMother.DATA_UUID;
import static com.connection.bufferjsondata.mother.BufferJsonDataObjectMother.INVALID_TOKEN;
import static com.connection.bufferjsondata.mother.BufferJsonDataObjectMother.VALID_TOKEN;
import static com.connection.bufferjsondata.mother.BufferJsonDataObjectMother.createValidBufferBLM;
import static com.connection.bufferjsondata.mother.BufferJsonDataObjectMother.createValidBufferJsonDataBLM;
import static com.connection.bufferjsondata.mother.BufferJsonDataObjectMother.createValidBufferJsonDataDALM;
import static com.connection.bufferjsondata.mother.BufferJsonDataObjectMother.createValidBufferJsonDataDTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.connection.bufferjsondata.client.AuthServiceClient;
import com.connection.bufferjsondata.client.BufferServiceClient;
import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.objects.json.converter.BufferJsonDataConverter;
import com.connection.processing.buffer.objects.json.exception.BufferJsonDataAlreadyExistsException;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataBLM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDALM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDTO;
import com.connection.processing.buffer.objects.json.repository.BufferJsonDataRepository;
import com.connection.processing.buffer.objects.json.validator.BufferJsonDataValidator;

@ExtendWith(MockitoExtension.class)
@DisplayName("Buffer Json Data Service Implementation Tests")
class BufferJsonDataServiceImplLondonTest {

    @Mock
    private BufferJsonDataRepository jsonDataRepository;

    @Mock
    private BufferJsonDataConverter jsonDataConverter;

    @Mock
    private BufferJsonDataValidator jsonDataValidator;

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private BufferServiceClient bufferServiceClient;

    @InjectMocks
    private BufferJsonDataServiceImpl bufferJsonDataService;

    @BeforeEach
    void setUp() {
        // Настраиваем базовые моки для валидации токена
        when(authServiceClient.getAccessTokenClientUID(VALID_TOKEN)).thenReturn(CLIENT_UUID);
    }

    @Test
    @DisplayName("Add JSON data - Positive")
    void shouldAddJsonDataWhenValidData() {
        // Arrange
        BufferJsonDataDTO jsonDataDTO = createValidBufferJsonDataDTO();
        BufferJsonDataBLM jsonDataBLM = createValidBufferJsonDataBLM();
        BufferJsonDataDALM jsonDataDALM = createValidBufferJsonDataDALM();
        BufferBLM buffer = createValidBufferBLM();

        when(jsonDataConverter.toBLM(jsonDataDTO)).thenReturn(jsonDataBLM);
        when(jsonDataConverter.toDALM(jsonDataBLM)).thenReturn(jsonDataDALM);
        when(bufferServiceClient.getBuffer(VALID_TOKEN, BUFFER_UUID)).thenReturn(buffer);
        when(jsonDataRepository.exists(DATA_UUID)).thenReturn(false);

        // Act
        BufferJsonDataBLM result = bufferJsonDataService.addJsonData(VALID_TOKEN, jsonDataDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(DATA_UUID);
        verify(jsonDataValidator).validate(jsonDataDTO);
        verify(bufferServiceClient).getBuffer(VALID_TOKEN, BUFFER_UUID);
        verify(jsonDataRepository).add(jsonDataDALM);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Add JSON data - Negative: Token validation fails")
    void shouldThrowExceptionWhenTokenValidationFails() {
        // Arrange
        BufferJsonDataDTO jsonDataDTO = createValidBufferJsonDataDTO();
        
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED))
            .when(authServiceClient).validateAccessToken(INVALID_TOKEN);

        // Act & Assert
        assertThatThrownBy(() -> bufferJsonDataService.addJsonData(INVALID_TOKEN, jsonDataDTO))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("401 UNAUTHORIZED");

        verify(jsonDataValidator, never()).validate(any(BufferJsonDataBLM.class));
        verify(bufferServiceClient, never()).getBuffer(any(), any());
        verify(jsonDataRepository, never()).add(any());
    }

    @Test
    @DisplayName("Add JSON data - Negative: Data already exists")
    void shouldThrowExceptionWhenDataAlreadyExists() {
        // Arrange
        BufferJsonDataDTO jsonDataDTO = createValidBufferJsonDataDTO();
        BufferJsonDataBLM jsonDataBLM = createValidBufferJsonDataBLM();
        BufferBLM buffer = createValidBufferBLM();

        when(jsonDataConverter.toBLM(jsonDataDTO)).thenReturn(jsonDataBLM);
        when(bufferServiceClient.getBuffer(VALID_TOKEN, BUFFER_UUID)).thenReturn(buffer);
        when(jsonDataRepository.exists(DATA_UUID)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> bufferJsonDataService.addJsonData(VALID_TOKEN, jsonDataDTO))
            .isInstanceOf(BufferJsonDataAlreadyExistsException.class)
            .hasMessageContaining("already exists");

        verify(jsonDataValidator).validate(jsonDataDTO);
        verify(bufferServiceClient).getBuffer(VALID_TOKEN, BUFFER_UUID);
        verify(jsonDataRepository, never()).add(any());
    }

    @Test
    @DisplayName("Get JSON data - Positive")
    void shouldGetJsonDataWhenValidRequest() {
        // Arrange
        BufferJsonDataDALM jsonDataDALM = createValidBufferJsonDataDALM();
        BufferJsonDataBLM expectedBLM = createValidBufferJsonDataBLM();
        BufferBLM buffer = createValidBufferBLM();

        when(jsonDataRepository.findByUid(DATA_UUID)).thenReturn(jsonDataDALM);
        when(jsonDataConverter.toBLM(jsonDataDALM)).thenReturn(expectedBLM);
        when(bufferServiceClient.getBuffer(VALID_TOKEN, BUFFER_UUID)).thenReturn(buffer);

        // Act
        BufferJsonDataBLM result = bufferJsonDataService.getJsonData(VALID_TOKEN, DATA_UUID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(DATA_UUID);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(jsonDataRepository).findByUid(DATA_UUID);
        verify(bufferServiceClient).getBuffer(VALID_TOKEN, BUFFER_UUID);
    }

    @Test
    @DisplayName("Get JSON data by buffer - Positive")
    void shouldGetJsonDataByBufferWhenValidRequest() {
        // Arrange
        BufferJsonDataDALM jsonDataDALM = createValidBufferJsonDataDALM();
        BufferJsonDataBLM expectedBLM = createValidBufferJsonDataBLM();
        BufferBLM buffer = createValidBufferBLM();
        List<BufferJsonDataDALM> dataList = Collections.singletonList(jsonDataDALM);

        when(jsonDataRepository.findByBufferUid(BUFFER_UUID)).thenReturn(dataList);
        when(jsonDataConverter.toBLM(jsonDataDALM)).thenReturn(expectedBLM);
        when(bufferServiceClient.getBuffer(VALID_TOKEN, BUFFER_UUID)).thenReturn(buffer);

        // Act
        List<BufferJsonDataBLM> result = bufferJsonDataService.getJsonDataByBuffer(VALID_TOKEN, BUFFER_UUID);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getUid()).isEqualTo(DATA_UUID);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(jsonDataRepository).findByBufferUid(BUFFER_UUID);
        verify(bufferServiceClient).getBuffer(VALID_TOKEN, BUFFER_UUID);
    }

    @Test
    @DisplayName("Get newest JSON data by buffer - Positive")
    void shouldGetNewestJsonDataByBufferWhenValidRequest() {
        // Arrange
        BufferJsonDataDALM jsonDataDALM = createValidBufferJsonDataDALM();
        BufferJsonDataBLM expectedBLM = createValidBufferJsonDataBLM();
        BufferBLM buffer = createValidBufferBLM();

        when(jsonDataRepository.findNewestByBufferUid(BUFFER_UUID)).thenReturn(jsonDataDALM);
        when(jsonDataConverter.toBLM(jsonDataDALM)).thenReturn(expectedBLM);
        when(bufferServiceClient.getBuffer(VALID_TOKEN, BUFFER_UUID)).thenReturn(buffer);

        // Act
        BufferJsonDataBLM result = bufferJsonDataService.getNewestJsonDataByBuffer(VALID_TOKEN, BUFFER_UUID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(DATA_UUID);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(jsonDataRepository).findNewestByBufferUid(BUFFER_UUID);
        verify(bufferServiceClient).getBuffer(VALID_TOKEN, BUFFER_UUID);
    }

    @Test
    @DisplayName("Delete JSON data - Positive")
    void shouldDeleteJsonDataWhenValidRequest() {
        // Arrange
        BufferJsonDataDALM existingData = createValidBufferJsonDataDALM();
        BufferBLM buffer = createValidBufferBLM();

        when(jsonDataRepository.findByUid(DATA_UUID)).thenReturn(existingData);
        when(bufferServiceClient.getBuffer(VALID_TOKEN, BUFFER_UUID)).thenReturn(buffer);

        // Act
        bufferJsonDataService.deleteJsonData(VALID_TOKEN, DATA_UUID);

        // Assert
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(jsonDataRepository).delete(DATA_UUID);
        verify(bufferServiceClient).getBuffer(VALID_TOKEN, BUFFER_UUID);
    }

    @Test
    @DisplayName("Count JSON data by buffer - Positive")
    void shouldCountJsonDataByBufferWhenValidRequest() {
        // Arrange
        BufferBLM buffer = createValidBufferBLM();

        when(jsonDataRepository.countByBufferUid(BUFFER_UUID)).thenReturn(5);
        when(bufferServiceClient.getBuffer(VALID_TOKEN, BUFFER_UUID)).thenReturn(buffer);

        // Act
        int result = bufferJsonDataService.countJsonDataByBuffer(VALID_TOKEN, BUFFER_UUID);

        // Assert
        assertThat(result).isEqualTo(5);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(jsonDataRepository).countByBufferUid(BUFFER_UUID);
        verify(bufferServiceClient).getBuffer(VALID_TOKEN, BUFFER_UUID);
    }

    @Test
    @DisplayName("JSON data exists - Positive")
    void shouldReturnTrueWhenJsonDataExists() {
        // Arrange
        when(jsonDataRepository.exists(DATA_UUID)).thenReturn(true);

        // Act
        boolean result = bufferJsonDataService.jsonDataExists(VALID_TOKEN, DATA_UUID);

        // Assert
        assertThat(result).isTrue();
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(jsonDataRepository).exists(DATA_UUID);
    }

    @Test
    @DisplayName("Health check - Positive")
    void shouldReturnHealthStatus() {
        // Arrange
        Map<String, Object> authHealth = Map.of("status", "OK");
        Map<String, Object> bufferHealth = Map.of("status", "OK");

        when(authServiceClient.healthCheck()).thenReturn(authHealth);
        when(bufferServiceClient.healthCheck()).thenReturn(bufferHealth);

        // Act
        Map<String, Object> result = bufferJsonDataService.getHealthStatus();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("OK");
        assertThat(result.get("service")).isEqualTo("buffer-json-data-service");
        assertThat(result.get("auth-service")).isEqualTo(authHealth);
        assertThat(result.get("buffer-service")).isEqualTo(bufferHealth);
        verify(authServiceClient).healthCheck();
        verify(bufferServiceClient).healthCheck();
    }

    @Test
    @DisplayName("Health check - Negative: Auth service down")
    void shouldHandleAuthServiceDownInHealthCheck() {
        // Arrange
        Map<String, Object> bufferHealth = Map.of("status", "OK");

        when(authServiceClient.healthCheck())
            .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR));
        when(bufferServiceClient.healthCheck()).thenReturn(bufferHealth);

        // Act
        Map<String, Object> result = bufferJsonDataService.getHealthStatus();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("status")).isEqualTo("OK");
        assertThat(result.get("service")).isEqualTo("buffer-json-data-service");
        assertThat(result.get("auth-service")).isInstanceOf(ResponseStatusException.class);
        assertThat(result.get("buffer-service")).isEqualTo(bufferHealth);
        verify(authServiceClient).healthCheck();
        verify(bufferServiceClient).healthCheck();
    }

    @Test
    @DisplayName("Get JSON data by buffer and date range - Positive")
    void shouldGetJsonDataByBufferAndDateRangeWhenValidRequest() {
        // Arrange
        Instant startDate = Instant.now().minusSeconds(3600);
        Instant endDate = Instant.now();
        BufferJsonDataDALM jsonDataDALM = createValidBufferJsonDataDALM();
        BufferJsonDataBLM expectedBLM = createValidBufferJsonDataBLM();
        BufferBLM buffer = createValidBufferBLM();
        List<BufferJsonDataDALM> dataList = Collections.singletonList(jsonDataDALM);

        when(jsonDataRepository.findByBufferUidAndCreatedBetween(BUFFER_UUID, startDate, endDate)).thenReturn(dataList);
        when(jsonDataConverter.toBLM(jsonDataDALM)).thenReturn(expectedBLM);
        when(bufferServiceClient.getBuffer(VALID_TOKEN, BUFFER_UUID)).thenReturn(buffer);

        // Act
        List<BufferJsonDataBLM> result = bufferJsonDataService.getJsonDataByBufferAndCreatedBetween(
            VALID_TOKEN, BUFFER_UUID, startDate, endDate);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getUid()).isEqualTo(DATA_UUID);
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(jsonDataRepository).findByBufferUidAndCreatedBetween(BUFFER_UUID, startDate, endDate);
        verify(bufferServiceClient).getBuffer(VALID_TOKEN, BUFFER_UUID);
    }

    @Test
    @DisplayName("Delete old JSON data - Positive")
    void shouldDeleteOldJsonDataWhenValidRequest() {
        // Arrange
        Instant olderThan = Instant.now().minusSeconds(86400); // 24 hours ago

        // Act
        bufferJsonDataService.deleteOldJsonData(VALID_TOKEN, olderThan);

        // Assert
        verify(authServiceClient).validateAccessToken(VALID_TOKEN);
        verify(jsonDataRepository).deleteOldData(olderThan);
    }
}