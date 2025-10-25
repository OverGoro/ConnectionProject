// TransactionNotFoundException.java
package com.connection.transaction.exception;

/** Исключение при отсутствии транзакции */
public class TransactionNotFoundException extends BaseTransactionException {
    public TransactionNotFoundException(String transactionIdentifier) {
        super(transactionIdentifier);
    }

    public String toString() {
        return super.toString() + "\ndescription: transaction not found";
    }
}