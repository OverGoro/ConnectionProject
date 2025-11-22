package com.connection.token.converter;

import com.connection.token.generator.AccessTokenGenerator;
import com.connection.token.model.AccessTokenBlm;
import com.connection.token.model.AccessTokenDalm;
import com.connection.token.model.AccessTokenDto;
import io.jsonwebtoken.JwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** . */
@RequiredArgsConstructor
public class AccessTokenConverter {
    @NonNull
    private final AccessTokenGenerator accessTokenGenerator;

    /** . */
    public AccessTokenBlm toBlm(AccessTokenDalm dalm) {
        try {
            String token = accessTokenGenerator.generateAccessToken(
                    dalm.getClientUid(), dalm.getCreatedAt(),
                    dalm.getExpiresAt());
            return new AccessTokenBlm(token, dalm.getClientUid(),
                    dalm.getCreatedAt(), dalm.getExpiresAt());
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(),
                    e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Malformed JWT token", e);
        }
    }

    /** . */
    public AccessTokenBlm toBlm(AccessTokenDto dto) {
        try {
            AccessTokenBlm blm =
                    accessTokenGenerator.getAccessTokenBlm(dto.getToken());
            return blm;
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(),
                    e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Malformed JWT token", e);
        }
    }

    /** . */
    public AccessTokenDto toDto(AccessTokenBlm blm) {
        return new AccessTokenDto(blm.getToken());
    }

    /** . */
    public AccessTokenDalm toDalm(AccessTokenBlm blm) {
        return new AccessTokenDalm(blm.getClientUid(), blm.getCreatedAt(),
                blm.getExpiresAt());
    }
}
