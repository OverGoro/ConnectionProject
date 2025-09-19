package com.connection.client.repository;

import java.util.UUID;

import com.connection.client.exception.ClientAlreadyExisistsException;
import com.connection.client.exception.ClientNotFoundException;
import com.connection.client.model.ClientDALM;

public interface ClientRepository {
    public void add(ClientDALM clientDALM)
            throws ClientAlreadyExisistsException;

    public ClientDALM findByUid(UUID uuid)
            throws ClientNotFoundException;

    public ClientDALM findByEmail(String emailString)
            throws ClientNotFoundException;

    public ClientDALM findByUsername(String usernameString)
            throws ClientNotFoundException;

    public ClientDALM findByEmailPassword(String emailString, String passwordString)
            throws ClientNotFoundException;

    public ClientDALM findByUsernamePassword(String usernameString, String passwordString)
            throws ClientNotFoundException;
    
    public void deleteByUid(UUID uuid)
        throws ClientNotFoundException;
    
}
