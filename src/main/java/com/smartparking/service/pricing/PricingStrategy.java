package com.smartparking.service.pricing;

import java.math.BigDecimal;

/**
 * One numbered stage in the dynamic-pricing chain.
 * Implementations receive the current amount and update it in-place in {@link PricingContext}.
 */
public interface PricingStrategy {
    /**
     * Applies this strategy and returns the resulting running amount.
     */
    BigDecimal apply(PricingContext context);
}
