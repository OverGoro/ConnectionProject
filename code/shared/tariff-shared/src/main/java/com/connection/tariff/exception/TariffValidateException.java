// TariffValidateException.java
package com.connection.tariff.exception;

/**
 * Исключение, возникающее при валидации данных тарифа
 * Содержит подробное описание ошибки валидации
 */
public class TariffValidateException extends BaseTariffException {
    String descriptionString;

    /**
     * Конструктор исключения валидации
     * @param tariffIdentifier идентификатор тарифа
     * @param description подробное описание ошибки валидации
     */
    public TariffValidateException(String tariffIdentifier, String description) {
        super(tariffIdentifier);
        this.descriptionString = description;
    }

    /**
     * @return строковое представление исключения с описанием ошибки
     */
    public String toString() {
        String res = super.toString();
        res += "\n" + "description: tariff is invalid";
        res += "\n" + descriptionString;
        return res;
    }
}