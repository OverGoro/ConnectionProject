package com.connection.message.controller;

import com.connection.message.model.MessageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** . */
@AllArgsConstructor
@Getter
@Schema(description = "Ответ с списком сообщений")
public class MessageResponse {
    
    @Schema(description = "Список сообщений")
    private final List<MessageDto> messageDtos;
}