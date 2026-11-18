package com.smartparking.domain.state;

import com.smartparking.domain.ParkingSlot;
import com.smartparking.domain.Reservation;
import com.smartparking.domain.SlotStatus;
import com.smartparking.domain.Vehicle;
import com.smartparking.domain.exception.InvalidSlotStateException;

/**
 * State for a bay that is ready for reservation or direct vehicle entry.
 */
public final class AvailableState implements SlotState {
    @Override
    public SlotStatus status() {
        return SlotStatus.AVAILABLE;
    }

    @Override
    public void reserve(ParkingSlot slot, Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("reservation must not be null");
        }
        slot.setState(new ReservedState());
        slot.setReservation(reservation);
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
        slot.setState(new MaintenanceState());
        slot.clearVehicle();
        slot.clearReservation();
    }

    @Override
    public void finishMaintenance(ParkingSlot slot) {
        throw invalid(slot, "finishMaintenance");
    }

    @Override
    public void cancelReservation(ParkingSlot slot) {
        throw invalid(slot, "cancelReservation");
    }

    private InvalidSlotStateException invalid(ParkingSlot slot, String action) {
        return new InvalidSlotStateException(slot.getId(), action, status().name());
    }
}
