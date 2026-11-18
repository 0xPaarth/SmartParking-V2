package com.smartparking.repository;

import com.smartparking.domain.Reservation;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Simple thread-safe reservation repository for tests and application bootstrap. */
public final class InMemoryReservationRepository implements ReservationRepository {
    private final Map<UUID, Reservation> reservations = new ConcurrentHashMap<>();

    @Override
    public Reservation save(Reservation reservation) {
        reservations.put(reservation.getId(), reservation);
        return reservation;
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        return Optional.ofNullable(reservations.get(id));
    }
}
