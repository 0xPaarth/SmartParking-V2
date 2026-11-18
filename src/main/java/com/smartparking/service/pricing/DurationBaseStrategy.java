package com.smartparking.service.pricing;

import java.math.BigDecimal;

/** Step 1: rounds stay time to full billed hours and applies vehicle base hourly rate. */
public final class DurationBaseStrategy implements PricingStrategy {
    @Override
    public BigDecimal apply(PricingContext context) {
        BigDecimal amount = context.getVehicle().getType().getBaseHourlyRate()
                .multiply(BigDecimal.valueOf(context.billedHours()));
        context.setAmount(amount);
        return amount;
    }
}
