package com.smartparking.domain;

import java.math.BigDecimal;

/**
 * Vehicle categories and their exact base hourly rates.
 * These prices are in Indian Rupees and are expressed as {@link BigDecimal} values.
 */
public enum VehicleType {
    BIKE("Bike", "20"),
    CAR("Car", "40"),
    EV_CAR("Electric car", "50"),
    HEAVY_VEHICLE("Heavy vehicle", "80");

    private final String displayName;
    private final BigDecimal baseHourlyRate;

    VehicleType(String displayName, String baseHourlyRate) {
        this.displayName = displayName;
        this.baseHourlyRate = new BigDecimal(baseHourlyRate);
    }

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getBaseHourlyRate() {
        return baseHourlyRate;
    }
}
