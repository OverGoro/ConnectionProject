// UpdateBufferRequest.java
package com.service.buffer.controller;

import java.util.UUID;

import com.connection.processing.buffer.model.BufferDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UpdateBufferRequest {
    private BufferDTO bufferDTO;
    private UUID connectionSchemeUid;
}