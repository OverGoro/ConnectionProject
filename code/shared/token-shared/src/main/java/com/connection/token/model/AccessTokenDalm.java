package com.connection.token.model;

import jakarta.persistence.Entity;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** . */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@EqualsAndHashCode
public class AccessTokenDalm {
    protected UUID clientUid;
    protected Date createdAt;
    protected Date expiresAt;
}
