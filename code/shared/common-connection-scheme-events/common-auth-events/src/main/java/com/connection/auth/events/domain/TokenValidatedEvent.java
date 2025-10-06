package com.connection.auth.events.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

import com.connection.common.events.BaseEvent;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TokenValidatedEvent extends BaseEvent {
    private UUID clientUid;
    private boolean isValid;
    private String tokenType;
    
    public TokenValidatedEvent(UUID clientUid, boolean isValid, String tokenType) {
        super("TOKEN_VALIDATED", "auth-service");
        this.clientUid = clientUid;
        this.isValid = isValid;
        this.tokenType = tokenType;
    }
}