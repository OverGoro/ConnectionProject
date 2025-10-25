// TariffRepositorySQLImpl.java
package com.connection.tariff.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import com.connection.tariff.exception.TariffAlreadyExistsException;
import com.connection.tariff.exception.TariffNotFoundException;
import com.connection.tariff.model.TariffDALM;

/**
 * Реализация репозитория тарифов на основе Spring JDBC
 * Использует NamedParameterJdbcTemplate для безопасного выполнения SQL запросов
 */
@Repository
public class TariffRepositorySQLImpl implements TariffRepository {

    private static final String SELECT_TARIFF = "SELECT uid, tariff_name, amount, currency_code";
    private static final String FROM_TARIFF = " FROM core.tariff";

    private static final String SELECT_TARIFF_BY_UID = SELECT_TARIFF + FROM_TARIFF + " WHERE uid = :uid";
    private static final String SELECT_TARIFF_BY_NAME = SELECT_TARIFF + FROM_TARIFF + " WHERE tariff_name = :tariff_name";
    private static final String SELECT_ALL_TARIFFS = SELECT_TARIFF + FROM_TARIFF + " ORDER BY tariff_name";

    private static final String INSERT_TARIFF = "INSERT INTO core.tariff (uid, tariff_name, amount, currency_code) " +
            "VALUES (:uid, :tariff_name, :amount, :currency_code)";

    private static final String UPDATE_TARIFF = "UPDATE core.tariff SET tariff_name = :tariff_name, amount = :amount, currency_code = :currency_code " +
            "WHERE uid = :uid";

    private static final String DELETE_TARIFF = "DELETE FROM core.tariff WHERE uid = :uid";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<TariffDALM> tariffRowMapper = (rs, rowNum) -> {
        TariffDALM tariff = new TariffDALM();
        tariff.setUid(UUID.fromString(rs.getString("uid")));
        tariff.setTariffName(rs.getString("tariff_name"));
        tariff.setAmount(rs.getBigDecimal("amount"));
        tariff.setCurrencyCode(rs.getString("currency_code"));
        return tariff;
    };

    /**
     * Конструктор репозитория
     * @param jdbcTemplate инстанс NamedParameterJdbcTemplate для работы с БД
     */
    public TariffRepositorySQLImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void add(TariffDALM tariff) throws TariffAlreadyExistsException {
        // Проверка существования по UID
        if (exists(tariff.getUid())) {
            throw new TariffAlreadyExistsException("Tariff with UID " + tariff.getUid() + " already exists");
        }

        // Проверка существования по имени
        if (existsByTariffName(tariff.getTariffName())) {
            throw new TariffAlreadyExistsException("Tariff with name '" + tariff.getTariffName() + "' already exists");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", tariff.getUid());
        params.addValue("tariff_name", tariff.getTariffName());
        params.addValue("amount", tariff.getAmount());
        params.addValue("currency_code", tariff.getCurrencyCode());

        jdbcTemplate.update(INSERT_TARIFF, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void update(TariffDALM tariff) throws TariffNotFoundException {
        // Проверка существования тарифа
        if (!exists(tariff.getUid())) {
            throw new TariffNotFoundException("Tariff with UID " + tariff.getUid() + " not found");
        }

        // Проверка что новое имя не конфликтует с другими тарифами
        TariffDALM existingTariff = findByUid(tariff.getUid());
        if (!existingTariff.getTariffName().equals(tariff.getTariffName()) && 
            existsByTariffName(tariff.getTariffName())) {
            throw new TariffAlreadyExistsException("Tariff with name '" + tariff.getTariffName() + "' already exists");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", tariff.getUid());
        params.addValue("tariff_name", tariff.getTariffName());
        params.addValue("amount", tariff.getAmount());
        params.addValue("currency_code", tariff.getCurrencyCode());

        jdbcTemplate.update(UPDATE_TARIFF, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(UUID uid) throws TariffNotFoundException {
        // Проверка существования тарифа
        if (!exists(uid)) {
            throw new TariffNotFoundException("Tariff with UID " + uid + " not found");
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);

        jdbcTemplate.update(DELETE_TARIFF, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public TariffDALM findByUid(UUID uid) throws TariffNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            return jdbcTemplate.queryForObject(SELECT_TARIFF_BY_UID, params, tariffRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new TariffNotFoundException("Tariff with UID " + uid + " not found");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public TariffDALM findByTariffName(String tariffName) throws TariffNotFoundException {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("tariff_name", tariffName);
        try {
            return jdbcTemplate.queryForObject(SELECT_TARIFF_BY_NAME, params, tariffRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new TariffNotFoundException("Tariff with name '" + tariffName + "' not found");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TariffDALM> findAll() {
        return jdbcTemplate.query(SELECT_ALL_TARIFFS, tariffRowMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID uid) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("uid", uid);
        try {
            jdbcTemplate.queryForObject(SELECT_TARIFF_BY_UID, params, tariffRowMapper);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByTariffName(String tariffName) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("tariff_name", tariffName);
        try {
            jdbcTemplate.queryForObject(SELECT_TARIFF_BY_NAME, params, tariffRowMapper);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}