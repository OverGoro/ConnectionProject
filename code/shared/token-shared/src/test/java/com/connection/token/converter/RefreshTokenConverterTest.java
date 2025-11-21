package com.connection.token.converter;

import static com.connection.token.mother.TokenObjectMother.createValidRefreshTokenBlm;
import static com.connection.token.mother.TokenObjectMother.createValidRefreshTokenDalm;
import static com.connection.token.mother.TokenObjectMother.createValidRefreshTokenDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import com.connection.token.generator.RefreshTokenGenerator;
import com.connection.token.model.RefreshTokenBlm;
import com.connection.token.model.RefreshTokenDalm;
import com.connection.token.model.RefreshTokenDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Refresh Token Converter Tests")
class RefreshTokenConverterTest {

    @Mock
    private RefreshTokenGenerator tokenGenerator;

    @InjectMocks
    private RefreshTokenConverter converter;

    private RefreshTokenDalm testDalm;
    private RefreshTokenDto testDto;
    private RefreshTokenBlm testBlm;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testDalm = createValidRefreshTokenDalm();
        testDto = createValidRefreshTokenDto();
        testBlm = createValidRefreshTokenBlm();

        when(tokenGenerator.generateRefreshToken(any(RefreshTokenDalm.class))).thenReturn("generated-token");
        when(tokenGenerator.getRefreshToken(any(String.class))).thenReturn(testBlm);
    }

    @Test
    @DisplayName("Convert Dalm to Blm - Positive")
    void testToBlmFromDalm_Positive() {
        RefreshTokenBlm result = converter.toBlm(testDalm);
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("generated-token");
    }

    @Test
    @DisplayName("Convert Dto to Blm - Positive")
    void testToBlmFromDto_Positive() {
        RefreshTokenBlm result = converter.toBlm(testDto);
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(testBlm.getToken());
    }

    @Test
    @DisplayName("Convert Blm to Dto - Positive")
    void testToDtoFromBlm_Positive() {
        RefreshTokenDto result = converter.toDto(testBlm);
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(testBlm.getToken());
    }

    @Test
    @DisplayName("Convert Blm to Dalm - Positive")
    void testToDalmFromBlm_Positive() {
        RefreshTokenDalm result = converter.toDalm(testBlm);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(testBlm.getUid());
        assertThat(result.getClientUid()).isEqualTo(testBlm.getClientUid());
        assertThat(result.getCreatedAt()).isEqualTo(testBlm.getCreatedAt());
        assertThat(result.getExpiresAt()).isEqualTo(testBlm.getExpiresAt());
    }
}