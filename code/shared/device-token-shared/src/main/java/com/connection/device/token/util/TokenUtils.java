package com.connection.device.token.util;

import java.util.Date;
import java.util.UUID;

/**
 * Утилитарный класс для извлечения параметров из строки токена
 */
public class TokenUtils {
    
    private TokenUtils() {
        // Приватный конструктор для предотвращения создания экземпляров
    }
    
    /**
     * Извлекает device UID из токена устройства
     * 
     * @param token строка токена
     * @return device UID
     * @throws IllegalArgumentException если токен невалидный или не содержит device UID
     */
    public static UUID extractDeviceUidFromDeviceToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        
        try {
            // Разбиваем JWT токен на части
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }
            
            // Декодируем payload (вторая часть)
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            
            // Парсим JSON для извлечения deviceUid
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);
            
            if (!jsonNode.has("deviceUid")) {
                throw new IllegalArgumentException("Token does not contain deviceUid");
            }
            
            String deviceUidStr = jsonNode.get("deviceUid").asText();
            return UUID.fromString(deviceUidStr);
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract device UID from token: " + e.getMessage(), e);
        }
    }
    
    /**
     * Извлекает device token UID из токена доступа устройства
     * 
     * @param token строка токена
     * @return device token UID
     * @throws IllegalArgumentException если токен невалидный или не содержит device token UID
     */
    public static UUID extractDeviceTokenUidFromAccessToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        
        try {
            // Разбиваем JWT токен на части
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }
            
            // Декодируем payload (вторая часть)
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            
            // Парсим JSON для извлечения deviceTokenUid
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);
            
            if (!jsonNode.has("deviceTokenUid")) {
                throw new IllegalArgumentException("Token does not contain deviceTokenUid");
            }
            
            String deviceTokenUidStr = jsonNode.get("deviceTokenUid").asText();
            return UUID.fromString(deviceTokenUidStr);
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract device token UID from token: " + e.getMessage(), e);
        }
    }
    
    /**
     * Извлекает тип токена
     * 
     * @param token строка токена
     * @return тип токена ("device_token" или "device_access_token")
     * @throws IllegalArgumentException если токен невалидный или не содержит тип
     */
    public static String extractTokenType(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }
            
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);
            
            if (!jsonNode.has("type")) {
                throw new IllegalArgumentException("Token does not contain type");
            }
            
            return jsonNode.get("type").asText();
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract token type: " + e.getMessage(), e);
        }
    }
    
    /**
     * Извлекает issuer из токена
     * 
     * @param token строка токена
     * @return issuer
     * @throws IllegalArgumentException если токен невалидный или не содержит issuer
     */
    public static String extractIssuer(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }
            
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);
            
            if (!jsonNode.has("iss")) {
                throw new IllegalArgumentException("Token does not contain issuer");
            }
            
            return jsonNode.get("iss").asText();
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract issuer: " + e.getMessage(), e);
        }
    }
    
    /**
     * Извлекает subject из токена
     * 
     * @param token строка токена
     * @return subject
     * @throws IllegalArgumentException если токен невалидный или не содержит subject
     */
    public static String extractSubject(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }
            
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);
            
            if (!jsonNode.has("sub")) {
                throw new IllegalArgumentException("Token does not contain subject");
            }
            
            return jsonNode.get("sub").asText();
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract subject: " + e.getMessage(), e);
        }
    }
    
    /**
     * Извлекает дату создания токена
     * 
     * @param token строка токена
     * @return дата создания
     * @throws IllegalArgumentException если токен невалидный или не содержит дату создания
     */
    public static Date extractIssuedAt(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }
            
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);
            
            if (!jsonNode.has("iat")) {
                throw new IllegalArgumentException("Token does not contain issued at date");
            }
            
            long iat = jsonNode.get("iat").asLong();
            return new Date(iat * 1000); // Конвертируем из секунд в миллисекунды
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract issued at date: " + e.getMessage(), e);
        }
    }
    
    /**
     * Извлекает дату истечения токена
     * 
     * @param token строка токена
     * @return дата истечения
     * @throws IllegalArgumentException если токен невалидный или не содержит дату истечения
     */
    public static Date extractExpiration(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }
            
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);
            
            if (!jsonNode.has("exp")) {
                throw new IllegalArgumentException("Token does not contain expiration date");
            }
            
            long exp = jsonNode.get("exp").asLong();
            return new Date(exp * 1000); // Конвертируем из секунд в миллисекунды
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract expiration date: " + e.getMessage(), e);
        }
    }
    
    /**
     * Проверяет, является ли токен device token
     * 
     * @param token строка токена
     * @return true если это device token
     */
    public static boolean isDeviceToken(String token) {
        try {
            return "device_token".equals(extractTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Проверяет, является ли токен device access token
     * 
     * @param token строка токена
     * @return true если это device access token
     */
    public static boolean isDeviceAccessToken(String token) {
        try {
            return "device_access_token".equals(extractTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Проверяет, истек ли токен
     * 
     * @param token строка токена
     * @return true если токен истек
     */
    public static boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true; // Если не можем извлечь дату, считаем токен невалидным
        }
    }
    
    /**
     * Извлекает все основные параметры из токена в виде читаемой строки
     * 
     * @param token строка токена
     * @return строка с параметрами токена
     */
    public static String extractTokenInfo(String token) {
        try {
            StringBuilder info = new StringBuilder();
            info.append("Token Type: ").append(extractTokenType(token)).append("\n");
            info.append("Issuer: ").append(extractIssuer(token)).append("\n");
            info.append("Subject: ").append(extractSubject(token)).append("\n");
            info.append("Issued At: ").append(extractIssuedAt(token)).append("\n");
            info.append("Expires At: ").append(extractExpiration(token)).append("\n");
            info.append("Is Expired: ").append(isTokenExpired(token)).append("\n");
            
            if (isDeviceToken(token)) {
                info.append("Device UID: ").append(extractDeviceUidFromDeviceToken(token)).append("\n");
            } else if (isDeviceAccessToken(token)) {
                info.append("Device Token UID: ").append(extractDeviceTokenUidFromAccessToken(token)).append("\n");
            }
            
            return info.toString();
            
        } catch (Exception e) {
            return "Failed to extract token info: " + e.getMessage();
        }
    }
}