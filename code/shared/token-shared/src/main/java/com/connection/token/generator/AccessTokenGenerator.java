package com.connection.token.generator;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import com.connection.token.exception.AccessTokenValidateException;
import com.connection.token.model.AccessTokenBLM;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccessTokenGenerator {
    @NonNull
    private final SecretKey jwtSecretKey;

    @NonNull
    private final String appNameString;

    @NonNull
    private final String jwtSubjecString;

    public String generateAccessToken(UUID clientUuid, Date createdAtDate, Date expiresAtDate) {
        String token = Jwts.builder()
                .issuer(appNameString)
                .subject(jwtSubjecString)
                .claim("clientUid", clientUuid.toString())
                .issuedAt(createdAtDate)
                .expiration(expiresAtDate)
                .signWith(jwtSecretKey)
                .compact();
        return token;
    }

    public AccessTokenBLM getAccessTokenBLM(String token) {
        try{
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token);

        Claims claims = jws.getPayload();

        UUID clientUid = UUID.fromString(claims.get("clientUid", String.class));
        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();

        if (!jwtSubjecString.equals(claims.getSubject())) {
            throw new RuntimeException("Invalid token subject");
        }

        return new AccessTokenBLM(token, clientUid, issuedAt, expiration);
    }
    catch (RuntimeException e){
        throw new AccessTokenValidateException(token, "Invalid jwt");
    }
    }
}
