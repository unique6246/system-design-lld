# 📘 Low Level Design (LLD) — Complete Guide from Scratch

> A comprehensive guide to understanding, approaching, and solving LLD problems in interviews and real-world scenarios.

---

## 📌 Table of Contents

1. [What is LLD?](#1-what-is-lld)
2. [LLD vs HLD](#2-lld-vs-hld)
3. [Core OOP Concepts](#3-core-oop-concepts)
4. [SOLID Principles](#4-solid-principles)
5. [Design Patterns](#5-design-patterns)
6. [UML & Class Diagrams](#6-uml--class-diagrams)
7. [How to Approach an LLD Problem](#7-how-to-approach-an-lld-problem)
8. [Step-by-Step Problem Solving Framework](#8-step-by-step-problem-solving-framework)
9. [Common LLD Problems & Solutions](#9-common-lld-problems--solutions)
10. [Tips & Best Practices](#10-tips--best-practices)

---

## 1. What is LLD?

**Low Level Design (LLD)** is the process of defining the **detailed design of individual components** of a system.

It focuses on:
- **Class design** — What classes, interfaces, and objects are needed
- **Relationships** — How classes interact with each other
- **Behavior** — Methods, attributes, and logic inside each class
- **Design Patterns** — Reusable solutions to common problems

LLD answers: _"How will we implement this system?"_

---

## 2. LLD vs HLD

| Aspect             | HLD (High Level Design)              | LLD (Low Level Design)                   |
|--------------------|---------------------------------------|------------------------------------------|
| Focus              | System architecture                   | Class/component design                   |
| Abstraction Level  | High (services, databases, APIs)      | Low (classes, methods, attributes)       |
| Output             | Architecture diagrams, tech stack     | Class diagrams, sequence diagrams        |
| Audience           | Architects, Managers                  | Developers                               |
| Example            | Microservices layout, DB selection    | Parking Lot class structure              |

---

## 3. Core OOP Concepts

These are the **building blocks** of every LLD solution.

### 3.1 Encapsulation
> Bundling data and methods that operate on data within a single unit (class), and restricting direct access.

```java
public class BankAccount {
    private double balance; // hidden from outside

    public double getBalance() { return balance; }
    public void deposit(double amount) { balance += amount; }
}
```

### 3.2 Abstraction
> Hiding complex implementation details and exposing only the necessary interface.

```java
public abstract class Shape {
    public abstract double area(); // what to do, not how
}
public class Circle extends Shape {
    private double radius;
    public double area() { return Math.PI * radius * radius; }
}
```

### 3.3 Inheritance
> A class (child) acquires properties and behaviors of another class (parent).

```java
public class Vehicle { 
    protected String brand;
    public void start() { System.out.println("Starting..."); }
}
public class Car extends Vehicle {
    private int doors;
}
```

### 3.4 Polymorphism
> One interface, multiple implementations. Achieved via method overriding and overloading.

```java
// Runtime Polymorphism
Shape s = new Circle();
s.area(); // calls Circle's area()

// Compile-time Polymorphism (Overloading)
public int add(int a, int b) { return a + b; }
public double add(double a, double b) { return a + b; }
```

---

## 4. SOLID Principles

SOLID is the **backbone of clean LLD**. Every good design follows these.

### S — Single Responsibility Principle (SRP)
> A class should have **only one reason to change**.

❌ Bad:
```java
class UserService {
    public void saveUser() { /* DB logic */ }
    public void sendEmail() { /* Email logic */ }
    public void generateReport() { /* Report logic */ }
}
```
✅ Good:
```java
class UserRepository  { public void saveUser() {} }
class EmailService    { public void sendEmail() {} }
class ReportGenerator { public void generateReport() {} }
```

---

### O — Open/Closed Principle (OCP)
> Classes should be **open for extension, closed for modification**.

❌ Bad: Modifying existing class for each new payment type  
✅ Good:
```java
interface PaymentProcessor {
    void processPayment(double amount);
}
class CreditCardPayment implements PaymentProcessor { ... }
class UPIPayment implements PaymentProcessor { ... }
// Adding new payment = new class, no old code change
```

---

### L — Liskov Substitution Principle (LSP)
> Subtypes must be **substitutable for their base types** without breaking the application.

❌ Bad:
```java
class Bird { public void fly() {} }
class Penguin extends Bird {
    public void fly() { throw new UnsupportedOperationException(); } // ❌ breaks LSP
}
```
✅ Good:
```java
interface Flyable { void fly(); }
class Sparrow implements Flyable { public void fly() {} }
class Penguin extends Bird { /* no fly */ }
```

---

### I — Interface Segregation Principle (ISP)
> Clients should **not be forced to implement interfaces they don't use**.

❌ Bad:
```java
interface Animal {
    void eat(); void fly(); void swim(); void run();
}
```
✅ Good:
```java
interface Eatable { void eat(); }
interface Flyable  { void fly(); }
interface Swimmable{ void swim(); }
class Duck implements Eatable, Flyable, Swimmable { ... }
```

---

### D — Dependency Inversion Principle (DIP)
> High-level modules should **not depend on low-level modules**. Both should depend on **abstractions**.

❌ Bad:
```java
class OrderService {
    MySQLDatabase db = new MySQLDatabase(); // tightly coupled
}
```
✅ Good:
```java
interface Database { void save(Order o); }
class OrderService {
    Database db; // depends on abstraction
    OrderService(Database db) { this.db = db; }
}
```

---

## 5. Design Patterns

Design patterns are **proven, reusable solutions** to commonly occurring problems.

### 5.1 Creational Patterns
> Deal with **object creation** mechanisms.

| Pattern          | Intent                                                        | Use Case                          |
|------------------|---------------------------------------------------------------|-----------------------------------|
| **Singleton**    | Only one instance of a class exists                           | Logger, Config, DB Connection     |
| **Factory**      | Delegate object creation to subclasses                        | Shape factory, Notification factory |
| **Abstract Factory** | Family of related objects without specifying concrete classes | UI theme components               |
| **Builder**      | Construct complex objects step by step                        | QueryBuilder, HttpRequest         |
| **Prototype**    | Clone existing objects                                        | Copying game objects              |

#### Singleton Pattern
```java
public class Logger {
    private static Logger instance;
    private Logger() {}
    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) instance = new Logger();
            }
        }
        return instance;
    }
}
```

#### Factory Pattern
```java
interface Notification { void send(String msg); }
class EmailNotification implements Notification { public void send(String msg) { /* email */ } }
class SMSNotification   implements Notification { public void send(String msg) { /* sms */ } }

class NotificationFactory {
    public static Notification create(String type) {
        return switch (type) {
            case "EMAIL" -> new EmailNotification();
            case "SMS"   -> new SMSNotification();
            default -> throw new IllegalArgumentException("Unknown type");
        };
    }
}
```

#### Builder Pattern
```java
public class Pizza {
    private String size;
    private boolean cheese, pepperoni;

    private Pizza(Builder b) { this.size = b.size; this.cheese = b.cheese; this.pepperoni = b.pepperoni; }

    public static class Builder {
        private String size;
        private boolean cheese, pepperoni;
        public Builder size(String s) { this.size = s; return this; }
        public Builder cheese()       { this.cheese = true; return this; }
        public Builder pepperoni()    { this.pepperoni = true; return this; }
        public Pizza build()          { return new Pizza(this); }
    }
}
// Usage: Pizza p = new Pizza.Builder().size("Large").cheese().build();
```

---

### 5.2 Structural Patterns
> Deal with **class and object composition**.

| Pattern      | Intent                                             | Use Case                        |
|--------------|----------------------------------------------------|---------------------------------|
| **Adapter**  | Convert interface to another interface             | Legacy system integration       |
| **Decorator**| Add behavior to objects dynamically                | Java I/O streams, UI components |
| **Facade**   | Simplified interface to complex subsystem          | API Gateway, SDK                |
| **Proxy**    | Placeholder/surrogate for another object           | Lazy loading, access control    |
| **Composite**| Treat individual and composite objects uniformly   | File system, UI hierarchy       |

#### Decorator Pattern
```java
interface Coffee { double cost(); }
class SimpleCoffee implements Coffee { public double cost() { return 5; } }

class MilkDecorator implements Coffee {
    private Coffee coffee;
    MilkDecorator(Coffee c) { this.coffee = c; }
    public double cost() { return coffee.cost() + 1.5; }
}
// Usage: Coffee c = new MilkDecorator(new SimpleCoffee()); // cost = 6.5
```

---

### 5.3 Behavioral Patterns
> Deal with **communication/interaction between objects**.

| Pattern          | Intent                                                  | Use Case                        |
|------------------|---------------------------------------------------------|---------------------------------|
| **Observer**     | Notify multiple objects about state changes             | Event systems, notifications    |
| **Strategy**     | Define a family of algorithms, make them interchangeable| Sorting, payment strategies     |
| **Command**      | Encapsulate requests as objects                         | Undo/Redo, Task queues          |
| **Iterator**     | Access elements without knowing internal structure      | Collections                     |
| **State**        | Object changes behavior when state changes              | Vending machine, traffic light  |
| **Template Method** | Define skeleton of algorithm in base class           | Data processing pipelines       |
| **Chain of Responsibility** | Pass request along a chain of handlers       | Logging levels, middleware      |

#### Strategy Pattern
```java
interface SortStrategy { void sort(int[] arr); }
class BubbleSort implements SortStrategy { public void sort(int[] arr) { /* bubble sort */ } }
class QuickSort  implements SortStrategy { public void sort(int[] arr) { /* quick sort */ } }

class Sorter {
    private SortStrategy strategy;
    Sorter(SortStrategy s) { this.strategy = s; }
    public void sort(int[] arr) { strategy.sort(arr); }
}
```

#### Observer Pattern
```java
interface Observer { void update(String event); }
interface Subject  { void subscribe(Observer o); void notifyAll(String event); }

class EventManager implements Subject {
    List<Observer> observers = new ArrayList<>();
    public void subscribe(Observer o) { observers.add(o); }
    public void notifyAll(String event) { observers.forEach(o -> o.update(event)); }
}
```

---

## 6. UML & Class Diagrams

### Relationships

| Relationship   | Symbol  | Meaning                                      | Example                        |
|----------------|---------|----------------------------------------------|--------------------------------|
| Association    | `——>`   | Class A uses Class B                         | Student uses Library           |
| Aggregation    | `◇——>`  | Has-a (weak): B can exist without A          | Department has Employees       |
| Composition    | `◆——>`  | Has-a (strong): B cannot exist without A     | House has Rooms                |
| Inheritance    | `——▷`   | Is-a relationship                            | Dog is-a Animal                |
| Realization    | `- -▷`  | Class implements interface                   | Dog implements Runnable        |
| Dependency     | `- ->`  | Class A temporarily uses Class B             | OrderService uses Payment      |

### Class Diagram Notation
```
┌──────────────────────┐
│      ClassName        │   ← Class Name
├──────────────────────┤
│ - privateField: Type  │   ← Attributes
│ + publicField: Type   │     (- private, + public, # protected)
├──────────────────────┤
│ + method(): ReturnType│   ← Methods
│ - helper(): void      │
└──────────────────────┘
```

---

## 7. How to Approach an LLD Problem

### The 5-Step Mental Framework

```
1. UNDERSTAND  →  2. IDENTIFY ENTITIES  →  3. DEFINE RELATIONSHIPS  →  4. APPLY PATTERNS  →  5. CODE
```

### Step 1: UNDERSTAND the Problem
- Read the problem statement carefully
- Ask clarifying questions:
  - What are the **core features** needed?
  - What are the **constraints**? (scale, concurrency, etc.)
  - What are the **actors** (users, systems)?
  - What are **edge cases**?

### Step 2: IDENTIFY ENTITIES (Nouns = Classes)
- Underline all **nouns** in the problem → these are potential classes
- Example: "Design a Parking Lot where vehicles can park..."
  - → `ParkingLot`, `Vehicle`, `ParkingSpot`, `Ticket`, `Payment`

### Step 3: DEFINE RELATIONSHIPS
- What **belongs to** what? (Composition/Aggregation)
- What **is a** type of what? (Inheritance)
- What **uses** what? (Association/Dependency)

### Step 4: APPLY DESIGN PATTERNS
- Do you need a **single instance**? → Singleton
- Do you need to **create objects**? → Factory / Builder
- Do you need **interchangeable algorithms**? → Strategy
- Do you need **event notification**? → Observer
- Do you need to **add behavior dynamically**? → Decorator

### Step 5: CODE it out
- Start with **interfaces/abstract classes**
- Then **concrete implementations**
- Apply **SOLID** throughout
- Handle **edge cases** and **exceptions**

---

## 8. Step-by-Step Problem Solving Framework

Here is the **exact approach** to use during interviews or when designing:

```
┌─────────────────────────────────────────────────────────┐
│              LLD PROBLEM SOLVING CHECKLIST               │
├─────────────────────────────────────────────────────────┤
│ ✅ Step 1: Clarify Requirements                          │
│    • Functional requirements (what it must do)           │
│    • Non-functional requirements (scale, concurrency)    │
│                                                          │
│ ✅ Step 2: List Core Use Cases                           │
│    • Main flows (happy path)                             │
│    • Edge cases                                          │
│                                                          │
│ ✅ Step 3: Identify Entities & Attributes                │
│    • Nouns → Classes                                     │
│    • Properties → Attributes                             │
│    • Actions → Methods                                   │
│                                                          │
│ ✅ Step 4: Define Relationships                          │
│    • Inheritance, Composition, Association               │
│                                                          │
│ ✅ Step 5: Apply Design Patterns                         │
│    • Justify pattern choices                             │
│                                                          │
│ ✅ Step 6: Define Interfaces/Abstract Classes First      │
│    • Program to interfaces, not implementations          │
│                                                          │
│ ✅ Step 7: Implement Concrete Classes                    │
│    • Follow SOLID principles                             │
│                                                          │
│ ✅ Step 8: Handle Concurrency (if needed)               │
│    • Thread safety with synchronized / locks             │
│                                                          │
│ ✅ Step 9: Review & Refactor                             │
│    • Check SOLID violations                              │
│    • Simplify if over-engineered                         │
└─────────────────────────────────────────────────────────┘
```

---

## 9. Common LLD Problems & Solutions

### 9.1 Parking Lot System

**Entities:** `ParkingLot`, `ParkingFloor`, `ParkingSpot`, `Vehicle`, `Ticket`, `Payment`

**Key Design Decisions:**
- `ParkingLot` → Singleton (only one instance)
- `Vehicle` → Abstract class with `Car`, `Motorcycle`, `Truck` as subtypes
- `ParkingSpot` → Has size (SMALL, MEDIUM, LARGE), can be available/occupied
- Strategy Pattern for spot allocation (find nearest, find cheapest)

```
ParkingLot (Singleton)
  └── has many ParkingFloor
        └── has many ParkingSpot
              └── can hold one Vehicle
```

---

### 9.2 Coffee Vending Machine

**Entities:** `CoffeeMachine`, `Coffee`, `Ingredient`, `Payment`

**Key Design Decisions:**
- `CoffeeMachine` → Singleton
- `Coffee` → Has a recipe (list of ingredients)
- State Pattern → `IDLE`, `SELECTING`, `PROCESSING`, `DISPENSING`
- Strategy Pattern → different payment methods

```
CoffeeMachine (Singleton)
  └── has Inventory of Ingredients
  └── accepts Payment
  └── dispenses Coffee
```

---

### 9.3 Logging Framework

**Entities:** `Logger`, `LogMessage`, `LogAppender`, `LogConfig`

**Key Design Decisions:**
- `Logger` → Singleton
- `LogAppender` → Interface with `ConsoleAppender`, `FileAppender`, `DBAppender`
- Chain of Responsibility → log levels filter messages
- Observer Pattern → multiple appenders notified

```
Logger (Singleton)
  └── has LogConfig (log level, appenders)
  └── creates LogMessage
  └── sends to LogAppender(s)
        ├── ConsoleAppender
        ├── FileAppender
        └── DBAppender
```

---

### 9.4 Elevator System

**Entities:** `ElevatorSystem`, `Elevator`, `Floor`, `Request`, `Door`

**Key Design Decisions:**
- State Pattern → `IDLE`, `MOVING_UP`, `MOVING_DOWN`, `DOOR_OPEN`
- Strategy Pattern → scheduling algorithm (FCFS, SCAN, LOOK)
- Observer Pattern → notify when elevator arrives

---

### 9.5 Library Management System

**Entities:** `Library`, `Book`, `BookItem`, `Member`, `Librarian`, `Loan`, `Catalog`

**Key Design Decisions:**
- `Book` vs `BookItem` — Book is the concept, BookItem is the physical copy
- Strategy Pattern → search by title, author, ISBN
- Composite Pattern → catalog sections

---

### 9.6 Chess Game

**Entities:** `Game`, `Board`, `Piece`, `Player`, `Move`

**Key Design Decisions:**
- `Piece` → Abstract class: `King`, `Queen`, `Rook`, `Bishop`, `Knight`, `Pawn`
- Each Piece implements `getValidMoves()`
- Command Pattern → for moves (supports undo)
- State Pattern → `ACTIVE`, `CHECK`, `CHECKMATE`, `STALEMATE`

---

### 9.7 ATM Machine

**Entities:** `ATM`, `Card`, `Account`, `Transaction`, `CashDispenser`

**Key Design Decisions:**
- State Pattern → `IDLE`, `HAS_CARD`, `AUTHENTICATED`, `SELECTING_TRANSACTION`
- Strategy Pattern → transaction types (withdraw, deposit, check balance)
- Singleton → ATM controller

---

### 9.8 Food Delivery System (Zomato/Swiggy)

**Entities:** `Customer`, `Restaurant`, `MenuItem`, `Order`, `DeliveryAgent`, `Payment`

**Key Design Decisions:**
- Observer Pattern → notify customer and restaurant on order status change
- Strategy Pattern → payment methods
- Factory Pattern → order creation
- State Pattern → `PLACED`, `ACCEPTED`, `PREPARING`, `OUT_FOR_DELIVERY`, `DELIVERED`

---

## 10. Tips & Best Practices

### ✅ DO's
- **Start with interfaces** — always code to abstraction
- **Follow SOLID** — especially SRP and OCP
- **Use enums** for fixed sets of values (status, types, sizes)
- **Make classes immutable** where possible
- **Handle null gracefully** — use Optional or null checks
- **Use meaningful names** — self-documenting code
- **Think about thread safety** — mention it even if not implementing

### ❌ DON'Ts
- Don't **over-engineer** — keep it simple first, extend later
- Don't create **God classes** — one class doing everything
- Don't use **global state** unless justified (Singleton has a cost)
- Don't ignore **edge cases** — empty inputs, concurrent access
- Don't **copy-paste code** — extract to methods/classes

### 🎯 Interview Tips
1. **Think out loud** — explain your thought process
2. **Clarify before coding** — ask questions upfront
3. **Draw class diagram first** — visualize before coding
4. **Justify your design choices** — why Singleton? why Strategy?
5. **Start simple, then extend** — MVP first, then refine
6. **Mention what you would do differently** at scale

---

## 🔁 Quick Reference Cheat Sheet

```
PROBLEM TYPE                    PATTERN TO USE
─────────────────────────────────────────────────
Single instance needed?       → Singleton
Create objects without knowing
  exact class?                → Factory / Abstract Factory
Build complex objects?        → Builder
Add behavior at runtime?      → Decorator
Interchangeable algorithms?   → Strategy
Notify multiple objects?      → Observer
Object changes by state?      → State
Simplify complex subsystem?   → Facade
Legacy interface mismatch?    → Adapter
Request passed to handlers?   → Chain of Responsibility
Undo/Redo support?            → Command
─────────────────────────────────────────────────

OOP PILLAR        USE WHEN
────────────────────────────────────────────────
Encapsulation   → Always — protect your data
Abstraction     → Hide complexity behind interfaces
Inheritance     → True "is-a" relationships only
Polymorphism    → Same operation, different behavior
────────────────────────────────────────────────

SOLID             ONE-LINER
────────────────────────────────────────────────
S  SRP          → One class = one job
O  OCP          → Extend, don't modify
L  LSP          → Subclass must honor parent contract
I  ISP          → Small, focused interfaces
D  DIP          → Depend on abstractions, not concretions
────────────────────────────────────────────────
```

---

> 💡 **Final Advice:** LLD is a skill built through **practice**. Design at least 2-3 systems per week. Review existing codebases. The more systems you design, the more patterns become second nature.

---

*Guide created for the `system-design-lld` project — covers Parking Lot, Coffee Vending Machine, Logging Framework, and more.*

