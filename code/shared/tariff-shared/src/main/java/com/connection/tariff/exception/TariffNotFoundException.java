// TariffNotFoundException.java
package com.connection.tariff.exception;

/**
 * Исключение, возникающее при попытке найти несуществующий тариф
 */
public class TariffNotFoundException extends BaseTariffException {
    /**
     * Конструктор исключения
     * @param tariffIdentifier идентификатор тарифа (UID или название)
     */
    public TariffNotFoundException(String tariffIdentifier) {
        super(tariffIdentifier);
    }

    /**
     * @return строковое представление исключения с описанием
     */
    public String toString() {
        String res = super.toString();
        res += "\n" + "description: tariff not found";
        return res;
    }
}