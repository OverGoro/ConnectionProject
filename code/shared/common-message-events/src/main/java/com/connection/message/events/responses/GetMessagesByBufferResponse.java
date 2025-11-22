
package com.connection.message.events.responses;

import com.connection.common.events.CommandResponse;
import com.connection.message.model.MessageDto;
import java.util.List;
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
public class GetMessagesByBufferResponse extends CommandResponse {
    private List<MessageDto> messages;

    /** . */
    public static GetMessagesByBufferResponse success(String correlationId,
            List<MessageDto> messages) {
        return GetMessagesByBufferResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(true).messages(messages)
                .timestamp(java.time.Instant.now()).build();
    }

    /** . */
    public static GetMessagesByBufferResponse error(String correlationId,
            String error) {
        return GetMessagesByBufferResponse.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId).success(false).error(error)
                .timestamp(java.time.Instant.now()).build();
    }
}
