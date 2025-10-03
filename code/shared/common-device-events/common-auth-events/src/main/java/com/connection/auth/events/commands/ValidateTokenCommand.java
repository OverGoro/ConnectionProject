package com.connection.auth.events.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import com.connection.common.events.Command;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ValidateTokenCommand extends Command {
    private final String token;
    private final TokenType tokenType;
    
    public ValidateTokenCommand() {
        super("VALIDATE_TOKEN", "unknown", "auth.responses");
        this.token = "unknown";
        this.tokenType = TokenType.UNKNOWN;
    }
    
    public ValidateTokenCommand(String token, String sourceService) {
        super("VALIDATE_TOKEN", sourceService, "auth.responses");
        this.token = token;
        this.tokenType = TokenType.ACCESS;
    }
    
    public ValidateTokenCommand(String token, TokenType tokenType, String sourceService) {
        super("VALIDATE_TOKEN", sourceService, "auth.responses");
        this.token = token;
        this.tokenType = tokenType;
    }
    
    public enum TokenType {
        ACCESS, REFRESH, UNKNOWN
    }
}