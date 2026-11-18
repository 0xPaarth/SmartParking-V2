package com.smartparking.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Records one vehicle stay from entry until exit.
 * Amount is null until checkout pricing is calculated and the session is closed.
 */
public final class ParkingSession {
    private final UUID id;
    private final String slotId;
    private final Vehicle vehicle;
    private final Instant entryTime;
    private Instant exitTime;
    private BigDecimal amount;
    private boolean closed;

    public ParkingSession(UUID id, String slotId, Vehicle vehicle, Instant entryTime) {
        this.id = Objects.requireNonNull(id, "id");
        this.slotId = requireText(slotId, "slotId");
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle");
        this.entryTime = Objects.requireNonNull(entryTime, "entryTime");
    }

    public void close(Instant exitTime, BigDecimal amount) {
        if (closed) {
            throw new IllegalStateException("Parking session is already closed");
        }
        if (!Objects.requireNonNull(exitTime, "exitTime").isAfter(entryTime)) {
            throw new IllegalArgumentException("exitTime must be after entryTime");
        }
        BigDecimal finalAmount = Objects.requireNonNull(amount, "amount");
        if (finalAmount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        this.exitTime = exitTime;
        this.amount = finalAmount;
        this.closed = true;
    }

    public UUID getId() {
        return id;
    }

    public String getSlotId() {
        return slotId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Instant getEntryTime() {
        return entryTime;
    }

    public Instant getExitTime() {
        return exitTime;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public boolean isClosed() {
        return closed;
    }

    private static String requireText(String value, String field) {
        String normalized = Objects.requireNonNull(value, field).trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized;
    }
}
