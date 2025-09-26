// DeviceTokenGenerator.java
package com.connection.device.token.generator;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import com.connection.device.token.model.DeviceTokenBLM;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeviceTokenGenerator {
    @NonNull
    private final SecretKey jwtSecretKey;

    @NonNull
    private final String appNameString;

    @NonNull
    private final String jwtSubjectString;

    public String generateDeviceToken(UUID deviceUid, Date createdAt, Date expiresAt) {
        String token = Jwts.builder()
                .issuer(appNameString)
                .subject(jwtSubjectString)
                .claim("deviceUid", deviceUid.toString())
                .claim("type", "device_token")
                .issuedAt(createdAt)
                .expiration(expiresAt)
                .signWith(jwtSecretKey)
                .compact();
        return token;
    }

    public DeviceTokenBLM getDeviceTokenBLM(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token);

        Claims claims = jws.getPayload();

        UUID deviceUid = UUID.fromString(claims.get("deviceUid", String.class));
        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();

        if (!jwtSubjectString.equals(claims.getSubject())) {
            throw new RuntimeException("Invalid token subject");
        }

        if (!"device_token".equals(claims.get("type", String.class))) {
            throw new RuntimeException("Invalid token type");
        }

        return DeviceTokenBLM.builder()
                .token(token)
                .deviceUid(deviceUid)
                .createdAt(issuedAt)
                .expiresAt(expiration)
                .build();
    }
}