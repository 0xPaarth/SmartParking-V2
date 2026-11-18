# SmartParking V2

**SmartParking V2** is a modern Java 17 and JavaFX 21 parking management system built with Maven. It uses a strict Domain → Service → Repository → UI architecture, INR (`₹`) pricing with an `en-IN` locale, Jackson JSON persistence, and JUnit 5 testing.

## Planned Architecture

```text
com.smartparking
├── domain       # Entities, state patterns, pricing strategies, and exceptions
├── repository   # Interfaces and JSON-backed implementations
├── service      # Reservation, check-in, check-out, and pricing workflows
└── ui           # JavaFX views, role navigation, CSS, charts, and transitions
```

The UI never implements parking business rules. Controllers call services, and services coordinate repositories and domain patterns.

## Roles

| Role | Scope |
|---|---|
| Driver | Reserve a bay, enter/exit details, view reservations and payments |
| Attendant | Check vehicles in and out, inspect bays, and record maintenance |
| Admin | Manage users, slots, tariffs, reports, and system-wide pricing |

## Use Case Diagram

```mermaid
usecaseDiagram
actor Driver
actor Attendant
actor Admin

rectangle "SmartParking V2" {
  usecase "Register / Login" as Auth
  usecase "View Parking Slots" as ViewSlots
  usecase "Make Reservation" as Reserve
  usecase "Cancel Reservation" as Cancel
  usecase "View Reservation" as MyReserve
  usecase "Make Payment" as Pay

  usecase "Vehicle Check-In" as CheckIn
  usecase "Vehicle Check-Out" as CheckOut
  usecase "View Slot Details" as SlotDetails
  usecase "Mark Maintenance" as Maintenance

  usecase "Manage Users" as Users
  usecase "Manage Parking Slots" as Slots
  usecase "Configure Pricing" as Pricing
  usecase "View Reports" as Reports
}

Driver --> Auth
Driver --> ViewSlots
Driver --> Reserve
Driver --> Cancel
Driver --> MyReserve
Driver --> Pay

Attendant --> Auth
Attendant --> CheckIn
Attendant --> CheckOut
Attendant --> SlotDetails
Attendant --> Maintenance

Admin --> Auth
Admin --> Users
Admin --> Slots
Admin --> Pricing
Admin --> Reports
```

## Class Diagram

```mermaid
classDiagram
direction LR

class User {
  <<abstract>>
  -UUID id
  -String name
  -String email
  +getRole() Role
}
class Driver
class Attendant
class Admin
User <|-- Driver
User <|-- Attendant
User <|-- Admin

class ParkingSlot {
  -String id
  -String zone
  -SlotState state
  -Vehicle vehicle
  +reserve(Reservation)
  +occupy(Vehicle)
  +release()
  +maintain()
}
class SlotState {
  <<interface>>
  +reserve(ParkingSlot, Reservation)
  +occupy(ParkingSlot, Vehicle)
  +release(ParkingSlot)
  +maintain(ParkingSlot)
}
class AvailableState
class ReservedState
class OccupiedState
class MaintenanceState
SlotState <|.. AvailableState
SlotState <|.. ReservedState
SlotState <|.. OccupiedState
SlotState <|.. MaintenanceState
ParkingSlot --> SlotState

class Vehicle {
  -String plateNumber
  -VehicleType type
}
class Reservation {
  -UUID id
  -Instant startTime
  -Instant endTime
  -ReservationStatus status
}
class ParkingSession {
  -UUID id
  -Instant entryTime
  -Instant exitTime
  -BigDecimal amount
}
class Payment {
  -UUID id
  -BigDecimal amount
  -PaymentStatus status
  -Instant paidAt
}

ParkingSlot "1" o-- "0..1" Vehicle
Reservation "*" --> "1" Driver
Reservation "*" --> "1" ParkingSlot
ParkingSession "*" --> "1" Vehicle
ParkingSession "*" --> "1" ParkingSlot
ParkingSession "1" --> "0..1" Payment

class PricingStrategy {
  <<interface>>
  +calculate(PricingContext) BigDecimal
}
class DynamicPricingStrategy
class FlatPricingStrategy
PricingStrategy <|.. DynamicPricingStrategy
PricingStrategy <|.. FlatPricingStrategy

class SmartParkingException
class SlotUnavailableException
class ReservationNotFoundException
class InvalidSessionException
class PaymentException
SmartParkingException <|-- SlotUnavailableException
SmartParkingException <|-- ReservationNotFoundException
SmartParkingException <|-- InvalidSessionException
SmartParkingException <|-- PaymentException
```

