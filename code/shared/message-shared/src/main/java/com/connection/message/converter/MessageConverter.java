// Конвертер
package com.connection.message.converter;

import org.springframework.stereotype.Component;

import com.connection.message.model.MessageBLM;
import com.connection.message.model.MessageDALM;
import com.connection.message.model.MessageDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MessageConverter {
    public MessageBLM toBLM(MessageDTO dto) {
        return new MessageBLM(dto.getUid(), dto.getBufferUid(), dto.getContent(), dto.getContentType(), dto.getCreatedAt());
    }
    
    public MessageBLM toBLM(MessageDALM dalm) {
        return new MessageBLM(dalm.getUid(), dalm.getBufferUid(), dalm.getContent(), dalm.getContentType(), dalm.getCreatedAt());
    }
    
    public MessageDTO toDTO(MessageBLM blm) {
        return new MessageDTO(blm.getUid(), blm.getBufferUid(), blm.getContent(), blm.getContentType(), blm.getCreatedAt());
    }
    
    public MessageDALM toDALM(MessageBLM blm) {
        return new MessageDALM(blm.getUid(), blm.getBufferUid(), blm.getContent(), blm.getContentType(), blm.getCreatedAt());
    }
}