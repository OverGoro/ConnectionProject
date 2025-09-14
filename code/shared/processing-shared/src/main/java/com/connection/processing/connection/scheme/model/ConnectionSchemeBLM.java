// ConnectionSchemeBLM.java
package com.connection.processing.connection.scheme.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * BLM для схемы подключения
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConnectionSchemeBLM {
    @NonNull
    protected UUID uid;
    @NonNull
    protected UUID clientUid;
    @NonNull
    protected String schemeJson;
}