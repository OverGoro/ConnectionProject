// ConnectionSchemeDTO.java
package com.connection.scheme.model;

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
public class ConnectionSchemeDTO {
    
    protected String uid;
    
    protected String clientUid;
    
    protected String schemeJson;
}