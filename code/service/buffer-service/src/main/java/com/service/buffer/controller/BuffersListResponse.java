
package com.service.buffer.controller;

import com.connection.processing.buffer.model.BufferDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** . */
@AllArgsConstructor
@Getter
public class BuffersListResponse {
    private final List<BufferDto> buffers;
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

    /** . */
    // Конструктор для обратной совместимости
    public BuffersListResponse(List<BufferDto> buffers) {
        this.buffers = buffers;
        this.pagination = null;
    }
}
