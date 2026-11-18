package com.smartparking.service.pricing;

import java.math.BigDecimal;

/** Step 5: applies a 10 percent advance-reservation discount. */
public final class ReservationDiscountStrategy implements PricingStrategy {
    public static final BigDecimal RESERVATION_MULTIPLIER = new BigDecimal("0.90");

    @Override
    public BigDecimal apply(PricingContext context) {
        if (context.isReservationApplied()) {
            context.setAmount(context.getAmount().multiply(RESERVATION_MULTIPLIER));
        }
        return context.getAmount();
    }
}
