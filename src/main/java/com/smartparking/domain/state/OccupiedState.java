package com.smartparking.domain.state;

import com.smartparking.domain.ParkingSlot;
import com.smartparking.domain.Reservation;
import com.smartparking.domain.SlotStatus;
import com.smartparking.domain.Vehicle;
import com.smartparking.domain.exception.InvalidSlotStateException;

/**
 * State for a bay containing a currently parked vehicle.
 */
public final class OccupiedState implements SlotState {
    @Override
    public SlotStatus status() {
        return SlotStatus.OCCUPIED;
    }

    @Override
    public void reserve(ParkingSlot slot, Reservation reservation) {
        throw invalid(slot, "reserve");
    }

    @Override
    public void occupy(ParkingSlot slot, Vehicle vehicle) {
        throw invalid(slot, "occupy");
    }

    @Override
    public void release(ParkingSlot slot) {
        slot.clearVehicle();
        slot.clearReservation();
        slot.setState(new AvailableState());
    }

    @Override
    public void maintain(ParkingSlot slot) {
        slot.clearVehicle();
        slot.clearReservation();
        slot.setState(new MaintenanceState());
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
