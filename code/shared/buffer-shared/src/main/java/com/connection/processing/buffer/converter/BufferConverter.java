
package com.connection.processing.buffer.converter;

import com.connection.processing.buffer.model.BufferBlm;
import com.connection.processing.buffer.model.BufferDalm;
import com.connection.processing.buffer.model.BufferDto;
import java.util.UUID;

/** . */
public class BufferConverter {
    /** . */
    public BufferBlm toBlm(BufferDalm dalm) {
        return new BufferBlm(dalm.getUid(), dalm.getDeviceUid(),
                dalm.getMaxMessagesNumber(), dalm.getMaxMessageSize(),
                dalm.getMessagePrototype());
    }

    /** . */
    public BufferBlm toBlm(BufferDto dto) {
        return new BufferBlm(UUID.fromString(dto.getUid()),
                UUID.fromString(dto.getDeviceUid()), dto.getMaxMessagesNumber(),
                dto.getMaxMessageSize(), dto.getMessagePrototype());
    }

    /** . */
    public BufferDto toDto(BufferBlm blm) {
        return new BufferDto(blm.getUid().toString(),
                blm.getDeviceUid().toString(), blm.getMaxMessagesNumber(),
                blm.getMaxMessageSize(), blm.getMessagePrototype());
    }

    /** . */
    public BufferDalm toDalm(BufferBlm blm) {
        return new BufferDalm(blm.getUid(), blm.getDeviceUid(),
                blm.getMaxMessagesNumber(), blm.getMaxMessageSize(),
                blm.getMessagePrototype());
    }
}
