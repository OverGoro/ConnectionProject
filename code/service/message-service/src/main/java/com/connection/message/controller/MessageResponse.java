// MessageResponse.java
package com.connection.message.controller;

import java.util.List;

import com.connection.message.model.MessageDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MessageResponse {
    private final List<MessageDTO> messageDTOs;
}