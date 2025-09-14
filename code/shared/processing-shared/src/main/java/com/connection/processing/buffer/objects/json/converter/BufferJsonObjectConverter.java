// BufferJsonObjectConverter.java
package com.connection.processing.buffer.objects.json.converter;

import java.util.UUID;

import com.connection.processing.buffer.objects.json.model.BufferJsonObjectBLM;
import com.connection.processing.buffer.objects.json.model.BufferJsonObjectDALM;
import com.connection.processing.buffer.objects.json.model.BufferJsonObjectDTO;

public class BufferJsonObjectConverter {
    public BufferJsonObjectBLM toBLM(BufferJsonObjectDALM dalm) {
        return new BufferJsonObjectBLM(
            dalm.getUid(),
            dalm.getBufferUid(),
            dalm.getData()
        );
    }

    public BufferJsonObjectBLM toBLM(BufferJsonObjectDTO dto) {
        return new BufferJsonObjectBLM(
            UUID.fromString(dto.getUid()),
            UUID.fromString(dto.getBufferUid()),
            dto.getData()
        );
    }

    public BufferJsonObjectDTO toDTO(BufferJsonObjectBLM blm) {
        return new BufferJsonObjectDTO(
            blm.getUid().toString(),
            blm.getBufferUid().toString(),
            blm.getData()
        );
    }

    public BufferJsonObjectDALM toDALM(BufferJsonObjectBLM blm) {
        return new BufferJsonObjectDALM(
            blm.getUid(),
            blm.getBufferUid(),
            blm.getData()
        );
    }
}