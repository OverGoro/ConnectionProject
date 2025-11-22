package com.connection.processing.buffer.repository;

import com.connection.processing.buffer.exception.BufferAlreadyExistsException;
import com.connection.processing.buffer.exception.BufferNotFoundException;
import com.connection.processing.buffer.model.BufferBlm;
import java.util.List;
import java.util.UUID;

/** . */
public interface BufferRepository {
    /** . */
    void add(BufferBlm buffer) throws BufferAlreadyExistsException;

    /** . */
    void update(BufferBlm buffer) throws BufferNotFoundException;

    /** . */
    void delete(UUID uid) throws BufferNotFoundException;

    /** . */
    BufferBlm findByUid(UUID uid) throws BufferNotFoundException;

    /** . */
    List<BufferBlm> findByDeviceUid(UUID deviceUid);

    /** . */
    boolean exists(UUID uid);

    /** . */
    void deleteByDeviceUid(UUID deviceUid);

    /** . */
    void addBufferToConnectionScheme(UUID bufferUid, UUID connectionSchemeUid);

    /** . */
    void removeBufferFromConnectionScheme(UUID bufferUid,
            UUID connectionSchemeUid);

    /** . */
    List<BufferBlm> findByConnectionSchemeUid(UUID connectionSchemeUid);
}
