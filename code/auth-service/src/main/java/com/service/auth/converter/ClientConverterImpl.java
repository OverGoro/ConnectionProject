package com.service.auth.converter;

import org.springframework.stereotype.Component;

import com.service.auth.model.ClientBLM;
import com.service.auth.model.ClientDALM;
import com.service.auth.model.ClientDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ClientConverterImpl {
    public ClientBLM toBLM(ClientDTO dto){
        return new ClientBLM(dto.getUid(), dto.getBirthDate(), dto.getEmail(), dto.getPassword(), dto.getUsername());
    }
    public ClientBLM toBlm(ClientDALM dalm){
        return new ClientBLM(dalm.getUid(), dalm.getBirthDate(), dalm.getEmail(), dalm.getPassword(), dalm.getUsername());
    }
    public ClientDTO toDTO(ClientBLM blm){
        return new ClientDTO(blm.getUid(), blm.getBirthDate(), blm.getEmail(), blm.getPassword(), blm.getUsername());
    }
    public ClientDALM toDALM(ClientBLM blm){
        return new ClientDALM(blm.getUid(), blm.getBirthDate(), blm.getEmail(), blm.getPassword(), blm.getUsername());
    }
}
