package com.smartparking.service;

import com.smartparking.domain.ParkingSession;
import com.smartparking.domain.ParkingSlot;
import com.smartparking.domain.Vehicle;
import com.smartparking.domain.VehicleType;
import com.smartparking.domain.exception.SlotUnavailableException;
import com.smartparking.repository.ParkingSessionRepository;
import com.smartparking.repository.ParkingSlotRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Thin attendant-facing facade that combines walk-in entry and vehicle check-out.
 * UI controllers call this service instead of coordinating repositories or domain states.
 */
public final class ParkingService {
    private final ParkingSlotRepository slots;
    private final ParkingSessionRepository sessions;
    private final EntryService entryService;
    private final CheckoutService checkoutService;

    public ParkingService(ParkingSlotRepository slots,
                          ParkingSessionRepository sessions,
                          EntryService entryService,
                          CheckoutService checkoutService) {
        this.slots = Objects.requireNonNull(slots, "slots");
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.entryService = Objects.requireNonNull(entryService, "entryService");
        this.checkoutService = Objects.requireNonNull(checkoutService, "checkoutService");
    }

    /**
     * Checks in a walk-in vehicle by first selecting or explicitly using a bay.
     *
     * @return the open parking session created for the vehicle
     */
    public ParkingSession vehicleEntry(String plateNumber, VehicleType type, String requestedSlotId) {
        Vehicle vehicle = new Vehicle(plateNumber, type);
        String slotId = requestedSlotId == null || requestedSlotId.isBlank()
                ? suggestAvailableSlot().map(ParkingSlot::getId)
                .orElseThrow(() -> new SlotUnavailableException("No available parking slot"))
                : requestedSlotId;

        ParkingSlot slot = slots.findById(slotId)
                .orElseThrow(() -> new SlotUnavailableException(slotId));
        if (slot.getStatus() != com.smartparking.domain.SlotStatus.AVAILABLE) {
            throw new SlotUnavailableException(slotId);
        }
        return entryService.checkIn(slotId, vehicle, Instant.now());
    }

    /**
     * Finds the first currently available bay for the check-in form.
     */
    public Optional<ParkingSlot> suggestAvailableSlot() {
        return slots.findAll().stream()
                .filter(slot -> slot.getStatus() == com.smartparking.domain.SlotStatus.AVAILABLE)
                .findFirst();
    }

    /**
     * Looks up an open session by normalized license plate.
     */
    public Optional<ParkingSession> findActiveSession(String plateNumber) {
        if (plateNumber == null || plateNumber.isBlank()) {
            return Optional.empty();
        }
        return sessions.findActiveByPlate(plateNumber);
    }

    /**
     * Calculates the current fare and closes the active vehicle stay.
     *
     * @param session active session
     * @return exact payable INR amount
     */
    public BigDecimal vehicleExit(ParkingSession session) {
        Objects.requireNonNull(session, "session");
        int occupiedSlots = (int) slots.findAll().stream()
                .filter(slot -> slot.getStatus() == com.smartparking.domain.SlotStatus.OCCUPIED)
                .count();
        double occupancyRatio = slots.findAll().isEmpty()
                ? 0
                : (double) occupiedSlots / slots.findAll().size();
        return checkoutService.checkOut(
                session.getId(), Instant.now(), occupancyRatio, false, false, false);
    }
}
