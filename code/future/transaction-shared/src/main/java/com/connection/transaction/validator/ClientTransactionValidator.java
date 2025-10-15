// ClientTransactionValidator.java
package com.connection.transaction.validator;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.connection.transaction.exception.TransactionValidateException;
import com.connection.transaction.model.ClientTransactionBLM;
import com.connection.transaction.model.ClientTransactionDALM;
import com.connection.transaction.model.ClientTransactionDTO;

/** Валидатор клиентских транзакций */
public class ClientTransactionValidator {
    
    public void validate(ClientTransactionDTO transaction) {
        if (transaction == null) throw new TransactionValidateException("null", "Transaction is null");
        try {
            validateUid(transaction.getUid());
            validateClientUid(transaction.getClientUid());
            validateTransactionDate(transaction.getTransactionDate());
            validateAmount(transaction.getAmount());
            validateCurrencyCode(transaction.getCurrencyCode());
            validateDescription(transaction.getDescription());
        } catch (IllegalArgumentException e) {
            throw new TransactionValidateException(transaction.getUid(), e.getMessage());
        }
    }

    public void validate(ClientTransactionBLM transaction) {
        if (transaction == null) throw new TransactionValidateException("null", "Transaction is null");
        try {
            validateUid(transaction.getUid());
            validateClientUid(transaction.getClientUid());
            validateTransactionDate(transaction.getTransactionDate());
            validateAmount(transaction.getAmount());
            validateCurrencyCode(transaction.getCurrencyCode());
            validateDescription(transaction.getDescription());
        } catch (IllegalArgumentException e) {
            throw new TransactionValidateException(transaction.getUid().toString(), e.getMessage());
        }
    }

    public void validate(ClientTransactionDALM transaction) {
        if (transaction == null) throw new TransactionValidateException("null", "Transaction is null");
        try {
            validateUid(transaction.getUid());
            validateClientUid(transaction.getClientUid());
            validateTransactionDate(transaction.getTransactionDate());
            validateAmount(transaction.getAmount());
            validateCurrencyCode(transaction.getCurrencyCode());
            validateDescription(transaction.getDescription());
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

    private void validateClientUid(String clientUid) {
        if (clientUid == null || clientUid.trim().isEmpty()) throw new IllegalArgumentException("Client UID cannot be empty");
        try { UUID.fromString(clientUid); } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Client UID format");
        }
    }

    private void validateClientUid(UUID clientUid) {
        if (clientUid == null) throw new IllegalArgumentException("Client UID cannot be null");
    }

    private void validateTransactionDate(ZonedDateTime date) {
        if (date == null) throw new IllegalArgumentException("Transaction date cannot be null");
        if (date.isAfter(ZonedDateTime.now())) throw new IllegalArgumentException("Transaction date cannot be in the future");
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) throw new IllegalArgumentException("Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (amount.scale() > 8) throw new IllegalArgumentException("Amount cannot have more than 8 decimal places");
    }

    private void validateCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) throw new IllegalArgumentException("Currency code cannot be empty");
        if (currencyCode.length() != 3) throw new IllegalArgumentException("Currency code must be 3 characters");
        if (!currencyCode.equals(currencyCode.toUpperCase())) throw new IllegalArgumentException("Currency code must be uppercase");
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > 500) throw new IllegalArgumentException("Description cannot exceed 500 characters");
    }
}