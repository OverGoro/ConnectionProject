package com.connection.token.converter;

import com.connection.token.generator.RefreshTokenGenerator;
import com.connection.token.model.RefreshTokenBlm;
import com.connection.token.model.RefreshTokenDalm;
import com.connection.token.model.RefreshTokenDto;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

/** . */
@RequiredArgsConstructor
public class RefreshTokenConverter {
    @Nonnull
    private final RefreshTokenGenerator tokenGenerator;

    /** . */
    public RefreshTokenBlm toBlm(RefreshTokenDalm dalm) {
        String token = tokenGenerator.generateRefreshToken(dalm);
        return new RefreshTokenBlm(token, dalm.getUid(), dalm.getClientUid(),
                dalm.getCreatedAt(), dalm.getExpiresAt());
    }

    /** . */
    public RefreshTokenBlm toBlm(RefreshTokenDto dto) {
        try {
            RefreshTokenBlm tokenBlm =
                    tokenGenerator.getRefreshToken(dto.getToken());
            return tokenBlm;
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(),
                    e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Malformed JWT token", e);
        }
    }

    /** . */
    public RefreshTokenDto toDto(RefreshTokenBlm blm) {
        return new RefreshTokenDto(blm.getToken());
    }

    /** . */
    public RefreshTokenDalm toDalm(RefreshTokenBlm blm) {
        return new RefreshTokenDalm(blm.getToken(), blm.getUid(),
                blm.getClientUid(), blm.getCreatedAt(), blm.getExpiresAt());
    }
}

