package com.smartparking.domain;

/**
 * Authentication roles supported by SmartParking.
 * UI navigation must be derived from this enum so a user never sees another role's features.
 */
public enum Role {
    DRIVER,
    ATTENDANT,
    ADMIN
}
