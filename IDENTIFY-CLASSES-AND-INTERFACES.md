# 🔎 How to Identify Classes & Interfaces in Any LLD Problem

> The complete thought process — from a problem statement to a full class/interface list.  
> Uses all 3 projects in this repo as live examples: **Parking Lot**, **Coffee Vending Machine**, **Logging Framework**.

---

## 🧠 The Core Mental Trick

When you read a problem statement, your brain needs to ask **4 different questions**, each revealing a different type of thing to create:

```
┌─────────────────────────────────────────────────────────────────────┐
│  QUESTION                        → WHAT YOU CREATE                  │
├─────────────────────────────────────────────────────────────────────┤
│  1. "What are the THINGS/NOUNS?" → Classes (model/entity classes)   │
│  2. "What are the ROLES/TYPES?"  → Abstract class OR Interface      │
│  3. "What are the ACTIONS?"      → Methods inside classes           │
│  4. "What are the FIXED VALUES?" → Enums                            │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🔬 The 5-Filter System

Run every noun/concept through these 5 filters to decide WHAT to create:

```
FILTER 1 → Is it a "thing" with data?              → Concrete Class
FILTER 2 → Does it come in multiple types/variants? → Abstract Class or Interface
FILTER 3 → Is it a CAPABILITY that many can share?  → Interface
FILTER 4 → Is it a fixed set of named constants?    → Enum
FILTER 5 → Is there only ever ONE of it?            → Singleton Class
```

Let's see each filter in action across all 3 projects.

---

## 📖 TECHNIQUE 1: Noun Extraction (Find Your Classes)

### How it works:
1. Read the problem statement
2. **Underline every noun** (thing/object)
3. Each meaningful noun = a potential class
4. Remove duplicates and obvious non-objects (like "system", "user input")

---

### Example 1 — Parking Lot

**Problem statement:**
> _"Design a parking lot where **vehicles** can park in **spots**. The lot has multiple **floors**. Each spot has a **size** (motorcycle, compact, large). A **car**, **truck**, or **motorcycle** can enter and exit."_

**Nouns extracted:**
```
vehicles    → Vehicle (abstract class — has types)
spots       → ParkingSpot (concrete class — one physical spot)
floors      → ParkingFloor (concrete class — contains spots)
parking lot → ParkingLot (singleton — only one lot)
car         → Car (concrete class — a type of vehicle)
truck       → Truck (concrete class — a type of vehicle)
motorcycle  → Motorcycle (concrete class — a type of vehicle)
size        → VehicleSize (enum — fixed set: COMPACT, LARGE, MOTORCYCLE)
```

**Result: 8 things identified just from reading nouns!**

---

### Example 2 — Coffee Vending Machine

**Problem statement:**
> _"Design a **coffee vending machine** that holds **ingredients**. Users select a **coffee** type from the **menu**, insert **payment**, and the machine dispenses the coffee."_

**Nouns extracted:**
```
coffee vending machine → CoffeeMachine (singleton)
ingredients            → Ingredient (concrete class)
coffee                 → Coffee (concrete class — has name, price, recipe)
menu                   → Map<String, Coffee> inside CoffeeMachine (not a separate class here)
payment                → Payment (concrete class)
```

---

### Example 3 — Logging Framework

**Problem statement:**
> _"Design a **logging framework** where **log messages** are written to different **destinations** (console, file, database). Each message has a **log level** (DEBUG, INFO, WARN, ERROR). A **logger** manages the configuration."_

**Nouns extracted:**
```
logging framework → Logger (singleton)
log messages      → LogMessage (concrete class)
destinations      → LogAppender (interface — console/file/DB are all destinations)
console           → ConsoleAppender (concrete class)
file              → FileAppender (concrete class)
database          → DBAppender (concrete class)
log level         → LogLevel (enum — DEBUG, INFO, WARNING, ERROR)
configuration     → LogConfig (concrete class)
```

---

## 🔀 TECHNIQUE 2: Spot Abstract Classes vs Interfaces

This is the **most confusing** part. Here's the exact rule:

```
USE ABSTRACT CLASS when:                  USE INTERFACE when:
─────────────────────────────             ──────────────────────────────
✅ Things SHARE common data/fields         ✅ Things share BEHAVIOUR only (no shared data)
✅ "IS-A" family (Car IS-A Vehicle)        ✅ "CAN-DO" capability (can append, can pay)
✅ Common base implementation exists       ✅ Multiple unrelated classes need same contract
✅ You want to share constructor logic     ✅ You want plug-and-play swappability
```

### Decision Table with Real Examples:

| Concept | Abstract Class or Interface? | Why | Real Code |
|---|---|---|---|
| `Vehicle` | **Abstract Class** | Car/Truck/Motorcycle all share `licensePlate` + `size` fields. Common data exists. | `Vehicle.java` |
| `LogAppender` | **Interface** | ConsoleAppender, FileAppender, DBAppender share NO data. They only share the `append()` behaviour. | `LogAppender.java` |
| `PaymentProcessor` | **Interface** | Cash/Card/UPI share no data, just the `process()` behaviour |  |
| `Shape` | **Abstract Class** | Circle/Square share `color`, `position` fields + some common logic |  |

---

### The "IS-A" vs "CAN-DO" Test

Ask yourself about the relationship:

```
"A Car IS-A Vehicle"         → Abstract Class  (Car inherits Vehicle's identity)
"A FileAppender CAN append"  → Interface       (FileAppender has the ABILITY to append)
"A DBAppender CAN append"    → Interface       (DBAppender ALSO has the ability)
```

> 💡 **Quick trick:** If you'd naturally say **"is a ___"** → Abstract Class  
> If you'd naturally say **"can do ___"** or **"supports ___"** → Interface

---

## 🎯 TECHNIQUE 3: Spot Enums (Fixed Value Sets)

**Rule:** Any time you have a concept with a **finite, known list of values** → make it an Enum.

**Trigger words:** "types of", "status", "level", "size", "category", "mode"

| Trigger in problem | Enum to create | Values |
|---|---|---|
| "spot size (motorcycle, compact, large)" | `VehicleSize` | `MOTORCYCLE, COMPACT, LARGE` |
| "log level (debug, info, warn, error)" | `LogLevel` | `DEBUG, INFO, WARNING, ERROR` |
| "order status (placed, preparing, delivered)" | `OrderStatus` | `PLACED, PREPARING, DELIVERED` |
| "payment type (cash, card, upi)" | `PaymentType` | `CASH, CARD, UPI` |
| "elevator direction (up, down, idle)" | `Direction` | `UP, DOWN, IDLE` |

From your codebase:
```java
// VehicleSize.java — spotted because problem says "size: motorcycle, compact, large"
public enum VehicleSize { MOTORCYCLE, COMPACT, LARGE }

// LogLevel.java — spotted because problem says "levels: DEBUG, INFO, WARN, ERROR"
public enum LogLevel { DEBUG, INFO, WARNING, ERROR }
```

---

## 🔄 TECHNIQUE 4: Spot Interfaces from "Multiple Implementations"

**Rule:** Whenever you see **"supports multiple types of ___"** or **"can be extended with new ___"** → that's an Interface.

| Phrase in Problem | Interface to Create |
|---|---|
| "log to console, file, OR database" | `LogAppender` |
| "pay by cash, card, OR UPI" | `PaymentProcessor` |
| "sort by bubble, quick, OR merge sort" | `SortStrategy` |
| "notify by email, SMS, OR push" | `NotificationSender` |
| "store in MySQL, MongoDB, OR Redis" | `Repository` or `Storage` |

**Your Logging Framework example:**
```
Problem says: "written to different destinations (console, file, database)"
                                   ↑
              "different destinations" = multiple implementations of ONE thing
                                   ↓
              → Create interface: LogAppender
              → Concrete classes: ConsoleAppender, FileAppender, DBAppender
```

```java
// The interface — defines the CONTRACT
public interface LogAppender {
    void append(LogMessage message);   // ← all destinations must do this
}

// Each destination IMPLEMENTS the contract differently
class ConsoleAppender implements LogAppender {
    public void append(LogMessage msg) { System.out.println(msg); }
}
class FileAppender implements LogAppender {
    public void append(LogMessage msg) { /* write to file */ }
}
class DBAppender implements LogAppender {
    public void append(LogMessage msg) { /* write to DB */ }
}
```

---

## 🔁 TECHNIQUE 5: Spot Singletons (Only-One Rule)

**Rule:** If the problem describes something where **only one should ever exist** → Singleton.

**Trigger phrases:** "the system", "the machine", "the lot", "the server", "the manager"

| Phrase | Singleton |
|---|---|
| "the parking lot" (not "a" lot) | `ParkingLot` |
| "the coffee machine" | `CoffeeMachine` |
| "the logger" | `Logger` |
| "the ATM controller" | `ATMController` |
| "the database connection pool" | `ConnectionPool` |

> 💡 **Trick:** The word **"the"** (definite article) in a problem often hints at a Singleton.  
> **"a vehicle"** = many vehicles = regular class  
> **"the machine"** = only one = Singleton

---

## 🗂️ Full Class Identification Walkthrough

Here's the **complete step-by-step process** — applied to all 3 projects side by side:

### STEP 1: Extract ALL nouns

| Parking Lot | Coffee Machine | Logging Framework |
|---|---|---|
| vehicle | coffee | log message |
| car, truck, motorcycle | ingredient | logger |
| parking spot | payment | appender |
| parking floor | coffee machine | console, file, database |
| parking lot | menu | log level |
| ticket | recipe | configuration |
| size | | |

### STEP 2: Apply the 5 Filters to each noun

| Noun | Filter Applied | Result |
|---|---|---|
| vehicle | Has subtypes (car/truck/motorcycle) with SHARED data | **Abstract Class** `Vehicle` |
| car | Specific type of vehicle | **Concrete Class** `Car extends Vehicle` |
| parking spot | Single physical spot, holds data | **Concrete Class** `ParkingSpot` |
| parking floor | Groups spots together | **Concrete Class** `ParkingFloor` |
| parking lot | Only ONE exists | **Singleton Class** `ParkingLot` |
| size | Fixed values: MOTORCYCLE, COMPACT, LARGE | **Enum** `VehicleSize` |
| ingredient | Single ingredient, has name + qty | **Concrete Class** `Ingredient` |
| coffee | Single type with recipe | **Concrete Class** `Coffee` |
| payment | Carries amount data | **Concrete Class** `Payment` |
| coffee machine | Only ONE exists | **Singleton Class** `CoffeeMachine` |
| appender | Multiple types (console/file/DB) — capability only | **Interface** `LogAppender` |
| console appender | Specific implementation | **Concrete Class** `ConsoleAppender implements LogAppender` |
| log message | Carries level + text data | **Concrete Class** `LogMessage` |
| logger | Only ONE exists | **Singleton Class** `Logger` |
| log level | Fixed values: DEBUG, INFO, WARN, ERROR | **Enum** `LogLevel` |
| configuration | Groups logger settings | **Concrete Class** `LogConfig` |

### STEP 3: Organize into a hierarchy

```
Parking Lot
├── Enum:     VehicleSize
├── Abstract: Vehicle
│   ├── Class: Car
│   ├── Class: Truck
│   └── Class: Motorcycle
├── Class:    ParkingSpot  (uses Vehicle, VehicleSize)
├── Class:    ParkingFloor (contains List<ParkingSpot>)
└── Singleton:ParkingLot   (contains List<ParkingFloor>)

Coffee Machine
├── Class:    Ingredient
├── Class:    Coffee       (uses Map<String,Integer> recipe)
├── Class:    Payment
└── Singleton:CoffeeMachine (contains Map<Ingredient>, Map<Coffee>)

Logging Framework
├── Enum:     LogLevel
├── Class:    LogMessage   (uses LogLevel)
├── Interface:LogAppender
│   ├── Class: ConsoleAppender
│   ├── Class: FileAppender
│   └── Class: DBAppender
├── Class:    LogConfig    (uses LogLevel + List<LogAppender>)
└── Singleton:Logger       (uses LogConfig)
```

---

## 📌 The Decision Flowchart

Use this every time you're unsure what to create:

```
You found a concept/noun in the problem
               │
               ▼
   Does it have only FIXED VALUES?
   (status, level, type, size)
        YES ──────────────────────► ENUM
        │
        NO
        ▼
   Should only ONE instance exist?
   ("the system", "the machine", "the manager")
        YES ──────────────────────► SINGLETON CLASS
        │
        NO
        ▼
   Does it come in MULTIPLE TYPES that
   share common DATA/FIELDS?
   ("Car is a Vehicle", "Sparrow is a Bird")
        YES ──────────────────────► ABSTRACT CLASS
        │
        NO
        ▼
   Can MULTIPLE unrelated classes do this
   ACTION/BEHAVIOUR?
   ("can append", "can pay", "can notify")
        YES ──────────────────────► INTERFACE
        │
        NO
        ▼
   Is it just a plain THING with data?
        YES ──────────────────────► CONCRETE CLASS
```

---

## 🧪 Practice: Apply to a New Problem

**Problem:** _"Design a notification system where users can subscribe to events and receive notifications via email, SMS, or push. Each notification has a priority (LOW, MEDIUM, HIGH)."_

Try it yourself first, then check:

<details>
<summary>👁️ Click to reveal the answer</summary>

```
Nouns found:
  user, event, notification, email, SMS, push, priority

Apply filters:
  user         → Concrete Class  User (has name, email, phone data)
  event        → Concrete Class  Event (has name, description)
  notification → Concrete Class  Notification (has message, priority)
  priority     → ENUM            NotificationPriority { LOW, MEDIUM, HIGH }
  email/SMS/push → "multiple ways to notify" = INTERFACE NotificationSender
                   → EmailSender implements NotificationSender
                   → SMSSender   implements NotificationSender
                   → PushSender  implements NotificationSender
  notification system → SINGLETON NotificationService

Final list:
  Enum:      NotificationPriority
  Class:     User
  Class:     Event
  Class:     Notification
  Interface: NotificationSender
  Class:     EmailSender  implements NotificationSender
  Class:     SMSSender    implements NotificationSender
  Class:     PushSender   implements NotificationSender
  Singleton: NotificationService
```
</details>

---

## 🚀 Quick Reference Card

```
WHAT YOU SEE IN PROBLEM          →  WHAT YOU CREATE
────────────────────────────────────────────────────────
A "thing" with data              →  Concrete Class
Multiple TYPES of a thing        →  Abstract Class (shared data)
  sharing common fields
Multiple ways to DO something    →  Interface (no shared data)
"the system / the machine"       →  Singleton
Fixed named values               →  Enum
  (status, level, type, size)
"supports pluggable ___"         →  Interface
"is a type of ___"               →  Subclass / extends
"can do ___"                     →  implements Interface
────────────────────────────────────────────────────────
```

---

*Part of the `system-design-lld` project guide series.*

