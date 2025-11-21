package com.connection.message.converter;

import com.connection.message.model.MessageBlm;
import com.connection.message.model.MessageDalm;
import com.connection.message.model.MessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** . */
@Component
@RequiredArgsConstructor
public class MessageConverter {
    /** . */
    public MessageBlm toBlm(MessageDto dto) {
        return new MessageBlm(dto.getUid(), dto.getBufferUid(),
                dto.getContent(), dto.getContentType(), dto.getCreatedAt());
    }

    /** . */
    public MessageBlm toBlm(MessageDalm dalm) {
        return new MessageBlm(dalm.getUid(), dalm.getBufferUid(),
                dalm.getContent(), dalm.getContentType(), dalm.getCreatedAt());
    }

    /** . */
    public MessageDto toDto(MessageBlm blm) {
        return new MessageDto(blm.getUid(), blm.getBufferUid(),
                blm.getContent(), blm.getContentType(), blm.getCreatedAt());
    }

    /** . */
    public MessageDalm toDalm(MessageBlm blm) {
        return new MessageDalm(blm.getUid(), blm.getBufferUid(),
                blm.getContent(), blm.getContentType(), blm.getCreatedAt());
    }
}
