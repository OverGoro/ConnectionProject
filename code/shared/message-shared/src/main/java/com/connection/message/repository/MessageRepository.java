package com.connection.message.repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.connection.message.exception.MessageAddException;
import com.connection.message.exception.MessageNotFoundException;
import com.connection.message.model.MessageBLM;

public interface MessageRepository {
    public void add(MessageBLM messageBLM)
            throws MessageAddException;

    public MessageBLM findByUid(UUID uuid)
            throws MessageNotFoundException;

    public List<MessageBLM> findByBufferUid(UUID bufferUid)
            throws MessageNotFoundException;

    public List<MessageBLM> findByBufferUidAndTimeRange(UUID bufferUid, Date startTime, Date endTime)
            throws MessageNotFoundException;

    public void deleteByUid(UUID uuid)
            throws MessageNotFoundException;

    public void deleteByBufferUid(UUID bufferUid)
            throws MessageNotFoundException;
}