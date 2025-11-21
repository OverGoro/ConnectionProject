package com.connection.device.auth.events.responses;

import com.connection.common.events.CommandResponse;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** . */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TokenValidationResponse extends CommandResponse {
    private boolean isValid;
    private UUID deviceUid;
    private String tokenType;
    private String errorDetails;

    /** . */
    public static TokenValidationResponse valid(String correlationId,
            UUID deviceUid, String tokenType) {
        return TokenValidationResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(true).isValid(true)
                .deviceUid(deviceUid).tokenType(tokenType)
                .timestamp(java.time.Instant.now()).build();
    }

    /** . */
    public static TokenValidationResponse invalid(String correlationId,
            String error, String tokenType) {
        return TokenValidationResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(true).isValid(false)
                .tokenType(tokenType).errorDetails(error)
                .timestamp(java.time.Instant.now()).build();
    }

    /** . */
    public static TokenValidationResponse error(String correlationId,
            String error) {
        return TokenValidationResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(false).isValid(false)
                .error(error).timestamp(java.time.Instant.now()).build();
    }
}
