package com.smartparking.repository;

import com.smartparking.domain.ParkingSlot;

import java.util.List;
import java.util.Optional;

/** Persistence contract for parking-bay data. */
public interface ParkingSlotRepository {
    Optional<ParkingSlot> findById(String slotId);
    List<ParkingSlot> findAll();
    ParkingSlot save(ParkingSlot slot);
}
