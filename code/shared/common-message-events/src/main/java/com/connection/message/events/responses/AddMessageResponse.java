
package com.connection.message.events.responses;

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
public class AddMessageResponse extends CommandResponse {
    private UUID messageUid;

    /** . */
    public static AddMessageResponse success(String correlationId,
            UUID messageUid) {
        return AddMessageResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(true)
                .messageUid(messageUid).timestamp(java.time.Instant.now())
                .build();
    }

    /** . */
    public static AddMessageResponse error(String correlationId, String error) {
        return AddMessageResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(false).error(error)
                .timestamp(java.time.Instant.now()).build();
    }
}
