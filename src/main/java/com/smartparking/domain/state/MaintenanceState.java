package com.smartparking.domain.state;

import com.smartparking.domain.ParkingSlot;
import com.smartparking.domain.Reservation;
import com.smartparking.domain.SlotStatus;
import com.smartparking.domain.Vehicle;
import com.smartparking.domain.exception.InvalidSlotStateException;

/**
 * State for a bay temporarily removed from service.
 */
public final class MaintenanceState implements SlotState {
    @Override
    public SlotStatus status() {
        return SlotStatus.MAINTENANCE;
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
        throw invalid(slot, "release");
    }

    @Override
    public void maintain(ParkingSlot slot) {
        throw invalid(slot, "maintain");
    }

    @Override
    public void finishMaintenance(ParkingSlot slot) {
        slot.clearVehicle();
        slot.clearReservation();
        slot.setState(new AvailableState());
    }

    @Override
    public void cancelReservation(ParkingSlot slot) {
        throw invalid(slot, "cancelReservation");
    }

    private InvalidSlotStateException invalid(ParkingSlot slot, String action) {
        return new InvalidSlotStateException(slot.getId(), action, status().name());
    }
}
