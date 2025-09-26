package com.connection.device.token.repository;

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

import com.connection.device.token.exception.DeviceAccessTokenExistsException;
import com.connection.device.token.model.DeviceAccessTokenDALM;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Device Access Token Repository Tests")
class DeviceAccessTokenRepositorySQLImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @InjectMocks
    private DeviceAccessTokenRepositorySQLImpl repository;

    private DeviceAccessTokenDALM testToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testToken = createValidDeviceAccessTokenDALM();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Add device access token - Positive")
    void testAddDeviceAccessToken_Positive() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.add(testToken);

        verify(jdbcTemplate, times(2)).queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class));
        verify(jdbcTemplate, times(1)).update(anyString(), any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Add device access token with existing device token UID - Negative")
    void testAddDeviceAccessTokenWithExistingDeviceTokenUid_Negative() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testToken);

        assertThatThrownBy(() -> repository.add(testToken))
                .isInstanceOf(DeviceAccessTokenExistsException.class);

        verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find device access token by token - Positive")
    void testFindByToken_Positive() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testToken);

        DeviceAccessTokenDALM result = repository.findByToken(testToken.getToken());

        assertThat(result).isEqualTo(testToken);
    }

    @Test
    @DisplayName("Revoke by device token UID - Positive")
    void testRevokeByDeviceTokenUid_Positive() {
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.revokeByDeviceTokenUid(testToken.getDeviceTokenUid());

        verify(jdbcTemplate, times(1)).update(
                eq("DELETE FROM access.device_access_token WHERE device_token_uid = :device_token_uid"),
                any(MapSqlParameterSource.class));
    }
}