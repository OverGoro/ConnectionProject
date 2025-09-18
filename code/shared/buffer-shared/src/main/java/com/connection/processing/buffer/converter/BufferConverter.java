// BufferConverter.java
package com.connection.processing.buffer.converter;

import java.util.UUID;

import com.connection.processing.buffer.model.BufferBLM;
import com.connection.processing.buffer.model.BufferDALM;
import com.connection.processing.buffer.model.BufferDTO;

public class BufferConverter {
    public BufferBLM toBLM(BufferDALM dalm) {
        return new BufferBLM(
            dalm.getUid(),
            dalm.getConnectionSchemeUid(),
            dalm.getMaxMessagesNumber(),
            dalm.getMaxMessageSize(),
            dalm.getMessagePrototype()
        );
    }

    public BufferBLM toBLM(BufferDTO dto) {
        return new BufferBLM(
            UUID.fromString(dto.getUid()),
            UUID.fromString(dto.getConnectionSchemeUid()),
            dto.getMaxMessagesNumber(),
            dto.getMaxMessageSize(),
            dto.getMessagePrototype()
        );
    }

    public BufferDTO toDTO(BufferBLM blm) {
        return new BufferDTO(
            blm.getUid().toString(),
            blm.getConnectionSchemeUid().toString(),
            blm.getMaxMessagesNumber(),
            blm.getMaxMessageSize(),
            blm.getMessagePrototype()
        );
    }

    public BufferDALM toDALM(BufferBLM blm) {
        return new BufferDALM(
            blm.getUid(),
            blm.getConnectionSchemeUid(),
            blm.getMaxMessagesNumber(),
            blm.getMaxMessageSize(),
            blm.getMessagePrototype()
        );
    }
}