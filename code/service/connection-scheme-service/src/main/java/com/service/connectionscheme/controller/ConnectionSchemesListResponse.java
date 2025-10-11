// ConnectionSchemesListResponse.java
package com.service.connectionscheme.controller;

import com.connection.scheme.model.ConnectionSchemeDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ConnectionSchemesListResponse {
    private final List<ConnectionSchemeDTO> schemes;
    private final PaginationInfo pagination;

    @AllArgsConstructor
    @Getter
    public static class PaginationInfo {
        private final int offset;
        private final int limit;
        private final int totalCount;
        private final boolean hasMore;
    }
}