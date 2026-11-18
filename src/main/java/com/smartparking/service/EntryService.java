package com.smartparking.service;

import com.smartparking.domain.ParkingSession;
import com.smartparking.domain.ParkingSlot;
import com.smartparking.domain.Vehicle;
import com.smartparking.domain.exception.InvalidSlotStateException;
import com.smartparking.domain.exception.SlotUnavailableException;
import com.smartparking.repository.ParkingSessionRepository;
import com.smartparking.repository.ParkingSlotRepository;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Handles vehicle entry and creates an open parking session.
 */
public final class EntryService {
    private final ParkingSlotRepository slots;
    private final ParkingSessionRepository sessions;

    public EntryService(ParkingSlotRepository slots, ParkingSessionRepository sessions) {
        this.slots = Objects.requireNonNull(slots, "slots");
        this.sessions = Objects.requireNonNull(sessions, "sessions");
    }

    /**
     * Occupies a bay and records entry in one service operation.
     */
    public synchronized ParkingSession checkIn(String slotId, Vehicle vehicle, Instant entryTime) {
        ParkingSlot slot = slots.findById(slotId)
                .orElseThrow(() -> new SlotUnavailableException(slotId));
        Objects.requireNonNull(vehicle, "vehicle");
        Instant entry = Objects.requireNonNull(entryTime, "entryTime");
        try {
            slot.occupy(vehicle);
        } catch (InvalidSlotStateException exception) {
            throw new SlotUnavailableException(slotId);
        }
        ParkingSession session = new ParkingSession(UUID.randomUUID(), slotId, vehicle, entry);
        sessions.save(session);
        slots.save(slot);
        return session;
    }
}
