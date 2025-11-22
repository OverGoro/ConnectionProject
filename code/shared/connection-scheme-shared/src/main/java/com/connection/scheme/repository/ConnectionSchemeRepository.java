
package com.connection.scheme.repository;

import com.connection.scheme.exception.ConnectionSchemeAlreadyExistsException;
import com.connection.scheme.exception.ConnectionSchemeNotFoundException;
import com.connection.scheme.model.ConnectionSchemeBlm;
import java.util.List;
import java.util.UUID;

/** . */
public interface ConnectionSchemeRepository {
    /**
     * Добавить новуб схему.
     * 
     * @param scheme .
     * @throws ConnectionSchemeAlreadyExistsException .
     */
    void add(ConnectionSchemeBlm scheme)
            throws ConnectionSchemeAlreadyExistsException;

    /**
     * Обновить параметры схемы.
     * 
     * @param scheme .
     * @throws ConnectionSchemeNotFoundException .
     */
    void update(ConnectionSchemeBlm scheme)
            throws ConnectionSchemeNotFoundException;

    /**
     * Удалить схему по Uid.
     * 
     * @param uid .
     * @throws ConnectionSchemeNotFoundException .
     */
    void delete(UUID uid) throws ConnectionSchemeNotFoundException;

    /**
     * Получить схему по Uid.
     * 
     * @param uid .
     * @return .
     * @throws ConnectionSchemeNotFoundException .
     */
    ConnectionSchemeBlm findByUid(UUID uid)
            throws ConnectionSchemeNotFoundException;

    /**
     * Получить все схемы клиента.
     * 
     * @param clientUid .
     * @return .
     */
    List<ConnectionSchemeBlm> findByClientUid(UUID clientUid);

    /**
     * Получить все схемы, в которых участвует буфер .
     * 
     * @param bufferUid .
     * @return .
     */
    List<ConnectionSchemeBlm> findByBufferUid(UUID bufferUid);

    /** . */
    boolean exists(UUID uid);
}
