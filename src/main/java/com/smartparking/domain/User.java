package com.smartparking.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Base user entity. Role-specific behavior is represented by subclasses while authentication
 * and identity data remain common to all users.
 */
public abstract class User {
    private UUID id;
    private String name;
    private String email;

    protected User(UUID id, String name, String email) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = requireText(name, "name");
        this.email = requireText(email, "email");
    }

    public abstract Role getRole();

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = requireText(name, "name");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = requireText(email, "email");
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
