package com.smartparking.repository;

import com.smartparking.domain.ParkingSession;

import java.util.Optional;
import java.util.UUID;

/** Persistence contract for active and completed parking sessions. */
public interface ParkingSessionRepository {
    ParkingSession save(ParkingSession session);
    Optional<ParkingSession> findById(UUID id);
}
