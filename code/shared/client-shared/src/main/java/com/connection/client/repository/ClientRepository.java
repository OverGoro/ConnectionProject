package com.connection.client.repository;

import java.util.UUID;

import com.connection.client.exception.ClientAlreadyExisistsException;
import com.connection.client.exception.ClientNotFoundException;
import com.connection.client.model.ClientBLM;

import reactor.core.publisher.Mono;

public interface ClientRepository {
    public Mono<Void> add(ClientBLM clientBLM)
            throws ClientAlreadyExisistsException;

    public Mono<ClientBLM> findByUid(UUID uuid)
            throws ClientNotFoundException;

    public Mono<ClientBLM> findByEmail(String emailString)
            throws ClientNotFoundException;

    public Mono<ClientBLM> findByUsername(String usernameString)
            throws ClientNotFoundException;

    public Mono<ClientBLM> findByEmailPassword(String emailString, String passwordString)
            throws ClientNotFoundException;

    public Mono<ClientBLM> findByUsernamePassword(String usernameString, String passwordString)
            throws ClientNotFoundException;
    
    public Mono<Void> deleteByUid(UUID uuid)
        throws ClientNotFoundException;
    
}