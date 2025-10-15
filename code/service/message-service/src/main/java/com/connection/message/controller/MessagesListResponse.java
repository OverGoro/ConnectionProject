// MessagesListResponse.java
package com.connection.message.controller;

import com.connection.message.model.MessageDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class MessagesListResponse {
    private final List<MessageDTO> messages;
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