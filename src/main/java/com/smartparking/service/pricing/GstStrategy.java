package com.smartparking.service.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Step 10: adds 18 percent GST and rounds the final payable rupee amount. */
public final class GstStrategy implements PricingStrategy {
    public static final BigDecimal GST_RATE = new BigDecimal("0.18");
    public static final BigDecimal GST_MULTIPLIER = new BigDecimal("1.18");

    @Override
    public BigDecimal apply(PricingContext context) {
        BigDecimal payable = context.getAmount().multiply(GST_MULTIPLIER);
        context.setAmount(payable.setScale(0, RoundingMode.HALF_UP));
        return context.getAmount();
    }
}
