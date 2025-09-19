// TariffConverter.java
package com.connection.tariff.converter;

import java.math.BigDecimal;
import java.util.UUID;

import com.connection.tariff.model.TariffBLM;
import com.connection.tariff.model.TariffDALM;
import com.connection.tariff.model.TariffDTO;

/**
 * Конвертер для преобразования между различными представлениями моделей тарифов
 * Обеспечивает согласованное преобразование данных между слоями приложения
 */
public class TariffConverter {
    
    /**
     * Преобразование TariffDALM в TariffBLM
     * @param dalm Data Access Layer Model
     * @return Business Logic Model
     */
    public TariffBLM toBLM(TariffDALM dalm) {
        return new TariffBLM(
            dalm.getUid(),
            dalm.getTariffName(),
            dalm.getAmount(),
            dalm.getCurrencyCode()
        );
    }

    /**
     * Преобразование TariffDTO в TariffBLM
     * @param dto Data Transfer Object
     * @return Business Logic Model
     * @throws IllegalArgumentException если преобразование данных невозможно
     */
    public TariffBLM toBLM(TariffDTO dto) {
        return new TariffBLM(
            UUID.fromString(dto.getUid()),
            dto.getTariffName(),
            new BigDecimal(dto.getAmount()),
            dto.getCurrencyCode()
        );
    }

    /**
     * Преобразование TariffBLM в TariffDTO
     * @param blm Business Logic Model
     * @return Data Transfer Object
     */
    public TariffDTO toDTO(TariffBLM blm) {
        return new TariffDTO(
            blm.getUid().toString(),
            blm.getTariffName(),
            blm.getAmount().toPlainString(),
            blm.getCurrencyCode()
        );
    }

    /**
     * Преобразование TariffBLM в TariffDALM
     * @param blm Business Logic Model
     * @return Data Access Layer Model
     */
    public TariffDALM toDALM(TariffBLM blm) {
        return new TariffDALM(
            blm.getUid(),
            blm.getTariffName(),
            blm.getAmount(),
            blm.getCurrencyCode()
        );
    }
}