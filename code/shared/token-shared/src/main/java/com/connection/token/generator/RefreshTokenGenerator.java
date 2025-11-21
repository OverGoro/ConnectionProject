package com.connection.token.generator;

import com.connection.token.exception.RefreshTokenValidateException;
import com.connection.token.model.RefreshTokenBlm;
import com.connection.token.model.RefreshTokenDalm;
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
public class RefreshTokenGenerator {
    @NonNull
    private final SecretKey jwtSecretKey;

    @NonNull
    private final String appNameString;

    @NonNull
    private final String jwtSubjecString;

    /** . */
    public String generateRefreshToken(RefreshTokenDalm dalm) {
        String token = Jwts.builder().issuer(appNameString)
                .subject(jwtSubjecString).claim("uid", dalm.getUid().toString())
                .claim("clientUid", dalm.getClientUid().toString())
                .issuedAt(dalm.getCreatedAt()).expiration(dalm.getExpiresAt())
                .signWith(jwtSecretKey).compact();
        return token;
    }

    /** . */
    public String generateRefreshToken(UUID uid, UUID clientUuid,
            Date createdAt, Date expiresAt) {
        String token = Jwts.builder().issuer(appNameString)
                .subject(jwtSubjecString).claim("uid", uid.toString())
                .claim("clientUid", clientUuid.toString()).issuedAt(createdAt)
                .expiration(expiresAt).signWith(jwtSecretKey).compact();
        return token;
    }

    /** . */
    public RefreshTokenBlm getRefreshToken(String token) {
        try {
            Jws<Claims> jws = Jwts.parser().verifyWith(jwtSecretKey).build()
                    .parseSignedClaims(token);

            Claims claims = jws.getPayload();

            UUID uid = UUID.fromString(claims.get("uid", String.class));
            UUID clientUid =
                    UUID.fromString(claims.get("clientUid", String.class));
            Date issuedAt = claims.getIssuedAt();
            Date expiration = claims.getExpiration();

            if (!jwtSubjecString.equals(claims.getSubject())) {
                throw new RuntimeException("Invalid token subject");
            }

            return new RefreshTokenBlm(token, uid, clientUid, issuedAt,
                    expiration);
        } catch (RuntimeException e) {
            throw new RefreshTokenValidateException(token, "Invalid jwt");
        }
    }
}
