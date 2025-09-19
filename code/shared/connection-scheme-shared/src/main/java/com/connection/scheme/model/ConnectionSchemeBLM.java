// ConnectionSchemeBLM.java
package com.connection.scheme.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ConnectionSchemeBLM {
    
    protected UUID uid;
    
    protected UUID clientUid;
    
    protected String schemeJson;
}