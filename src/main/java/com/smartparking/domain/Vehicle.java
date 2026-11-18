package com.smartparking.domain;

import java.util.Objects;

/**
 * Identifies a vehicle by its registration plate and category.
 * Plates are normalized to upper case and have surrounding whitespace removed.
 */
public final class Vehicle {
    private final String plateNumber;
    private final VehicleType type;

    public Vehicle(String plateNumber, VehicleType type) {
        Objects.requireNonNull(type, "type");
        String normalized = Objects.requireNonNull(plateNumber, "plateNumber").trim().toUpperCase();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("plateNumber must not be blank");
        }
        this.plateNumber = normalized;
        this.type = type;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public VehicleType getType() {
        return type;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Vehicle vehicle && plateNumber.equals(vehicle.plateNumber);
    }

    @Override
    public int hashCode() {
        return plateNumber.hashCode();
    }

    @Override
    public String toString() {
        return plateNumber + " (" + type.getDisplayName() + ")";
    }
}
