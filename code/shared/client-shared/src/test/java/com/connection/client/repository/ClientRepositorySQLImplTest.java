package com.connection.client.repository;

import static com.connection.client.mother.ClientObjectMother.createValidClientBLM;
import static com.connection.client.mother.ClientObjectMother.createValidClientDALM;
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

import com.connection.client.exception.ClientAlreadyExisistsException;
import com.connection.client.exception.ClientNotFoundException;
import com.connection.client.model.ClientBLM;
import com.connection.client.model.ClientDALM;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Client Repository Tests - SQL implementation tests")
class ClientRepositorySQLImplTest {

        @Mock
        private NamedParameterJdbcTemplate jdbcTemplate;

        @InjectMocks
        private ClientRepositorySQLImpl repository;

        private ClientDALM testClientDALM;
        private ClientBLM testClientBLM;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                testClientDALM = createValidClientDALM();
                testClientBLM = createValidClientBLM();
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Add client - Positive")
        void testAddClient_Positive() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenThrow(new EmptyResultDataAccessException(1));
                when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

                repository.add(testClientBLM);

                verify(jdbcTemplate, times(2)).queryForObject(anyString(), any(MapSqlParameterSource.class),
                                any(RowMapper.class));
                verify(jdbcTemplate, times(1)).update(anyString(), any(MapSqlParameterSource.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Add client with existing email - Negative")
        void testAddClientWithExistingEmail_Negative() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testClientDALM);

                assertThatThrownBy(() -> repository.add(testClientBLM))
                                .isInstanceOf(ClientAlreadyExisistsException.class);

                verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Find client by UID - Positive")
        void testFindByUid_Positive() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testClientDALM);

                ClientBLM result = repository.findByUid(testClientDALM.getUid());

                assertThat(result).isEqualTo(testClientBLM);
                verify(jdbcTemplate, times(1)).queryForObject(
                                eq("SELECT uid, email, birth_date, username, password FROM core.client WHERE uid = :uid"),
                                any(MapSqlParameterSource.class),
                                any(RowMapper.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Find non-existent client by UID - Negative")
        void testFindByUid_Negative() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenThrow(new EmptyResultDataAccessException(1));

                assertThatThrownBy(() -> repository.findByUid(testClientDALM.getUid()))
                                .isInstanceOf(ClientNotFoundException.class);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Find client by email - Positive")
        void testFindByEmail_Positive() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testClientDALM);

                ClientBLM result = repository.findByEmail(testClientBLM.getEmail());

                assertThat(result).isEqualTo(testClientBLM);
                verify(jdbcTemplate, times(1)).queryForObject(
                                eq("SELECT uid, email, birth_date, username, password FROM core.client WHERE email = :email"),
                                any(MapSqlParameterSource.class),
                                any(RowMapper.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Find client by username - Positive")
        void testFindByUsername_Positive() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testClientDALM);

                ClientBLM result = repository.findByUsername(testClientBLM.getUsername());

                assertThat(result).isEqualTo(testClientBLM);
                verify(jdbcTemplate, times(1)).queryForObject(
                                eq("SELECT uid, email, birth_date, username, password FROM core.client WHERE username = :username"),
                                any(MapSqlParameterSource.class),
                                any(RowMapper.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Find client by email and password - Positive")
        void testFindByEmailPassword_Positive() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testClientDALM);

                ClientBLM result = repository.findByEmailPassword(testClientBLM.getEmail(), testClientBLM.getPassword());

                assertThat(result).isEqualTo(testClientBLM);
                verify(jdbcTemplate, times(1)).queryForObject(
                                eq("SELECT uid, email, birth_date, username, password FROM core.client WHERE email = :email AND password = :password"),
                                any(MapSqlParameterSource.class),
                                any(RowMapper.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Delete client by UID - Positive")
        void testDeleteByUid_Positive() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenReturn(testClientDALM);
                when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

                repository.deleteByUid(testClientBLM.getUid());

                verify(jdbcTemplate, times(1)).update(
                                eq("DELETE FROM core.client WHERE uid = :uid"),
                                any(MapSqlParameterSource.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Delete non-existent client by UID - Negative")
        void testDeleteByUid_Negative() {
                when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                                .thenThrow(new EmptyResultDataAccessException(1));

                assertThatThrownBy(() -> repository.deleteByUid(testClientBLM.getUid()))
                                .isInstanceOf(ClientNotFoundException.class);

                verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
        }
}