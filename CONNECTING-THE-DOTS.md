# 🔗 Connecting the Dots in LLD — How to Link Classes Together

> **The exact mental model** to figure out: which class to create first, what goes in each constructor, and how to wire everything together.  
> Uses your own **Parking Lot** code as the example throughout.

---

## 😖 The Problem You're Facing

When you look at a system, you see many classes:
```
ParkingLot, ParkingFloor, ParkingSpot, Vehicle, Car, Truck, Motorcycle
```

And your brain asks:
- _"Where do I even start?"_
- _"Who creates whom?"_
- _"What goes in the constructor?"_
- _"How do they talk to each other?"_

---

## 🧠 The Golden Rule: Think in LAYERS

Every LLD system has **3 layers**. Always build from **bottom → top**.

```
┌──────────────────────────────────────────────┐
│         LAYER 3: CONTROLLER / ENTRY POINT     │  ← ParkingLot (Singleton)
│         (The "brain" — coordinates everything)│     Main.java
├──────────────────────────────────────────────┤
│         LAYER 2: CONTAINER CLASSES            │  ← ParkingFloor
│         (Groups of Layer 1 objects)           │
├──────────────────────────────────────────────┤
│         LAYER 1: CORE MODEL CLASSES           │  ← ParkingSpot, Vehicle
│         (Smallest units, no dependencies)     │     Car, Truck, Motorcycle
└──────────────────────────────────────────────┘
```

> **Rule: Start coding from Layer 1 → Layer 2 → Layer 3**  
> Because lower layers don't know about upper layers, but upper layers depend on lower ones.

---

## 🔍 The 3-Question Formula for Every Class

Before writing any class, answer these 3 questions:

```
1. WHAT does this class OWN?         → these become fields (private variables)
2. WHAT does this class NEED to work? → these become constructor parameters
3. WHAT can this class DO?           → these become methods
```

Let's apply this to every class in the Parking Lot system.

---

## 🧱 LAYER 1 — Core Model Classes (Start Here)

### Class 1: `Vehicle` (Abstract)

**Ask the 3 questions:**

| Question | Answer |
|---|---|
| What does it OWN? | A license plate, a size (COMPACT/LARGE/MOTORCYCLE) |
| What does it NEED to exist? | licensePlate + size must be given at creation |
| What can it DO? | getSize(), getLicensePlate(), toString() |

```java
// WHAT it owns → fields
protected String licensePlate;
protected VehicleSize size;

// WHAT it needs → constructor params
public Vehicle(String licensePlate, VehicleSize size) {
    this.licensePlate = licensePlate;
    this.size = size;
}
```

**Why abstract?** Because you never park a "Vehicle", you park a Car or Truck.  
The concrete subclasses just hardcode the size:

```java
public class Car extends Vehicle {
    public Car(String licensePlate) {
        super(licensePlate, VehicleSize.COMPACT); // size is fixed for Car
    }
}

public class Truck extends Vehicle {
    public Truck(String licensePlate) {
        super(licensePlate, VehicleSize.LARGE);   // size is fixed for Truck
    }
}

public class Motorcycle extends Vehicle {
    public Motorcycle(String licensePlate) {
        super(licensePlate, VehicleSize.MOTORCYCLE);
    }
}
```

> 💡 **Key insight:** Subclasses don't need size in their constructor because they KNOW their own size. They pass it up via `super()`.

---

### Class 2: `ParkingSpot`

**Ask the 3 questions:**

| Question | Answer |
|---|---|
| What does it OWN? | A spotId, a spotSize, the vehicle currently parked in it |
| What does it NEED to exist? | spotId + spotSize must be given (vehicle starts as null) |
| What can it DO? | isAvailable(), canFitVehicle(), assignVehicle(), removeVehicle() |

```java
// WHAT it owns
private final int spotId;          // identity of this spot
private final VehicleSize spotSize; // what SIZE fits here
private Vehicle parkedVehicle;      // who is currently parked (null = empty)

// WHAT it needs to exist
public ParkingSpot(int spotId, VehicleSize spotSize) {
    this.spotId = spotId;
    this.spotSize = spotSize;
    this.parkedVehicle = null; // starts empty
}
```

**Why is `parkedVehicle` NOT in the constructor?**  
Because a spot is created EMPTY. Vehicle comes later, through `assignVehicle()`.

**The logic flow inside ParkingSpot:**
```
isAvailable()       → checks if parkedVehicle == null
canFitVehicle(v)    → isAvailable() AND spotSize matches vehicle.getSize()
assignVehicle(v)    → sets parkedVehicle = v
removeVehicle(v)    → sets parkedVehicle = null
```

---

## 🧱 LAYER 2 — Container Classes

### Class 3: `ParkingFloor`

Now that `ParkingSpot` exists, `ParkingFloor` can hold a **list of spots**.

**Ask the 3 questions:**

| Question | Answer |
|---|---|
| What does it OWN? | A floor number, a LIST of ParkingSpots |
| What does it NEED to exist? | floorNumber + how many spots of each type |
| What can it DO? | parkVehicle(), unparkVehicle(), getAvailableSpots() |

