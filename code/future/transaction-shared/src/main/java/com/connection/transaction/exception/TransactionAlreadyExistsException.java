// TransactionAlreadyExistsException.java
package com.connection.transaction.exception;

/** Исключение при существующей транзакции */
public class TransactionAlreadyExistsException extends BaseTransactionException {
    public TransactionAlreadyExistsException(String transactionIdentifier) {
        super(transactionIdentifier);
    }

    public String toString() {
        return super.toString() + "\ndescription: transaction already exists";
    }
}