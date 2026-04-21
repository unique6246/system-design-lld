
## Parking Lot LLD - Class Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        «Singleton»                                      │
│                         test.ParkingLot                                      │
├─────────────────────────────────────────────────────────────────────────┤
│ - instance: test.ParkingLot  (volatile)                                      │
│ - floors: List<ParkingFloor>                                            │
├─────────────────────────────────────────────────────────────────────────┤
│ + getInstance(): test.ParkingLot                                             │
│ + addFloor(floor: ParkingFloor): void                                   │
│ + parkVehicle(vehicle: Vehicle): boolean        «synchronized»          │
│ + unparkVehicle(vehicle: Vehicle): boolean      «synchronized»          │
│ + displayAvailability(): void                   «synchronized»          │
└──────────────────────────┬──────────────────────────────────────────────┘
                           │  1
                           │  has many
                           │  *
          ┌────────────────▼──────────────────────┐
          │             ParkingFloor               │
          ├───────────────────────────────────────┤
          │ - floorNumber: int                     │
          │ - spots: List<ParkingSpot>             │
          ├───────────────────────────────────────┤
          │ + parkVehicle(v: Vehicle): boolean     │  «synchronized»
          │ + unparkVehicle(v: Vehicle): boolean   │  «synchronized»
          │ + getAvailableSpots(): int             │  «synchronized»
          │ + getTotalSpots(): int                 │  «synchronized»
          │ + displayAvailability(): void          │  «synchronized»
          └──────────────┬────────────────────────┘
                         │  1
                         │  has many
                         │  *
          ┌──────────────▼────────────────────────┐
          │             ParkingSpot                │
          ├───────────────────────────────────────┤
          │ - spotId: int                          │
          │ - spotSize: VehicleSize                │
          │ - parkedVehicle: Vehicle               │
          ├───────────────────────────────────────┤
          │ + isAvailable(): boolean               │  «synchronized»
          │ + canFitVehicle(v: Vehicle): boolean   │  «synchronized»
          │ + assignVehicle(v: Vehicle): boolean   │  «synchronized»
          │ + removeVehicle(v: Vehicle): boolean   │  «synchronized»
          └──────────────┬────────────────────────┘
                         │ uses
                         ▼
          ┌──────────────────────────┐
          │       «enum»             │
          │      VehicleSize         │
          ├──────────────────────────┤
          │  MOTORCYCLE              │
          │  COMPACT                 │
          │  LARGE                   │
          └──────────────────────────┘


          ┌───────────────────────────────────────┐
          │           «abstract»                  │
          │             Vehicle                   │
          ├───────────────────────────────────────┤
          │ # licensePlate: String                │
          │ # size: VehicleSize                   │
          ├───────────────────────────────────────┤
          │ + getLicensePlate(): String            │
          │ + getSize(): VehicleSize               │
          │ + toString(): String                   │
          └──────────┬────────────────────────────┘
                     │ extends
         ┌───────────┼────────────┐
         │           │            │
         ▼           ▼            ▼
   ┌──────────┐ ┌──────────┐ ┌──────────┐
   │   Car    │ │Motorcycle│ │  Truck   │
   ├──────────┤ ├──────────┤ ├──────────┤
   │ COMPACT  │ │MOTORCYCLE│ │  LARGE   │
   └──────────┘ └──────────┘ └──────────┘
```

---

## Relationships Summary

| From         | To            | Relationship     | Multiplicity |
|--------------|---------------|------------------|--------------|
| test.ParkingLot   | ParkingFloor  | Composition (has)| 1 → many     |
| ParkingFloor | ParkingSpot   | Composition (has)| 1 → many     |
| ParkingSpot  | Vehicle       | Association      | 0..1         |
| ParkingSpot  | VehicleSize   | Dependency       | —            |
| Car          | Vehicle       | Inheritance      | —            |
| Motorcycle   | Vehicle       | Inheritance      | —            |
| Truck        | Vehicle       | Inheritance      | —            |

---

## Flow Diagram — Park Vehicle

```
  Client Thread
       │
       ▼
  test.ParkingLot.parkVehicle(vehicle)   ← synchronized
       │
       ├── Loop through floors
       │        │
       │        ▼
       │   ParkingFloor.parkVehicle(vehicle)   ← synchronized
       │        │
       │        ├── Loop through spots
       │        │        │
       │        │        ▼
       │        │   ParkingSpot.canFitVehicle(vehicle)  ← synchronized
       │        │        │
       │        │   spotSize == vehicle.getSize()
       │        │   && isAvailable()
       │        │        │
       │        │   YES ─▶ ParkingSpot.assignVehicle(vehicle)
       │        │               parkedVehicle = vehicle
       │        │               return true ──────────────────▶ Parked ✅
       │        │
       │        └── No spot found → return false
       │
       └── All floors exhausted → "No spot available" ❌
```

---

## Thread Safety

```
Double-Checked Locking (Singleton)
──────────────────────────────────
if (instance == null) {               ← 1st check (no lock)
    synchronized (test.ParkingLot.class) {
        if (instance == null) {       ← 2nd check (with lock)
            instance = new test.ParkingLot();
        }
    }
}

Layered Synchronization
──────────────────────────────────
test.ParkingLot.parkVehicle()    ← outer lock (lot level)
  └─ ParkingFloor.parkVehicle()   ← floor-level lock
       └─ ParkingSpot.assignVehicle()  ← spot-level lock
```
