package com.connection.client.converter;

import com.connection.client.model.ClientBlm;
import com.connection.client.model.ClientDalm;
import com.connection.client.model.ClientDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** . */
@Component
@RequiredArgsConstructor
public class ClientConverter {
    /** . */
    public ClientBlm toBlm(ClientDto dto) {
        return new ClientBlm(dto.getUid(), dto.getBirthDate(), dto.getEmail(),
                dto.getPassword(), dto.getUsername());
    }

    /** . */
    public ClientBlm toBlm(ClientDalm dalm) {
        return new ClientBlm(dalm.getUid(), dalm.getBirthDate(),
                dalm.getEmail(), dalm.getPassword(), dalm.getUsername());
    }

    /** . */
    public ClientDto toDto(ClientBlm blm) {
        return new ClientDto(blm.getUid(), blm.getBirthDate(), blm.getEmail(),
                blm.getPassword(), blm.getUsername());
    }

    /** . */
    public ClientDalm toDalm(ClientBlm blm) {
        return new ClientDalm(blm.getUid(), blm.getBirthDate(), blm.getEmail(),
                blm.getPassword(), blm.getUsername());
    }

}
