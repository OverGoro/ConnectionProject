package com.connection.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** . */
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public abstract class Command extends BaseEvent {
    protected String replyTopic;
    protected String commandType;

    /** . */
    protected Command(String commandType, String sourceService,
            String replyTopic) {
        super(commandType, sourceService);
        this.commandType = commandType;
        this.replyTopic = replyTopic;
    }
}
