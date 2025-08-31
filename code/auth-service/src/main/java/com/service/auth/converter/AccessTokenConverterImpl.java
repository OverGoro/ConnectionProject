package com.service.auth.converter;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.service.auth.model.AccessTokenBLM;
import com.service.auth.model.AccessTokenDALM;
import com.service.auth.model.AccessTokenDTO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccessTokenConverterImpl {
    @Qualifier("jwtSecretKey")
    SecretKey jwtSecretKey;

    @Qualifier("appName")
    String appNameString;
    
    @Qualifier("jwtSubject")
    String jwtSubjecString;
    

    public AccessTokenBLM toBLM(AccessTokenDALM dalm) {
        String token = Jwts.builder()
                .issuer(appNameString)
                .subject(jwtSubjecString)
                .claim("clientUid", dalm.getClientUID().toString())
                .issuedAt(dalm.getCreatedAt())
                .expiration(dalm.getExpiresAt())
                .signWith(jwtSecretKey)
                .compact();
        return new AccessTokenBLM(token,
                dalm.getClientUID(),
                dalm.getCreatedAt(),
                dalm.getExpiresAt());
    }

    public AccessTokenBLM toBLM(AccessTokenDTO dto) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(dto.getToken());

            Claims claims = jws.getPayload();

            UUID clientUid = UUID.fromString(claims.get("clientUid", String.class));
            Date issuedAt = claims.getIssuedAt();
            Date expiration = claims.getExpiration();

            if (!jwtSubjecString.equals(claims.getSubject())) {
                throw new RuntimeException("Invalid token subject");
            }
            
            return new AccessTokenBLM(dto.getToken(), clientUid, issuedAt, expiration);
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Malformed JWT token", e);
        }
    }

    public AccessTokenDTO toDTO(AccessTokenBLM blm){
        return new AccessTokenDTO(blm.getToken());
    }

    public AccessTokenDALM toDALM(AccessTokenBLM blm){
        return new AccessTokenDALM(blm.getClientUID(), blm.getCreatedAt(), blm.getExpiresAt());
    }
}
