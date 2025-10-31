// MessageResponse.java
package com.connection.message.controller;

import java.util.List;

import com.connection.message.model.MessageDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(description = "Ответ с списком сообщений")
public class MessageResponse {
    
    @Schema(description = "Список сообщений")
    private final List<MessageDTO> messageDTOs;
}