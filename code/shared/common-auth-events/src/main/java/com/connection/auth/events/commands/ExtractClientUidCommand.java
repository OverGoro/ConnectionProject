package com.connection.auth.events.commands;

import com.connection.common.events.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ExtractClientUidCommand extends Command {
    private String token;
    private TokenType tokenType;
    
    public ExtractClientUidCommand() {
        super("EXTRACT_CLIENT_UID", "unknown", "auth.responses");
        this.token = "unknown";
        this.tokenType = TokenType.UNKNOWN;
    }
    
    public ExtractClientUidCommand(String token, String sourceService) {
        super("EXTRACT_CLIENT_UID", sourceService, "auth.responses");
        this.token = token;
        this.tokenType = TokenType.ACCESS;
    }
    
    public ExtractClientUidCommand(String token, TokenType tokenType, String sourceService) {
        super("EXTRACT_CLIENT_UID", sourceService, "auth.responses");
        this.token = token;
        this.tokenType = tokenType;
    }
    
    public enum TokenType {
        ACCESS, REFRESH, UNKNOWN
    }
}