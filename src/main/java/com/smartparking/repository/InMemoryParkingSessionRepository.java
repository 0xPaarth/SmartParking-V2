package com.smartparking.repository;

import com.smartparking.domain.ParkingSession;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Simple thread-safe parking-session repository for tests and application bootstrap. */
public final class InMemoryParkingSessionRepository implements ParkingSessionRepository {
    private final Map<UUID, ParkingSession> sessions = new ConcurrentHashMap<>();

    @Override
    public ParkingSession save(ParkingSession session) {
        sessions.put(session.getId(), session);
        return session;
    }

    @Override
    public Optional<ParkingSession> findById(UUID id) {
        return Optional.ofNullable(sessions.get(id));
    }
}
