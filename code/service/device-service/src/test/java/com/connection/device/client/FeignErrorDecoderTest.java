package com.connection.device.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import feign.Request;
import feign.Response;

@DisplayName("Feign Error Decoder Tests")
class FeignErrorDecoderTest {

    private FeignErrorDecoder errorDecoder;
    private Request originalRequest;

    @BeforeEach
    void setUp() {
        errorDecoder = new FeignErrorDecoder();

        originalRequest = Request.create(
                Request.HttpMethod.GET,
                "http://auth-service/validate/token/access",
                Map.of("Authorization", List.of("Bearer token")),
                new byte[0],
                null,
                null);
    }

    @Test
    @DisplayName("Decode 401 - Unauthorized")
    void shouldReturnUnauthorizedExceptionFor401() {

        Response response = Response.builder()
                .status(401)
                .request(originalRequest)
                .build();

        Exception result = errorDecoder.decode("methodKey", response);

        assertThat(result).isInstanceOf(ResponseStatusException.class);
        ResponseStatusException exception = (ResponseStatusException) result;
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getReason()).isEqualTo("Invalid access token");
    }

    @Test
    @DisplayName("Decode 403 - Forbidden")
    void shouldReturnForbiddenExceptionFor403() {

        Response response = Response.builder()
                .status(403)
                .request(originalRequest)
                .build();

        Exception result = errorDecoder.decode("methodKey", response);

        assertThat(result).isInstanceOf(ResponseStatusException.class);
        ResponseStatusException exception = (ResponseStatusException) result;
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exception.getReason()).isEqualTo("Access denied");
    }

    @Test
    @DisplayName("Decode 404 - Not Found")
    void shouldReturnNotFoundExceptionFor404() {

        Response response = Response.builder()
                .status(404)
                .request(originalRequest)
                .build();

        Exception result = errorDecoder.decode("methodKey", response);

        assertThat(result).isInstanceOf(ResponseStatusException.class);
        ResponseStatusException exception = (ResponseStatusException) result;
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Resource not found");
    }

    @Test
    @DisplayName("Decode 500 - Internal Server Error")
    void shouldReturnInternalServerErrorFor500() {

        Response response = Response.builder()
                .status(500)
                .request(originalRequest)
                .build();

        Exception result = errorDecoder.decode("methodKey", response);

        assertThat(result).isInstanceOf(ResponseStatusException.class);
        ResponseStatusException exception = (ResponseStatusException) result;
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getReason()).isEqualTo("Auth service error: 500");
    }

    @Test
    @DisplayName("Decode unknown status code")
    void shouldReturnInternalServerErrorForUnknownStatusCode() {

        Response response = Response.builder()
                .status(999)
                .request(originalRequest)
                .build();

        Exception result = errorDecoder.decode("methodKey", response);

        assertThat(result).isInstanceOf(ResponseStatusException.class);
        ResponseStatusException exception = (ResponseStatusException) result;
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getReason()).isEqualTo("Auth service error: 999");
    }

    @Test
    @DisplayName("Decode with response body")
    void shouldIncludeStatusCodeWhenResponseHasBody() {

        String responseBody = "Service unavailable";
        Response response = Response.builder()
                .status(503)
                .request(originalRequest)
                .body(responseBody, java.nio.charset.StandardCharsets.UTF_8)
                .build();

        Exception result = errorDecoder.decode("methodKey", response);

        assertThat(result).isInstanceOf(ResponseStatusException.class);
        ResponseStatusException exception = (ResponseStatusException) result;
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getReason()).isEqualTo("Auth service error: 503");
    }
}