## Parking Slot State Diagram

```mermaid
stateDiagram-v2
[*] --> Available
Available --> Reserved : reserve()
Reserved --> Occupied : checkIn()
Reserved --> Available : cancelReservation()
Occupied --> Available : checkOut()
Occupied --> Maintenance : reportDamage()
Available --> Maintenance : scheduleMaintenance()
Maintenance --> Available : completeMaintenance()
Maintenance --> [*]
```

## Vehicle Entry / Check-In Sequence

```mermaid
sequenceDiagram
actor Attendant
participant UI as Attendant UI
participant EntryService
participant SlotRepository
participant SessionRepository
participant ParkingSlot as Slot / State

Attendant->>UI: Select slot and enter plate
UI->>EntryService: checkIn(slotId, vehicle)
EntryService->>SlotRepository: findAvailableSlot(slotId)
SlotRepository-->>EntryService: ParkingSlot
EntryService->>ParkingSlot: occupy(vehicle)
ParkingSlot-->>EntryService: state = Occupied
EntryService->>SessionRepository: save(ParkingSession)
SessionRepository-->>EntryService: saved session
EntryService-->>UI: check-in confirmed
UI-->>Attendant: Show occupied bay and toast
```

## Check-Out and Payment Sequence

```mermaid
sequenceDiagram
actor Attendant
participant UI as Attendant UI
participant CheckoutService
participant SessionRepository
participant PricingStrategy
participant PaymentRepository
participant ParkingSlot as Slot / State

Attendant->>UI: Enter plate / select session
UI->>CheckoutService: checkOut(sessionId)
CheckoutService->>SessionRepository: findById(sessionId)
SessionRepository-->>CheckoutService: ParkingSession
CheckoutService->>PricingStrategy: calculate(pricingContext)
PricingStrategy-->>CheckoutService: INR amount
CheckoutService->>PaymentRepository: createAndSave(amount)
PaymentRepository-->>CheckoutService: Payment
CheckoutService->>ParkingSlot: release()
ParkingSlot-->>CheckoutService: state = Available
CheckoutService->>SessionRepository: closeSession(payment)
CheckoutService-->>UI: receipt and amount
UI-->>Attendant: Show success and available bay
```

## Reservation Sequence

```mermaid
sequenceDiagram
actor Driver
participant UI as Driver UI
participant ReservationService
participant SlotRepository
participant ParkingSlot as Slot / State
participant ReservationRepository

Driver->>UI: Choose bay and time
UI->>ReservationService: reserve(driverId, slotId, interval)
ReservationService->>SlotRepository: findById(slotId)
SlotRepository-->>ReservationService: ParkingSlot
ReservationService->>ReservationService: validate interval and slot
ReservationService->>ParkingSlot: reserve(reservation)
ParkingSlot-->>ReservationService: state = Reserved
ReservationService->>ReservationRepository: save(reservation)
ReservationRepository-->>ReservationService: saved reservation
ReservationService-->>UI: confirmation
UI-->>Driver: Reservation details and toast
```

## Dynamic Pricing Calculation Flow

```mermaid
flowchart TD
  A([Start: vehicle exits]) --> B[1. Load parking session]
  B --> C[2. Calculate occupied duration]
  C --> D[3. Find applicable tariff]
  D --> E[4. Check vehicle type multiplier]
  E --> F[5. Check occupancy ratio]
  F --> G[6. Apply peak-hour surcharge]
  G --> H[7. Apply weekend / holiday rule]
  H --> I[8. Calculate dynamic subtotal]
  I --> J[9. Apply reservation discount]
  J --> K[10. Round to nearest ₹1 using en-IN scale]
  K --> L([Return BigDecimal amount])
```

## Build & Run

```bash
mvn clean test
mvn javafx:run
```

Maven initially uses Java 17 and JavaFX 21 dependencies; the application shell will be added in later implementation steps.
