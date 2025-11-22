package com.connection.token.repository;

import static com.connection.token.mother.TokenObjectMother.createValidRefreshTokenDalm;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.connection.token.exception.RefreshTokenAlreadyExisistsException;
import com.connection.token.exception.RefreshTokenNotFoundException;
import com.connection.token.model.RefreshTokenDalm;
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

@TestMethodOrder(MethodOrderer.Random.class)
@DisplayName("Refresh Token Repository Tests - Sql implementation tests")
class RefreshTokenRepositorySqlImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @InjectMocks
    private RefreshTokenRepositorySqlImpl repository;

    private RefreshTokenDalm testToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testToken = createValidRefreshTokenDalm();
    }

    @SuppressWarnings("unchecked")
@Test
    @DisplayName("Add refresh token - Positive")
    void testAddRefreshToken_Positive() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.add(testToken);

        verify(jdbcTemplate, times(2)).queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class));
        verify(jdbcTemplate, times(1)).update(anyString(), any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
@Test
    @DisplayName("Add existing refresh token - Negative")
    void testAddExistingRefreshToken_Negative() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testToken);

        assertThatThrownBy(() -> repository.add(testToken))
                .isInstanceOf(RefreshTokenAlreadyExisistsException.class);

        verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
@Test
    @DisplayName("Revoke refresh token - Positive")
    void testRevokeRefreshToken_Positive() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testToken);
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.revoke(testToken);

        verify(jdbcTemplate, times(1)).update(
                eq("DELETE FROM \"access\".refresh_token WHERE uid = :uid"),
                any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
@Test
    @DisplayName("Revoke non-existent refresh token - Negative")
    void testRevokeNonExistentRefreshToken_Negative() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> repository.revoke(testToken))
                .isInstanceOf(RefreshTokenNotFoundException.class);

        verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    @DisplayName("Revoke all client tokens - Positive")
    void testRevokeAllClientTokens_Positive() {
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.revokeAll(testToken.getClientUid());

        verify(jdbcTemplate, times(1)).update(
                eq("DELETE FROM \"access\".refresh_token WHERE client_id = :client_id"),
                any(MapSqlParameterSource.class));
    }

    @Test
    @DisplayName("Cleanup expired tokens - Positive")
    void testCleanupExpiredTokens_Positive() {
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.cleanUpExpired();

        verify(jdbcTemplate, times(1)).update(
                eq("DELETE FROM \"access\".refresh_token WHERE expires_at < NOW()"),
                any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
@Test
    @DisplayName("Check token exists - Positive")
    void testTokenExists_Positive() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testToken);

        boolean result = repository.tokenExists(testToken.getToken());

        assertThat(result).isTrue();
    }

    @SuppressWarnings("unchecked")
@Test
    @DisplayName("Check token exists - Negative")
    void testTokenExists_Negative() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        boolean result = repository.tokenExists(testToken.getToken());

        assertThat(result).isFalse();
    }
}