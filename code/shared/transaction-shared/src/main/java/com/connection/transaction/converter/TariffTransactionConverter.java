// TariffTransactionConverter.java
package com.connection.transaction.converter;

import java.util.UUID;

import com.connection.transaction.model.TariffTransactionBLM;
import com.connection.transaction.model.TariffTransactionDALM;
import com.connection.transaction.model.TariffTransactionDTO;

/** Конвертер тарифных транзакций */
public class TariffTransactionConverter {
    
    public TariffTransactionBLM toBLM(TariffTransactionDALM dalm) {
        return new TariffTransactionBLM(
            dalm.getUid(),
            dalm.getTariffUid(),
            dalm.getTransactionUid(),
            dalm.getTransactionDate(),
            dalm.getExpiresAt()
        );
    }

    public TariffTransactionBLM toBLM(TariffTransactionDTO dto) {
        return new TariffTransactionBLM(
            UUID.fromString(dto.getUid()),
            UUID.fromString(dto.getTariffUid()),
            UUID.fromString(dto.getTransactionUid()),
            dto.getTransactionDate(),
            dto.getExpiresAt()
        );
    }

    public TariffTransactionDTO toDTO(TariffTransactionBLM blm) {
        return new TariffTransactionDTO(
            blm.getUid().toString(),
            blm.getTariffUid().toString(),
            blm.getTransactionUid().toString(),
            blm.getTransactionDate(),
            blm.getExpiresAt()
        );
    }

    public TariffTransactionDALM toDALM(TariffTransactionBLM blm) {
        return new TariffTransactionDALM(
            blm.getUid(),
            blm.getTariffUid(),
            blm.getTransactionUid(),
            blm.getTransactionDate(),
            blm.getExpiresAt()
        );
    }
}