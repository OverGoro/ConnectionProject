package com.connection.token.converter;

import static com.connection.token.mother.TokenObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.connection.token.generator.RefreshTokenGenerator;
import com.connection.token.model.RefreshTokenBLM;
import com.connection.token.model.RefreshTokenDALM;
import com.connection.token.model.RefreshTokenDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Refresh Token Converter Tests")
class RefreshTokenConverterTest {

    @Mock
    private RefreshTokenGenerator tokenGenerator;

    @InjectMocks
    private RefreshTokenConverter converter;

    private RefreshTokenDALM testDALM;
    private RefreshTokenDTO testDTO;
    private RefreshTokenBLM testBLM;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testDALM = createValidRefreshTokenDALM();
        testDTO = createValidRefreshTokenDTO();
        testBLM = createValidRefreshTokenBLM();

        when(tokenGenerator.generateRefreshToken(any(RefreshTokenDALM.class))).thenReturn("generated-token");
        when(tokenGenerator.getRefreshToken(any(String.class))).thenReturn(testBLM);
    }

    @Test
    @DisplayName("Convert DALM to BLM - Positive")
    void testToBLMFromDALM_Positive() {
        RefreshTokenBLM result = converter.toBLM(testDALM);
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("generated-token");
    }

    @Test
    @DisplayName("Convert DTO to BLM - Positive")
    void testToBLMFromDTO_Positive() {
        RefreshTokenBLM result = converter.toBLM(testDTO);
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(testBLM.getToken());
    }

    @Test
    @DisplayName("Convert BLM to DTO - Positive")
    void testToDTOFromBLM_Positive() {
        RefreshTokenDTO result = converter.toDTO(testBLM);
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(testBLM.getToken());
    }

    @Test
    @DisplayName("Convert BLM to DALM - Positive")
    void testToDALMFromBLM_Positive() {
        RefreshTokenDALM result = converter.toDALM(testBLM);
        assertThat(result).isNotNull();
        assertThat(result.getUid()).isEqualTo(testBLM.getUid());
        assertThat(result.getClientUID()).isEqualTo(testBLM.getClientUID());
        assertThat(result.getCreatedAt()).isEqualTo(testBLM.getCreatedAt());
        assertThat(result.getExpiresAt()).isEqualTo(testBLM.getExpiresAt());
    }
}