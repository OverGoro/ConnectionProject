package com.connection.token.generator;

import static com.connection.token.mother.TokenObjectMother.createValidRefreshTokenDalm;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.connection.token.model.RefreshTokenDalm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Refresh Token Generator Tests")
class RefreshTokenGeneratorTest {

    private RefreshTokenGenerator generator;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        secretKey = Keys.hmacShaKeyFor("test-secret-key-1234567890-1234567890".getBytes());
        generator = new RefreshTokenGenerator(secretKey, "test-app", "refresh-token");
    }

    @Test
    @DisplayName("Generate refresh token from Dalm - Positive")
    void testGenerateRefreshTokenFromDalm_Positive() {
        RefreshTokenDalm dalM = createValidRefreshTokenDalm();
        String token = generator.generateRefreshToken(dalM);
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Generate refresh token from parameters - Positive")
    void testGenerateRefreshTokenFromParameters_Positive() {
        String token = generator.generateRefreshToken(
            createValidRefreshTokenDalm().getUid(),
            createValidRefreshTokenDalm().getClientUid(),
            createValidRefreshTokenDalm().getCreatedAt(),
            createValidRefreshTokenDalm().getExpiresAt()
        );
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Parse valid refresh token - Positive")
    void testGetRefreshToken_Positive() {
        RefreshTokenDalm dalM = createValidRefreshTokenDalm();
        String tokenString = generator.generateRefreshToken(dalM);
        
        var result = generator.getRefreshToken(tokenString);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(dalM.getUid());
        assertThat(result.getClientUid()).isEqualTo(dalM.getClientUid());
    }

    @Test
    @DisplayName("Parse invalid refresh token - Negative")
    void testGetRefreshTokenWithInvalidToken_Negative() {
        assertThatThrownBy(() -> generator.getRefreshToken("invalid.token.here"))
                .isInstanceOf(RuntimeException.class);
    }
}