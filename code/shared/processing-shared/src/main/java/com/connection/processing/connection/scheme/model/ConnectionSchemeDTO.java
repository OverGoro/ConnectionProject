// ConnectionSchemeDTO.java
package com.connection.processing.connection.scheme.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * DTO для схемы подключения
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConnectionSchemeDTO {
    @NonNull
    protected String uid;
    @NonNull
    protected String clientUid;
    @NonNull
    protected String schemeJson;
}