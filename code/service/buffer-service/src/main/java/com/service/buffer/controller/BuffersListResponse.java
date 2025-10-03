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
}