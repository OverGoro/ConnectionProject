// TransactionValidateException.java
package com.connection.transaction.exception;

/** Исключение при валидации транзакции */
public class TransactionValidateException extends BaseTransactionException {
    private final String description;

    public TransactionValidateException(String transactionIdentifier, String description) {
        super(transactionIdentifier);
        this.description = description;
    }

    public String toString() {
        return super.toString() + "\ndescription: " + description;
    }
}