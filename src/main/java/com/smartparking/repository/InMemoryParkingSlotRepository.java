package com.smartparking.repository;

import com.smartparking.domain.ParkingSlot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Thread-safe-lock-ready in-memory implementation for tests and the initial desktop prototype. */
public final class InMemoryParkingSlotRepository implements ParkingSlotRepository {
    private final Map<String, ParkingSlot> slots = new LinkedHashMap<>();

    public InMemoryParkingSlotRepository(List<ParkingSlot> initialSlots) {
        for (ParkingSlot slot : initialSlots) {
            save(slot);
        }
    }

    @Override
    public synchronized Optional<ParkingSlot> findById(String slotId) {
        return Optional.ofNullable(slots.get(slotId));
    }

    @Override
    public synchronized List<ParkingSlot> findAll() {
        return new ArrayList<>(slots.values());
    }

    @Override
    public synchronized ParkingSlot save(ParkingSlot slot) {
        slots.put(slot.getId(), slot);
        return slot;
    }
}
