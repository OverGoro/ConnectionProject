package com.connection.message;

import com.connection.message.model.MessageBlm;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** . */
public interface MessageService {
    /** . */
    void addMessage(MessageBlm messageBlm);

    /** . */
    List<MessageBlm> getMessagesByBuffer(UUID bufferUuid, boolean deleteOnGet,
            int offset, int limit);

    /** . */
    List<MessageBlm> getMessagesByScheme(UUID schemeUuid, boolean deleteOnGet,
            int offset, int limit);

    /** . */
    List<MessageBlm> getMessagesByDevice(UUID devicUuid, boolean deleteOnGet,
            int offset, int limit);

    /** . */
    Map<String, String> health();
}
