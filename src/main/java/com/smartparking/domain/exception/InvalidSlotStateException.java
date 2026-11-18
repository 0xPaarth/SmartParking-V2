package com.smartparking.domain.exception;

/**
 * Thrown when code asks a parking slot to perform a transition forbidden by its current state.
 * The State pattern uses this exception to stop actions such as reserving an occupied bay.
 */
public final class InvalidSlotStateException extends SmartParkingException {
    public InvalidSlotStateException(String slotId, String requestedAction, String currentState) {
        super("Slot " + slotId + " cannot perform '" + requestedAction
                + "' while in " + currentState + " state");
    }
}
