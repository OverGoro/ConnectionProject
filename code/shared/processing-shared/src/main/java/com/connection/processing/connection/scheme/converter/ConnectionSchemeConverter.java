// ConnectionSchemeConverter.java
package com.connection.processing.connection.scheme.converter;

import java.util.UUID;

import com.connection.processing.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.processing.connection.scheme.model.ConnectionSchemeDALM;
import com.connection.processing.connection.scheme.model.ConnectionSchemeDTO;

public class ConnectionSchemeConverter {
    public ConnectionSchemeBLM toBLM(ConnectionSchemeDALM dalm) {
        return new ConnectionSchemeBLM(
            dalm.getUid(),
            dalm.getClientUid(),
            dalm.getSchemeJson()
        );
    }

    public ConnectionSchemeBLM toBLM(ConnectionSchemeDTO dto) {
        return new ConnectionSchemeBLM(
            UUID.fromString(dto.getUid()),
            UUID.fromString(dto.getClientUid()),
            dto.getSchemeJson()
        );
    }

    public ConnectionSchemeDTO toDTO(ConnectionSchemeBLM blm) {
        return new ConnectionSchemeDTO(
            blm.getUid().toString(),
            blm.getClientUid().toString(),
            blm.getSchemeJson()
        );
    }

    public ConnectionSchemeDALM toDALM(ConnectionSchemeBLM blm) {
        return new ConnectionSchemeDALM(
            blm.getUid(),
            blm.getClientUid(),
            blm.getSchemeJson()
        );
    }
}