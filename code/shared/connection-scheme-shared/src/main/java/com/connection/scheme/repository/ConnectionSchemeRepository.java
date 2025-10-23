// ConnectionSchemeRepository.java
package com.connection.scheme.repository;

import java.util.List;
import java.util.UUID;

import com.connection.scheme.exception.ConnectionSchemeAlreadyExistsException;
import com.connection.scheme.exception.ConnectionSchemeNotFoundException;
import com.connection.scheme.model.ConnectionSchemeBLM;

public interface ConnectionSchemeRepository {
    /**
     * Добавить новуб схему
     * @param scheme
     * @throws ConnectionSchemeAlreadyExistsException
     */
    void add(ConnectionSchemeBLM scheme) throws ConnectionSchemeAlreadyExistsException;
    /**
     * Обновить параметры схемы
     * @param scheme
     * @throws ConnectionSchemeNotFoundException
     */
    void update(ConnectionSchemeBLM scheme) throws ConnectionSchemeNotFoundException;

    /**
     * Удалить схему по Uid
     * @param uid
     * @throws ConnectionSchemeNotFoundException
     */
    void delete(UUID uid) throws ConnectionSchemeNotFoundException;
    /**
     * Получить схему по Uid
     * @param uid
     * @return
     * @throws ConnectionSchemeNotFoundException
     */
    ConnectionSchemeBLM findByUid(UUID uid) throws ConnectionSchemeNotFoundException;
    /**
     * Получить все схемы клиента
     * @param clientUid
     * @return
     */
    List<ConnectionSchemeBLM> findByClientUid(UUID clientUid);

    /**
     * Получить все схемы, в которых участвует буфер
     * @param bufferUid
     * @return
     */
    List<ConnectionSchemeBLM> findByBufferUid(UUID bufferUid);
    /**
     * Проверить существование схемы
     * @param uid
     * @return
     */
    boolean exists(UUID uid);
}