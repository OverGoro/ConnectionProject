// Репозиторий (интерфейс)
package com.connection.message.repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.connection.message.exception.MessageAddException;
import com.connection.message.exception.MessageNotFoundException;
import com.connection.message.model.MessageDALM;

public interface MessageRepository {
    public void add(MessageDALM messageDALM)
            throws MessageAddException;

    public MessageDALM findByUid(UUID uuid)
            throws MessageNotFoundException;

    public List<MessageDALM> findByBufferUid(UUID bufferUid)
            throws MessageNotFoundException;

    public List<MessageDALM> findByBufferUidAndTimeRange(UUID bufferUid, Date startTime, Date endTime)
            throws MessageNotFoundException;

    public void deleteByUid(UUID uuid)
            throws MessageNotFoundException;

    public void deleteByBufferUid(UUID bufferUid)
            throws MessageNotFoundException;
}