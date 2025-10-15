// TariffAlreadyExistsException.java
package com.connection.tariff.exception;

/**
 * Исключение, возникающее при попытке создать тариф с уже существующим UID или наименованием
 */
public class TariffAlreadyExistsException extends BaseTariffException {
    /**
     * Конструктор исключения
     * @param tariffIdentifier идентификатор тарифа (UID или название)
     */
    public TariffAlreadyExistsException(String tariffIdentifier) {
        super(tariffIdentifier);
    }

    /**
     * @return строковое представление исключения с описанием
     */
    public String toString() {
        String res = super.toString();
        res += "\n" + "description: tariff already exists";
        return res;
    }
}