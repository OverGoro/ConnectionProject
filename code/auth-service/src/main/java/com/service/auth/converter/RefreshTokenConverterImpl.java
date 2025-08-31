package com.service.auth.converter;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.service.auth.model.RefreshTokenBLM;
import com.service.auth.model.RefreshTokenDALM;
import com.service.auth.model.RefreshTokenDTO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshTokenConverterImpl {

    @NotNull
    @Qualifier("jwtSecretKey")
    private final SecretKey jwtSecretKey;

    @NotNull
    @Qualifier("appName")
    private final String appNameString;
    
    @NotNull
    @Qualifier("jwtSubject")
    private final String jwtSubjecString;

    public RefreshTokenBLM toBLM(RefreshTokenDALM dalm) {
        String token = Jwts.builder()
                .issuer(appNameString)
                .subject(jwtSubjecString)
                .claim("uid", dalm.getUid().toString())
                .claim("clientUid", dalm.getClientUID().toString())
                .issuedAt(dalm.getCreatedAt())
                .expiration(dalm.getExpiresAt())
                .signWith(jwtSecretKey)
                .compact();
        return new RefreshTokenBLM(token,
                dalm.getUid(),
                dalm.getClientUID(),
                dalm.getCreatedAt(),
                dalm.getExpiresAt());
    }

    public RefreshTokenBLM toBLM(RefreshTokenDTO dto) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(dto.getToken());

            Claims claims = jws.getPayload();

            UUID uid = UUID.fromString(claims.get("uid", String.class));
            UUID clientUid = UUID.fromString(claims.get("clientUid", String.class));
            Date issuedAt = claims.getIssuedAt();
            Date expiration = claims.getExpiration();

            if (!jwtSubjecString.equals(claims.getSubject())) {
                throw new RuntimeException("Invalid token subject");
            }
            
            return new RefreshTokenBLM(dto.getToken(), uid, clientUid, issuedAt, expiration);
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
        return new RefreshTokenDALM(blm.getUid(), blm.getClientUID(), blm.getCreatedAt(), blm.getExpiresAt());
    }
}

