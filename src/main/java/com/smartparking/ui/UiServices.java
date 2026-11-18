package com.smartparking.ui;

import com.smartparking.domain.ParkingSlot;
import com.smartparking.repository.InMemoryParkingSessionRepository;
import com.smartparking.repository.InMemoryParkingSlotRepository;
import com.smartparking.repository.InMemoryReservationRepository;
import com.smartparking.repository.ParkingSessionRepository;
import com.smartparking.repository.ParkingSlotRepository;
import com.smartparking.repository.ReservationRepository;
import com.smartparking.service.CheckoutService;
import com.smartparking.service.EntryService;
import com.smartparking.service.PricingService;
import com.smartparking.service.ParkingService;
import com.smartparking.service.ReservationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Composition root for UI-visible services and demo repositories.
 * Business operations remain in service classes; views only call this container.
 */
public final class UiServices {
    private final ParkingSlotRepository slotRepository;
    private final ReservationRepository reservationRepository;
    private final ParkingSessionRepository sessionRepository;
    private final ReservationService reservationService;
    private final EntryService entryService;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;
    private final ParkingService parkingService;

    private UiServices(ParkingSlotRepository slotRepository,
                       ReservationRepository reservationRepository,
                       ParkingSessionRepository sessionRepository,
                       ReservationService reservationService,
                       EntryService entryService,
                       CheckoutService checkoutService,
                       PricingService pricingService,
                       ParkingService parkingService) {
        this.slotRepository = slotRepository;
        this.reservationRepository = reservationRepository;
        this.sessionRepository = sessionRepository;
        this.reservationService = reservationService;
        this.entryService = entryService;
        this.checkoutService = checkoutService;
        this.pricingService = pricingService;
        this.parkingService = parkingService;
    }

    public static UiServices createDemo() {
        List<ParkingSlot> slots = new ArrayList<>();
        for (int row = 1; row <= 4; row++) {
            for (int column = 1; column <= 5; column++) {
                String id = (char) ('A' + row - 1) + "-" + column;
                slots.add(new ParkingSlot(id, "Zone " + (char) ('A' + row - 1)));
            }
        }

        try {
            slots.get(2).occupy(new com.smartparking.domain.Vehicle(
                    "MH12AB1234", com.smartparking.domain.VehicleType.CAR));
            slots.get(6).occupy(new com.smartparking.domain.Vehicle(
                    "DL01EV7001", com.smartparking.domain.VehicleType.EV_CAR));
            slots.get(14).maintain();
        } catch (RuntimeException ignored) {
            throw new IllegalStateException("Demo slot setup failed", ignored);
        }

        ParkingSlotRepository slotsRepo = new InMemoryParkingSlotRepository(slots);
        ReservationRepository reservationsRepo = new InMemoryReservationRepository();
        ParkingSessionRepository sessionsRepo = new InMemoryParkingSessionRepository();
        PricingService pricing = new PricingService();

        EntryService entry = new EntryService(slotsRepo, sessionsRepo);
        CheckoutService checkout = new CheckoutService(slotsRepo, sessionsRepo, pricing);
        return new UiServices(
                slotsRepo,
                reservationsRepo,
                sessionsRepo,
                new ReservationService(slotsRepo, reservationsRepo),
                entry,
                checkout,
                pricing,
                new ParkingService(slotsRepo, sessionsRepo, entry, checkout));
    }

    public ParkingSlotRepository slots() {
        return slotRepository;
    }

    public ReservationRepository reservations() {
        return reservationRepository;
    }

    public ParkingSessionRepository sessions() {
        return sessionRepository;
    }

    public ReservationService reservationService() {
        return reservationService;
    }

    public EntryService entryService() {
        return entryService;
    }

    public CheckoutService checkoutService() {
        return checkoutService;
    }

    public PricingService pricingService() {
        return pricingService;
    }

    public ParkingService parkingService() {
        return parkingService;
    }
}
