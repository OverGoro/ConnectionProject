package com.connection.device;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.connection.auth.events.AuthEventConstants;
import com.connection.auth.events.commands.ExtractClientUidCommand;
import com.connection.auth.events.commands.HealthCheckCommand;
import com.connection.auth.events.commands.ValidateTokenCommand;
import com.connection.auth.events.responses.ClientUidResponse;
import com.connection.auth.events.responses.HealthCheckResponse;
import com.connection.auth.events.responses.TokenValidationResponse;
import com.connection.device.kafka.TypedAuthKafkaClient;

@ExtendWith(MockitoExtension.class)
@DisplayName("Typed Auth Kafka Client Tests")
class TypedAuthKafkaClientTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Captor
    private ArgumentCaptor<Object> commandCaptor;

    @Captor
    private ArgumentCaptor<String> topicCaptor;

    @Captor
    private ArgumentCaptor<String> keyCaptor;

    private TypedAuthKafkaClient authKafkaClient;

    @BeforeEach
    void setUp() {
        authKafkaClient = new TypedAuthKafkaClient(kafkaTemplate);
    }

    @Test
    @DisplayName("Validate token - sends correct command")
    void shouldSendValidateTokenCommand() {
        // Arrange
        String token = "test-token";
        String sourceService = "device-service";
        
        when(kafkaTemplate.send(anyString(), anyString(), any(ValidateTokenCommand.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        CompletableFuture<TokenValidationResponse> future = 
            authKafkaClient.validateToken(token, sourceService);

        // Assert
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), commandCaptor.capture());
        
        assertThat(topicCaptor.getValue()).isEqualTo(AuthEventConstants.AUTH_COMMANDS_TOPIC);
        assertThat(keyCaptor.getValue()).isNotNull();
        
        ValidateTokenCommand command = (ValidateTokenCommand) commandCaptor.getValue();
        assertThat(command.getToken()).isEqualTo(token);
        assertThat(command.getSourceService()).isEqualTo(sourceService);
        assertThat(command.getReplyTopic()).isEqualTo(AuthEventConstants.AUTH_RESPONSES_TOPIC);
        assertThat(command.getTokenType()).isEqualTo(ValidateTokenCommand.TokenType.ACCESS);
    }

    @Test
    @DisplayName("Get client UID - sends correct command")
    void shouldSendExtractClientUidCommand() {
        // Arrange
        String token = "test-token";
        String sourceService = "device-service";
        
        when(kafkaTemplate.send(anyString(), anyString(), any(ExtractClientUidCommand.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        CompletableFuture<ClientUidResponse> future = 
            authKafkaClient.getClientUid(token, sourceService);

        // Assert
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), commandCaptor.capture());
        
        assertThat(topicCaptor.getValue()).isEqualTo(AuthEventConstants.AUTH_COMMANDS_TOPIC);
        
        ExtractClientUidCommand command = (ExtractClientUidCommand) commandCaptor.getValue();
        assertThat(command.getToken()).isEqualTo(token);
        assertThat(command.getSourceService()).isEqualTo(sourceService);
        assertThat(command.getReplyTopic()).isEqualTo(AuthEventConstants.AUTH_RESPONSES_TOPIC);
    }

    @Test
    @DisplayName("Health check - sends correct command")
    void shouldSendHealthCheckCommand() {
        // Arrange
        String sourceService = "device-service";
        
        when(kafkaTemplate.send(anyString(), anyString(), any(HealthCheckCommand.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        CompletableFuture<HealthCheckResponse> future = 
            authKafkaClient.healthCheck(sourceService);

        // Assert
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), commandCaptor.capture());
        
        assertThat(topicCaptor.getValue()).isEqualTo(AuthEventConstants.AUTH_COMMANDS_TOPIC);
        
        HealthCheckCommand command = (HealthCheckCommand) commandCaptor.getValue();
        assertThat(command.getSourceService()).isEqualTo(sourceService);
        assertThat(command.getReplyTopic()).isEqualTo(AuthEventConstants.AUTH_RESPONSES_TOPIC);
    }

    @Test
    @DisplayName("Handle response - completes future for matching correlation ID")
    void shouldCompleteFutureWhenResponseReceived() throws Exception {
        // Arrange
        String correlationId = "test-correlation-id";
        String token = "test-token";
        String sourceService = "device-service";
        
        when(kafkaTemplate.send(anyString(), anyString(), any(ValidateTokenCommand.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<TokenValidationResponse> future = 
            authKafkaClient.validateToken(token, sourceService);
        
        // Extract correlation ID from sent command
        verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), any());
        String actualCorrelationId = keyCaptor.getValue();

        TokenValidationResponse response = TokenValidationResponse.valid(
            actualCorrelationId, java.util.UUID.randomUUID(), "ACCESS");

        // Act
        authKafkaClient.handleResponse(actualCorrelationId, response);

        // Assert
        TokenValidationResponse result = future.get(1, TimeUnit.SECONDS);
        assertThat(result).isEqualTo(response);
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("Handle response - ignores unknown correlation ID")
    void shouldIgnoreResponseForUnknownCorrelationId() {
        // Arrange
        String unknownCorrelationId = "unknown-correlation-id";
        TokenValidationResponse response = TokenValidationResponse.valid(
            unknownCorrelationId, java.util.UUID.randomUUID(), "ACCESS");

        // Act
        authKafkaClient.handleResponse(unknownCorrelationId, response);

        // Assert - No exception should be thrown
        assertThat(true).isTrue(); // Just to have an assertion
    }

    @Test
    @DisplayName("Handle response - completes exceptionally for type mismatch")
    void shouldCompleteExceptionallyForTypeMismatch() {
        // Arrange
        String correlationId = "test-correlation-id";
        String token = "test-token";
        String sourceService = "device-service";
        
        when(kafkaTemplate.send(anyString(), anyString(), any(ValidateTokenCommand.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<TokenValidationResponse> future = 
            authKafkaClient.validateToken(token, sourceService);
        
        verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), any());
        String actualCorrelationId = keyCaptor.getValue();

        // Wrong response type
        ClientUidResponse wrongResponse = ClientUidResponse.success(
            actualCorrelationId, java.util.UUID.randomUUID(), "ACCESS");

        // Act
        authKafkaClient.handleResponse(actualCorrelationId, wrongResponse);

        // Assert
        assertThatThrownBy(() -> future.get(1, TimeUnit.SECONDS))
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(ClassCastException.class);
    }

    @Test
    @DisplayName("Send request - handles Kafka send failure")
    void shouldHandleKafkaSendFailure() {
        // Arrange
        String token = "test-token";
        String sourceService = "device-service";
        
        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka error"));
        
        when(kafkaTemplate.send(anyString(), anyString(), any(ValidateTokenCommand.class)))
            .thenReturn(failedFuture);

        // Act
        CompletableFuture<TokenValidationResponse> future = 
            authKafkaClient.validateToken(token, sourceService);

        // Assert
        assertThatThrownBy(() -> future.get(1, TimeUnit.SECONDS))
            .isInstanceOf(ExecutionException.class)
            .hasMessageContaining("Kafka error");
    }
}