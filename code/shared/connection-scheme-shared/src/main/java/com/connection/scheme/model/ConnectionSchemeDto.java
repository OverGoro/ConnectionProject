
package com.connection.scheme.model;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** . */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ConnectionSchemeDto {

    protected String uid;

    protected String clientUid;

    protected List<UUID> usedBuffers;

    protected String schemeJson;
}
