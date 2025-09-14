// TariffTransactionValidator.java
package com.connection.transaction.validator;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.connection.transaction.exception.TransactionValidateException;
import com.connection.transaction.model.TariffTransactionBLM;
import com.connection.transaction.model.TariffTransactionDALM;
import com.connection.transaction.model.TariffTransactionDTO;

/** Валидатор тарифных транзакций */
public class TariffTransactionValidator {
    
    public void validate(TariffTransactionDTO transaction) {
        if (transaction == null) throw new TransactionValidateException("null", "Transaction is null");
        try {
            validateUid(transaction.getUid());
            validateTariffUid(transaction.getTariffUid());
            validateTransactionUid(transaction.getTransactionUid());
            validateTransactionDate(transaction.getTransactionDate());
            validateExpiresAt(transaction.getExpiresAt());
        } catch (IllegalArgumentException e) {
            throw new TransactionValidateException(transaction.getUid(), e.getMessage());
        }
    }

    public void validate(TariffTransactionBLM transaction) {
        if (transaction == null) throw new TransactionValidateException("null", "Transaction is null");
        try {
            validateUid(transaction.getUid());
            validateTariffUid(transaction.getTariffUid());
            validateTransactionUid(transaction.getTransactionUid());
            validateTransactionDate(transaction.getTransactionDate());
            validateExpiresAt(transaction.getExpiresAt());
        } catch (IllegalArgumentException e) {
            throw new TransactionValidateException(transaction.getUid().toString(), e.getMessage());
        }
    }

    public void validate(TariffTransactionDALM transaction) {
        if (transaction == null) throw new TransactionValidateException("null", "Transaction is null");
        try {
            validateUid(transaction.getUid());
            validateTariffUid(transaction.getTariffUid());
            validateTransactionUid(transaction.getTransactionUid());
            validateTransactionDate(transaction.getTransactionDate());
            validateExpiresAt(transaction.getExpiresAt());
        } catch (IllegalArgumentException e) {
            throw new TransactionValidateException(transaction.getUid().toString(), e.getMessage());
        }
    }

    private void validateUid(String uid) {
        if (uid == null || uid.trim().isEmpty()) throw new IllegalArgumentException("UID cannot be empty");
        try { UUID.fromString(uid); } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UID format");
        }
    }

    private void validateUid(UUID uid) {
        if (uid == null) throw new IllegalArgumentException("UID cannot be null");
    }

    private void validateTariffUid(String tariffUid) {
        if (tariffUid == null || tariffUid.trim().isEmpty()) throw new IllegalArgumentException("Tariff UID cannot be empty");
        try { UUID.fromString(tariffUid); } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Tariff UID format");
        }
    }

    private void validateTariffUid(UUID tariffUid) {
        if (tariffUid == null) throw new IllegalArgumentException("Tariff UID cannot be null");
    }

    private void validateTransactionUid(String transactionUid) {
        if (transactionUid == null || transactionUid.trim().isEmpty()) throw new IllegalArgumentException("Transaction UID cannot be empty");
        try { UUID.fromString(transactionUid); } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Transaction UID format");
        }
    }

    private void validateTransactionUid(UUID transactionUid) {
        if (transactionUid == null) throw new IllegalArgumentException("Transaction UID cannot be null");
    }

    private void validateTransactionDate(ZonedDateTime date) {
        if (date == null) throw new IllegalArgumentException("Transaction date cannot be null");
        if (date.isAfter(ZonedDateTime.now())) throw new IllegalArgumentException("Transaction date cannot be in the future");
    }

    private void validateExpiresAt(ZonedDateTime expiresAt) {
        if (expiresAt == null) throw new IllegalArgumentException("Expires at cannot be null");
        if (expiresAt.isBefore(ZonedDateTime.now())) throw new IllegalArgumentException("Expires at cannot be in the past");
    }
}