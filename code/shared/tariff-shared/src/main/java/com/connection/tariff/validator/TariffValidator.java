// TariffValidator.java
package com.connection.tariff.validator;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import com.connection.tariff.exception.TariffValidateException;
import com.connection.tariff.model.TariffBLM;
import com.connection.tariff.model.TariffDALM;
import com.connection.tariff.model.TariffDTO;

/**
 * Валидатор для проверки корректности данных тарифов
 * Выполняет проверки для всех типов моделей (DTO, BLM, DALM)
 */
public class TariffValidator {
    
    /**
     * Валидация TariffDTO
     * @param tariff DTO объект для валидации
     * @throws TariffValidateException если данные не прошли валидацию
     */
    public void validate(TariffDTO tariff) {
        if (tariff == null) {
            throw new TariffValidateException("null", "Tariff is null");
        }
        try {
            validateUid(tariff.getUid());
            validateTariffName(tariff.getTariffName());
            validateAmount(tariff.getAmount());
            validateCurrencyCode(tariff.getCurrencyCode());
        } catch (IllegalArgumentException e) {
            throw new TariffValidateException(tariff.getUid(), e.getMessage());
        }
    }

    /**
     * Валидация TariffBLM
     * @param tariff BLM объект для валидации
     * @throws TariffValidateException если данные не прошли валидацию
     */
    public void validate(TariffBLM tariff) {
        if (tariff == null) {
            throw new TariffValidateException("null", "Tariff is null");
        }
        try {
            validateUid(tariff.getUid());
            validateTariffName(tariff.getTariffName());
            validateAmount(tariff.getAmount());
            validateCurrencyCode(tariff.getCurrencyCode());
        } catch (IllegalArgumentException e) {
            throw new TariffValidateException(tariff.getUid().toString(), e.getMessage());
        }
    }

    /**
     * Валидация TariffDALM
     * @param tariff DALM объект для валидации
     * @throws TariffValidateException если данные не прошли валидацию
     */
    public void validate(TariffDALM tariff) {
        if (tariff == null) {
            throw new TariffValidateException("null", "Tariff is null");
        }
        try {
            validateUid(tariff.getUid());
            validateTariffName(tariff.getTariffName());
            validateAmount(tariff.getAmount());
            validateCurrencyCode(tariff.getCurrencyCode());
        } catch (IllegalArgumentException e) {
            throw new TariffValidateException(tariff.getUid().toString(), e.getMessage());
        }
    }

    /**
     * Валидация UUID в строковом формате
     * @param uid строковое представление UUID
     * @throws IllegalArgumentException если UID невалиден
     */
    private void validateUid(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            throw new IllegalArgumentException("UID cannot be empty");
        }
        try {
            UUID.fromString(uid);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UID format");
        }
    }

    /**
     * Валидация UUID объекта
     * @param uid UUID объект
     * @throws IllegalArgumentException если UID null
     */
    private void validateUid(UUID uid) {
        if (uid == null) {
            throw new IllegalArgumentException("UID cannot be null");
        }
    }

    /**
     * Валидация наименования тарифа
     * @param tariffName наименование тарифа
     * @throws IllegalArgumentException если название невалидно
     */
    private void validateTariffName(String tariffName) {
        if (tariffName == null || tariffName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tariff name cannot be empty");
        }
        if (tariffName.length() > 100) {
            throw new IllegalArgumentException("Tariff name cannot exceed 100 characters");
        }
    }

    /**
     * Валидация суммы тарифа в строковом формате
     * @param amount строковое представление суммы
     * @throws IllegalArgumentException если сумма невалидна
     */
    private void validateAmount(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount cannot be empty");
        }
        try {
            BigDecimal value = new BigDecimal(amount);
            validateAmount(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format");
        }
    }

    /**
     * Валидация суммы тарифа в BigDecimal формате
     * @param amount сумма тарифа
     * @throws IllegalArgumentException если сумма невалидна
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (amount.scale() > 8) {
            throw new IllegalArgumentException("Amount cannot have more than 8 decimal places");
        }
        // Проверка на максимальное значение 999999999999.99999999
        if (amount.compareTo(new BigDecimal("999999999999.99999999")) > 0) {
            throw new IllegalArgumentException("Amount exceeds maximum allowed value");
        }
    }

    /**
     * Валидация кода валюты
     * @param currencyCode код валюты ISO 4217
     * @throws IllegalArgumentException если код валюты невалиден
     */
    private void validateCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency code cannot be empty");
        }
        if (currencyCode.length() != 3) {
            throw new IllegalArgumentException("Currency code must be exactly 3 characters");
        }
        if (!currencyCode.equals(currencyCode.toUpperCase())) {
            throw new IllegalArgumentException("Currency code must be in uppercase");
        }
        try {
            // Проверка что код валюты существует в ISO 4217
            Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid currency code: " + currencyCode);
        }
    }
}