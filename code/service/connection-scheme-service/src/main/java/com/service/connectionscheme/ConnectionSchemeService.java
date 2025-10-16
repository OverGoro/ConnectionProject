// ConnectionSchemeService.java
package com.service.connectionscheme;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDTO;

public interface ConnectionSchemeService {
    ConnectionSchemeBLM createScheme(ConnectionSchemeDTO schemeDTO);
    
    ConnectionSchemeBLM getSchemeByUid(UUID schemeUid);
    List<ConnectionSchemeBLM> getSchemeByUid(List<UUID> schemeUid);

    List<ConnectionSchemeBLM> getSchemesByClient(UUID clientUuid);

    List<ConnectionSchemeBLM> getSchemesByBuffer(UUID bufferUuid);
    List<ConnectionSchemeBLM> getSchemesByBuffer(List<UUID> bufferUuid);

    ConnectionSchemeBLM updateScheme(UUID schemeUid, ConnectionSchemeDTO schemeDTO);

    void deleteScheme(UUID schemeUid);
    boolean schemeExists(UUID schemeUid);
    Map<String, Object> getHealthStatus();
}