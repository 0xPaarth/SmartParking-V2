package com.smartparking.repository;

import com.smartparking.domain.Reservation;

import java.util.Optional;
import java.util.UUID;

/** Persistence contract for reservations. */
public interface ReservationRepository {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(UUID id);
}
