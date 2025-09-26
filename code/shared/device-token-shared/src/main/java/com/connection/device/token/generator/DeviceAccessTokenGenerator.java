// DeviceAccessTokenGenerator.java
package com.connection.device.token.generator;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import com.connection.device.token.model.DeviceAccessTokenBLM;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeviceAccessTokenGenerator {
    @NonNull
    private final SecretKey jwtSecretKey;

    @NonNull
    private final String appNameString;

    @NonNull
    private final String jwtSubjectString;

    public String generateDeviceAccessToken(UUID deviceTokenUid, Date createdAt, Date expiresAt) {
        String token = Jwts.builder()
                .issuer(appNameString)
                .subject(jwtSubjectString)
                .claim("deviceTokenUid", deviceTokenUid.toString())
                .claim("type", "device_access_token")
                .issuedAt(createdAt)
                .expiration(expiresAt)
                .signWith(jwtSecretKey)
                .compact();
        return token;
    }

    public DeviceAccessTokenBLM getDeviceAccessTokenBLM(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token);

        Claims claims = jws.getPayload();

        UUID deviceTokenUid = UUID.fromString(claims.get("deviceTokenUid", String.class));
        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();

        if (!jwtSubjectString.equals(claims.getSubject())) {
            throw new RuntimeException("Invalid token subject");
        }

        if (!"device_access_token".equals(claims.get("type", String.class))) {
            throw new RuntimeException("Invalid token type");
        }

        return DeviceAccessTokenBLM.builder()
                .token(token)
                .deviceTokenUid(deviceTokenUid)
                .createdAt(issuedAt)
                .expiresAt(expiration)
                .build();
    }
}