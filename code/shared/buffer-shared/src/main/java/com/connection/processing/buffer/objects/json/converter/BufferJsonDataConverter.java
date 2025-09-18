// BufferJsonDataConverter.java
package com.connection.processing.buffer.objects.json.converter;

import java.util.UUID;

import com.connection.processing.buffer.objects.json.model.BufferJsonDataBLM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDALM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDTO;

public class BufferJsonDataConverter {
    public BufferJsonDataBLM toBLM(BufferJsonDataDALM dalm) {
        return new BufferJsonDataBLM(
            dalm.getUid(),
            dalm.getBufferUid(),
            dalm.getData(),
            dalm.getCreatedAt()
        );
    }

    public BufferJsonDataBLM toBLM(BufferJsonDataDTO dto) {
        return new BufferJsonDataBLM(
            UUID.fromString(dto.getUid()),
            UUID.fromString(dto.getBufferUid()),
            dto.getData(),
            dto.getCreatedAt()
        );
    }

    public BufferJsonDataDTO toDTO(BufferJsonDataBLM blm) {
        return new BufferJsonDataDTO(
            blm.getUid().toString(),
            blm.getBufferUid().toString(),
            blm.getData(),
            blm.getCreatedAt()
        );
    }

    public BufferJsonDataDALM toDALM(BufferJsonDataBLM blm) {
        return new BufferJsonDataDALM(
            blm.getUid(),
            blm.getBufferUid(),
            blm.getData(),
            blm.getCreatedAt()
        );
    }
}