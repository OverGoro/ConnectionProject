
package com.service.connectionscheme;

import com.connection.scheme.model.ConnectionSchemeBlm;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** . */

public interface ConnectionSchemeService {
    /** . */
    ConnectionSchemeBlm createScheme(ConnectionSchemeBlm schemeBlm);

    /** . */

    ConnectionSchemeBlm getSchemeByUid(UUID schemeUid);

    /** . */

    List<ConnectionSchemeBlm> getSchemeByUid(List<UUID> schemeUid);

    /** . */

    List<ConnectionSchemeBlm> getSchemesByClient(UUID clientUuid);

    /** . */

    List<ConnectionSchemeBlm> getSchemesByBuffer(UUID bufferUuid);

    /** . */

    List<ConnectionSchemeBlm> getSchemesByBuffer(List<UUID> bufferUuid);

    /** . */

    ConnectionSchemeBlm updateScheme(UUID schemeUid,
            ConnectionSchemeBlm schemeBlm);

    /** . */

    void deleteScheme(UUID schemeUid);

    /** . */

    boolean schemeExists(UUID schemeUid);

    /** . */

    Map<String, Object> getHealthStatus();
}
