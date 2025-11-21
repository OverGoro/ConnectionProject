package com.connection.token.converter;

import static com.connection.token.mother.TokenObjectMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import com.connection.token.model.AccessTokenBlm;
import com.connection.token.model.AccessTokenDalm;
import com.connection.token.model.AccessTokenDto;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Access Token Converter Tests")
class AccessTokenConverterTest {

    @Mock
    private AccessTokenGenerator accessTokenGenerator;

    @InjectMocks
    private AccessTokenConverter converter;

    private AccessTokenDalm testDalm;
    private AccessTokenDto testDto;
    private AccessTokenBlm testBlm;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testDalm = createValidAccessTokenDalm();
        testDto = createValidAccessTokenDto();
        testBlm = createValidAccessTokenBlm();

        when(accessTokenGenerator.generateAccessToken(any(UUID.class), any(Date.class), any(Date.class)))
                .thenReturn("generated-access-token");
        when(accessTokenGenerator.getAccessTokenBlm(any(String.class))).thenReturn(testBlm);
    }

    @Test
    @DisplayName("Convert Dalm to Blm - Positive")
    void testToBlmFromDalm_Positive() {
        AccessTokenBlm result = converter.toBlm(testDalm);
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("generated-access-token");
    }

    @Test
    @DisplayName("Convert Dto to Blm - Positive")
    void testToBlmFromDto_Positive() {
        AccessTokenBlm result = converter.toBlm(testDto);
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(testBlm.getToken());
    }

    @Test
    @DisplayName("Convert Blm to Dto - Positive")
    void testToDtoFromBlm_Positive() {
        AccessTokenDto result = converter.toDto(testBlm);
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(testBlm.getToken());
    }

    @Test
    @DisplayName("Convert Blm to Dalm - Positive")
    void testToDalmFromBlm_Positive() {
        AccessTokenDalm result = converter.toDalm(testBlm);
        assertThat(result).isNotNull();
        assertThat(result.getClientUid()).isEqualTo(testBlm.getClientUid());
        assertThat(result.getCreatedAt()).isEqualTo(testBlm.getCreatedAt());
        assertThat(result.getExpiresAt()).isEqualTo(testBlm.getExpiresAt());
    }
}