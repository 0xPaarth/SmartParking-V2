package com.smartparking.domain.state;

import com.smartparking.domain.ParkingSlot;
import com.smartparking.domain.Reservation;
import com.smartparking.domain.SlotStatus;
import com.smartparking.domain.Vehicle;
import com.smartparking.domain.exception.InvalidSlotStateException;

/**
 * State for a bay already assigned to a reservation.
 * Entry is allowed only during the vehicle-arrival transition; another reservation is rejected.
 */
public final class ReservedState implements SlotState {
    @Override
    public SlotStatus status() {
        return SlotStatus.RESERVED;
    }

    @Override
    public void reserve(ParkingSlot slot, Reservation reservation) {
        throw invalid(slot, "reserve");
    }

    @Override
    public void occupy(ParkingSlot slot, Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("vehicle must not be null");
        }
        slot.setState(new OccupiedState());
        slot.setVehicle(vehicle);
    }

    @Override
    public void release(ParkingSlot slot) {
        throw invalid(slot, "release");
    }

    @Override
    public void maintain(ParkingSlot slot) {
        throw invalid(slot, "maintain");
    }

    @Override
    public void finishMaintenance(ParkingSlot slot) {
        throw invalid(slot, "finishMaintenance");
    }

    @Override
    public void cancelReservation(ParkingSlot slot) {
        slot.clearReservation();
        slot.setState(new AvailableState());
    }

    private InvalidSlotStateException invalid(ParkingSlot slot, String action) {
        return new InvalidSlotStateException(slot.getId(), action, status().name());
    }
}
