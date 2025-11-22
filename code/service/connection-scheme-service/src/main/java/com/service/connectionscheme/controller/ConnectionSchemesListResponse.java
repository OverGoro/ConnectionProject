
package com.service.connectionscheme.controller;

import com.connection.scheme.model.ConnectionSchemeDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** . */
@AllArgsConstructor
@Getter
public class ConnectionSchemesListResponse {
    private final List<ConnectionSchemeDto> schemes;
    private final PaginationInfo pagination;

    /** . */
    @AllArgsConstructor
    @Getter
    public static class PaginationInfo {
        private final int offset;
        private final int limit;
        private final int totalCount;
        private final boolean hasMore;
    }
}
