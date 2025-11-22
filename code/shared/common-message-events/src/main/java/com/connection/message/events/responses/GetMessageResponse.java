
package com.connection.message.events.responses;

import com.connection.common.events.CommandResponse;
import com.connection.message.model.MessageDto;
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
public class GetMessageResponse extends CommandResponse {
    private MessageDto message;

    /** . */
    public static GetMessageResponse success(String correlationId,
            MessageDto message) {
        return GetMessageResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(true).message(message)
                .timestamp(java.time.Instant.now()).build();
    }

    /** . */
    public static GetMessageResponse error(String correlationId, String error) {
        return GetMessageResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(false).error(error)
                .timestamp(java.time.Instant.now()).build();
    }
}
