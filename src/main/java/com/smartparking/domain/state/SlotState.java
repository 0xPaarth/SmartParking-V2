package com.smartparking.domain.state;

import com.smartparking.domain.ParkingSlot;
import com.smartparking.domain.Reservation;
import com.smartparking.domain.Vehicle;

/**
 * Strategy for transitions allowed for one parking-slot lifecycle state.
 * Each concrete state validates its own operations before mutating the owning slot.
 */
public interface SlotState {
    /**
     * Returns the externally visible status represented by this state.
     */
    com.smartparking.domain.SlotStatus status();

    void reserve(ParkingSlot slot, Reservation reservation);

    void occupy(ParkingSlot slot, Vehicle vehicle);

    void release(ParkingSlot slot);

    void maintain(ParkingSlot slot);

    void finishMaintenance(ParkingSlot slot);

    void cancelReservation(ParkingSlot slot);
}