```java
// WHAT it owns
private final int floorNumber;
private final List<ParkingSpot> spots;   // ← owns a collection of Layer 1 objects

// WHAT it needs
public ParkingFloor(int floorNumber, int motorcycleSpots, int compactSpots, int largeSpots) {
    this.floorNumber = floorNumber;
    this.spots = new ArrayList<>();

    // It CREATES ParkingSpot objects internally ← key linking moment!
    int id = 1;
    for (int i = 0; i < motorcycleSpots; i++) spots.add(new ParkingSpot(id++, VehicleSize.MOTORCYCLE));
    for (int i = 0; i < compactSpots; i++)    spots.add(new ParkingSpot(id++, VehicleSize.COMPACT));
    for (int i = 0; i < largeSpots; i++)      spots.add(new ParkingSpot(id++, VehicleSize.LARGE));
}
```

> 💡 **Key insight:** `ParkingFloor` **creates** `ParkingSpot` objects inside its constructor.  
> You tell the floor "give me 2 motorcycle spots, 3 compact spots", and it builds them.  
> You **never** manually create `ParkingSpot` objects outside — the floor handles that.

**How does parkVehicle() work?**  
It loops through its spots and delegates to spot:
```java
public boolean parkVehicle(Vehicle vehicle) {
    for (ParkingSpot spot : spots) {
        if (spot.canFitVehicle(vehicle)) {  // ← calls Layer 1 method
            spot.assignVehicle(vehicle);     // ← calls Layer 1 method
            return true;
        }
    }
    return false;
}
```

> The Floor doesn't KNOW how to park — it just **asks each Spot** "can you fit this vehicle?"

---

## 🧱 LAYER 3 — The Controller

### Class 4: `ParkingLot` (Singleton)

The top-level brain. It owns floors, coordinates everything.

**Ask the 3 questions:**

| Question | Answer |
|---|---|
| What does it OWN? | A LIST of ParkingFloors |
| What does it NEED to exist? | Nothing! (Singleton — creates itself) |
| What can it DO? | parkVehicle(), unparkVehicle(), displayAvailability() |

```java
// WHAT it owns
private final List<ParkingFloor> floors;

// Constructor is PRIVATE (Singleton pattern)
private ParkingLot() {
    this.floors = new ArrayList<>(); // starts with no floors
}

// Floors are ADDED later, not in constructor
public void addFloor(ParkingFloor floor) {
    floors.add(floor);
}
```

**Why no floors in the constructor?**  
Because floors are added dynamically (the lot can be configured).  
This is the **Builder-style initialization** pattern.

**How does parkVehicle() work?**  
Same pattern — it loops through floors and delegates:
```java
public void parkVehicle(Vehicle vehicle) {
    for (ParkingFloor floor : floors) {
        if (floor.parkVehicle(vehicle)) {  // ← calls Layer 2 method
            return; // done!
        }
    }
    // no spot found
}
```

---

## 🔌 THE WIRING — Main.java (How Everything Connects)

Now the magic. In `Main.java`, you wire all layers together:

```java
// ─────────────────────────────────────────────
// STEP 1: Get the top-level controller (Layer 3)
// ─────────────────────────────────────────────
ParkingLot lot = ParkingLot.getInstance();
//  └─ Internally creates: List<ParkingFloor> floors = new ArrayList<>()

// ─────────────────────────────────────────────
// STEP 2: Create Layer 2 objects and add to Layer 3
// ─────────────────────────────────────────────
lot.addFloor(new ParkingFloor(1, 2, 3, 1));
//             └─ Internally creates:
//                  new ParkingSpot(1, MOTORCYCLE)
//                  new ParkingSpot(2, MOTORCYCLE)
//                  new ParkingSpot(3, COMPACT)
//                  new ParkingSpot(4, COMPACT)
//                  new ParkingSpot(5, COMPACT)
//                  new ParkingSpot(6, LARGE)

lot.addFloor(new ParkingFloor(2, 1, 2, 2));
//             └─ Internally creates more spots...

// ─────────────────────────────────────────────
// STEP 3: Create the "actors" (vehicles)
// ─────────────────────────────────────────────
Car car1      = new Car("CAR-001");
Motorcycle m1 = new Motorcycle("MOTO-001");
Truck t1      = new Truck("TRUCK-001");

// ─────────────────────────────────────────────
// STEP 4: Use the system via the top-level controller only
// ─────────────────────────────────────────────
lot.parkVehicle(car1);
//  └─ lot loops floors
//       └─ floor loops spots
//            └─ spot.canFitVehicle(car1)?
//                 └─ spot.assignVehicle(car1) ✅
```

> 💡 **You only ever talk to `ParkingLot` from Main. You never call `floor.parkVehicle()` or `spot.assignVehicle()` directly from Main.**  
> That's the beauty of layering — each layer hides the complexity of the layer below it.

---

## 🗺️ Visual Dependency Map

