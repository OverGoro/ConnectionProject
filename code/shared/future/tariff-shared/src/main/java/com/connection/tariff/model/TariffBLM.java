// TariffBLM.java
package com.connection.tariff.model;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Business Logic Model тарифа - основная модель для бизнес-логики
 * Содержит типизированные поля для валидации и вычислений
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TariffBLM {
    /**
     * Уникальный идентификатор тарифа
     */
    @NonNull
    protected UUID uid;
    
    /**
     * Наименование тарифа (максимум 100 символов)
     */
    @NonNull
    protected String tariffName;
    
    /**
     * Стоимость тарифа с высокой точностью (20 цифр, 8 знаков после запятой)
     * Используется для финансовых расчетов
     */
    @NonNull
    protected BigDecimal amount;
    
    /**
     * Код валюты в формате ISO 4217 (3 символа)
     * Пример: USD, EUR, RUB
     */
    @NonNull
    protected String currencyCode;
}