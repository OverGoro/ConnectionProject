package com.connection.token.model;

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class RefreshTokenBLM {
    @NonNull
    protected String token;
    @NonNull
    protected UUID uid;
    @NonNull
    protected UUID clientUID;
    
    @NonNull
    protected Date createdAt;
    @NonNull
    protected Date expiresAt;

}
