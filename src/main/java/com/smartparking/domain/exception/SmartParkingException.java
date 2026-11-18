package com.smartparking.domain.exception;

/**
 * Base checked-style domain exception for anticipated parking business-rule failures.
 * Subclasses describe one specific error so callers can present a precise message.
 */
public class SmartParkingException extends RuntimeException {
    public SmartParkingException(String message) {
        super(message);
    }

    public SmartParkingException(String message, Throwable cause) {
        super(message, cause);
    }
}
