package com.connection.token.converter;

import static com.connection.token.mother.TokenObjectMother.createValidAccessTokenBLM;
import static com.connection.token.mother.TokenObjectMother.createValidAccessTokenDALM;
import static com.connection.token.mother.TokenObjectMother.createValidAccessTokenDTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.connection.token.generator.AccessTokenGenerator;
import com.connection.token.model.AccessTokenBLM;
import com.connection.token.model.AccessTokenDALM;
import com.connection.token.model.AccessTokenDTO;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Access Token Converter Tests")
class AccessTokenConverterTest {

    @Mock
    private AccessTokenGenerator accessTokenGenerator;

    @InjectMocks
    private AccessTokenConverter converter;

    private AccessTokenDALM testDALM;
    private AccessTokenDTO testDTO;
    private AccessTokenBLM testBLM;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testDALM = createValidAccessTokenDALM();
        testDTO = createValidAccessTokenDTO();
        testBLM = createValidAccessTokenBLM();

        when(accessTokenGenerator.generateAccessToken(any(UUID.class), any(Date.class), any(Date.class)))
                .thenReturn("generated-access-token");
        when(accessTokenGenerator.getAccessTokenBLM(any(String.class))).thenReturn(testBLM);
    }

    @Test
    @DisplayName("Convert DALM to BLM - Positive")
    void testToBLMFromDALM_Positive() {
        AccessTokenBLM result = converter.toBLM(testDALM);
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("generated-access-token");
    }

    @Test
    @DisplayName("Convert DTO to BLM - Positive")
    void testToBLMFromDTO_Positive() {
        AccessTokenBLM result = converter.toBLM(testDTO);
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(testBLM.getToken());
    }

    @Test
    @DisplayName("Convert BLM to DTO - Positive")
    void testToDTOFromBLM_Positive() {
        AccessTokenDTO result = converter.toDTO(testBLM);
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(testBLM.getToken());
    }

    @Test
    @DisplayName("Convert BLM to DALM - Positive")
    void testToDALMFromBLM_Positive() {
        AccessTokenDALM result = converter.toDALM(testBLM);
        assertThat(result).isNotNull();
        assertThat(result.getClientUID()).isEqualTo(testBLM.getClientUID());
        assertThat(result.getCreatedAt()).isEqualTo(testBLM.getCreatedAt());
        assertThat(result.getExpiresAt()).isEqualTo(testBLM.getExpiresAt());
    }
}