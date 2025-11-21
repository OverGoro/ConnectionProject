package com.connection.client.repository;

import com.connection.client.exception.ClientAlreadyExisistsException;
import com.connection.client.exception.ClientNotFoundException;
import com.connection.client.model.ClientBlm;
import java.util.UUID;

/** . */
public interface ClientRepository {
    /** . */
    public void add(ClientBlm clientBlm) throws ClientAlreadyExisistsException;

    /** . */
    public ClientBlm findByUid(UUID uuid) throws ClientNotFoundException;

    /** . */
    public ClientBlm findByEmail(String emailString)
            throws ClientNotFoundException;

    /** . */
    public ClientBlm findByUsername(String usernameString)
            throws ClientNotFoundException;

    /** . */
    public ClientBlm findByEmailPassword(String emailString,
            String passwordString) throws ClientNotFoundException;

    /** . */
    public ClientBlm findByUsernamePassword(String usernameString,
            String passwordString) throws ClientNotFoundException;

    /** . */
    public void deleteByUid(UUID uuid) throws ClientNotFoundException;

}
