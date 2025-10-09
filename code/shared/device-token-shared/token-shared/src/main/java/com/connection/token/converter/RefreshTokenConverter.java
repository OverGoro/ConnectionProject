package com.connection.token.converter;

import com.connection.token.generator.RefreshTokenGenerator;
import com.connection.token.model.RefreshTokenBLM;
import com.connection.token.model.RefreshTokenDALM;
import com.connection.token.model.RefreshTokenDTO;

import io.jsonwebtoken.JwtException;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RefreshTokenConverter {
    @Nonnull
    private final RefreshTokenGenerator tokenGenerator;
    public RefreshTokenBLM toBLM(RefreshTokenDALM dalm) {
        String token  = tokenGenerator.generateRefreshToken(dalm);
        return new RefreshTokenBLM(token,
                dalm.getUid(),
                dalm.getClientUID(),
                dalm.getCreatedAt(),
                dalm.getExpiresAt());
    }

    public RefreshTokenBLM toBLM(RefreshTokenDTO dto) {
        try {
            RefreshTokenBLM tokenBLM = tokenGenerator.getRefreshToken(dto.getToken());
            return tokenBLM;
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Malformed JWT token", e);
        }
    }

    public RefreshTokenDTO toDTO(RefreshTokenBLM blm){
        return new RefreshTokenDTO(blm.getToken());
    }

    public RefreshTokenDALM toDALM(RefreshTokenBLM blm){
        return new RefreshTokenDALM(blm.getToken(), 
                                    blm.getUid(),
                                    blm.getClientUID(), 
                                    blm.getCreatedAt(), 
                                    blm.getExpiresAt());
    }
}

