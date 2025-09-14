// ClientTransactionConverter.java
package com.connection.transaction.converter;

import java.util.UUID;

import com.connection.transaction.model.ClientTransactionBLM;
import com.connection.transaction.model.ClientTransactionDALM;
import com.connection.transaction.model.ClientTransactionDTO;

/** Конвертер клиентских транзакций */
public class ClientTransactionConverter {
    
    public ClientTransactionBLM toBLM(ClientTransactionDALM dalm) {
        return new ClientTransactionBLM(
            dalm.getUid(),
            dalm.getClientUid(),
            dalm.getTransactionDate(),
            dalm.getAmount(),
            dalm.getCurrencyCode(),
            dalm.getDescription()
        );
    }

    public ClientTransactionBLM toBLM(ClientTransactionDTO dto) {
        return new ClientTransactionBLM(
            UUID.fromString(dto.getUid()),
            UUID.fromString(dto.getClientUid()),
            dto.getTransactionDate(),
            dto.getAmount(),
            dto.getCurrencyCode(),
            dto.getDescription()
        );
    }

    public ClientTransactionDTO toDTO(ClientTransactionBLM blm) {
        return new ClientTransactionDTO(
            blm.getUid().toString(),
            blm.getClientUid().toString(),
            blm.getTransactionDate(),
            blm.getAmount(),
            blm.getCurrencyCode(),
            blm.getDescription()
        );
    }

    public ClientTransactionDALM toDALM(ClientTransactionBLM blm) {
        return new ClientTransactionDALM(
            blm.getUid(),
            blm.getClientUid(),
            blm.getTransactionDate(),
            blm.getAmount(),
            blm.getCurrencyCode(),
            blm.getDescription()
        );
    }
}