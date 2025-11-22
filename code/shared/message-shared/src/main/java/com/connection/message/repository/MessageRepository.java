package com.connection.message.repository;

import com.connection.message.exception.MessageAddException;
import com.connection.message.exception.MessageNotFoundException;
import com.connection.message.model.MessageBlm;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/** . */
public interface MessageRepository {
    /** . */
    public void add(MessageBlm messageBlm) throws MessageAddException;

    /** . */
    public MessageBlm findByUid(UUID uuid) throws MessageNotFoundException;

    /** . */
    public List<MessageBlm> findByBufferUid(UUID bufferUid)
            throws MessageNotFoundException;

    /** . */
    public List<MessageBlm> findByBufferUidAndTimeRange(UUID bufferUid,
            Date startTime, Date endTime) throws MessageNotFoundException;

    /** . */
    public void deleteByUid(UUID uuid) throws MessageNotFoundException;

    /** . */
    public void deleteByBufferUid(UUID bufferUid)
            throws MessageNotFoundException;
}
