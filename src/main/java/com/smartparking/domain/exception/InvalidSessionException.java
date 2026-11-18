package com.smartparking.domain.exception;

import java.util.UUID;

/**
 * Thrown when a parking session cannot be entered or exited in its current lifecycle state.
 */
public final class InvalidSessionException extends SmartParkingException {
    public InvalidSessionException(UUID id, String reason) {
        super("Parking session " + id + " is invalid: " + reason);
    }
}
