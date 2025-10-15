package com.connection.message.repository;

import static com.connection.message.mother.MessageObjectMother.createValidMessageDALM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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

import com.connection.message.exception.MessageAddException;
import com.connection.message.exception.MessageNotFoundException;
import com.connection.message.model.MessageDALM;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Message Repository Tests - SQL implementation tests")
class MessageRepositorySQLImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @InjectMocks
    private MessageRepositorySQLImpl repository;

    private MessageDALM testMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testMessage = createValidMessageDALM();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Add message - Positive")
    void testAddMessage_Positive() {
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.add(testMessage);

        verify(jdbcTemplate, times(1)).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    @DisplayName("Add message with exception - Negative")
    void testAddMessageWithException_Negative() {
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> repository.add(testMessage))
                .isInstanceOf(MessageAddException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find message by UID - Positive")
    void testFindByUid_Positive() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testMessage);

        MessageDALM result = repository.findByUid(testMessage.getUid());

        assertThat(result).isEqualTo(testMessage);
        verify(jdbcTemplate, times(1)).queryForObject(
                eq("SELECT uid, buffer_uid, content, content_type, created_at FROM processing.message WHERE uid = :uid"),
                any(MapSqlParameterSource.class),
                any(RowMapper.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find non-existent message by UID - Negative")
    void testFindByUid_Negative() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> repository.findByUid(testMessage.getUid()))
                .isInstanceOf(MessageNotFoundException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find messages by buffer UID - Positive")
    void testFindByBufferUid_Positive() {
        List<MessageDALM> messages = Arrays.asList(testMessage);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(messages);

        List<MessageDALM> result = repository.findByBufferUid(testMessage.getBufferUid());

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testMessage);
        verify(jdbcTemplate, times(1)).query(
                eq("SELECT uid, buffer_uid, content, content_type, created_at FROM processing.message WHERE buffer_uid = :buffer_uid ORDER BY created_at DESC"),
                any(MapSqlParameterSource.class),
                any(RowMapper.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find no messages by buffer UID - Negative")
    void testFindByBufferUid_Negative() {
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> repository.findByBufferUid(testMessage.getBufferUid()))
                .isInstanceOf(MessageNotFoundException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find messages by buffer UID and time range - Positive")
    void testFindByBufferUidAndTimeRange_Positive() {
        List<MessageDALM> messages = Arrays.asList(testMessage);
        Date startTime = new Date(System.currentTimeMillis() - 1000 * 60 * 60); // 1 hour ago
        Date endTime = new Date();

        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(messages);

        List<MessageDALM> result = repository.findByBufferUidAndTimeRange(testMessage.getBufferUid(), startTime, endTime);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testMessage);
        verify(jdbcTemplate, times(1)).query(
                eq("SELECT uid, buffer_uid, content, content_type, created_at FROM processing.message WHERE buffer_uid = :buffer_uid AND created_at BETWEEN :start_time AND :end_time ORDER BY created_at DESC"),
                any(MapSqlParameterSource.class),
                any(RowMapper.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Find no messages by buffer UID and time range - Negative")
    void testFindByBufferUidAndTimeRange_Negative() {
        Date startTime = new Date(System.currentTimeMillis() - 1000 * 60 * 60);
        Date endTime = new Date();

        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> repository.findByBufferUidAndTimeRange(testMessage.getBufferUid(), startTime, endTime))
                .isInstanceOf(MessageNotFoundException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Delete message by UID - Positive")
    void testDeleteByUid_Positive() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(testMessage);
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.deleteByUid(testMessage.getUid());

        verify(jdbcTemplate, times(1)).update(
                eq("DELETE FROM processing.message WHERE uid = :uid"),
                any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Delete non-existent message by UID - Negative")
    void testDeleteByUid_Negative() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> repository.deleteByUid(testMessage.getUid()))
                .isInstanceOf(MessageNotFoundException.class);

        verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Delete messages by buffer UID - Positive")
    void testDeleteByBufferUid_Positive() {
        List<MessageDALM> messages = Arrays.asList(testMessage);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(messages);
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);

        repository.deleteByBufferUid(testMessage.getBufferUid());

        verify(jdbcTemplate, times(1)).update(
                eq("DELETE FROM processing.message WHERE buffer_uid = :buffer_uid"),
                any(MapSqlParameterSource.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Delete messages by non-existent buffer UID - Negative")
    void testDeleteByBufferUid_Negative() {
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> repository.deleteByBufferUid(testMessage.getBufferUid()))
                .isInstanceOf(MessageNotFoundException.class);

        verify(jdbcTemplate, never()).update(anyString(), any(MapSqlParameterSource.class));
    }
}