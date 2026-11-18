package com.smartparking.service;

import com.smartparking.domain.ParkingSlot;
import com.smartparking.domain.Reservation;
import com.smartparking.domain.exception.InvalidSlotStateException;
import com.smartparking.domain.exception.SlotUnavailableException;
import com.smartparking.repository.ParkingSlotRepository;
import com.smartparking.repository.ReservationRepository;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Coordinates a reservation transaction across repositories and the parking-slot state machine.
 * Slot transitions occur before persistence; a repository failure must not leave a confirmed booking.
 */
public final class ReservationService {
    private final ParkingSlotRepository slots;
    private final ReservationRepository reservations;

    public ReservationService(ParkingSlotRepository slots, ReservationRepository reservations) {
        this.slots = Objects.requireNonNull(slots, "slots");
        this.reservations = Objects.requireNonNull(reservations, "reservations");
    }

    /**
     * Atomically reserves the selected bay for a driver. The state machine rejects occupied,
     * reserved, or maintenance bays, preventing two simultaneous reservations.
     *
     * @param driverId authenticated driver's identifier
     * @param slotId bay identifier
     * @param start beginning of the requested interval
     * @param end end of the requested interval
     * @return persisted reservation
     */
    public synchronized Reservation reserve(UUID driverId, String slotId,
                                            Instant start, Instant end) {
        ParkingSlot slot = slots.findById(slotId)
                .orElseThrow(() -> new SlotUnavailableException(slotId));
        Objects.requireNonNull(driverId, "driverId");
        Instant validStart = Objects.requireNonNull(start, "start");
        Instant validEnd = Objects.requireNonNull(end, "end");
        if (!validEnd.isAfter(validStart)) {
            throw new IllegalArgumentException("Reservation end must be after start");
        }

        Reservation reservation = new Reservation(
                UUID.randomUUID(), driverId, slotId, validStart, validEnd);
        try {
            slot.reserve(reservation);
        } catch (InvalidSlotStateException exception) {
            throw new SlotUnavailableException(slotId);
        }

        Reservation saved = reservations.save(reservation);
        slots.save(slot);
        return saved;
    }

    /**
     * Cancels a reservation and returns the bay to Available only when its state permits it.
     */
    public synchronized void cancel(UUID reservationId) {
        Reservation reservation = reservations.findById(reservationId)
                .orElseThrow(() -> new com.smartparking.domain.exception.ReservationNotFoundException(reservationId));
        ParkingSlot slot = slots.findById(reservation.getSlotId())
                .orElseThrow(() -> new SlotUnavailableException(reservation.getSlotId()));
        slot.cancelReservation();
        reservation.cancel();
        reservations.save(reservation);
        slots.save(slot);
    }
}
