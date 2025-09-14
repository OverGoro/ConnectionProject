// TariffTransactionRepository.java
package com.connection.transaction.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.connection.transaction.exception.TransactionAlreadyExistsException;
import com.connection.transaction.exception.TransactionNotFoundException;
import com.connection.transaction.model.TariffTransactionDALM;

/** Репозиторий тарифных транзакций */
public interface TariffTransactionRepository {
    void add(TariffTransactionDALM transaction) throws TransactionAlreadyExistsException;
    void update(TariffTransactionDALM transaction) throws TransactionNotFoundException;
    void delete(UUID uid) throws TransactionNotFoundException;
    TariffTransactionDALM findByUid(UUID uid) throws TransactionNotFoundException;
    List<TariffTransactionDALM> findByTariffUid(UUID tariffUid);
    List<TariffTransactionDALM> findByTransactionUid(UUID transactionUid);
    List<TariffTransactionDALM> findExpiringSoon(ZonedDateTime threshold);
    boolean exists(UUID uid);
    void cleanupExpired();
}