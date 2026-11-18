package com.smartparking.service;

import com.smartparking.domain.Vehicle;
import com.smartparking.domain.VehicleType;
import com.smartparking.service.pricing.PricingContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTest {
    @Test
    void calculatesBikeExampleWithEveryAppliedCharge() {
        Vehicle bike = new Vehicle("MH12BIKE01", VehicleType.BIKE);
        Instant entry = Instant.parse("2026-11-18T12:00:00Z");
        Instant exit = Instant.parse("2026-11-18T15:30:00Z");
        PricingContext context = new PricingContext(
                bike, entry, exit, 0.90, true, true, true);

        BigDecimal amount = new PricingService().calculate(context);

        // 4 hours x ₹20 = ₹80; 90% surge = ₹100; all 10% discounts = ₹72.90;
        // 90 excess minutes adds ₹150; GST gives ₹262.62, rounded to ₹263.
        assertEquals(new BigDecimal("263"), amount);
    }

    @Test
    void bikeChargesTwentyRupeesPerHour() {
        PricingContext context = new PricingContext(
                new Vehicle("MH12BIKE02", VehicleType.BIKE),
                Instant.parse("2026-11-18T09:00:00Z"),
                Instant.parse("2026-11-18T10:00:00Z"),
                0.20, false, false, false);

        assertEquals(new BigDecimal("24"), new PricingService().calculate(context));
    }
}
