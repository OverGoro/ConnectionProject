// TariffDTO.java
package com.connection.tariff.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Data Transfer Object для передачи данных тарифа между слоями приложения
 * Содержит строковые представления UUID для удобства сериализации
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TariffDTO {
    /**
     * Уникальный идентификатор тарифа в строковом формате
     */
    @NonNull
    protected String uid;
    
    /**
     * Наименование тарифа
     */
    @NonNull
    protected String tariffName;
    
    /**
     * Стоимость тарифа с точностью до 8 знаков после запятой
     */
    @NonNull
    protected String amount;
    
    /**
     * Код валюты в формате ISO 4217 (3 символа)
     */
    @NonNull
    protected String currencyCode;
}