package com.connection.device.token.repository;

import static com.connection.device.token.mother.DeviceTokenObjectMother.createValidDeviceAccessTokenBLM;
import static com.connection.device.token.mother.DeviceTokenObjectMother.createValidDeviceAccessTokenDALM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.connection.device.token.converter.DeviceAccessTokenConverter;
import com.connection.device.token.exception.DeviceAccessTokenExistsException;
import com.connection.device.token.exception.DeviceAccessTokenNotFoundException;
import com.connection.device.token.generator.DeviceAccessTokenGenerator;
import com.connection.device.token.model.DeviceAccessTokenBLM;
import com.connection.device.token.model.DeviceAccessTokenDALM;
import com.connection.device.token.repository.DeviceAccessTokenRepositorySQLImpl;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Device Access Token Repository Tests")
class DeviceAccessTokenRepositorySQLImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock 
    private DeviceAccessTokenGenerator generator;

    @Mock
    private DeviceAccessTokenConverter converter;

    @InjectMocks
    private DeviceAccessTokenRepositorySQLImpl repository;

    private DeviceAccessTokenBLM testTokenBLM;
    private DeviceAccessTokenDALM testTokenDALM;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testTokenBLM = createValidDeviceAccessTokenBLM();
        testTokenDALM = createValidDeviceAccessTokenDALM();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Add device access token - Positive")
    void testAddDeviceAccessToken_Positive() {
        // Мокируем конвертацию
        when(converter.toDALM(testTokenBLM)).thenReturn(testTokenDALM);
        
        // Мокируем проверку существования device_token - возвращаем true
        when(jdbcTemplate.queryForObject(
            eq("SELECT COUNT(*) FROM access.device_token WHERE uid = :device_token_uid"), 
            any(MapSqlParameterSource.class), 
            eq(Integer.class)
        )).thenReturn(1);
        
        // Мокируем проверку активного токена - возвращаем false
        when(jdbcTemplate.queryForObject(
            eq("SELECT COUNT(*) FROM access.device_access_token WHERE device_token_uid = :device_token_uid AND expires_at > NOW()"), 
            any(MapSqlParameterSource.class), 
            eq(Integer.class)
        )).thenReturn(0);
        
        // Мокируем проверки существования токена и uid - возвращаем исключения (не найдено)
        when(jdbcTemplate.queryForObject(
            anyString(), 
            any(MapSqlParameterSource.class), 
            any(RowMapper.class)
        )).thenThrow(new EmptyResultDataAccessException(1));
        
        // Мокируем успешное выполнение INSERT
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.add(testTokenBLM);

        // Проверяем, что была вызвана конвертация
        
        // Проверяем, что были вызваны все необходимые проверки
        verify(jdbcTemplate, times(1)).queryForObject(
            eq("SELECT COUNT(*) FROM access.device_token WHERE uid = :device_token_uid"), 
            any(MapSqlParameterSource.class), 
            eq(Integer.class)
        );
        
        // Проверяем выполнение INSERT
        verify(jdbcTemplate, times(1)).update(anyString(), any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Add existing device access token - Negative")
    void testAddExistingDeviceAccessToken_Negative() {
        // Мокируем конвертацию
        when(converter.toDALM(testTokenBLM)).thenReturn(testTokenDALM);
        
        // Мокируем проверку существования device_token - возвращаем true
        when(jdbcTemplate.queryForObject(
            eq("SELECT COUNT(*) FROM access.device_token WHERE uid = :device_token_uid"), 
            any(MapSqlParameterSource.class), 
            eq(Integer.class)
        )).thenReturn(1);
        
        // Мокируем проверку активного токена - возвращаем true (уже существует)
        when(jdbcTemplate.queryForObject(
            eq("SELECT COUNT(*) FROM access.device_access_token WHERE device_token_uid = :device_token_uid AND expires_at > NOW()"), 
            any(MapSqlParameterSource.class), 
            eq(Integer.class)
        )).thenReturn(1);

        assertThatThrownBy(() -> repository.add(testTokenBLM))
                .isInstanceOf(DeviceAccessTokenExistsException.class);

        verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find device access token by UID - Positive")
    void testFindByUid_Positive() {
        UUID uid = UUID.randomUUID();
        
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testTokenDALM);
        when(converter.toBLM(testTokenDALM)).thenReturn(testTokenBLM);
        when(generator.getDeviceAccessTokenBLM(any())).thenReturn(testTokenBLM);

        DeviceAccessTokenBLM result = repository.findByUid(uid);

        assertThat(result).isEqualTo(testTokenBLM);
        // verify(converter, times(1)).toBLM(testTokenDALM);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find non-existent device access token by UID - Negative")
    void testFindByUid_Negative() {
        UUID uid = UUID.randomUUID();
        
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> repository.findByUid(uid))
                .isInstanceOf(DeviceAccessTokenNotFoundException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find device access token by token - Positive")
    void testFindByToken_Positive() {
        String token = "test.token.string";
        
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testTokenDALM);
        when(converter.toBLM(testTokenDALM)).thenReturn(testTokenBLM);
        when(generator.getDeviceAccessTokenBLM(any())).thenReturn(testTokenBLM);


        DeviceAccessTokenBLM result = repository.findByToken(token);

        assertThat(result).isEqualTo(testTokenBLM);
        // verify(converter, times(1)).toBLM(testTokenDALM);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find device access token by device token UID - Positive")
    void testFindByDeviceTokenUid_Positive() {
        UUID deviceTokenUid = UUID.randomUUID();
        
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testTokenDALM);
        when(converter.toBLM(testTokenDALM)).thenReturn(testTokenBLM);
        when(generator.getDeviceAccessTokenBLM(any())).thenReturn(testTokenBLM);


        DeviceAccessTokenBLM result = repository.findByDeviceTokenUid(deviceTokenUid);

        assertThat(result).isEqualTo(testTokenBLM);
        // verify(converter, times(1)).toBLM(testTokenDALM);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Revoke device access token - Positive")
    void testRevoke_Positive() {
        UUID uid = UUID.randomUUID();
        
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testTokenDALM);
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.revoke(uid);

        verify(jdbcTemplate, times(1)).update(
                eq("DELETE FROM access.device_access_token WHERE uid = :uid"),
                any(MapSqlParameterSource.class));
    }

    @Test
    @DisplayName("Check has device access token - Positive")
    void testHasDeviceAccessToken_Positive() {
        UUID deviceTokenUid = UUID.randomUUID();
        
        when(jdbcTemplate.queryForObject(
            eq("SELECT COUNT(*) FROM access.device_access_token WHERE device_token_uid = :device_token_uid AND expires_at > NOW()"), 
            any(MapSqlParameterSource.class), 
            eq(Integer.class)
        )).thenReturn(1);

        boolean result = repository.hasDeviceAccessToken(deviceTokenUid);

        assertThat(result).isTrue();
    }
}