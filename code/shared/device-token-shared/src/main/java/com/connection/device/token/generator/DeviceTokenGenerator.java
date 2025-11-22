
package com.connection.device.token.generator;

import com.connection.device.token.model.DeviceTokenBlm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** . */
@RequiredArgsConstructor
public class DeviceTokenGenerator {
    @NonNull
    private final SecretKey jwtSecretKey;

    @NonNull
    private final String appNameString;

    @NonNull
    private final String jwtSubjectString;

    /** . */
    public String generateDeviceToken(UUID deviceUid, UUID deviceTokenUid,
            Date createdAt, Date expiresAt) {
        String token =
                Jwts.builder().issuer(appNameString).subject(jwtSubjectString)
                        .claim("deviceTokenUid", deviceTokenUid)
                        .claim("deviceUid", deviceUid.toString())
                        .claim("type", "device_token").issuedAt(createdAt)
                        .expiration(expiresAt).signWith(jwtSecretKey).compact();
        return token;
    }

    /** . */
    public DeviceTokenBlm getDeviceTokenBlm(String token) {
        Jws<Claims> jws = Jwts.parser().verifyWith(jwtSecretKey).build()
                .parseSignedClaims(token);

        Claims claims = jws.getPayload();

        UUID deviceUid = UUID.fromString(claims.get("deviceUid", String.class));
        UUID deviceTokenUid =
                UUID.fromString(claims.get("deviceTokenUid", String.class));
        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();

        if (!jwtSubjectString.equals(claims.getSubject())) {
            throw new RuntimeException("Invalid token subject");
        }

        if (!"device_token".equals(claims.get("type", String.class))) {
            throw new RuntimeException("Invalid token type");
        }

        return DeviceTokenBlm.builder().token(token).uid(deviceTokenUid)
                .deviceUid(deviceUid).createdAt(issuedAt).expiresAt(expiration)
                .build();
    }
}
