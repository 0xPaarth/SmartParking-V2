package com.smartparking.service.pricing;

import java.math.BigDecimal;

/** Step 2: applies 25 percent surge when occupancy is at least 85 percent. */
public final class SurgeStrategy implements PricingStrategy {
    public static final BigDecimal SURGE_MULTIPLIER = new BigDecimal("1.25");

    @Override
    public BigDecimal apply(PricingContext context) {
        if (context.getOccupancyRatio() >= 0.85) {
            context.setAmount(context.getAmount().multiply(SURGE_MULTIPLIER));
        }
        return context.getAmount();
    }
}
