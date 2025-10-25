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

import com.connection.device.token.model.DeviceTokenBLM;

import io.jsonwebtoken.security.Keys;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Device Token Generator Tests")
class DeviceTokenGeneratorTest {

    private DeviceTokenGenerator generator;
    private SecretKey secretKey;
    private UUID tokenUid;

    @BeforeEach
    void setUp() {
        secretKey = Keys.hmacShaKeyFor("test-secret-key-1234567890-1234567890".getBytes());
        generator = new DeviceTokenGenerator(secretKey, "test-app", "device-token");
        tokenUid = UUID.randomUUID();
    }

    @Test
    @DisplayName("Generate device token - Positive")
    void testGenerateDeviceToken_Positive() {
        String token = generator.generateDeviceToken(
            createValidDeviceTokenDALM().getDeviceUid(),
            tokenUid,
            createValidDeviceTokenDALM().getCreatedAt(),
            createValidDeviceTokenDALM().getExpiresAt()
        );
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Parse valid device token - Positive")
    void testGetDeviceTokenBLM_Positive() {
        String tokenString = generator.generateDeviceToken(
            createValidDeviceTokenDALM().getDeviceUid(),
            tokenUid,
            createValidDeviceTokenDALM().getCreatedAt(),
            createValidDeviceTokenDALM().getExpiresAt()
        );
        
        DeviceTokenBLM result = generator.getDeviceTokenBLM(tokenString);
        assertThat(result).isNotNull();
        assertThat(result.getDeviceUid()).isEqualTo(createValidDeviceTokenDALM().getDeviceUid());
        assertThat(result.getToken()).isEqualTo(tokenString);
    }

    @Test
    @DisplayName("Parse invalid device token - Negative")
    void testGetDeviceTokenBLMWithInvalidToken_Negative() {
        assertThatThrownBy(() -> generator.getDeviceTokenBLM("invalid.token.here"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Parse device token with wrong subject - Negative")
    void testGetDeviceTokenBLMWithWrongSubject_Negative() {
        DeviceTokenGenerator wrongSubjectGenerator = new DeviceTokenGenerator(secretKey, "test-app", "wrong-subject");
        
        String tokenString = generator.generateDeviceToken(
            createValidDeviceTokenDALM().getDeviceUid(),
            tokenUid,
            createValidDeviceTokenDALM().getCreatedAt(),
            createValidDeviceTokenDALM().getExpiresAt()
        );
        
        assertThatThrownBy(() -> wrongSubjectGenerator.getDeviceTokenBLM(tokenString))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid token subject");
    }

    @Test
    @DisplayName("Parse device token with wrong type - Negative")
    void testGetDeviceTokenBLMWithWrongType_Negative() {
        DeviceTokenGenerator wrongTypeGenerator = new DeviceTokenGenerator(secretKey, "test-app", "device-token") {
            @Override
            public String generateDeviceToken(java.util.UUID deviceUid, java.util.UUID tokenUid, Date createdAt, Date expiresAt) {
                return io.jsonwebtoken.Jwts.builder()
                    .issuer("test-app")
                    .subject("device-token")
                    .claim("deviceUid", deviceUid.toString())
                    .claim("type", "wrong-type")
                    .issuedAt(createdAt)
                    .expiration(expiresAt)
                    .signWith(secretKey)
                    .compact();
            }
        };
        
        String tokenString = wrongTypeGenerator.generateDeviceToken(
            createValidDeviceTokenDALM().getDeviceUid(),
            tokenUid,
            createValidDeviceTokenDALM().getCreatedAt(),
            createValidDeviceTokenDALM().getExpiresAt()
        );
        
        assertThatThrownBy(() -> generator.getDeviceTokenBLM(tokenString))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Generate and parse round trip - Positive")
    void testGenerateAndParseRoundTrip_Positive() {
        String token = generator.generateDeviceToken(
            createValidDeviceTokenDALM().getDeviceUid(),
            tokenUid,
            createValidDeviceTokenDALM().getCreatedAt(),
            createValidDeviceTokenDALM().getExpiresAt()
        );
        
        DeviceTokenBLM parsed = generator.getDeviceTokenBLM(token);
        
        assertThat(parsed.getDeviceUid()).isEqualTo(createValidDeviceTokenDALM().getDeviceUid());
        assertThat(parsed.getToken()).isEqualTo(token);
        assertThat(parsed.getCreatedAt()).isNotNull();
        assertThat(parsed.getExpiresAt()).isNotNull();
    }

    @Test
    @DisplayName("Generate token with different dates - Positive")
    void testGenerateDeviceTokenWithDifferentDates_Positive() {
        Date createdAt = new Date(System.currentTimeMillis() - 1000L * 60 * 10);
        Date expiresAt = new Date(System.currentTimeMillis() + 1000L * 60 * 30);
        
        String token = generator.generateDeviceToken(
            createValidDeviceTokenDALM().getDeviceUid(),
            tokenUid,
            createdAt,
            expiresAt
        );
        
        DeviceTokenBLM result = generator.getDeviceTokenBLM(token);
        
        assertThat(result.getDeviceUid()).isEqualTo(result.getDeviceUid());
        assertThat(result.getToken()).isEqualTo(token);
    }

    @Test
    @DisplayName("Generate multiple tokens for same device - Positive")
    void testGenerateMultipleTokensForSameDevice_Positive() {
        UUID deviceUid = createValidDeviceTokenDALM().getDeviceUid();
        Date createdAt1 = new Date(System.currentTimeMillis() - 1000L * 60 * 5);
        Date expiresAt1 = new Date(System.currentTimeMillis() + 1000L * 60 * 15);
        
        Date createdAt2 = new Date(System.currentTimeMillis() - 1000L * 60 * 2);
        Date expiresAt2 = new Date(System.currentTimeMillis() + 1000L * 60 * 25);

        UUID tokeUuid2 = UUID.randomUUID();
        
        String token1 = generator.generateDeviceToken(deviceUid, tokenUid,createdAt1, expiresAt1);
        String token2 = generator.generateDeviceToken(deviceUid, tokeUuid2,createdAt2, expiresAt2);
        
        DeviceTokenBLM result1 = generator.getDeviceTokenBLM(token1);
        DeviceTokenBLM result2 = generator.getDeviceTokenBLM(token2);
        
        assertThat(result1.getDeviceUid()).isEqualTo(deviceUid);
        assertThat(result2.getDeviceUid()).isEqualTo(deviceUid);
        assertThat(token1).isNotEqualTo(token2);
    }
}