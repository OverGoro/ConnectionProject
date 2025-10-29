package com.connection.service.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.ActiveProfiles;

import com.connection.client.model.ClientBLM;
import com.connection.client.repository.ClientRepository;
import com.connection.service.auth.AuthService;
import com.connection.service.auth.mother.AuthObjectMother;
import com.connection.token.model.AccessTokenBLM;
import com.connection.token.model.RefreshTokenBLM;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ActiveProfiles("integrationtest")
@DisplayName("Auth Service Integration Tests")
public class AuthServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private ClientRepository clientRepository;

    private ClientBLM testClient;
    private String uniqueEmail;

    @BeforeEach
    void setUp() {
        String timestamp = String.valueOf(System.currentTimeMillis()) + UUID.randomUUID().toString().substring(0, 8);
        uniqueEmail = "integration_test_" + timestamp + "@example.com";
        
        testClient = new ClientBLM(
            UUID.randomUUID(),
            new Date(System.currentTimeMillis() - 25L * 365 * 24 * 60 * 60 * 1000),
            uniqueEmail,
            "SecurePassword123!" + timestamp,
            "integration_user_" + timestamp
        );
        
        log.info("Created test client with email: {}", uniqueEmail);
    }

    @AfterEach
    void cleanup() {
        // Очищаем данные после каждого теста
        if (uniqueEmail != null) {
            cleanupClientData(uniqueEmail);
            log.info("Cleaned up test data for email: {}", uniqueEmail);
        }
    }

    @Test
    @DisplayName("Should register client successfully")
    void shouldRegisterClientSuccessfully() {
        // When
        authService.register(testClient);

        // Then
        ClientBLM foundClient = clientRepository.findByEmail(uniqueEmail);
        assertThat(foundClient).isNotNull();
        assertThat(foundClient.getEmail()).isEqualTo(uniqueEmail);
        assertThat(foundClient.getUsername()).isEqualTo(testClient.getUsername());
        
        log.info("Successfully registered and found client: {}", uniqueEmail);
    }

    @Test
    @DisplayName("Should authorize by email and generate tokens")
    void shouldAuthorizeByEmailAndGenerateTokens() {
        // Given
        authService.register(testClient);

        // When
        Pair<AccessTokenBLM, RefreshTokenBLM> tokens = authService.authorizeByEmail(
            testClient.getEmail(), testClient.getPassword());

        // Then
        assertThat(tokens).isNotNull();
        assertThat(tokens.getFirst()).isNotNull();
        assertThat(tokens.getSecond()).isNotNull();
        
        AccessTokenBLM accessToken = tokens.getFirst();
        RefreshTokenBLM refreshToken = tokens.getSecond();
        
        assertThat(accessToken.getToken()).isNotBlank();
        assertThat(accessToken.getClientUID()).isEqualTo(testClient.getUid());
        assertThat(accessToken.getExpiresAt()).isAfter(new Date());
        
        assertThat(refreshToken.getToken()).isNotBlank();
        assertThat(refreshToken.getClientUID()).isEqualTo(testClient.getUid());
        assertThat(refreshToken.getExpiresAt()).isAfter(new Date());
        
        log.info("Successfully authorized and generated tokens for: {}", uniqueEmail);
    }

    @Test
    @DisplayName("Should refresh tokens successfully")
    void shouldRefreshTokensSuccessfully() {
        // Given
        authService.register(testClient);
        Pair<AccessTokenBLM, RefreshTokenBLM> originalTokens = authService.authorizeByEmail(
            uniqueEmail, testClient.getPassword());
        
        RefreshTokenBLM originalRefreshToken = originalTokens.getSecond();

        sleep(1000);
        // When
        Pair<AccessTokenBLM, RefreshTokenBLM> newTokens = authService.refresh(originalRefreshToken);

        // Then
        assertThat(newTokens).isNotNull();
        assertThat(newTokens.getFirst()).isNotNull();
        assertThat(newTokens.getSecond()).isNotNull();
        
        // Verify new tokens are different from original ones
        assertThat(newTokens.getFirst().getToken()).isNotEqualTo(originalTokens.getFirst().getToken());
        assertThat(newTokens.getSecond().getToken()).isNotEqualTo(originalTokens.getSecond().getToken());
        
        log.info("Successfully refreshed tokens for: {}", uniqueEmail);
    }

    @Test
    @DisplayName("Should validate access token successfully")
    void shouldValidateAccessTokenSuccessfully() {
        // Given
        authService.register(testClient);
        Pair<AccessTokenBLM, RefreshTokenBLM> tokens = authService.authorizeByEmail(
            uniqueEmail, testClient.getPassword());
        
        AccessTokenBLM accessToken = tokens.getFirst();

        // When & Then - Should not throw exception
        authService.validateAccessToken(accessToken);
        
        log.info("Successfully validated access token for: {}", uniqueEmail);
    }

    @Test
    @DisplayName("Should validate refresh token successfully")
    void shouldValidateRefreshTokenSuccessfully() {
        // Given
        authService.register(testClient);
        Pair<AccessTokenBLM, RefreshTokenBLM> tokens = authService.authorizeByEmail(
            uniqueEmail, testClient.getPassword());
        
        RefreshTokenBLM refreshToken = tokens.getSecond();

        // When & Then - Should not throw exception
        authService.validateRefreshToken(refreshToken);
        
        log.info("Successfully validated refresh token for: {}", uniqueEmail);
    }

    @Test
    @DisplayName("Should throw exception when authorizing with invalid credentials")
    void shouldThrowExceptionWhenAuthorizingWithInvalidCredentials() {
        // Given
        authService.register(testClient);

        // When & Then
        assertThatThrownBy(() -> authService.authorizeByEmail(uniqueEmail, "wrong_password"))
            .isInstanceOf(RuntimeException.class);
            
        log.info("Correctly rejected invalid credentials for: {}", uniqueEmail);
    }

    @Test
    @DisplayName("Should throw exception when refreshing with invalid token")
    void shouldThrowExceptionWhenRefreshingWithInvalidToken() {
        // Given
        RefreshTokenBLM invalidRefreshToken = AuthObjectMother.createExpiredRefreshTokenBLM();

        // When & Then
        assertThatThrownBy(() -> authService.refresh(invalidRefreshToken))
            .isInstanceOf(RuntimeException.class);
            
        log.info("Correctly rejected invalid refresh token");
    }
}