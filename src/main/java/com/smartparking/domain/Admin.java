package com.smartparking.domain;

import java.util.UUID;

/**
 * Administrative role that manages users, slots, reports, and tariff configuration.
 */
public final class Admin extends User {
    public Admin(UUID id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }
}
