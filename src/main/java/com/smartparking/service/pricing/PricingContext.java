package com.smartparking.service.pricing;

import com.smartparking.domain.Vehicle;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Mutable input and running total used by the dynamic-pricing strategy chain.
 * The amount starts at zero rupees and each strategy adds or multiplies in a documented order.
 */
public final class PricingContext {
    private final Vehicle vehicle;
    private final Instant entryTime;
    private final Instant exitTime;
    private final double occupancyRatio;
    private final boolean reservationApplied;
    private final boolean promoApplied;
    private final boolean loyaltyMember;
    private BigDecimal amount;

    public PricingContext(Vehicle vehicle, Instant entryTime, Instant exitTime,
                          double occupancyRatio, boolean reservationApplied,
                          boolean promoApplied, boolean loyaltyMember) {
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle");
        this.entryTime = Objects.requireNonNull(entryTime, "entryTime");
        this.exitTime = Objects.requireNonNull(exitTime, "exitTime");
        if (!exitTime.isAfter(entryTime)) {
            throw new IllegalArgumentException("exitTime must be after entryTime");
        }
        if (occupancyRatio < 0 || occupancyRatio > 1) {
            throw new IllegalArgumentException("occupancyRatio must be between 0 and 1");
        }
        this.occupancyRatio = occupancyRatio;
        this.reservationApplied = reservationApplied;
        this.promoApplied = promoApplied;
        this.loyaltyMember = loyaltyMember;
        this.amount = BigDecimal.ZERO;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Instant getEntryTime() {
        return entryTime;
    }

    public Instant getExitTime() {
        return exitTime;
    }

    public double getOccupancyRatio() {
        return occupancyRatio;
    }

    public boolean isReservationApplied() {
        return reservationApplied;
    }

    public boolean isPromoApplied() {
        return promoApplied;
    }

    public boolean isLoyaltyMember() {
        return loyaltyMember;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = Objects.requireNonNull(amount, "amount");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
    }

    public long billedHours() {
        long minutes = java.time.Duration.between(entryTime, exitTime).toMinutes();
        if (minutes <= 0) {
            return 0;
        }
        return Math.max(1, (minutes + 59) / 60);
    }
}
