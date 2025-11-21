package com.connection.token.model;

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
public class RefreshTokenDalm {
    protected String token;

    
    protected UUID uid;
    
    protected UUID clientUID;
    
    protected Date createdAt;
    
    protected Date expiresAt;
}
