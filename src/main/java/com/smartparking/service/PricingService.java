package com.smartparking.service;

import com.smartparking.service.pricing.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Executes all ten dynamic-pricing stages in a fixed, auditable order.
 * The chain is ordered as: base, surge, peak, EV, reservation, promo,
 * loyalty, overstay, rupee rounding, and GST.
 */
public final class PricingService {
    private final List<PricingStrategy> strategies;

    public PricingService() {
        this(List.of(
                new DurationBaseStrategy(),
                new SurgeStrategy(),
                new PeakHourStrategy(),
                new ElectricVehicleStrategy(),
                new ReservationDiscountStrategy(),
                new PromoStrategy(),
                new LoyaltyStrategy(),
                new OverstayStrategy(),
                new RoundingStrategy(),
                new GstStrategy()
        ));
    }

    public PricingService(List<PricingStrategy> strategies) {
        if (strategies == null || strategies.isEmpty()) {
            throw new IllegalArgumentException("strategies must not be empty");
        }
        this.strategies = List.copyOf(strategies);
    }

    /**
     * Applies every pricing strategy exactly once and leaves GST as the final stage.
     *
     * @param context mutable pricing calculation context
     * @return final payable amount in Indian rupees
     */
    public BigDecimal calculate(PricingContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        for (PricingStrategy strategy : strategies) {
            strategy.apply(context);
        }
        return context.getAmount();
    }
}
