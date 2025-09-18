// ConnectionSchemeDALM.java
package com.connection.scheme.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * DALM для схемы подключения
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConnectionSchemeDALM {
    @NonNull
    protected UUID uid;
    @NonNull
    protected UUID clientUid;
    @NonNull
    protected String schemeJson;
}