```
Main.java
    │
    ▼
ParkingLot (Singleton)          ← you call this
    │  owns List<ParkingFloor>
    │
    ▼
ParkingFloor                    ← ParkingLot delegates to this
    │  owns List<ParkingSpot>
    │  creates ParkingSpots in constructor
    │
    ▼
ParkingSpot                     ← ParkingFloor delegates to this
    │  owns Vehicle (nullable)
    │
    ▼
Vehicle (abstract)              ← ParkingSpot checks this
    │
    ├── Car        (size = COMPACT)
    ├── Truck      (size = LARGE)
    └── Motorcycle (size = MOTORCYCLE)
```

**Arrow = "depends on" / "uses"**  
**Everything flows downward. Lower layers never know about upper layers.**

---

## 📋 Constructor Cheat Sheet for Parking Lot

| Class | Constructor Parameters | Why |
|---|---|---|
| `Vehicle` | `licensePlate, size` | Every vehicle needs identity + size |
| `Car` | `licensePlate` only | Size is always COMPACT, hardcoded |
| `Truck` | `licensePlate` only | Size is always LARGE, hardcoded |
| `Motorcycle` | `licensePlate` only | Size is always MOTORCYCLE, hardcoded |
| `ParkingSpot` | `spotId, spotSize` | Spot needs identity + what fits in it |
| `ParkingFloor` | `floorNumber, motorcycleCount, compactCount, largeCount` | Creates spots internally |
| `ParkingLot` | *(none — Singleton)* | Gets instance via `getInstance()` |

---

## 🧭 Universal Framework to "Connect the Dots"

Use this every time you start a new LLD problem:

```
STEP 1 — FIND THE SMALLEST UNITS
   Ask: "What is the most basic thing in this system that holds data?"
   → These are your Layer 1 classes
   → They have NO dependencies on other custom classes
   → Parking Lot → ParkingSpot, Vehicle

STEP 2 — FIND THE CONTAINERS
   Ask: "What groups/contains the Layer 1 objects?"
   → These are your Layer 2 classes
   → They OWN a List<Layer1Class>
   → Parking Lot → ParkingFloor owns List<ParkingSpot>

STEP 3 — FIND THE COORDINATOR
   Ask: "Who is the single entry point that orchestrates everything?"
   → This is your Layer 3 class (often a Singleton)
   → It OWNs a List<Layer2Class>
   → Parking Lot → ParkingLot owns List<ParkingFloor>

STEP 4 — DEFINE CONSTRUCTORS
   For each class ask:
   "What MUST be known at the time of creation?"
   → Those are constructor params
   "What can be set/added LATER?"
   → Those get setter methods or addXxx() methods

STEP 5 — DEFINE METHOD DELEGATION CHAIN
   Top class method → calls middle class method → calls bottom class method
   ParkingLot.parkVehicle()
       └─► ParkingFloor.parkVehicle()
               └─► ParkingSpot.canFitVehicle() + assignVehicle()

STEP 6 — WIRE IN MAIN / DRIVER CLASS
   1. Create Layer 3 (or get Singleton)
   2. Create Layer 2 objects, add to Layer 3
   3. Create actors (vehicles, users, etc.)
   4. Call methods ONLY on Layer 3
```

---

## ⚡ Quick Mental Checklist

When you're stuck on any class, ask:

```
❓ Does this class CONTAIN other objects?
    YES → it has a List<SomeClass> field
    YES → it probably creates those objects in its constructor or via add() method

❓ Does this class NEED another object to do its job?
    YES → that object is a constructor parameter OR a method parameter

❓ Is this class always the SAME instance?
    YES → make it a Singleton (private constructor + getInstance())

❓ Does this class come in MULTIPLE TYPES?
    YES → make it abstract/interface, create subclasses

❓ Does this class DELEGATE work to inner objects?
    YES → loop through the list and call their methods
```

---

## 🔄 Applying This to Another System (Coffee Vending Machine)

Quick application of the same framework:

```
Layer 1 (smallest units):
  Ingredient(name, quantity)          ← owns name + qty
  Coffee(name, List<Ingredient>)      ← owns recipe
  Payment(amount, type)               ← owns payment info

Layer 2 (containers):
  Inventory(List<Ingredient>)         ← manages ingredients
    └─ checkAvailability(Coffee)
    └─ deductIngredients(Coffee)

Layer 3 (coordinator):
  CoffeeMachine — Singleton
    └─ owns Inventory
    └─ selectCoffee(Coffee)
    └─ processPayment(Payment)
    └─ dispenseCoffee(Coffee)

Main wiring:
  CoffeeMachine machine = CoffeeMachine.getInstance();
  machine.loadIngredient(new Ingredient("Milk", 100));
  machine.dispenseCoffee(new Coffee("Latte", ingredients), new Payment(50, "CASH"));
```

**Same pattern, different domain.**

---

> 💡 **The secret:** You're never designing the whole system at once.  
> You're answering ONE question at a time:  
> _"What does THIS class own? What does it need? What does it do?"_  
> Once you answer that for each class — the wiring writes itself.

---

*Part of the `system-design-lld` project guide series.*

