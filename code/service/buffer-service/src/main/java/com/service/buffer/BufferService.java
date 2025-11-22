package com.service.buffer;

import com.connection.processing.buffer.model.BufferBlm;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** . */
public interface BufferService {
    /** . */
    BufferBlm createBuffer(BufferBlm bufferBlm);

    /** . */
    BufferBlm getBufferByUid(UUID bufferUid);

    /** . */
    List<BufferBlm> getBuffersByClient(UUID clientUid);

    /** . */
    List<BufferBlm> getBuffersByDevice(UUID deviceUid);

    /** . */
    List<BufferBlm> getBuffersByConnectionScheme(UUID connectionSchemeUid);

    /** . */
    BufferBlm updateBuffer(UUID bufferUid, BufferBlm bufferBlm);

    /** . */
    void deleteBuffer(UUID bufferUid);

    /** . */
    void deleteBufferFromConnectionScheme(UUID connectionSchemeUid,
            UUID bufferUid);

    /** . */
    void deleteAllBuffersFromConnectionScheme(UUID connectionSchemeUid);

    /** . */
    boolean bufferExists(UUID bufferUid);

    /** . */
    Map<String, Object> getHealthStatus();
}
