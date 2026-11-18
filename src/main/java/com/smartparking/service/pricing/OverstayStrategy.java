package com.smartparking.service.pricing;

import java.math.BigDecimal;
import java.time.Duration;

/** Step 8: adds ₹50 for every 30 minutes beyond two hours. */
public final class OverstayStrategy implements PricingStrategy {
    public static final BigDecimal OVERSTAY_FEE = new BigDecimal("50");

    @Override
    public BigDecimal apply(PricingContext context) {
        long minutes = Duration.between(context.getEntryTime(), context.getExitTime()).toMinutes();
        long excessMinutes = Math.max(0, minutes - 120);
        long excessBlocks = (excessMinutes + 29) / 30;
        if (excessBlocks > 0) {
            context.setAmount(context.getAmount().add(
                    OVERSTAY_FEE.multiply(BigDecimal.valueOf(excessBlocks))));
        }
        return context.getAmount();
    }
}
