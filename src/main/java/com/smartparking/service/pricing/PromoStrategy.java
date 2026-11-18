package com.smartparking.service.pricing;

import java.math.BigDecimal;

/** Step 6: applies a 10 percent promo-code discount when requested. */
public final class PromoStrategy implements PricingStrategy {
    public static final BigDecimal PROMO_MULTIPLIER = new BigDecimal("0.90");

    @Override
    public BigDecimal apply(PricingContext context) {
        if (context.isPromoApplied()) {
            context.setAmount(context.getAmount().multiply(PROMO_MULTIPLIER));
        }
        return context.getAmount();
    }
}
