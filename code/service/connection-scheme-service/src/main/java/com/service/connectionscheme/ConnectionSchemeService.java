// ConnectionSchemeService.java
package com.service.connectionscheme;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDTO;

public interface ConnectionSchemeService {
    ConnectionSchemeBLM createScheme(UUID clientUuid, ConnectionSchemeDTO schemeDTO);
    ConnectionSchemeBLM getSchemeByUid(UUID clientUuid, UUID schemeUid);
    List<ConnectionSchemeBLM> getSchemesByClient(UUID clientUuid);
    List<ConnectionSchemeBLM> getSchemesByBuffer(UUID bufferUuid);
    ConnectionSchemeBLM updateScheme(UUID clientUuid, UUID schemeUid, ConnectionSchemeDTO schemeDTO);
    void deleteScheme(UUID clientUuid, UUID schemeUid);
    boolean schemeExists(UUID clientUuid, UUID schemeUid);
    Map<String, Object> getHealthStatus();
}