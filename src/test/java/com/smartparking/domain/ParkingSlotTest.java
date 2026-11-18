package com.smartparking.domain;

import com.smartparking.domain.exception.InvalidSlotStateException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ParkingSlotTest {
    @Test
    void reservedSlotRejectsASecondReservation() {
        ParkingSlot slot = new ParkingSlot("A-1", "A");
        Reservation first = new Reservation(
                UUID.randomUUID(), UUID.randomUUID(), "A-1",
                Instant.parse("2026-11-18T06:00:00Z"),
                Instant.parse("2026-11-18T08:00:00Z"));
        Reservation second = new Reservation(
                UUID.randomUUID(), UUID.randomUUID(), "A-1",
                Instant.parse("2026-11-18T07:00:00Z"),
                Instant.parse("2026-11-18T09:00:00Z"));

        slot.reserve(first);

        assertEquals(SlotStatus.RESERVED, slot.getStatus());
        assertThrows(InvalidSlotStateException.class, () -> slot.reserve(second));
        assertSame(first, slot.getReservation());
    }

    @Test
    void occupiedSlotCannotBeReserved() {
        ParkingSlot slot = new ParkingSlot("A-2", "A");
        slot.occupy(new Vehicle("MH12AB1234", VehicleType.CAR));

        Reservation reservation = new Reservation(
                UUID.randomUUID(), UUID.randomUUID(), "A-2",
                Instant.parse("2026-11-18T06:00:00Z"),
                Instant.parse("2026-11-18T08:00:00Z"));

        assertThrows(InvalidSlotStateException.class, () -> slot.reserve(reservation));
    }

    @Test
    void releaseReturnsOccupiedSlotToAvailable() {
        ParkingSlot slot = new ParkingSlot("A-3", "A");
        slot.occupy(new Vehicle("DL01XX0001", VehicleType.BIKE));
        slot.release();

        assertEquals(SlotStatus.AVAILABLE, slot.getStatus());
        assertNull(slot.getVehicle());
    }
}
