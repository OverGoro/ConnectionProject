
package com.service.buffer.controller;

import com.connection.processing.buffer.model.BufferDto;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** . */
@AllArgsConstructor
@Getter
@Setter
public class UpdateBufferRequest {
    private BufferDto bufferDto;
    private UUID connectionSchemeUid;
}
