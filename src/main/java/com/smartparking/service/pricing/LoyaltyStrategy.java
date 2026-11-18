package com.smartparking.service.pricing;

import java.math.BigDecimal;

/** Step 7: applies a 10 percent loyalty discount to qualifying members. */
public final class LoyaltyStrategy implements PricingStrategy {
    public static final BigDecimal LOYALTY_MULTIPLIER = new BigDecimal("0.90");

    @Override
    public BigDecimal apply(PricingContext context) {
        if (context.isLoyaltyMember()) {
            context.setAmount(context.getAmount().multiply(LOYALTY_MULTIPLIER));
        }
        return context.getAmount();
    }
}
