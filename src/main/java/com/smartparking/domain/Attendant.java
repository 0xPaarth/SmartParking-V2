package com.smartparking.domain;

import java.util.UUID;

/**
 * Operational role that performs vehicle check-in, check-out, and slot maintenance updates.
 */
public final class Attendant extends User {
    public Attendant(UUID id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public Role getRole() {
        return Role.ATTENDANT;
    }
}
