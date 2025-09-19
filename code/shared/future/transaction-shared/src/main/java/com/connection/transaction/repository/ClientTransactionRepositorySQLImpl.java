// ClientTransactionRepositorySQLImpl.java
package com.connection.transaction.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.connection.transaction.exception.TransactionAlreadyExistsException;
import com.connection.transaction.exception.TransactionNotFoundException;
import com.connection.transaction.model.ClientTransactionDALM;

@Repository
public class ClientTransactionRepositorySQLImpl implements ClientTransactionRepository {

    private static final String SELECT_TRANSACTION = "SELECT uid, client_uid, transaction_date, amount, currency_code, description";
    private static final String FROM_TRANSACTION = " FROM transaction.client_transaction";

    private static final String SELECT_BY_UID = SELECT_TRANSACTION + FROM_TRANSACTION + " WHERE uid = :uid";
    private static final String SELECT_BY_CLIENT = SELECT_TRANSACTION + FROM_TRANSACTION + " WHERE client_uid = :client_uid ORDER BY transaction_date DESC";
    private static final String SELECT_BY_DATE_RANGE = SELECT_TRANSACTION + FROM_TRANSACTION + " WHERE transaction_date BETWEEN :start_date AND :end_date ORDER BY transaction_date DESC";
    private static final String CALCULATE_BALANCE = "SELECT COALESCE(SUM(amount), 0) FROM transaction.client_transaction WHERE client_uid = :client_uid AND currency_code = :currency_code";

    private static final String INSERT_TRANSACTION = "INSERT INTO transaction.client_transaction (uid, client_uid, transaction_date, amount, currency_code, description) VALUES (:uid, :client_uid, :transaction_date, :amount, :currency_code, :description)";
    private static final String UPDATE_TRANSACTION = "UPDATE transaction.client_transaction SET client_uid = :client_uid, transaction_date = :transaction_date, amount = :amount, currency_code = :currency_code, description = :description WHERE uid = :uid";
    private static final String DELETE_TRANSACTION = "DELETE FROM transaction.client_transaction WHERE uid = :uid";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<ClientTransactionDALM> transactionRowMapper = (rs, rowNum) -> {
        ClientTransactionDALM transaction = new ClientTransactionDALM();
        transaction.setUid(UUID.fromString(rs.getString("uid")));
        transaction.setClientUid(UUID.fromString(rs.getString("client_uid")));
        transaction.setTransactionDate(rs.getObject("transaction_date", ZonedDateTime.class));
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setCurrencyCode(rs.getString("currency_code"));
        transaction.setDescription(rs.getString("description"));
        return transaction;
    };

    public ClientTransactionRepositorySQLImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void add(ClientTransactionDALM transaction) throws TransactionAlreadyExistsException {
        if (exists(transaction.getUid())) {
            throw new TransactionAlreadyExistsException("Transaction with UID " + transaction.getUid() + " already exists");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", transaction.getUid());
        params.addValue("client_uid", transaction.getClientUid());
        params.addValue("transaction_date", Timestamp.from(transaction.getTransactionDate().toInstant()));
        params.addValue("amount", transaction.getAmount());
        params.addValue("currency_code", transaction.getCurrencyCode());
        params.addValue("description", transaction.getDescription());

        jdbcTemplate.update(INSERT_TRANSACTION, params);
    }

    @Override
    @Transactional
    public void update(ClientTransactionDALM transaction) throws TransactionNotFoundException {
        if (!exists(transaction.getUid())) {
            throw new TransactionNotFoundException("Transaction with UID " + transaction.getUid() + " not found");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", transaction.getUid());
        params.addValue("client_uid", transaction.getClientUid());
        params.addValue("transaction_date", Timestamp.from(transaction.getTransactionDate().toInstant()));
        params.addValue("amount", transaction.getAmount());
        params.addValue("currency_code", transaction.getCurrencyCode());
        params.addValue("description", transaction.getDescription());

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
    public ClientTransactionDALM findByUid(UUID uid) throws TransactionNotFoundException {
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
    public List<ClientTransactionDALM> findByClientUid(UUID clientUid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_uid", clientUid);
        return jdbcTemplate.query(SELECT_BY_CLIENT, params, transactionRowMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientTransactionDALM> findByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("start_date", Timestamp.from(startDate.toInstant()));
        params.addValue("end_date", Timestamp.from(endDate.toInstant()));
        return jdbcTemplate.query(SELECT_BY_DATE_RANGE, params, transactionRowMapper);
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
    @Transactional(readOnly = true)
    public BigDecimal getClientBalance(UUID clientUid, String currencyCode) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_uid", clientUid);
        params.addValue("currency_code", currencyCode);
        return jdbcTemplate.queryForObject(CALCULATE_BALANCE, params, BigDecimal.class);
    }
}