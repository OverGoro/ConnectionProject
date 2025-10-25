// TariffRepository.java
package com.connection.tariff.repository;

import java.util.List;
import java.util.UUID;

import com.connection.tariff.exception.TariffAlreadyExistsException;
import com.connection.tariff.exception.TariffNotFoundException;
import com.connection.tariff.model.TariffDALM;

/**
 * Интерфейс репозитория для работы с тарифами в базе данных
 * Определяет контракт операций CRUD и дополнительных запросов
 */
public interface TariffRepository {
    
    /**
     * Добавление нового тарифа в базу данных
     * @param tariff объект TariffDALM с данными тарифа
     * @throws TariffAlreadyExistsException если тариф с таким UID или названием уже существует
     */
    void add(TariffDALM tariff) throws TariffAlreadyExistsException;

    /**
     * Обновление данных существующего тарифа
     * @param tariff объект TariffDALM с обновленными данными
     * @throws TariffNotFoundException если тариф с указанным UID не найден
     */
    void update(TariffDALM tariff) throws TariffNotFoundException;

    /**
     * Удаление тарифа из базы данных
     * @param uid уникальный идентификатор тарифа
     * @throws TariffNotFoundException если тариф с указанным UID не найден
     */
    void delete(UUID uid) throws TariffNotFoundException;

    /**
     * Поиск тарифа по уникальному идентификатору
     * @param uid уникальный идентификатор тарифа
     * @return объект TariffDALM с данными тарифа
     * @throws TariffNotFoundException если тариф с указанным UID не найден
     */
    TariffDALM findByUid(UUID uid) throws TariffNotFoundException;

    /**
     * Поиск тарифа по наименованию
     * @param tariffName наименование тарифа
     * @return объект TariffDALM с данными тарифа
     * @throws TariffNotFoundException если тариф с указанным названием не найден
     */
    TariffDALM findByTariffName(String tariffName) throws TariffNotFoundException;

    /**
     * Получение списка всех тарифов
     * @return список всех тарифов в базе данных
     */
    List<TariffDALM> findAll();

    /**
     * Проверка существования тарифа по UID
     * @param uid уникальный идентификатор тарифа
     * @return true если тариф существует, false в противном случае
     */
    boolean exists(UUID uid);

    /**
     * Проверка существования тарифа по наименованию
     * @param tariffName наименование тарифа
     * @return true если тариф с таким названием существует, false в противном случае
     */
    boolean existsByTariffName(String tariffName);
}