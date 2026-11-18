package com.smartparking.service.pricing;

import com.smartparking.domain.VehicleType;
import java.math.BigDecimal;

/** Step 4: applies a 10 percent EV incentive to electric cars. */
public final class ElectricVehicleStrategy implements PricingStrategy {
    public static final BigDecimal EV_DISCOUNT = new BigDecimal("0.90");

    @Override
    public BigDecimal apply(PricingContext context) {
        if (context.getVehicle().getType() == VehicleType.EV_CAR) {
            context.setAmount(context.getAmount().multiply(EV_DISCOUNT));
        }
        return context.getAmount();
    }
}
