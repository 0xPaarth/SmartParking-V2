package com.smartparking.domain.exception;

import java.util.UUID;

/**
 * Thrown when a reservation identifier does not belong to a persisted reservation.
 */
public final class ReservationNotFoundException extends SmartParkingException {
    public ReservationNotFoundException(UUID id) {
        super("Reservation " + id + " was not found");
    }
}
