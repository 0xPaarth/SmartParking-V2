# SmartParking V2: Logic Cheat Sheet

This guide explains the reservation, pricing, and slot-state flows for viva preparation. It is written around the implementation in `domain`, `service`, and `repository`; JavaFX will only invoke these services later.

## Reservation Flow

1. **The driver chooses a bay.** The JavaFX UI displays bays from `ParkingSlotRepository.findAll()` but does not decide whether a bay is available.
2. **The driver chooses start and end times.** The UI submits `driverId`, `slotId`, `start`, and `end` to `ReservationService.reserve(...)`.
3. **The service validates the interval.** `ReservationService` rejects a null interval or an end time that is not after the start time.
4. **The slot is loaded from the repository.** If the identifier is unknown, the service throws `SlotUnavailableException`.
5. **A reservation entity is created.** It receives a random UUID and has initial status `CONFIRMED`.
6. **The slot state machine validates the bay.** `slot.reserve(reservation)` dispatches to the bay's current `SlotState`.
7. **Only `AvailableState` accepts the request.** It changes the slot to `ReservedState` and stores the reservation.
8. **A database/repository failure is isolated.** The service serializes repository saves and slot saves as the transaction boundary. For an invalid bay state, `InvalidSlotStateException` is converted to `SlotUnavailableException`.
9. **The reservation is saved.** `ReservationRepository.save(...)` persists the confirmed reservation.
10. **The slot is saved.** `ParkingSlotRepository.save(...)` persists the `Reserved` state, vehicle lock, and reservation reference.

The important rule is that `AvailableState` performs the lock. The UI's "green slot" label is only a visual hint; the same slot cannot be booked twice because the state transition is state-based and the method is synchronized.

## 10-Step Dynamic Pricing Flow

`PricingService` builds a fixed strategy chain in exact order:

1. **Duration base:** round stay time up to full billed hours and multiply by the vehicle hourly rate.
2. **Surge:** if occupancy is at least 85%, multiply by `1.25`.
3. **Peak:** if entry or exit is in IST 18:00–21:00, multiply by `1.20`.
4. **EV:** for `EV_CAR`, multiply by `0.90`.
5. **Reservation:** if reservation-used, multiply by `0.90`.
6. **Promo:** if a promo applies, multiply by `0.90`.
7. **Loyalty:** if the customer is a loyalty member, multiply by `0.90`.
8. **Overstay:** add ₹50 per 30-minute block beyond 120 minutes.
9. **Rounding:** round the pre-GST amount to the nearest whole rupee using `HALF_UP`.
10. **GST:** multiply by `1.18`, then round the final payable amount.

### Vehicle Base Rates

| Vehicle | Base |
|---|---:|
| Bike | ₹20/hour |
| Car | ₹40/hour |
| Electric car | ₹50/hour |
| Heavy vehicle | ₹80/hour |

### Worked Example

Consider a bike parked for exactly 3 hours 30 minutes:

| Step | Calculation | Result |
|---|---:|---:|
| 1 Base | 4 billed hours × ₹20 | ₹80.00 |
| 2 Surge | No: assumed occupancy 70% | ₹80.00 |
| 3 Peak | No | ₹80.00 |
| 4 EV | Not applicable | ₹80.00 |
| 5 Reservation | ×0.90 | ₹72.00 |
| 8 Overstay | 90 excess minutes = 3 × ₹50 | ₹222.00 |
| 9 Rounding | nearest rupee | ₹222.00 |
| 10 GST | ₹222 × 1.18 | ₹261.96 → ₹262 |

For completeness, steps 6 (promo) and 7 (loyalty) would be applied before overstay if flags are true. Discounts multiply the current amount; overstay is added afterward.

### Multi-Discount Example

A car is parked for 4 hours at 90% occupancy. The stay has no peak interval.

| Step | Calculation |
|---|---|
| Base | 4 × ₹40 = ₹160 |
| Surge | ₹160 × 1.25 = ₹200 |
| EV | Not applicable |
| Reservation | ₹200 × 0.90 = ₹180 |
| Promo | ₹180 × 0.90 = ₹162 |
| Loyalty | ₹162 × 0.90 = ₹145.80 |
| Overstay | 4 hours = 120 excess minutes = 4 × ₹50 = add ₹200 |
| Pre-GST rounded | ₹346 |
| GST | ₹346 × 1.18 = ₹408.28 → ₹408 |

The final payable amount is **₹408**.

## State Pattern Prevents Double-Booking

`ParkingSlot` does not expose a mutable public status field that callers can change directly. It delegates `reserve(...)`, `occupy(...)`, `release(...)`, and maintenance operations to `SlotState`.

The lifecycle is:

- `AvailableState`: accepts reserve and occupy.
- `ReservedState`: rejects reserve and accepts check-in or cancellation.
- `OccupiedState`: rejects reserve and supports checkout.
- `MaintenanceState`: rejects reserve and occupancy until maintenance finishes.

If two calls reach `ReservedState.reserve(...)`, the first call changes the state to `ReservedState`; the second dispatches to that state and throws `InvalidSlotStateException`. `ReservationService.reserve(...)` converts this to `SlotUnavailableException`, so the UI can safely show “slot already reserved.” For the in-memory implementation, `synchronized reserve(...)` plus repository synchronization ensures no second reservation enters the critical section concurrently.

## Check-Out Flow

1. `CheckoutService.checkOut(...)` loads the session and slot.
2. The session must exist and must not already be closed.
3. `PricingContext` combines entry time, exit time, vehicle, occupancy, reservation, promo, and loyalty flags.
4. `PricingService` runs the ten strategy stages.
5. `ParkingSession.close(...)` stores the exit time and final amount.
6. `slot.release()` delegates to `OccupiedState`, which changes the bay to `AvailableState`.
7. Session and slot repositories save the final state.
