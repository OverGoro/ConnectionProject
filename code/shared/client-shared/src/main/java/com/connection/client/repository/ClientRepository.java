package com.connection.client.repository;

import java.util.UUID;

import com.connection.client.exception.ClientAlreadyExisistsException;
import com.connection.client.exception.ClientNotFoundException;
import com.connection.client.model.ClientBLM;

public interface ClientRepository {
    public void add(ClientBLM clientBLM)
            throws ClientAlreadyExisistsException;

    public ClientBLM findByUid(UUID uuid)
            throws ClientNotFoundException;

    public ClientBLM findByEmail(String emailString)
            throws ClientNotFoundException;

    public ClientBLM findByUsername(String usernameString)
            throws ClientNotFoundException;

    public ClientBLM findByEmailPassword(String emailString, String passwordString)
            throws ClientNotFoundException;

    public ClientBLM findByUsernamePassword(String usernameString, String passwordString)
            throws ClientNotFoundException;
    
    public void deleteByUid(UUID uuid)
        throws ClientNotFoundException;
    
}