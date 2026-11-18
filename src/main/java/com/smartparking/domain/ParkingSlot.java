package com.smartparking.domain;

import com.smartparking.domain.state.AvailableState;
import com.smartparking.domain.state.SlotState;

import java.util.Objects;

/**
 * Aggregate representing one physical parking bay.
 * Valid transitions are owned by {@link SlotState}; UI or service code must not mutate status directly.
 */
public final class ParkingSlot {
    private final String id;
    private final String zone;
    private SlotState state;
    private Vehicle vehicle;
    private Reservation reservation;

    public ParkingSlot(String id, String zone) {
        this.id = requireText(id, "id");
        this.zone = requireText(zone, "zone");
        this.state = new AvailableState();
    }

    public void reserve(Reservation reservation) {
        state.reserve(this, reservation);
    }

    public void occupy(Vehicle vehicle) {
        state.occupy(this, vehicle);
    }

    public void release() {
        state.release(this);
    }

    public void maintain() {
        state.maintain(this);
    }

    public void finishMaintenance() {
        state.finishMaintenance(this);
    }

    public void cancelReservation() {
        state.cancelReservation(this);
    }

    public String getId() {
        return id;
    }

    public String getZone() {
        return zone;
    }

    public SlotStatus getStatus() {
        return state.status();
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setState(SlotState state) {
        this.state = Objects.requireNonNull(state, "state");
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void clearVehicle() {
        this.vehicle = null;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public void clearReservation() {
        this.reservation = null;
    }

    private static String requireText(String value, String field) {
        String normalized = Objects.requireNonNull(value, field).trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized;
    }
}
