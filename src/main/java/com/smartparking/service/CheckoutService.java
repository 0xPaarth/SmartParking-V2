package com.smartparking.service;

import com.smartparking.domain.ParkingSession;
import com.smartparking.domain.ParkingSlot;
import com.smartparking.domain.exception.InvalidSessionException;
import com.smartparking.domain.exception.SlotUnavailableException;
import com.smartparking.repository.ParkingSessionRepository;
import com.smartparking.repository.ParkingSlotRepository;
import com.smartparking.service.pricing.PricingContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Calculates the final fare, closes a parking session, and releases the slot.
 * UI code must call this service and must not calculate fares or perform state transitions itself.
 */
public final class CheckoutService {
    private final ParkingSlotRepository slots;
    private final ParkingSessionRepository sessions;
    private final PricingService pricing;

    public CheckoutService(ParkingSlotRepository slots, ParkingSessionRepository sessions,
                           PricingService pricing) {
        this.slots = Objects.requireNonNull(slots, "slots");
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.pricing = Objects.requireNonNull(pricing, "pricing");
    }

    /**
     * Calculates and applies checkout pricing, closes the session, and releases the parking bay.
     */
    public synchronized BigDecimal checkOut(UUID sessionId, Instant exitTime,
                                             double occupancyRatio, boolean reservationApplied,
                                             boolean promoApplied, boolean loyaltyMember) {
        ParkingSession session = sessions.findById(sessionId)
                .orElseThrow(() -> new InvalidSessionException(sessionId, "session was not found"));
        if (session.isClosed()) {
            throw new InvalidSessionException(sessionId, "session is already closed");
        }
        ParkingSlot slot = slots.findById(session.getSlotId())
                .orElseThrow(() -> new SlotUnavailableException(session.getSlotId()));

        PricingContext context = new PricingContext(
                session.getVehicle(), session.getEntryTime(), exitTime,
                occupancyRatio, reservationApplied, promoApplied, loyaltyMember);
        BigDecimal amount = pricing.calculate(context);

        session.close(exitTime, amount);
        slot.release();
        sessions.save(session);
        slots.save(slot);
        return amount;
    }
}
