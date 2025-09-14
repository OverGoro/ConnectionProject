// ClientTransactionRepository.java
package com.connection.transaction.repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.connection.transaction.exception.TransactionAlreadyExistsException;
import com.connection.transaction.exception.TransactionNotFoundException;
import com.connection.transaction.model.ClientTransactionDALM;

/** Репозиторий клиентских транзакций */
public interface ClientTransactionRepository {
    void add(ClientTransactionDALM transaction) throws TransactionAlreadyExistsException;
    void update(ClientTransactionDALM transaction) throws TransactionNotFoundException;
    void delete(UUID uid) throws TransactionNotFoundException;
    ClientTransactionDALM findByUid(UUID uid) throws TransactionNotFoundException;
    List<ClientTransactionDALM> findByClientUid(UUID clientUid);
    List<ClientTransactionDALM> findByDateRange(ZonedDateTime startDate, ZonedDateTime endDate);
    boolean exists(UUID uid);
    BigDecimal getClientBalance(UUID clientUid, String currencyCode);
}