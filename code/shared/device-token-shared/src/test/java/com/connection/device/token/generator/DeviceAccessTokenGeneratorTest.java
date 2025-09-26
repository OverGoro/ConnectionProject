package com.connection.device.token.generator;

import static com.connection.device.token.mother.DeviceTokenObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.connection.device.token.model.DeviceAccessTokenBLM;
import com.connection.device.token.model.DeviceTokenBLM;

import io.jsonwebtoken.security.Keys;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Device Access Token Generator Tests")
class DeviceAccessTokenGeneratorTest {

    private DeviceAccessTokenGenerator generator;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        secretKey = Keys.hmacShaKeyFor("test-secret-key-1234567890-1234567890".getBytes());
        generator = new DeviceAccessTokenGenerator(secretKey, "test-app", "device-access-token");
    }

    @Test
    @DisplayName("Generate device access token - Positive")
    void testGenerateDeviceAccessToken_Positive() {
        String token = generator.generateDeviceAccessToken(
            createValidDeviceAccessTokenDALM().getDeviceTokenUid(),
            createValidDeviceAccessTokenDALM().getCreatedAt(),
            createValidDeviceAccessTokenDALM().getExpiresAt()
        );
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Parse valid device access token - Positive")
    void testGetDeviceAccessTokenBLM_Positive() {
        String tokenString = generator.generateDeviceAccessToken(
            createValidDeviceAccessTokenDALM().getDeviceTokenUid(),
            createValidDeviceAccessTokenDALM().getCreatedAt(),
            createValidDeviceAccessTokenDALM().getExpiresAt()
        );
        
        DeviceAccessTokenBLM result = generator.getDeviceAccessTokenBLM(tokenString);
        assertThat(result).isNotNull();
        assertThat(result.getDeviceTokenUid()).isEqualTo(createValidDeviceAccessTokenDALM().getDeviceTokenUid());
        assertThat(result.getToken()).isEqualTo(tokenString);
    }

    @Test
    @DisplayName("Parse invalid device access token - Negative")
    void testGetDeviceAccessTokenBLMWithInvalidToken_Negative() {
        assertThatThrownBy(() -> generator.getDeviceAccessTokenBLM("invalid.token.here"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Parse device access token with wrong subject - Negative")
    void testGetDeviceAccessTokenBLMWithWrongSubject_Negative() {
        DeviceAccessTokenGenerator wrongSubjectGenerator = new DeviceAccessTokenGenerator(secretKey, "test-app", "wrong-subject");
        
        String tokenString = generator.generateDeviceAccessToken(
            createValidDeviceAccessTokenDALM().getDeviceTokenUid(),
            createValidDeviceAccessTokenDALM().getCreatedAt(),
            createValidDeviceAccessTokenDALM().getExpiresAt()
        );
        
        assertThatThrownBy(() -> wrongSubjectGenerator.getDeviceAccessTokenBLM(tokenString))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid token subject");
    }

    @Test
    @DisplayName("Parse device access token with wrong type - Negative")
    void testGetDeviceAccessTokenBLMWithWrongType_Negative() {
        DeviceAccessTokenGenerator wrongTypeGenerator = new DeviceAccessTokenGenerator(secretKey, "test-app", "device-access-token") {
            @Override
            public String generateDeviceAccessToken(java.util.UUID deviceTokenUid, Date createdAt, Date expiresAt) {
                return io.jsonwebtoken.Jwts.builder()
                    .issuer("test-app")
                    .subject("device-access-token")
                    .claim("deviceTokenUid", deviceTokenUid.toString())
                    .claim("type", "wrong-type")
                    .issuedAt(createdAt)
                    .expiration(expiresAt)
                    .signWith(secretKey)
                    .compact();
            }
        };
        
        String tokenString = wrongTypeGenerator.generateDeviceAccessToken(
            createValidDeviceAccessTokenDALM().getDeviceTokenUid(),
            createValidDeviceAccessTokenDALM().getCreatedAt(),
            createValidDeviceAccessTokenDALM().getExpiresAt()
        );
        
        assertThatThrownBy(() -> generator.getDeviceAccessTokenBLM(tokenString))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid token type");
    }

    @Test
    @DisplayName("Generate and parse round trip - Positive")
    void testGenerateAndParseRoundTrip_Positive() {
        String token = generator.generateDeviceAccessToken(
            createValidDeviceAccessTokenDALM().getDeviceTokenUid(),
            createValidDeviceAccessTokenDALM().getCreatedAt(),
            createValidDeviceAccessTokenDALM().getExpiresAt()
        );
        
        DeviceAccessTokenBLM parsed = generator.getDeviceAccessTokenBLM(token);
        
        assertThat(parsed.getDeviceTokenUid()).isEqualTo(createValidDeviceAccessTokenDALM().getDeviceTokenUid());
        assertThat(parsed.getToken()).isEqualTo(token);
        assertThat(parsed.getCreatedAt()).isNotNull();
        assertThat(parsed.getExpiresAt()).isNotNull();
    }

    @Test
    @DisplayName("Generate token with expired date - Negative")
    void testGenerateDeviceAccessTokenWithExpiredDate_Positive() {
        Date createdAt = new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 2);
        Date expiresAt = new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 1);
        
        String token = generator.generateDeviceAccessToken(
            createValidDeviceAccessTokenDALM().getDeviceTokenUid(),
            createdAt,
            expiresAt
        );
    
        assertThatThrownBy(() -> generator.getDeviceAccessTokenBLM(token))
            .isInstanceOf(RuntimeException.class);
      
    }

    @Test
    @DisplayName("Generate token with future creation date - Positive")
    void testGenerateDeviceAccessTokenWithFutureCreationDate_Positive() {
        Date createdAt = new Date(System.currentTimeMillis() + 1000L * 60 * 10);
        Date expiresAt = new Date(System.currentTimeMillis() + 1000L * 60 * 60);
        
        String token = generator.generateDeviceAccessToken(
            createValidDeviceAccessTokenDALM().getDeviceTokenUid(),
            createdAt,
            expiresAt
        );
        
        DeviceAccessTokenBLM result = generator.getDeviceAccessTokenBLM(token);
        
        assertThat(result.getCreatedAt().after(new Date())).isTrue();
    }

    @Test
    @DisplayName("Compare device token and device access token structure")
    void testCompareTokenStructures() {
        UUID deviceUid = createValidDeviceTokenDALM().getDeviceUid();
        UUID deviceTokenUid = createValidDeviceAccessTokenDALM().getDeviceTokenUid();
        Date createdAt = new Date();
        Date expiresAt = new Date(System.currentTimeMillis() + 3600000);
        
        DeviceTokenGenerator deviceTokenGenerator = new DeviceTokenGenerator(secretKey, "test-app", "device-token");
        String deviceToken = deviceTokenGenerator.generateDeviceToken(deviceUid, createdAt, expiresAt);
        
        String deviceAccessToken = generator.generateDeviceAccessToken(deviceTokenUid, createdAt, expiresAt);
        
        DeviceTokenBLM deviceTokenBLM = deviceTokenGenerator.getDeviceTokenBLM(deviceToken);
        DeviceAccessTokenBLM deviceAccessTokenBLM = generator.getDeviceAccessTokenBLM(deviceAccessToken);
        
        assertThat(deviceTokenBLM.getDeviceUid()).isEqualTo(deviceUid);
        assertThat(deviceAccessTokenBLM.getDeviceTokenUid()).isEqualTo(deviceTokenUid);
        assertThat(deviceToken).isNotEqualTo(deviceAccessToken);
    }
}