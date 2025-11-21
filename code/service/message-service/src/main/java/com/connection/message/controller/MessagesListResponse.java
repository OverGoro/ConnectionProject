package com.connection.message.controller;

import com.connection.message.model.MessageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** . */
@AllArgsConstructor
@Getter
@Schema(description = "Ответ с списком сообщений и информацией о пагинации")
public class MessagesListResponse {

    @Schema(description = "Список сообщений")
    private final List<MessageDto> messages;

    @Schema(description = "Информация о пагинации")
    private final PaginationInfo pagination;

    /** . */
    @AllArgsConstructor
    @Getter
    @Schema(description = "Информация о пагинации")
    public static class PaginationInfo {

        @Schema(description = "Смещение", example = "0")
        private final int offset;

        @Schema(description = "Лимит", example = "50")
        private final int limit;

        @Schema(description = "Общее количество элементов")
        private final int totalCount;

        @Schema(description = "Есть ли еще элементы")
        private final boolean hasMore;
    }
}
