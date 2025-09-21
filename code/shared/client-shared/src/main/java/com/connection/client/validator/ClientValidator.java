package com.connection.client.validator;

import com.connection.client.exception.ClientValidateException;
import com.connection.client.model.ClientBLM;
import com.connection.client.model.ClientDALM;
import com.connection.client.model.ClientDTO;

import java.util.Date;
import java.util.regex.Pattern;
import java.util.UUID;

public class ClientValidator {

    private final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private final Pattern PASSWORD_PATTERN = Pattern
            .compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{6,}$");

    public void validate(ClientDTO client) {
        if (client == null) {
            throw new ClientValidateException("null", "Client cannot be null");
        }
        try {
            validateUid(client.getUid());
            validateEmail(client.getEmail());
            validateUsername(client.getUsername());
            validatePassword(client.getPassword());
            validateBirthDate(client.getBirthDate());
        } catch (IllegalArgumentException e) {
            if (client.getUid() != null)
                throw new ClientValidateException(client.getUid().toString(), e.getMessage());
            else
                throw new ClientValidateException("null", e.getMessage());
        }
    }

    public void validate(ClientBLM client) {
        if (client == null) {
            throw new ClientValidateException("null", "Client cannot be null.");
        }
        try {
            validateUid(client.getUid());
            validateEmail(client.getEmail());
            validateUsername(client.getUsername());
            validatePassword(client.getPassword());
            validateBirthDate(client.getBirthDate());
        } catch (IllegalArgumentException e) {
            if (client.getUid() != null)
                throw new ClientValidateException(client.getUid().toString(), e.getMessage());
            else
                throw new ClientValidateException("null", e.getMessage());
        }
    }

    public void validate(ClientDALM client) {
        if (client == null) {
            throw new ClientValidateException("null", "Client cannot be null");
        }
        try {
            validateUid(client.getUid());
            validateEmail(client.getEmail());
            validateUsername(client.getUsername());
            validatePassword(client.getPassword());
            // validateBirthDate(client.getBirthDate());
        } catch (IllegalArgumentException e) {
            if (client.getUid() != null)
                throw new ClientValidateException(client.getUid().toString(), e.getMessage());
            else
                throw new ClientValidateException("null", e.getMessage());
        }

    }

    public void validateUid(UUID uid) {
        if (uid == null) {
            throw new IllegalArgumentException("Client UID cannot be null");
        }
    }

    public void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (email.length() > 255) {
            throw new IllegalArgumentException("Email too long (max 255 chars)");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    public void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (username.length() < 3) {
            throw new IllegalArgumentException("Username too short (min 3 chars)");
        }
        if (username.length() > 20) {
            throw new IllegalArgumentException("Username too long (max 20 chars)");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Username can only contain letters, numbers and underscores");
        }
    }

    public void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password too short (min 6 chars)");
        }
        if (password.length() > 100) {
            throw new IllegalArgumentException("Password too long (max 100 chars)");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException(
                    "Password must contain at least one digit, one lowercase and one uppercase letter");
        }
    }

    public void validateBirthDate(Date birthDate) {
        if (birthDate != null) {
            Date now = new Date();

            // Проверка что дата рождения не в будущем
            if (birthDate.after(now)) {
                throw new IllegalArgumentException("Birth date cannot be in the future");
            }
        }
    }
}