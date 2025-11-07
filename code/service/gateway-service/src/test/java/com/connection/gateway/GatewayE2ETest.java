package com.connection.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"server.port=18081"})
@ActiveProfiles("test")
public class GatewayE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

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
        System.out.println("=== STARTING E2E TEST ===");
        
        // 1. Регистрация нового клиента
        System.out.println("Step 1: Registering new client");
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
        System.out.println("Client registered successfully: " + clientEmail);

        // 2. Логин для получения токенов
        System.out.println("Step 2: Logging in to get tokens");
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
        System.out.println("Login successful, client UID: " + clientUid);

        // Создаем заголовки с авторизацией клиента (Bearer token)
        HttpHeaders clientHeaders = new HttpHeaders();
        clientHeaders.set("Authorization", "Bearer " + accessToken); // Правильный формат Bearer token
        clientHeaders.setContentType(MediaType.APPLICATION_JSON);

        // 3. Создание устройства
        System.out.println("Step 3: Creating device");
        String deviceName = generateUniqueDeviceName();
        Map<String, Object> deviceRequest = new HashMap<>();
        deviceRequest.put("uid", deviceId.toString());
        deviceRequest.put("clientUuid", clientUid);
        deviceRequest.put("deviceName", deviceName);
        deviceRequest.put("deviceDescription", "Test Device Description");

        HttpEntity<Map<String, Object>> deviceEntity = new HttpEntity<>(deviceRequest, clientHeaders);
        ResponseEntity<Map> deviceResponse = restTemplate.postForEntity(
            "/api/v1/device/devices",
            deviceEntity,
            Map.class
        );
        
        assertThat(deviceResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deviceResponse.getBody()).isNotNull();
        
        String deviceUid = (String) deviceResponse.getBody().get("deviceUid");
        assertThat(deviceUid).isEqualTo(deviceId.toString());
        System.out.println("Device created successfully: " + deviceUid);

        // 6. Создание буфера (требует авторизации клиента)
        System.out.println("Step 6: Creating buffer");
        Map<String, Object> bufferRequest = new HashMap<>();
        bufferRequest.put("uid", UUID.randomUUID().toString());
        bufferRequest.put("deviceUid", deviceUid);
        bufferRequest.put("maxMessagesNumber", 100);
        bufferRequest.put("maxMessageSize", 1024);
        bufferRequest.put("messagePrototype", "{\"type\":\"test\"}");

        HttpEntity<Map<String, Object>> bufferEntity = new HttpEntity<>(bufferRequest, clientHeaders);
        ResponseEntity<Map> bufferResponse = restTemplate.postForEntity(
            "/api/v1/buffer/buffers",
            bufferEntity,
            Map.class
        );
        
        assertThat(bufferResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(bufferResponse.getBody()).isNotNull();
        
        String bufferUid = (String) bufferResponse.getBody().get("bufferUuid");
        assertThat(bufferUid).isNotBlank();
        System.out.println("Buffer created successfully: " + bufferUid);

        // 7. Создание схемы подключения (требует авторизации клиента)
        System.out.println("Step 7: Creating connection scheme");
        Map<String, Object> schemeRequest = new HashMap<>();
        schemeRequest.put("uid", UUID.randomUUID().toString());
        schemeRequest.put("clientUid", clientUid);
        schemeRequest.put("schemeJson", "{\"" + bufferUid + "\":[]}"); // Правильный формат JSON согласно валидатору
        schemeRequest.put("usedBuffers", Collections.singletonList(bufferUid));

        HttpEntity<Map<String, Object>> schemeEntity = new HttpEntity<>(schemeRequest, clientHeaders);
        ResponseEntity<Map> schemeResponse = restTemplate.postForEntity(
            "/api/v1/scheme/schemes",
            schemeEntity,
            Map.class
        );

        assertThat(schemeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(schemeResponse.getBody()).isNotNull();

        String schemeUid = (String) schemeResponse.getBody().get("schemeUid");
        assertThat(schemeUid).isNotBlank();
        System.out.println("Connection scheme created successfully: " + schemeUid);

        // 8. Добавление сообщения от устройства (используем device Bearer token)
        System.out.println("Step 8: Adding message from device");
        HttpHeaders deviceHeaders = new HttpHeaders();
        deviceHeaders.set("Authorization", "Bearer " + accessToken);
        deviceHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> messageRequest = new HashMap<>();
        messageRequest.put("uid", UUID.randomUUID().toString()); // Добавляем обязательный uid
        messageRequest.put("bufferUid", bufferUid);
        messageRequest.put("content", "{\"sensor\":\"temperature\",\"value\":25.5}");
        messageRequest.put("contentType", "application/json");
        messageRequest.put("createdAt", new Date().toInstant().toString()); // Добавляем обязательное поле createdAt

        HttpEntity<Map<String, Object>> messageEntity = new HttpEntity<>(messageRequest, deviceHeaders);
        ResponseEntity<Void> messageResponse = restTemplate.postForEntity(
            "/api/v1/message/messages",
            messageEntity,
            Void.class
        );

        assertThat(messageResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println("Message added successfully");

        // 9. Получение сообщений (можно использовать как client, так и device авторизацию)
        System.out.println("Step 9: Retrieving messages using client token");
        HttpEntity<Void> getMessagesEntity = new HttpEntity<>(clientHeaders);
        ResponseEntity<Map> getMessagesResponse = restTemplate.exchange(
            "/api/v1/message/messages/?bufferUids=" + bufferUid + "&limit=10",
            HttpMethod.GET,
            getMessagesEntity,
            Map.class
        );
        
        assertThat(getMessagesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getMessagesResponse.getBody()).isNotNull();
        System.out.println("Messages retrieved successfully");

        // 10. Проверка health endpoints (не требуют авторизации)
        System.out.println("Step 10: Checking health endpoints");
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

        healthResponse = restTemplate.getForEntity("/api/v1/device/auth/health", Map.class);
        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println("All health checks passed");

        // 11. Очистка данных (удаление созданных сущностей)
        System.out.println("Step 11: Cleaning up test data");
        
        // Удаление схемы подключения (требует ROLE_CLIENT)
        System.out.println("Deleting connection scheme: " + schemeUid);
        String deleteSchemesUrl = "/api/v1/scheme/schemes?schemeUids=" + schemeUid;
        HttpEntity<Void> deleteSchemeEntity = new HttpEntity<>(clientHeaders);
        ResponseEntity<Void> deleteSchemeResponse = restTemplate.exchange(
            deleteSchemesUrl,
            HttpMethod.DELETE,
            deleteSchemeEntity,
            Void.class
        );

        // Удаление буфера (требует ROLE_CLIENT)
        System.out.println("Deleting buffer: " + bufferUid);
        String deleteBuffersUrl = "/api/v1/buffer/buffers?bufferUids=" + bufferUid;
        HttpEntity<Void> deleteBufferEntity = new HttpEntity<>(clientHeaders);
        ResponseEntity<Void> deleteBufferResponse = restTemplate.exchange(
            deleteBuffersUrl,
            HttpMethod.DELETE,
            deleteBufferEntity,
            Void.class
        );
        

        // Удаление токена устройства (требует ROLE_CLIENT)
        System.out.println("Deleting device token for device: " + deviceUid);
        String deleteDeviceTokenUrl = "/api/v1/device/auth/device-token?deviceUids=" + deviceUid;
        HttpEntity<Void> deleteDeviceTokenEntity = new HttpEntity<>(clientHeaders);
        ResponseEntity<Void> deleteDeviceTokenResponse = restTemplate.exchange(
            deleteDeviceTokenUrl,
            HttpMethod.DELETE,
            deleteDeviceTokenEntity,
            Void.class
        );

        // Удаление устройства (требует авторизации)
        System.out.println("Deleting device: " + deviceUid);
        String deleteDeviceUrl = "/api/v1/device/devices/" + deviceUid;
        HttpEntity<Void> deleteDeviceEntity = new HttpEntity<>(clientHeaders);
        ResponseEntity<Void> deleteDeviceResponse = restTemplate.exchange(
            deleteDeviceUrl,
            HttpMethod.DELETE,
            deleteDeviceEntity,
            Void.class
        );

        System.out.println("=== E2E TEST COMPLETED SUCCESSFULLY ===");
    }
}