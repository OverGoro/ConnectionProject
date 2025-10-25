package com.service.auth.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.service.auth.mother.AuthObjectMother;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisplayName("Auth Service API E2E Tests")
public class AuthApiE2ETest extends BaseE2ETest {

    private TestRestTemplate restTemplate = new TestRestTemplate();
    private String testEmail;

    @BeforeEach
    void setup() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        System.setProperty("com.atomikos.icatch.log_base_name", "test-tm-" + uniqueId);
        System.setProperty("com.atomikos.icatch.log_base_dir", "./test-logs");
        System.setProperty("com.atomikos.icatch.tm_unique_name", "test-tm-" + uniqueId);

        log.info("Base URL for tests: {}", baseUrl);

        // Генерируем уникальный email для каждого теста
        testEmail = "test_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId() + "@example.com";
    }

    @AfterEach
    void cleanup() {
        // Очищаем данные после каждого теста
        if (testEmail != null) {
            cleanupClientData(testEmail);
            log.info("Cleaned up test data for email: {}", testEmail);
        }
        else{
            log.error("cleanup: testEmail is null");
        }
    }

    @Test
    @DisplayName("Should register client via API")
    void shouldRegisterClientViaApi() {
        // Given
        var clientDTO = AuthObjectMother.randomValidClientDTO();
        // Используем уникальный email для этого теста
        testEmail = clientDTO.getEmail();
        log.info("Registering client with email: {}", clientDTO.getEmail());

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/register",
                createHttpEntity(clientDTO),
                Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Map<String, Object> body = response.getBody();
        assertThat(body.get("message")).isEqualTo("User registered successfully");
        assertThat(body.get("email")).isEqualTo(clientDTO.getEmail());

        log.info("Client registered successfully: {}", clientDTO.getEmail());
    }

    @Test
    @DisplayName("Should login via API and return tokens")
    void shouldLoginViaApiAndReturnTokens() {
        // Given
        var clientDTO = AuthObjectMother.randomValidClientDTO();
        testEmail = clientDTO.getEmail();
        // First register the client
        ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
                baseUrl + "/register",
                createHttpEntity(clientDTO),
                Map.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Prepare login request
        var loginRequest = new LoginRequest(clientDTO.getEmail(),
                clientDTO.getPassword());

        // When
        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
                baseUrl + "/login",
                createHttpEntity(loginRequest),
                LoginResponse.class);

        // Then
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();

        LoginResponse body = loginResponse.getBody();
        assertThat(body.getAccessToken()).isNotNull();
        assertThat(body.getRefreshToken()).isNotNull();
        assertThat(body.getAccessTokenExpiresAt()).isNotNull();
        assertThat(body.getRefreshTokenExpiresAt()).isNotNull();
        assertThat(body.getClientUid()).isNotNull();

        log.info("Login successful for: {}", clientDTO.getEmail());
    }

    @Test
    @DisplayName("Should refresh tokens via API")
    void shouldRefreshTokensViaApi() {
        // Given
        var clientDTO = AuthObjectMother.randomValidClientDTO();
        testEmail = clientDTO.getEmail();
        // First register the client
        ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
                baseUrl + "/register",
                createHttpEntity(clientDTO),
                Map.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Login to get tokens
        var loginRequest = new LoginRequest(clientDTO.getEmail(), clientDTO.getPassword());

        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
                baseUrl + "/login",
                createHttpEntity(loginRequest),
                LoginResponse.class);

        String refreshToken = loginResponse.getBody().getRefreshToken();
        sleep(1000);

        // Prepare refresh request
        var refreshRequest = new RefreshTokenRequest(refreshToken);

        // When
        ResponseEntity<LoginResponse> refreshResponse = restTemplate.postForEntity(
                baseUrl + "/refresh",
                createHttpEntity(refreshRequest),
                LoginResponse.class);

        // Then
        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshResponse.getBody()).isNotNull();

        LoginResponse body = refreshResponse.getBody();
        assertThat(body.getAccessToken()).isNotNull();
        assertThat(body.getRefreshToken()).isNotNull();
        assertThat(body.getAccessToken()).isNotEqualTo(loginResponse.getBody().getAccessToken());
        assertThat(body.getRefreshToken()).isNotEqualTo(refreshToken);

        log.info("Token refresh successful for: {}", clientDTO.getEmail());
    }

    @Test
    @DisplayName("Should validate access token via API")
    void shouldValidateAccessTokenViaApi() {
        // Given
        var clientDTO = AuthObjectMother.randomValidClientDTO();
        testEmail = clientDTO.getEmail(); // Используем уникальный email

        // Register and login
        restTemplate.postForEntity(
                baseUrl + "/register",
                createHttpEntity(clientDTO),
                Map.class);

        var loginRequest = new LoginRequest(clientDTO.getEmail(),
                clientDTO.getPassword());

        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
                baseUrl + "/login",
                createHttpEntity(loginRequest),
                LoginResponse.class);

        String accessToken = loginResponse.getBody().getAccessToken();

        // When
        ResponseEntity<ValidationResponse> validationResponse = restTemplate.postForEntity(
                baseUrl + "/validate/access?accessToken=" + accessToken,
                null,
                ValidationResponse.class);

        // Then
        assertThat(validationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(validationResponse.getBody()).isNotNull();
        assertThat(validationResponse.getBody().getStatus()).isEqualTo("OK");

        log.info("Access token validation successful");
    }

    @Test
    @DisplayName("Should validate refresh token via API")
    void shouldValidateRefreshTokenViaApi() {
        // Given
        var clientDTO = AuthObjectMother.randomValidClientDTO();
        testEmail = clientDTO.getEmail(); // Используем уникальный email

        // Register and login
        restTemplate.postForEntity(
                baseUrl + "/register",
                createHttpEntity(clientDTO),
                Map.class);

        var loginRequest = new LoginRequest(clientDTO.getEmail(),
                clientDTO.getPassword());

        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
                baseUrl + "/login",
                createHttpEntity(loginRequest),
                LoginResponse.class);

        String refreshToken = loginResponse.getBody().getRefreshToken();

        // When
        ResponseEntity<ValidationResponse> validationResponse = restTemplate.postForEntity(
                baseUrl + "/validate/refresh?refreshToken=" + refreshToken,
                null,
                ValidationResponse.class);

        // Then
        assertThat(validationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(validationResponse.getBody()).isNotNull();
        assertThat(validationResponse.getBody().getStatus()).isEqualTo("OK");

        log.info("Refresh token validation successful");
    }

    @Test
    @DisplayName("Should return error for duplicate registration")
    void shouldReturnErrorForDuplicateRegistration() {
        // Given
        var clientDTO = AuthObjectMother.randomValidClientDTO();
        testEmail = clientDTO.getEmail(); // Используем уникальный email

        // First registration
        restTemplate.postForEntity(
                baseUrl + "/register",
                createHttpEntity(clientDTO),
                Map.class);

        // When - Try to register same client again
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                baseUrl + "/register",
                createHttpEntity(clientDTO),
                ErrorResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        ErrorResponse body = response.getBody();
        assertThat(body.getError()).isEqualTo("client_already_exist");
        assertThat(body.getMessage()).isEqualTo("An account with such email or uid already exists");

        log.info("Duplicate registration handled correctly");
    }

    @Test
    @DisplayName("Full flow: register -> login -> validate -> refresh -> validate")
    void shouldCompleteFullAuthenticationFlow() {
        // Given
        var clientDTO = AuthObjectMother.randomValidClientDTO();
        testEmail = clientDTO.getEmail(); // Используем уникальный email
        log.info("Starting full flow for: {}", clientDTO.getEmail());

        // Step 1: Register
        ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
                baseUrl + "/register",
                createHttpEntity(clientDTO),
                Map.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        log.info("Step 1: Registration completed");

        // Step 2: Login
        var loginRequest = new LoginRequest(clientDTO.getEmail(),
                clientDTO.getPassword());
        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
                baseUrl + "/login",
                createHttpEntity(loginRequest),
                LoginResponse.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        String accessToken1 = loginResponse.getBody().getAccessToken();
        String refreshToken1 = loginResponse.getBody().getRefreshToken();
        log.info("Step 2: Login completed");

        // Step 3: Validate access token
        ResponseEntity<ValidationResponse> validateResponse1 = restTemplate.postForEntity(
                baseUrl + "/validate/access?accessToken=" + accessToken1,
                null,
                ValidationResponse.class);
        assertThat(validateResponse1.getStatusCode()).isEqualTo(HttpStatus.OK);
        log.info("Step 3: Access token validation completed");

        sleep(1000);

        // Step 4: Refresh tokens
        var refreshRequest = new RefreshTokenRequest(refreshToken1);
        ResponseEntity<LoginResponse> refreshResponse = restTemplate.postForEntity(
                baseUrl + "/refresh",
                createHttpEntity(refreshRequest),
                LoginResponse.class);
        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        String accessToken2 = refreshResponse.getBody().getAccessToken();
        String refreshToken2 = refreshResponse.getBody().getRefreshToken();
        log.info("Step 4: Token refresh completed");

        // Step 5: Validate new access token
        ResponseEntity<ValidationResponse> validateResponse2 = restTemplate.postForEntity(
                baseUrl + "/validate/access?accessToken=" + accessToken2,
                null,
                ValidationResponse.class);
        assertThat(validateResponse2.getStatusCode()).isEqualTo(HttpStatus.OK);
        log.info("Step 5: New access token validation completed");

        // Verify tokens are different
        assertThat(accessToken2).isNotEqualTo(accessToken1);
        assertThat(refreshToken2).isNotEqualTo(refreshToken1);

        log.info("Full authentication flow completed successfully for: {}",
                clientDTO.getEmail());
    }

    // Вспомогательные DTO классы для тестов (остаются без изменений)
    public static class LoginRequest {
        private String email;
        private String password;

        public LoginRequest() {
        }

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class RefreshTokenRequest {
        private String refreshToken;

        public RefreshTokenRequest() {
        }

        public RefreshTokenRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }

    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private java.util.Date accessTokenExpiresAt;
        private java.util.Date refreshTokenExpiresAt;
        private java.util.UUID clientUid;

        // геттеры и сеттеры
        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public java.util.Date getAccessTokenExpiresAt() {
            return accessTokenExpiresAt;
        }

        public void setAccessTokenExpiresAt(java.util.Date accessTokenExpiresAt) {
            this.accessTokenExpiresAt = accessTokenExpiresAt;
        }

        public java.util.Date getRefreshTokenExpiresAt() {
            return refreshTokenExpiresAt;
        }

        public void setRefreshTokenExpiresAt(java.util.Date refreshTokenExpiresAt) {
            this.refreshTokenExpiresAt = refreshTokenExpiresAt;
        }

        public java.util.UUID getClientUid() {
            return clientUid;
        }

        public void setClientUid(java.util.UUID clientUid) {
            this.clientUid = clientUid;
        }
    }

    public static class ValidationResponse {
        private String status;

        public ValidationResponse() {
        }

        public ValidationResponse(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class HealthResponse {
        private String status;
        private String service;
        private long timestamp;

        public HealthResponse() {
        }

        public HealthResponse(String status, String service, long timestamp) {
            this.status = status;
            this.service = service;
            this.timestamp = timestamp;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class ErrorResponse {
        private String error;
        private String message;

        public ErrorResponse() {
        }

        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}