package com.connection.message;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.connection.message.model.MessageBLM;

public interface MessageService {
    void addMessage(MessageBLM messageBLM);
    List<MessageBLM> getMessagesByBuffer(UUID bufferUuid, boolean deleteOnGet, int offset, int limit);
    List<MessageBLM> getMessagesByScheme(UUID schemeUuid, boolean deleteOnGet, int offset, int limit);
    List<MessageBLM> getMessagesByDevice(UUID devicUuid, boolean deleteOnGet, int offset, int limit);
    Map<String, String> health();
}