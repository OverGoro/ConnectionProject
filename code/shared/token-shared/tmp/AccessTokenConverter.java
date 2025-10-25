package com.connection.token.converter;

import com.connection.token.generator.AccessTokenGenerator;
import com.connection.token.model.AccessTokenBLM;
import com.connection.token.model.AccessTokenDALM;
import com.connection.token.model.AccessTokenDTO;

import io.jsonwebtoken.JwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccessTokenConverter {
    @NonNull
    private final AccessTokenGenerator accessTokenGenerator;

    public AccessTokenBLM toBLM(AccessTokenDALM dalm) {
        try {
            String token = accessTokenGenerator.generateAccessToken(dalm.getClientUID(), dalm.getCreatedAt(),
                    dalm.getExpiresAt());
            return new AccessTokenBLM(token,
                    dalm.getClientUID(),
                    dalm.getCreatedAt(),
                    dalm.getExpiresAt());
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Malformed JWT token", e);
        }
    }

    public AccessTokenBLM toBLM(AccessTokenDTO dto) {
        try {
            AccessTokenBLM blm = accessTokenGenerator.getAccessTokenBLM(dto.getToken());
            return blm;
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Malformed JWT token", e);
        }
    }

    public AccessTokenDTO toDTO(AccessTokenBLM blm) {
        return new AccessTokenDTO(blm.getToken());
    }

    public AccessTokenDALM toDALM(AccessTokenBLM blm) {
        return new AccessTokenDALM(blm.getClientUID(), blm.getCreatedAt(), blm.getExpiresAt());
    }
}
