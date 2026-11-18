package com.smartparking.domain;

import java.util.UUID;

/**
 * User role that may reserve bays and view its own sessions and payments.
 */
public final class Driver extends User {
    public Driver(UUID id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public Role getRole() {
        return Role.DRIVER;
    }
}
