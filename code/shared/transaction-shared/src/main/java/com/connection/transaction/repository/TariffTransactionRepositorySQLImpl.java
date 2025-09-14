// TariffTransactionRepositorySQLImpl.java
package com.connection.transaction.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.connection.transaction.exception.TransactionAlreadyExistsException;
import com.connection.transaction.exception.TransactionNotFoundException;
import com.connection.transaction.model.TariffTransactionDALM;

@Repository
public class TariffTransactionRepositorySQLImpl implements TariffTransactionRepository {

    private static final String SELECT_TRANSACTION = "SELECT uid, tariff_uid, transaction_uid, transaction_date, expires_at";
    private static final String FROM_TRANSACTION = " FROM transaction.tariff_transaction";

    private static final String SELECT_BY_UID = SELECT_TRANSACTION + FROM_TRANSACTION + " WHERE uid = :uid";
    private static final String SELECT_BY_TARIFF = SELECT_TRANSACTION + FROM_TRANSACTION + " WHERE tariff_uid = :tariff_uid ORDER BY transaction_date DESC";
    private static final String SELECT_BY_CLIENT_TRANSACTION = SELECT_TRANSACTION + FROM_TRANSACTION + " WHERE transaction_uid = :transaction_uid ORDER BY transaction_date DESC";
    private static final String SELECT_EXPIRING_SOON = SELECT_TRANSACTION + FROM_TRANSACTION + " WHERE expires_at <= :threshold AND expires_at > NOW() ORDER BY expires_at ASC";
    private static final String DELETE_EXPIRED = "DELETE FROM transaction.tariff_transaction WHERE expires_at <= NOW()";

    private static final String INSERT_TRANSACTION = "INSERT INTO transaction.tariff_transaction (uid, tariff_uid, transaction_uid, transaction_date, expires_at) VALUES (:uid, :tariff_uid, :transaction_uid, :transaction_date, :expires_at)";
    private static final String UPDATE_TRANSACTION = "UPDATE transaction.tariff_transaction SET tariff_uid = :tariff_uid, transaction_uid = :transaction_uid, transaction_date = :transaction_date, expires_at = :expires_at WHERE uid = :uid";
    private static final String DELETE_TRANSACTION = "DELETE FROM transaction.tariff_transaction WHERE uid = :uid";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<TariffTransactionDALM> transactionRowMapper = (rs, rowNum) -> {
        TariffTransactionDALM transaction = new TariffTransactionDALM();
        transaction.setUid(UUID.fromString(rs.getString("uid")));
        transaction.setTariffUid(UUID.fromString(rs.getString("tariff_uid")));
        transaction.setTransactionUid(UUID.fromString(rs.getString("transaction_uid")));
        transaction.setTransactionDate(rs.getObject("transaction_date", ZonedDateTime.class));
        transaction.setExpiresAt(rs.getObject("expires_at", ZonedDateTime.class));
        return transaction;
    };

    public TariffTransactionRepositorySQLImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void add(TariffTransactionDALM transaction) throws TransactionAlreadyExistsException {
        if (exists(transaction.getUid())) {
            throw new TransactionAlreadyExistsException("Transaction with UID " + transaction.getUid() + " already exists");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", transaction.getUid());
        params.addValue("tariff_uid", transaction.getTariffUid());
        params.addValue("transaction_uid", transaction.getTransactionUid());
        params.addValue("transaction_date", Timestamp.from(transaction.getTransactionDate().toInstant()));
        params.addValue("expires_at", Timestamp.from(transaction.getExpiresAt().toInstant()));

        jdbcTemplate.update(INSERT_TRANSACTION, params);
    }

    @Override
    @Transactional
    public void update(TariffTransactionDALM transaction) throws TransactionNotFoundException {
        if (!exists(transaction.getUid())) {
            throw new TransactionNotFoundException("Transaction with UID " + transaction.getUid() + " not found");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", transaction.getUid());
        params.addValue("tariff_uid", transaction.getTariffUid());
        params.addValue("transaction_uid", transaction.getTransactionUid());
        params.addValue("transaction_date", Timestamp.from(transaction.getTransactionDate().toInstant()));
        params.addValue("expires_at", Timestamp.from(transaction.getExpiresAt().toInstant()));

        jdbcTemplate.update(UPDATE_TRANSACTION, params);
    }

    @Override
    @Transactional
    public void delete(UUID uid) throws TransactionNotFoundException {
        if (!exists(uid)) {
            throw new TransactionNotFoundException("Transaction with UID " + uid + " not found");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        jdbcTemplate.update(DELETE_TRANSACTION, params);
    }

    @Override
    @Transactional(readOnly = true)
    public TariffTransactionDALM findByUid(UUID uid) throws TransactionNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            return jdbcTemplate.queryForObject(SELECT_BY_UID, params, transactionRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new TransactionNotFoundException("Transaction with UID " + uid + " not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TariffTransactionDALM> findByTariffUid(UUID tariffUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("tariff_uid", tariffUid);
        return jdbcTemplate.query(SELECT_BY_TARIFF, params, transactionRowMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TariffTransactionDALM> findByTransactionUid(UUID transactionUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("transaction_uid", transactionUid);
        return jdbcTemplate.query(SELECT_BY_CLIENT_TRANSACTION, params, transactionRowMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TariffTransactionDALM> findExpiringSoon(ZonedDateTime threshold) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("threshold", Timestamp.from(threshold.toInstant()));
        return jdbcTemplate.query(SELECT_EXPIRING_SOON, params, transactionRowMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID uid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            jdbcTemplate.queryForObject(SELECT_BY_UID, params, transactionRowMapper);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    @Transactional
    public void cleanupExpired() {
        jdbcTemplate.update(DELETE_EXPIRED, new MapSqlParameterSource());
    }
}