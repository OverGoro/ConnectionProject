// BaseTariffException.java
package com.connection.tariff.exception;

/**
 * Базовое исключение для всех ошибок, связанных с тарифами
 * Содержит идентификатор тарифа, для которого возникла ошибка
 */
public class BaseTariffException extends RuntimeException {
    private final String tariffIdentifier;

    /**
     * Конструктор базового исключения
     * @param tariffIdentifier идентификатор тарифа (UID или название)
     */
    public BaseTariffException(String tariffIdentifier) {
        super("tariff");
        this.tariffIdentifier = tariffIdentifier;
    }

    /**
     * @return строковое представление исключения с идентификатором тарифа
     */
    public String toString() {
        String res = super.toString();
        res += "\n" + "tariff: " + tariffIdentifier;
        return res;
    }
}