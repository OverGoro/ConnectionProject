// BuffersListResponse.java
package com.service.buffer.controller;

import com.connection.processing.buffer.model.BufferDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class BuffersListResponse {
    private final List<BufferDTO> buffers;
    private final PaginationInfo pagination;

    @AllArgsConstructor
    @Getter
    public static class PaginationInfo {
        private final int offset;
        private final int limit;
        private final int totalCount;
        private final boolean hasMore;
    }

    // Конструктор для обратной совместимости
    public BuffersListResponse(List<BufferDTO> buffers) {
        this.buffers = buffers;
        this.pagination = null;
    }
}