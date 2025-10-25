package com.connection.auth.events.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

import com.connection.common.events.BaseEvent;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class UserCreatedEvent extends BaseEvent {
    private UUID clientUid;
    private String email;
    private String username;
    
    public UserCreatedEvent(UUID clientUid, String email, String username) {
        super("USER_CREATED", "auth-service");
        this.clientUid = clientUid;
        this.email = email;
        this.username = username;
    }
}