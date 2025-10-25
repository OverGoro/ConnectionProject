package com.connection.message.model;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class MessageDTO {
    protected UUID uid;
    protected UUID bufferUid;
    protected String content;
    protected String contentType;
    protected Date createdAt;
}