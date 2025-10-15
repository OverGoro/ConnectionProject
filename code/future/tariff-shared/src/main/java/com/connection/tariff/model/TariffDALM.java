// TariffDALM.java
package com.connection.tariff.model;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Data Access Layer Model тарифа - модель для работы с базой данных
 * Соответствует структуре таблицы core.tariff
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TariffDALM {
    /**
     * Уникальный идентификатор тарифа (первичный ключ)
     */
    @NonNull
    protected UUID uid;
    
    /**
     * Наименование тарифа
     */
    @NonNull
    protected String tariffName;
    
    /**
     * Стоимость тарифа с точностью NUMERIC(20, 8)
     */
    @NonNull
    protected BigDecimal amount;
    
    /**
     * Код валюты в формате CHAR(3)
     */
    @NonNull
    protected String currencyCode;
}