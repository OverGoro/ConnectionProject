package com.connection.client.converter;

import org.springframework.stereotype.Component;

import com.connection.client.model.ClientBLM;
import com.connection.client.model.ClientDALM;
import com.connection.client.model.ClientDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ClientConverter {
    public ClientBLM toBLM(ClientDTO dto){
        return new ClientBLM(dto.getUid(), dto.getBirthDate(), dto.getEmail(), dto.getPassword(), dto.getUsername());
    }
    public ClientBLM toBLM(ClientDALM dalm){
        return new ClientBLM(dalm.getUid(), dalm.getBirthDate(), dalm.getEmail(), dalm.getPassword(), dalm.getUsername());
    }
    public ClientDTO toDTO(ClientBLM blm){
        return new ClientDTO(blm.getUid(), blm.getBirthDate(), blm.getEmail(), blm.getPassword(), blm.getUsername());
    }
    public ClientDALM toDALM(ClientBLM blm){
        return new ClientDALM(blm.getUid(), blm.getBirthDate(), blm.getEmail(), blm.getPassword(), blm.getUsername());
    }
    
}
