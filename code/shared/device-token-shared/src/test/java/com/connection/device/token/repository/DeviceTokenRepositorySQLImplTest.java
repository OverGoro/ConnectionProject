package com.connection.device.token.repository;

import static com.connection.device.token.mother.DeviceTokenObjectMother.createValidDeviceTokenDALM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.connection.device.token.exception.DeviceTokenAlreadyExistsException;
import com.connection.device.token.exception.DeviceTokenNotFoundException;
import com.connection.device.token.model.DeviceTokenDALM;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Device Token Repository Tests")
class DeviceTokenRepositorySQLImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @InjectMocks
    private DeviceTokenRepositorySQLImpl repository;

    private DeviceTokenDALM testToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testToken = createValidDeviceTokenDALM();
    }

    @SuppressWarnings("unchecked")
@Test
    @DisplayName("Add device token - Positive")
    void testAddDeviceToken_Positive() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.add(testToken);

        verify(jdbcTemplate, times(2)).queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class));
        verify(jdbcTemplate, times(1)).update(anyString(), any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
@Test
    @DisplayName("Add existing device token - Negative")
    void testAddExistingDeviceToken_Negative() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testToken);

        assertThatThrownBy(() -> repository.add(testToken))
                .isInstanceOf(DeviceTokenAlreadyExistsException.class);

        verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
@Test
    @DisplayName("Find device token by UID - Positive")
    void testFindByUid_Positive() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testToken);

        DeviceTokenDALM result = repository.findByUid(testToken.getUid());

        assertThat(result).isEqualTo(testToken);
    }

    @SuppressWarnings("unchecked")
@Test
    @DisplayName("Find non-existent device token by UID - Negative")
    void testFindByUid_Negative() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> repository.findByUid(testToken.getUid()))
                .isInstanceOf(DeviceTokenNotFoundException.class);
    }

    @SuppressWarnings("unchecked")
@Test
    @DisplayName("Revoke device token - Positive")
    void testRevoke_Positive() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testToken);
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.revoke(testToken.getUid());

        verify(jdbcTemplate, times(1)).update(
                eq("DELETE FROM access.device_token WHERE uid = :uid"),
                any(MapSqlParameterSource.class));
    }
}