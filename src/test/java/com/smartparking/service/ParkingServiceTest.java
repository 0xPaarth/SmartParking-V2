package com.smartparking.service;

import com.smartparking.domain.ParkingSession;
import com.smartparking.domain.ParkingSlot;
import com.smartparking.domain.SlotStatus;
import com.smartparking.domain.VehicleType;
import com.smartparking.repository.InMemoryParkingSessionRepository;
import com.smartparking.repository.InMemoryParkingSlotRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkingServiceTest {
    @Test
    void walkInEntryAndExitOccupyAndReleaseTheSlot() {
        ParkingSlot slot = new ParkingSlot("A-1", "A");
        var slots = new InMemoryParkingSlotRepository(List.of(slot));
        var sessions = new InMemoryParkingSessionRepository();
        var entry = new EntryService(slots, sessions);
        var checkout = new CheckoutService(slots, sessions, new PricingService());
        var parking = new ParkingService(slots, sessions, entry, checkout);

        ParkingSession session = parking.vehicleEntry("MH-12-3456", VehicleType.CAR, "A-1");

        assertEquals(SlotStatus.OCCUPIED, slot.getStatus());
        assertTrue(parking.findActiveSession("mh-12-3456").isPresent());

        parking.vehicleExit(session);

        assertEquals(SlotStatus.AVAILABLE, slot.getStatus());
        assertTrue(session.isClosed());
    }

    @Test
    void checkInAutomaticallySuggestsAvailableSlot() {
        ParkingSlot occupied = new ParkingSlot("A-1", "A");
        occupied.occupy(new com.smartparking.domain.Vehicle(
                "DL01AA0001", VehicleType.CAR));
        ParkingSlot available = new ParkingSlot("A-2", "A");
        var slots = new InMemoryParkingSlotRepository(List.of(occupied, available));
        var sessions = new InMemoryParkingSessionRepository();
        var entry = new EntryService(slots, sessions);
        var parking = new ParkingService(
                slots, sessions, entry,
                new CheckoutService(slots, sessions, new PricingService()));

        ParkingSession session = parking.vehicleEntry(
                "MH12BB2222", VehicleType.BIKE, null);

        assertEquals("A-2", session.getSlotId());
    }
}
