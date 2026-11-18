package com.smartparking.domain.exception;

/**
 * Thrown when no available slot matches a reservation or check-in request.
 */
public final class SlotUnavailableException extends SmartParkingException {
    public SlotUnavailableException(String slotId) {
        super("Parking slot " + slotId + " is unavailable");
    }
}
