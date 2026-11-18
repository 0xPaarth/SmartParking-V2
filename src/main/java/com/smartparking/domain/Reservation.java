package com.smartparking.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Time-bound assignment of a parking bay to a driver.
 * The immutable interval and status-oriented workflow prevent conflicting state changes.
 */
public final class Reservation {
    private final UUID id;
    private final UUID driverId;
    private final String slotId;
    private final Instant startTime;
    private final Instant endTime;
    private ReservationStatus status;

    public Reservation(UUID id, UUID driverId, String slotId, Instant startTime, Instant endTime) {
        this.id = Objects.requireNonNull(id, "id");
        this.driverId = Objects.requireNonNull(driverId, "driverId");
        this.slotId = requireText(slotId, "slotId");
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.endTime = Objects.requireNonNull(endTime, "endTime");
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDriverId() {
        return driverId;
    }

    public String getSlotId() {
        return slotId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void activate() {
        requireStatus(ReservationStatus.CONFIRMED);
        status = ReservationStatus.ACTIVE;
    }

    public void complete() {
        if (status != ReservationStatus.ACTIVE && status != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed or active reservations can be completed");
        }
        status = ReservationStatus.COMPLETED;
    }

    public void cancel() {
        if (status == ReservationStatus.ACTIVE || status == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("Active or completed reservations cannot be cancelled");
        }
        status = ReservationStatus.CANCELLED;
    }

    private void requireStatus(ReservationStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Expected " + expected + " but was " + status);
        }
    }

    private static String requireText(String value, String field) {
        String normalized = Objects.requireNonNull(value, field).trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized;
    }
}
