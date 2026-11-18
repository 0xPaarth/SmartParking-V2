package com.smartparking.service.pricing;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;

/** Step 3: adds 20 percent for entry or exit during 18:00–21:00 IST. */
public final class PeakHourStrategy implements PricingStrategy {
    public static final BigDecimal PEAK_MULTIPLIER = new BigDecimal("1.20");

    @Override
    public BigDecimal apply(PricingContext context) {
        ZoneId ist = ZoneId.of("Asia/Kolkata");
        LocalTime entry = context.getEntryTime().atZone(ist).toLocalTime();
        LocalTime exit = context.getExitTime().atZone(ist).toLocalTime();
        boolean peak = !entry.isBefore(LocalTime.of(18, 0))
                && entry.isBefore(LocalTime.of(21, 0))
                || !exit.isBefore(LocalTime.of(18, 0))
                && exit.isBefore(LocalTime.of(21, 0));
        if (peak) {
            context.setAmount(context.getAmount().multiply(PEAK_MULTIPLIER));
        }
        return context.getAmount();
    }
}
