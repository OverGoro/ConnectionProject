// ConnectionSchemeService.java
package com.service.connectionscheme;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.connection.scheme.model.ConnectionSchemeBLM;
import com.connection.scheme.model.ConnectionSchemeDTO;

public interface ConnectionSchemeService {
    ConnectionSchemeBLM createScheme(String accessToken, ConnectionSchemeDTO schemeDTO);
    ConnectionSchemeBLM getScheme(String accessToken, UUID schemeUid);
    List<ConnectionSchemeBLM> getSchemesByClient(String accessToken);
    List<ConnectionSchemeBLM> getSchemesByBuffer(String accessToken, UUID bufferUuid);
    ConnectionSchemeBLM updateScheme(String accessToken, UUID schemeUid, ConnectionSchemeDTO schemeDTO);
    void deleteScheme(String accessToken, UUID schemeUid);
    boolean schemeExists(String accessToken, UUID schemeUid);
    Map<String, Object> getHealthStatus();
}