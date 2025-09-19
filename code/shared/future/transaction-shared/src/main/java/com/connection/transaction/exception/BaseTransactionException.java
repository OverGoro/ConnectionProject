// BaseTransactionException.java
package com.connection.transaction.exception;

/** Базовое исключение для транзакций */
public class BaseTransactionException extends RuntimeException {
    private final String transactionIdentifier;

    public BaseTransactionException(String transactionIdentifier) {
        super("transaction");
        this.transactionIdentifier = transactionIdentifier;
    }

    public String toString() {
        return super.toString() + "\ntransaction: " + transactionIdentifier;
    }
}