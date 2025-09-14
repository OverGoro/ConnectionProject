// ConnectionSchemeRepository.java
package com.connection.processing.connection.scheme.repository;

import java.util.List;
import java.util.UUID;

import com.connection.processing.connection.scheme.exception.ConnectionSchemeAlreadyExistsException;
import com.connection.processing.connection.scheme.exception.ConnectionSchemeNotFoundException;
import com.connection.processing.connection.scheme.model.ConnectionSchemeDALM;

public interface ConnectionSchemeRepository {
    void add(ConnectionSchemeDALM scheme) throws ConnectionSchemeAlreadyExistsException;
    void update(ConnectionSchemeDALM scheme) throws ConnectionSchemeNotFoundException;
    void delete(UUID uid) throws ConnectionSchemeNotFoundException;
    ConnectionSchemeDALM findByUid(UUID uid) throws ConnectionSchemeNotFoundException;
    List<ConnectionSchemeDALM> findByClientUid(UUID clientUid);
    boolean exists(UUID uid);
}