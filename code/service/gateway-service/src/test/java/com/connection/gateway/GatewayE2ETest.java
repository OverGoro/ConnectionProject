package com.connection.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class GatewayE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String BASE_URL = "http://localhost:";
    private static final Random random = new Random();

    // Вспомогательные методы для генерации уникальных данных
    private String generateUniqueEmail() {
        return "testuser" + random.nextInt(100000) + "@test.com";
    }

    private String generateUniqueUsername() {
        return "testuser" + random.nextInt(100000);
    }

    private String generateUniqueDeviceName() {
        return "TestDevice" + random.nextInt(100000);
    }

    @Test
    public void testFullIntegrationFlow() {
    
        // 1. Регистрация нового клиента
        
        String clientEmail = generateUniqueEmail();
        String username = generateUniqueUsername();
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        
        Map<String, Object> clientRegistration = new HashMap<>();
        clientRegistration.put("email", clientEmail);
        clientRegistration.put("username", username);
        clientRegistration.put("password", "TestPassword123!");
        clientRegistration.put("birthDate", "2002-01-01T01:31:40.067Z");
        clientRegistration.put("uid", userId.toString());

        ResponseEntity<Map> registrationResponse = restTemplate.postForEntity(
            "/api/v1/auth/register",
            clientRegistration,
            Map.class
        );
        
        assertThat(registrationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(registrationResponse.getBody()).isNotNull();

        // 2. Логин для получения токенов
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", clientEmail);
        loginRequest.put("password", "TestPassword123!");

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
            "/api/v1/auth/login",
            loginRequest,
            Map.class
        );
        
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        
        String accessToken = (String) loginResponse.getBody().get("accessToken");
        String refreshToken = (String) loginResponse.getBody().get("refreshToken");
        String clientUid = (String) loginResponse.getBody().get("clientUid");
        
        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();
        assertThat(clientUid).isEqualTo(userId.toString());

        // Создаем заголовки с авторизацией
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 3. Создание устройства
        String deviceName = generateUniqueDeviceName();
        Map<String, Object> deviceRequest = new HashMap<>();
        deviceRequest.put("uid", deviceId.toString());
        deviceRequest.put("deviceName", deviceName);
        deviceRequest.put("deviceDescription", "Test Device Description");
        deviceRequest.put("clientUuid", clientUid);

        HttpEntity<Map<String, Object>> deviceEntity = new HttpEntity<>(deviceRequest, headers);
        ResponseEntity<Map> deviceResponse = restTemplate.postForEntity(
            "/api/v1/device/devices",
            deviceEntity,
            Map.class
        );
        
        assertThat(deviceResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deviceResponse.getBody()).isNotNull();
        
        String deviceUid = (String) deviceResponse.getBody().get("deviceUid");
        assertThat(deviceUid).isEqualTo(deviceId.toString());

        // 4. Создание токена устройства
        Map<String, String> deviceTokenRequest = new HashMap<>();
        deviceTokenRequest.put("deviceUid", deviceUid);

        HttpEntity<Map<String, String>> deviceTokenEntity = new HttpEntity<>(deviceTokenRequest, headers);
        ResponseEntity<Map> deviceTokenResponse = restTemplate.postForEntity(
            "/api/v1/device/auth/device-token",
            deviceTokenEntity,
            Map.class
        );
        
        assertThat(deviceTokenResponse.getBody()).isNotNull();
        
        String deviceToken = (String) deviceTokenResponse.getBody().get("token");

        // 5. Создание access токена устройства
        Map<String, String> deviceAccessTokenRequest = new HashMap<>();
        deviceAccessTokenRequest.put("token", deviceToken);

        ResponseEntity<Map> deviceAccessTokenResponse = restTemplate.postForEntity(
            "/api/v1/device/auth/access-token",
            deviceAccessTokenRequest,
            Map.class
        );
        
        assertThat(deviceAccessTokenResponse.getBody()).isNotNull();
        
        String deviceAccessToken = (String) deviceAccessTokenResponse.getBody().get("token");

        // 6. Создание буфера
        Map<String, Object> bufferRequest = new HashMap<>();
        bufferRequest.put("deviceUid", deviceUid);
        bufferRequest.put("maxMessagesNumber", 100);
        bufferRequest.put("maxMessageSize", 1024);
        bufferRequest.put("messagePrototype", "{\"type\":\"test\"}");

        HttpEntity<Map<String, Object>> bufferEntity = new HttpEntity<>(bufferRequest, headers);
        ResponseEntity<Map> bufferResponse = restTemplate.postForEntity(
            "/api/v1/buffer/buffers",
            bufferEntity,
            Map.class
        );
        
        assertThat(bufferResponse.getBody()).isNotNull();
        
        String bufferUid = (String) bufferResponse.getBody().get("bufferUuid");

        // 7. Создание схемы подключения
        Map<String, Object> schemeRequest = new HashMap<>();
        schemeRequest.put("clientUid", clientUid);
        schemeRequest.put("schemeJson", "{\"connections\":[]}");
        schemeRequest.put("usedBuffers", Collections.singletonList(bufferUid));

        HttpEntity<Map<String, Object>> schemeEntity = new HttpEntity<>(schemeRequest, headers);
        ResponseEntity<Map> schemeResponse = restTemplate.postForEntity(
            "/api/v1/scheme/schemes",
            schemeEntity,
            Map.class
        );
        
        assertThat(schemeResponse.getBody()).isNotNull();
        
        String schemeUid = (String) schemeResponse.getBody().get("schemeUid");

        // 8. Добавление сообщения от устройства
        HttpHeaders deviceHeaders = new HttpHeaders();
        deviceHeaders.setBearerAuth(deviceAccessToken);
        deviceHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> messageRequest = new HashMap<>();
        messageRequest.put("bufferUid", bufferUid);
        messageRequest.put("content", "{\"sensor\":\"temperature\",\"value\":25.5}");
        messageRequest.put("contentType", "application/json");

        HttpEntity<Map<String, Object>> messageEntity = new HttpEntity<>(messageRequest, deviceHeaders);
        ResponseEntity<Void> messageResponse = restTemplate.postForEntity(
            "/api/v1/message/messages",
            messageEntity,
            Void.class
        );
        

        // 9. Проверка health endpoints
        ResponseEntity<Map> healthResponse = restTemplate.getForEntity("/api/v1/auth/health", Map.class);
        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        healthResponse = restTemplate.getForEntity("/api/v1/device/health", Map.class);
        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        healthResponse = restTemplate.getForEntity("/api/v1/buffer/health", Map.class);
        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        healthResponse = restTemplate.getForEntity("/api/v1/scheme/health", Map.class);
        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        healthResponse = restTemplate.getForEntity("/api/v1/message/health", Map.class);
        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 10. Очистка данных (удаление созданных сущностей)
        
        // Удаление схемы подключения
        String deleteSchemesUrl = "/api/v1/scheme/schemes?schemeUids=" + schemeUid;
        HttpEntity<Void> deleteSchemeEntity = new HttpEntity<>(headers);
        ResponseEntity<Void> deleteSchemeResponse = restTemplate.exchange(
            deleteSchemesUrl,
            HttpMethod.DELETE,
            deleteSchemeEntity,
            Void.class
        );

        // Удаление буфера
        String deleteBuffersUrl = "/api/v1/buffer/buffers?bufferUids=" + bufferUid;
        HttpEntity<Void> deleteBufferEntity = new HttpEntity<>(headers);
        ResponseEntity<Void> deleteBufferResponse = restTemplate.exchange(
            deleteBuffersUrl,
            HttpMethod.DELETE,
            deleteBufferEntity,
            Void.class
        );

        // Удаление токена устройства
        String deleteDeviceTokenUrl = "/api/v1/device/auth/device-token?deviceUids=" + deviceUid;
        HttpEntity<Void> deleteDeviceTokenEntity = new HttpEntity<>(headers);
        ResponseEntity<Void> deleteDeviceTokenResponse = restTemplate.exchange(
            deleteDeviceTokenUrl,
            HttpMethod.DELETE,
            deleteDeviceTokenEntity,
            Void.class
        );

        // Удаление устройства
        String deleteDeviceUrl = "/api/v1/device/devices/" + deviceUid;
        HttpEntity<Void> deleteDeviceEntity = new HttpEntity<>(headers);
        ResponseEntity<Void> deleteDeviceResponse = restTemplate.exchange(
            deleteDeviceUrl,
            HttpMethod.DELETE,
            deleteDeviceEntity,
            Void.class
        );

        // Тест завершен успешно - все созданные данные удалены
    }
}