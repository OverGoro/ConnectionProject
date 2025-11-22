package com.connection.auth.events.domain;

import com.connection.common.events.BaseEvent;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/** . */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class UserCreatedEvent extends BaseEvent {
    private UUID clientUid;
    private String email;
    private String username;
    
    /** . */
    public UserCreatedEvent(UUID clientUid, String email, String username) {
        super("USER_CREATED", "auth-service");
        this.clientUid = clientUid;
        this.email = email;
        this.username = username;
    }
}