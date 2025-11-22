package com.connection.device.token.repository;

import static com.connection.device.token.mother.DeviceTokenObjectMother.createValidDeviceTokenBlm;
import static com.connection.device.token.mother.DeviceTokenObjectMother.createValidDeviceTokenDalm;
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

import javax.crypto.SecretKey;

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

import com.connection.device.token.converter.DeviceTokenConverter;
import com.connection.device.token.exception.DeviceTokenAlreadyExistsException;
import com.connection.device.token.exception.DeviceTokenNotFoundException;
import com.connection.device.token.generator.DeviceTokenGenerator;
import com.connection.device.token.model.DeviceTokenBlm;
import com.connection.device.token.model.DeviceTokenDalm;

import io.jsonwebtoken.security.Keys;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Device Token Repository Tests")
class DeviceTokenRepositorySqlImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private DeviceTokenConverter converter;

    @Mock
    private DeviceTokenGenerator generator;

    @InjectMocks
    private DeviceTokenRepositorySqlImpl repository;

    private DeviceTokenBlm testTokenBlm;
    private DeviceTokenDalm testTokenDalm;
    
    private static final String SELECT_DEVICE_TOKEN = "SELECT uid, device_uid, token, created_at, expires_at";
    private static final String FROM_DEVICE_TOKEN = " FROM access.device_token";

    private static final String SELECT_TOKEN_BY_UID = SELECT_DEVICE_TOKEN + FROM_DEVICE_TOKEN + " WHERE uid = :uid";
    private static final String SELECT_TOKEN_BY_TOKEN = SELECT_DEVICE_TOKEN + FROM_DEVICE_TOKEN + " WHERE token = :token";
    private static final String SELECT_TOKEN_BY_DEVICE_UID = SELECT_DEVICE_TOKEN + FROM_DEVICE_TOKEN + " WHERE device_uid = :device_uid";

    private static final String INSERT_DEVICE_TOKEN = "INSERT INTO access.device_token (uid, device_uid, token, created_at, expires_at) " +
            "VALUES (:uid, :device_uid, :token, :created_at, :expires_at)";

    private static final String UPDATE_TOKEN = "UPDATE access.device_token SET token = :token, expires_at = :expires_at " +
            "WHERE uid = :uid";

    private static final String REVOKE_TOKEN = "DELETE FROM access.device_token WHERE uid = :uid";
    private static final String REVOKE_BY_DEVICE_UID = "DELETE FROM access.device_token WHERE device_uid = :device_uid";
    private static final String CLEANUP_EXPIRED_TOKENS = "DELETE FROM access.device_token WHERE expires_at < NOW()";
    private static final String EXISTS_BY_DEVICE_UID = "SELECT COUNT(*) FROM access.device_token WHERE device_uid = :device_uid AND expires_at > NOW()";
    private static final String DEVICE_EXISTS = "SELECT COUNT(*) FROM core.device WHERE uid = :device_uid";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testTokenBlm = createValidDeviceTokenBlm();
        testTokenDalm = createValidDeviceTokenDalm();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Add device token - Positive")
    void testAddDeviceToken_Positive() {
        // Мокируем конвертацию
        when(converter.toDalm(testTokenBlm)).thenReturn(testTokenDalm);
        
        // Мокируем проверку существования устройства - возвращаем true
        when(jdbcTemplate.queryForObject(
            eq("SELECT COUNT(*) FROM core.device WHERE uid = :device_uid"), 
            any(MapSqlParameterSource.class), 
            eq(Integer.class)
        )).thenReturn(1);
        
        // Мокируем проверки существования токена и uid - возвращаем исключения (не найдено)
        when(jdbcTemplate.queryForObject(
            anyString(), 
            any(MapSqlParameterSource.class), 
            any(RowMapper.class)
        )).thenThrow(new EmptyResultDataAccessException(1));
        
        // Мокируем успешное выполнение INSERT
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.add(testTokenBlm);

        // Проверяем, что были вызваны все необходимые проверки
        verify(jdbcTemplate, times(1)).queryForObject(
            eq("SELECT COUNT(*) FROM core.device WHERE uid = :device_uid"), 
            any(MapSqlParameterSource.class), 
            eq(Integer.class)
        );
        
        // Проверяем выполнение INSERT
        verify(jdbcTemplate, times(1)).update(anyString(), any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Add existing device token - Negative")
    void testAddExistingDeviceToken_Negative() {
        // Мокируем конвертацию
        when(converter.toDalm(testTokenBlm)).thenReturn(testTokenDalm);
        
        // Сначала имитируем, что device существует
        when(jdbcTemplate.queryForObject(
            eq("SELECT COUNT(*) FROM core.device WHERE uid = :device_uid"), 
            any(MapSqlParameterSource.class), 
            eq(Integer.class)
        )).thenReturn(1);
        
        // Затем имитируем, что токен уже существует (по uid)
        when(jdbcTemplate.queryForObject(
            eq(SELECT_TOKEN_BY_UID), 
            any(MapSqlParameterSource.class), 
            any(RowMapper.class)
        )).thenReturn(testTokenDalm);

        assertThatThrownBy(() -> repository.add(testTokenBlm))
                .isInstanceOf(DeviceTokenAlreadyExistsException.class);

        verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
    }

    // @SuppressWarnings("unchecked")
    // @Test
    // @DisplayName("Find device token by UID - Positive")
    // void testFindByUid_Positive() {
    //     UUID uid = UUID.randomUUID();
        
    //     when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
    //             .thenReturn(testTokenDalm);
    //     when(converter.toBlm(testTokenDalm)).thenReturn(testTokenBlm);

    //     DeviceTokenBlm result = repository.findByUid(uid);

    //     assertThat(result).isEqualTo(testTokenBlm);
    //     verify(converter, times(1)).toBlm(testTokenDalm);
    // }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find non-existent device token by UID - Negative")
    void testFindByUid_Negative() {
        UUID uid = UUID.randomUUID();
        
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> repository.findByUid(uid))
                .isInstanceOf(DeviceTokenNotFoundException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Revoke device token - Positive")
    void testRevoke_Positive() {
        UUID uid = UUID.randomUUID();
        
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testTokenDalm);
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.revoke(uid);

        verify(jdbcTemplate, times(1)).update(
                eq("DELETE FROM access.device_token WHERE uid = :uid"),
                any(MapSqlParameterSource.class));
    }
}