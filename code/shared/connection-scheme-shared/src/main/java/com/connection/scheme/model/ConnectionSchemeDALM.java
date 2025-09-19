// ConnectionSchemeDALM.java
package com.connection.scheme.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DALM для схемы подключения
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ConnectionSchemeDALM {
    
    protected UUID uid;
    
    protected UUID clientUid;
    
    protected String schemeJson;
}