// Валидатор
package com.connection.message.validator;

import com.connection.message.exception.MessageValidateException;
import com.connection.message.model.MessageBLM;
import com.connection.message.model.MessageDALM;
import com.connection.message.model.MessageDTO;

import java.util.Date;
import java.util.UUID;

public class MessageValidator {

    public void validate(MessageDTO message) {
        if (message == null) {
            throw new MessageValidateException("null", "Message cannot be null");
        }
        try {
            validateUid(message.getUid());
            validateBufferUid(message.getBufferUid());
            validateContent(message.getContent());
            validateContentType(message.getContentType());
            validateCreatedAt(message.getCreatedAt());
        } catch (IllegalArgumentException e) {
            if (message.getUid() != null)
                throw new MessageValidateException(message.getUid().toString(), e.getMessage());
            else
                throw new MessageValidateException("null", e.getMessage());
        }
    }

    public void validate(MessageBLM message) {
        if (message == null) {
            throw new MessageValidateException("null", "Message cannot be null.");
        }
        try {
            validateUid(message.getUid());
            validateBufferUid(message.getBufferUid());
            validateContent(message.getContent());
            validateContentType(message.getContentType());
            validateCreatedAt(message.getCreatedAt());
        } catch (IllegalArgumentException e) {
            if (message.getUid() != null)
                throw new MessageValidateException(message.getUid().toString(), e.getMessage());
            else
                throw new MessageValidateException("null", e.getMessage());
        }
    }

    public void validate(MessageDALM message) {
        if (message == null) {
            throw new MessageValidateException("null", "Message cannot be null");
        }
        try {
            validateUid(message.getUid());
            validateBufferUid(message.getBufferUid());
            validateContent(message.getContent());
            validateContentType(message.getContentType());
            validateCreatedAt(message.getCreatedAt());
        } catch (IllegalArgumentException e) {
            if (message.getUid() != null)
                throw new MessageValidateException(message.getUid().toString(), e.getMessage());
            else
                throw new MessageValidateException("null", e.getMessage());
        }
    }

    public void validateUid(UUID uid) {
        if (uid == null) {
            throw new IllegalArgumentException("Message UID cannot be null");
        }
    }

    public void validateBufferUid(UUID bufferUid) {
        if (bufferUid == null) {
            throw new IllegalArgumentException("Buffer UID cannot be null");
        }
    }

    public void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }
        if (content.length() > 10000) { // Пример ограничения
            throw new IllegalArgumentException("Content too long (max 10000 chars)");
        }
    }

    public void validateContentType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Content type cannot be empty");
        }
        if (contentType.length() > 100) {
            throw new IllegalArgumentException("Content type too long (max 100 chars)");
        }
    }

    public void validateCreatedAt(Date createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("Created at date cannot be null");
        }
        
        Date now = new Date();
        if (createdAt.after(now)) {
            throw new IllegalArgumentException("Created at date cannot be in the future");
        }
    }
}