package com.connection.token.generator;

import static com.connection.token.mother.TokenObjectMother.createValidAccessTokenDalm;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.connection.token.model.AccessTokenBlm;
import com.connection.token.model.AccessTokenDalm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Access Token Generator Tests")
class AccessTokenGeneratorTest {
    private AccessTokenGenerator generator;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        secretKey = Keys.hmacShaKeyFor("test-secret-key-1234567890-1234567890".getBytes());
        generator = new AccessTokenGenerator(secretKey, "test-app", "access-token");
    }

    @Test
    @DisplayName("Generate access token - Positive")
    void testGenerateAccessToken_Positive() {
        AccessTokenDalm dalM = createValidAccessTokenDalm();
        String token = generator.generateAccessToken(
            dalM.getClientUid(),
            dalM.getCreatedAt(),
            dalM.getExpiresAt()
        );
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Parse valid access token - Positive")
    void testGetAccessTokenBlm_Positive() {
        AccessTokenDalm dalM = createValidAccessTokenDalm();
        String tokenString = generator.generateAccessToken(
            dalM.getClientUid(),
            dalM.getCreatedAt(),
            dalM.getExpiresAt()
        );
        
        AccessTokenBlm result = generator.getAccessTokenBlm(tokenString);
        assertThat(result).isNotNull();
        assertThat(result.getClientUid()).isEqualTo(dalM.getClientUid());
        
        // Сравниваем время в секундах (миллисекунды игнорируются)
        assertThat(result.getCreatedAt().getTime() / 1000).isEqualTo(dalM.getCreatedAt().getTime() / 1000);
        assertThat(result.getExpiresAt().getTime() / 1000).isEqualTo(dalM.getExpiresAt().getTime() / 1000);
    }

    @Test
    @DisplayName("Parse invalid access token - Negative")
    void testGetAccessTokenBlmWithInvalidToken_Negative() {
        assertThatThrownBy(() -> generator.getAccessTokenBlm("invalid.token.here"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Parse access token with wrong subject - Negative")
    void testGetAccessTokenBlmWithWrongSubject_Negative() {
        AccessTokenGenerator wrongSubjectGenerator = new AccessTokenGenerator(secretKey, "test-app", "wrong-subject");
        AccessTokenDalm dalM = createValidAccessTokenDalm();
        
        String tokenString = generator.generateAccessToken(
            dalM.getClientUid(),
            dalM.getCreatedAt(),
            dalM.getExpiresAt()
        );
        
        assertThatThrownBy(() -> wrongSubjectGenerator.getAccessTokenBlm(tokenString))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Generate and parse round trip - Positive")
    void testGenerateAndParseRoundTrip_Positive() {
        AccessTokenDalm original = createValidAccessTokenDalm();
        
        String token = generator.generateAccessToken(
            original.getClientUid(),
            original.getCreatedAt(),
            original.getExpiresAt()
        );
        
        AccessTokenBlm parsed = generator.getAccessTokenBlm(token);
        
        assertThat(parsed.getClientUid()).isEqualTo(original.getClientUid());
        
        // Сравниваем время в секундах (миллисекунды игнорируются)
        assertThat(parsed.getCreatedAt().getTime() / 1000).isEqualTo(original.getCreatedAt().getTime() / 1000);
        assertThat(parsed.getExpiresAt().getTime() / 1000).isEqualTo(original.getExpiresAt().getTime() / 1000);
        assertThat(parsed.getToken()).isEqualTo(token);
    }

    @Test
    @DisplayName("Generate token with different dates - Positive")
    void testGenerateAccessTokenWithDifferentDates_Positive() {
        Date createdAt = new Date(System.currentTimeMillis() - 1000L * 60 * 10);
        Date expiresAt = new Date(System.currentTimeMillis() + 1000L * 60 * 30);
        
        String token = generator.generateAccessToken(
            createValidAccessTokenDalm().getClientUid(),
            createdAt,
            expiresAt
        );
        
        AccessTokenBlm result = generator.getAccessTokenBlm(token);
        
        // Сравниваем время в секундах (миллисекунды игнорируются)
        assertThat(result.getCreatedAt().getTime() / 1000).isEqualTo(createdAt.getTime() / 1000);
        assertThat(result.getExpiresAt().getTime() / 1000).isEqualTo(expiresAt.getTime() / 1000);
    }

    @Test
    @DisplayName("Generate token and verify time precision - Milliseconds ignored")
    void testGenerateAccessToken_MillisecondsIgnored() {
        // Создаем даты с разными миллисекундами
        long baseTime = System.currentTimeMillis();
        Date createdAt = new Date(baseTime + 123); // +123 мс
        Date expiresAt = new Date(baseTime + 3600000 + 456); // +456 мс
        
        String token = generator.generateAccessToken(
            createValidAccessTokenDalm().getClientUid(),
            createdAt,
            expiresAt
        );
        
        AccessTokenBlm result = generator.getAccessTokenBlm(token);
        
        // Проверяем, что миллисекунды игнорируются при сравнении
        assertThat(result.getCreatedAt().getTime() / 1000).isEqualTo(createdAt.getTime() / 1000);
        assertThat(result.getExpiresAt().getTime() / 1000).isEqualTo(expiresAt.getTime() / 1000);
        
        // Но фактически миллисекунды могут быть разными
        assertThat(result.getCreatedAt().getTime()).isNotEqualTo(createdAt.getTime());
        assertThat(result.getExpiresAt().getTime()).isNotEqualTo(expiresAt.getTime());
    }
}