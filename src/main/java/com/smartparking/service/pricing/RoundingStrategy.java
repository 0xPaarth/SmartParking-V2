package com.smartparking.service.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Step 9: rounds the pre-GST amount to the nearest rupee using HALF_UP. */
public final class RoundingStrategy implements PricingStrategy {
    @Override
    public BigDecimal apply(PricingContext context) {
        context.setAmount(context.getAmount().setScale(0, RoundingMode.HALF_UP));
        return context.getAmount();
    }
}
