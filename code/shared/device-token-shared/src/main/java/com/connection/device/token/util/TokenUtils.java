package com.connection.device.token.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.UUID;

/**
 * Утилитарный класс для извлечения параметров из строки токена.
 */
public class TokenUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String DEVICE_TOKEN_TYPE = "device_token";
    private static final String DEVICE_ACCESS_TOKEN_TYPE =
            "device_access_token";

    private TokenUtils() {
        // Приватный конструктор для предотвращения создания экземпляров
    }

    /**
     * Извлекает device UID из токена устройства.
     */
    public static UUID extractDeviceUidFromDeviceToken(String token) {
        return extractUuidClaim(token, "deviceUid", "device UID");
    }

    /**
     * Извлекает device token UID из токена доступа устройства.
     */
    public static UUID extractDeviceTokenUidFromAccessToken(String token) {
        return extractUuidClaim(token, "deviceTokenUid", "device token UID");
    }

    /**
     * Извлекает тип токена.
     */
    public static String extractTokenType(String token) {
        return extractStringClaim(token, "type", "token type");
    }

    /**
     * Извлекает issuer из токена.
     */
    public static String extractIssuer(String token) {
        return extractStringClaim(token, "iss", "issuer");
    }

    /**
     * Извлекает subject из токена.
     */
    public static String extractSubject(String token) {
        return extractStringClaim(token, "sub", "subject");
    }

    /**
     * Извлекает дату создания токена.
     */
    public static Date extractIssuedAt(String token) {
        return extractDateClaim(token, "iat", "issued at date");
    }

    /**
     * Извлекает дату истечения токена.
     */
    public static Date extractExpiration(String token) {
        return extractDateClaim(token, "exp", "expiration date");
    }

    /**
     * Проверяет, является ли токен device token.
     */
    public static boolean isDeviceToken(String token) {
        return DEVICE_TOKEN_TYPE.equals(safeExtractTokenType(token));
    }

    /**
     * Проверяет, является ли токен device access token.
     */
    public static boolean isDeviceAccessToken(String token) {
        return DEVICE_ACCESS_TOKEN_TYPE.equals(safeExtractTokenType(token));
    }

    /**
     * Проверяет, истек ли токен.
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
     * Извлекает все основные параметры из токена в виде читаемой строки.
     */
    public static String extractTokenInfo(String token) {
        try {
            StringBuilder info = new StringBuilder();
            info.append("Token Type: ").append(extractTokenType(token))
                    .append("\n");
            info.append("Issuer: ").append(extractIssuer(token)).append("\n");
            info.append("Subject: ").append(extractSubject(token)).append("\n");
            info.append("Issued At: ").append(extractIssuedAt(token))
                    .append("\n");
            info.append("Expires At: ").append(extractExpiration(token))
                    .append("\n");
            info.append("Is Expired: ").append(isTokenExpired(token))
                    .append("\n");

            if (isDeviceToken(token)) {
                info.append("Device UID: ")
                        .append(extractDeviceUidFromDeviceToken(token))
                        .append("\n");
            } else if (isDeviceAccessToken(token)) {
                info.append("Device Token UID: ")
                        .append(extractDeviceTokenUidFromAccessToken(token))
                        .append("\n");
            }

            return info.toString();

        } catch (Exception e) {
            return "Failed to extract token info: " + e.getMessage();
        }
    }

    // ============ PRIVATE HELPER METHODS ============

    private static UUID extractUuidClaim(String token, String claimName,
            String claimDescription) {
        String claimValue =
                extractStringClaim(token, claimName, claimDescription);
        try {
            return UUID.fromString(claimValue);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid " + claimDescription + " format: " + claimValue,
                    e);
        }
    }

    private static String extractStringClaim(String token, String claimName,
            String claimDescription) {
        JsonNode jsonNode = parseTokenPayload(token);
        return extractClaim(jsonNode, claimName, claimDescription).asText();
    }

    private static Date extractDateClaim(String token, String claimName,
            String claimDescription) {
        JsonNode jsonNode = parseTokenPayload(token);
        long timestamp =
                extractClaim(jsonNode, claimName, claimDescription).asLong();
        return new Date(timestamp * 1000); // Конвертируем из секунд в миллисекунды
    }

    private static JsonNode extractClaim(JsonNode jsonNode, String claimName,
            String claimDescription) {
        if (!jsonNode.has(claimName)) {
            throw new IllegalArgumentException(
                    "Token does not contain " + claimDescription);
        }
        return jsonNode.get(claimName);
    }

    private static JsonNode parseTokenPayload(String token) {
        validateToken(token);

        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }

            String payload = new String(
                    java.util.Base64.getUrlDecoder().decode(parts[1]));
            return OBJECT_MAPPER.readTree(payload);

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to parse token payload: " + e.getMessage(), e);
        }
    }

    private static void validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
    }

    private static String safeExtractTokenType(String token) {
        try {
            return extractTokenType(token);
        } catch (Exception e) {
            return "";
        }
    }
}
