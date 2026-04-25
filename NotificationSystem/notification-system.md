# 📬 Notification System — LLD Implementation Guide

> Complete explanation of how the Notification System is designed, what each class does,
> which design patterns are used, and how everything connects end to end.

---

## 📌 Table of Contents

1. [Problem Statement](#1-problem-statement)
2. [Design Patterns Used](#2-design-patterns-used)
3. [Folder Structure](#3-folder-structure)
4. [Class Identification — How We Found Each Class](#4-class-identification)
5. [Class-by-Class Explanation](#5-class-by-class-explanation)
6. [How Everything Connects — The Full Flow](#6-how-everything-connects)
7. [Step-by-Step Code Walkthrough](#7-step-by-step-code-walkthrough)
8. [Expected Output](#8-expected-output)
9. [Design Decisions Explained](#9-design-decisions-explained)
10. [Class Diagram](#10-class-diagram)

---

## 1. Problem Statement

> Design a Notification System where:
> - Users can **subscribe** to receive notifications
> - Users can choose their **preferred channels** (Email, SMS, Push)
> - When an **event** fires, all subscribed users are notified via their own channels
> - Users can **unsubscribe** and stop receiving notifications
> - Events have a **priority** (LOW, MEDIUM, HIGH) and can be filtered
> - A single user can be targeted directly

---

## 2. Design Patterns Used

| Pattern | Where Used | Why |
|---|---|---|
| **Observer** | `Observer`, `Subject`, `EventPublisher`, `User` | Decouple event firing from user notification — users subscribe and get called automatically |
| **Singleton** | `NotificationSystem` | Only one system instance should exist in the application |
| **Strategy** | `Notification` interface + `EmailNotify`, `SmsNotify`, `PushNotify` | Each channel is an interchangeable algorithm for "how to deliver a notification" |

---

## 3. Folder Structure

```
NotificationSystem/
│
├── NotifyExample.java              ← Main / Driver class
│
├── model/
│   ├── Event.java                  ← Immutable event data (what happened)
│   └── Priority.java               ← Enum: LOW, MEDIUM, HIGH
│
├── observer/
│   ├── Observer.java               ← Interface: update(Event)
│   └── Subject.java                ← Interface: subscribe, unsubscribe, notifyObservers
│
├── publisher/
│   └── EventPublisher.java         ← Concrete Subject: holds observers, fires events
│
├── notify/
│   ├── Notification.java           ← Interface: sendNotification(User, Event)
│   └── impl/
│       ├── EmailNotify.java        ← Concrete channel: Email delivery
│       ├── SmsNotify.java          ← Concrete channel: SMS delivery
│       └── PushNotify.java         ← Concrete channel: Push delivery
│
├── users/
│   └── User.java                   ← Concrete Observer: has own channels, update() callback
│
└── singelton/
    └── NotificationSystem.java     ← Singleton coordinator: owns publisher + user list
```

---

## 4. Class Identification

How we identified each class from the problem statement using the **Noun Extraction** technique:

| Noun in Problem | What It Became | Type |
|---|---|---|
| "notification system" | `NotificationSystem` | **Singleton** — only one exists |
| "event" | `Event` | **Concrete Class** — carries data (message, priority, timestamp) |
| "priority (LOW, MEDIUM, HIGH)" | `Priority` | **Enum** — fixed set of values |
| "users" | `User` | **Concrete Class** — also implements `Observer` |
| "channels (email, sms, push)" | `Notification` interface | **Interface** — multiple ways to deliver |
| "email / SMS / push" | `EmailNotify`, `SmsNotify`, `PushNotify` | **Concrete classes** — each implements `Notification` |
| "subscribe / unsubscribe" | `Observer` + `Subject` | **Interfaces** — Observer pattern contracts |
| "publisher / broadcaster" | `EventPublisher` | **Concrete Subject** — manages observer list |

> **Rule applied:**  
> `"multiple ways to deliver"` → Interface (`Notification`)  
> `"fixed values"` → Enum (`Priority`)  
> `"only one exists"` → Singleton (`NotificationSystem`)  
> `"subscribe/notify relationship"` → Observer Pattern

---

## 5. Class-by-Class Explanation

---

### 5.1 `Priority` (Enum)

```java
public enum Priority {
    LOW, MEDIUM, HIGH
}
```

**Purpose:** Fixed set of event importance levels.  
**Why enum?** Because there are exactly 3 known values — nothing more, nothing less.  
**Used in:** `Event` constructor, `EventPublisher.publish(event, minPriority)` for filtering.

---

### 5.2 `Event` (Immutable Model Class)

```java
public class Event {
    private final int eId;
    private final Priority priority;   // final — cannot change after creation
    private final String message;      // final — prevents mutation bug
    private final String timeStamp;

    public Event(int eId, Priority priority, String message) { ... }
    // NO setters — immutable by design
}
```

**Purpose:** Carries the data about what happened (the "what").  
**Why immutable (`final` fields, no setters)?**  
An `Event` is shared across all channels (Email, SMS, Push). If any channel modified the event,
it would corrupt data for other channels. Making it immutable makes this bug impossible.

> **Rule:** Objects shared across multiple callers should be immutable.

**Constructor receives:**
| Param | Why |
|---|---|
| `eId` | Identity of the event |
| `priority` | Importance level (for filtering) |
| `message` | What to communicate |
| `timeStamp` | Auto-generated at creation time |

---

### 5.3 `Observer` Interface

```java
public interface Observer {
    void update(Event event);
}
```

**Purpose:** Contract that every "subscriber" must follow.  
**Who implements it:** `User`  
**Who calls it:** `EventPublisher.notifyObservers()` calls `observer.update(event)` on every subscriber.  

> Think of it as: "When something happens, I promise to respond via `update()`."

---

### 5.4 `Subject` Interface

```java
public interface Subject {
    void subscribe(Observer observer);
    void unsubscribe(Observer observer);
    void notifyObservers(Event event);
}
```

**Purpose:** Contract for the "thing being watched" — it manages the observer list.  
**Who implements it:** `EventPublisher`  
**Three responsibilities:**

| Method | What It Does |
|---|---|
| `subscribe(observer)` | Add a user to the notification list |
| `unsubscribe(observer)` | Remove a user — they stop receiving events |
| `notifyObservers(event)` | Loop all observers and call `update(event)` on each |

---

### 5.5 `EventPublisher` (Concrete Subject)

```java
public class EventPublisher implements Subject {
    private final List<Observer> observers = new ArrayList<>();

    public void subscribe(Observer o)   { observers.add(o); }
    public void unsubscribe(Observer o) { observers.remove(o); }

    public void notifyObservers(Event event) {
        new ArrayList<>(observers)          // copy — safe if someone unsubscribes mid-loop
            .forEach(o -> o.update(event)); // calls update() on every subscribed User
    }

    public void publish(Event event) {
        notifyObservers(event);             // fire event to all observers
    }

    public void publish(Event event, Priority minPriority) {
        if (event.getPriority().ordinal() >= minPriority.ordinal()) {
            publish(event);                 // only fire if priority is high enough
        }
    }
}
```

**Purpose:** The heart of the Observer pattern. Holds the list of all subscribed users and fires `update()` on each when an event occurs.

**Why copy the list before looping?**
```java
new ArrayList<>(observers).forEach(...)
// ↑ If a user unsubscribes WHILE the loop is running (concurrent scenario),
// modifying the original list during iteration causes ConcurrentModificationException.
// Iterating a copy is safe.
```

**Constructor receives:** Nothing — it starts with an empty observer list.  
**Observers are added later** via `subscribe()`.

---

### 5.6 `Notification` Interface (Strategy Pattern)

```java
public interface Notification {
    void sendNotification(User user, Event event);
}
```

**Purpose:** Defines the contract for "how to deliver" a notification.  
**Why interface?** Because Email, SMS, and Push all have the **same capability** but **different behaviour**. No shared data, just a shared contract.  
**User and Event come in at call time** — channels are stateless and reusable.

---

### 5.7 `EmailNotify` / `SmsNotify` / `PushNotify` (Concrete Channels)

```java
public class EmailNotify implements Notification {
    @Override
    public void sendNotification(User user, Event event) {
        System.out.println("[EMAIL] → " + user.getName()
            + " | Priority: " + event.getPriority()
            + " | Message: "  + event.getMessage());
        user.recordEvent(event);   // log in user's history
    }
}
```

**Purpose:** Each knows HOW to deliver — not WHO or WHAT (that comes from the method params).  
**Why stateless (no fields)?**  
One `EmailNotify` instance can serve ALL users and ALL events. You don't need a new object per user.

> Like a postman — one postman delivers to all houses. You don't hire a new postman per house.

**All 3 classes are identical in structure** — they just print a different channel prefix (`[EMAIL]`, `[SMS]`, `[PUSH]`). In a real system, each would call an actual API (SMTP server, Twilio SMS API, FCM push API).

---

### 5.8 `User` (Concrete Observer)

```java
public class User implements Observer {
    private final int uId;
    private final String name;
    private final List<Notification> channels;    // OWN preferred channels
    private final List<Event> eventHistory;

    public void addChannel(Notification channel) { channels.add(channel); }

    @Override
    public void update(Event event) {             // ← Observer callback
        for (Notification channel : channels) {
            channel.sendNotification(this, event); // send via each of user's channels
        }
        recordEvent(event);                        // log it in history
    }
}
```

**Purpose:** The "subscriber" — implements `Observer` and has its own channel preferences.

**Key design: Each user owns their own channels**
```
Alice   → [EmailNotify, SmsNotify]      ← Alice configured this
Bob     → [PushNotify]                  ← Bob configured this
Charlie → [EmailNotify, SmsNotify, PushNotify]
```

This mirrors real apps — users go to Settings → Notifications and choose what they want.

**Constructor receives:**
| Param | Why |
|---|---|
| `uId` | Identity for lookup and targeting |
| `name` | Display name |

Channels are added via `addChannel()` after construction.

---

### 5.9 `NotificationSystem` (Singleton Coordinator)

```java
public class NotificationSystem {                       // Singleton
    private final EventPublisher publisher;             // owns the Subject
    private final List<User> users;                     // for lookup + display

    public void addUser(User user) {
        users.add(user);
        publisher.subscribe(user);    // ← automatically subscribes as Observer
    }

    public void removeUser(int userId) {
        publisher.unsubscribe(user);  // ← automatically unsubscribes
        users.remove(user);
    }

    public void publish(Event event)                           { publisher.publish(event); }
    public void publish(Event event, Priority min)             { publisher.publish(event, min); }
    public void notifyUser(int userId, Event event)            { user.update(event); } // direct
}
```

**Purpose:** The single entry point for the whole system. Wires together the publisher and users.  
**Why Singleton?** There should be only ONE notification system in the application — just like there's only one database connection pool or one config system.

**What it owns:**
| Field | Why |
|---|---|
| `EventPublisher publisher` | The Subject — manages observer list and fires events |
| `List<User> users` | Kept separately for lookup by ID and for `displayUsers()` |

> Note: `publisher.observers` and `users` are **separate lists**.  
> `users` is for display/lookup. `publisher.observers` is the active subscriber list.  
> When you `removeUser`, both lists are updated.

---

## 6. How Everything Connects

### Observer Pattern Role Map

```
┌──────────────────────────────────────────────────────────────────┐
│                    OBSERVER PATTERN                               │
│                                                                   │
│  Subject Interface          Observer Interface                    │
│  ┌──────────────┐           ┌─────────────────┐                  │
│  │   Subject    │           │    Observer      │                  │
│  │─────────────│           │─────────────────│                  │
│  │ subscribe() │           │ update(Event)   │                  │
│  │ unsubscribe │           └────────┬────────┘                  │
│  │ notifyAll() │                    │ implemented by             │
│  └──────┬───────┘                   ▼                            │
│         │ implemented by    ┌───────────────────┐                │
│         ▼                   │       User         │                │
│  ┌──────────────────┐       │───────────────────│                │
│  │  EventPublisher  │       │ channels: List<>  │                │
│  │──────────────────│       │ eventHistory: []  │                │
│  │ observers: List  │──────►│ update(event) {   │                │
│  │ publish(event)   │calls  │   channel.send()  │                │
│  └──────────────────┘       │ }                 │                │
│                             └───────────────────┘                │
└──────────────────────────────────────────────────────────────────┘
```

### Dependency Map (Who Knows About Whom)

```
NotifyExample (Main)
        │ creates Users, calls system methods
        ▼
NotificationSystem (Singleton)
        │ owns
        ├──► EventPublisher (Subject)
        │         │ holds List<Observer>
        │         │ calls observer.update(event)
        │         ▼
        │       User (Observer)
        │         │ holds List<Notification> channels
        │         │ calls channel.sendNotification(this, event)
        │         ▼
        │       EmailNotify / SmsNotify / PushNotify
        │         │ implements Notification interface
        │         └── prints delivery + calls user.recordEvent(event)
        │
        └──► List<User> users  (for lookup / display only)
```

### Strategy Pattern Role Map (Channels)

```
Notification (interface)
        │
        ├── EmailNotify  ← strategy 1: deliver via email
        ├── SmsNotify    ← strategy 2: deliver via SMS
        └── PushNotify   ← strategy 3: deliver via push notification

Each User holds their own List<Notification> channels
→ user.update() loops channels and calls sendNotification(user, event)
→ Same event, different delivery strategy per user preference
```

---

## 7. Step-by-Step Code Walkthrough

### Step 1: Create Users with Preferred Channels
```java
User alice = new User(1, "Alice");
alice.addChannel(new EmailNotify());  // Alice's preference: Email + SMS
alice.addChannel(new SmsNotify());

User bob = new User(2, "Bob");
bob.addChannel(new PushNotify());     // Bob's preference: Push only
```
> At this point: Users exist, channels are registered on them.  
> Nobody is subscribed yet to the EventPublisher.

---

### Step 2: Register Users with the System (Subscribe as Observers)
```java
system.addUser(alice);
// Internally: users.add(alice) + publisher.subscribe(alice)
//             alice is now in EventPublisher.observers list
```
> Now: EventPublisher.observers = [Alice, Bob, Charlie]

---

### Step 3: Publish an Event
```java
system.publish(new Event(101, Priority.HIGH, "Server is down!"));
```

Trace what happens internally:
```
system.publish(event)
    └─► publisher.publish(event)
            └─► publisher.notifyObservers(event)
                    └─► loops observers list:
                            ├─► alice.update(event)
                            │       ├─► emailNotify.sendNotification(alice, event)
                            │       │       └─► prints: [EMAIL] → Alice | HIGH | Server is down!
                            │       │       └─► alice.recordEvent(event)
                            │       └─► smsNotify.sendNotification(alice, event)
                            │               └─► prints: [SMS] → Alice | HIGH | Server is down!
                            │               └─► alice.recordEvent(event)
                            │
                            ├─► bob.update(event)
                            │       └─► pushNotify.sendNotification(bob, event)
                            │               └─► prints: [PUSH] → Bob | HIGH | Server is down!
                            │
                            └─► charlie.update(event)
                                    ├─► [EMAIL] → Charlie
                                    ├─► [SMS]   → Charlie
                                    └─► [PUSH]  → Charlie
```

---

### Step 4: Priority-Filtered Publish
```java
system.publish(new Event(103, Priority.LOW, "Maintenance at midnight"), Priority.MEDIUM);
```
```
publisher.publish(event, MEDIUM)
    └─► event.priority = LOW, minPriority = MEDIUM
    └─► LOW.ordinal() = 0, MEDIUM.ordinal() = 1
    └─► 0 < 1  → condition FAILS
    └─► prints: "[PUBLISHER] Skipped (priority too low)"
    └─► notifyObservers() is NOT called — nobody gets notified
```

---

### Step 5: Direct / Targeted Notification
```java
system.notifyUser(2, new Event(104, Priority.HIGH, "Bob: your trial expires today"));
```
```
system.notifyUser(2, event)
    └─► findUser(2) → returns Bob
    └─► bob.update(event)               ← bypasses EventPublisher entirely
            └─► [PUSH] → Bob | HIGH | Bob: your trial expires today
```
> Only Bob gets this — it's a personal alert, not a broadcast.

---

### Step 6: Unsubscribe
```java
system.removeUser(1);  // Alice opts out
system.publish(new Event(106, Priority.HIGH, "Database failover!"));
```
```
removeUser(1)
    └─► publisher.unsubscribe(alice)
    └─► EventPublisher.observers = [Bob, Charlie]  ← Alice removed

publish(event)
    └─► notifyObservers loops [Bob, Charlie] only
    └─► Alice does NOT receive anything ✅
```

---

## 8. Expected Output

```
========== Setting Up Users ==========

========== Subscribing Observers ==========
[PUBLISHER] Subscribed  : User{ id=1, name='Alice' ... }
[SYSTEM] User added: User{ id=1, name='Alice' ... }
[PUBLISHER] Subscribed  : User{ id=2, name='Bob' ... }
[SYSTEM] User added: User{ id=2, name='Bob' ... }
[PUBLISHER] Subscribed  : User{ id=3, name='Charlie' ... }
[SYSTEM] User added: User{ id=3, name='Charlie' ... }

========== Publishing Events ==========

[PUBLISHER] Event published → Event{ id=101, priority=HIGH, message='Server is down!' }
  [OBSERVER] Alice notified about: Server is down!
[EMAIL]  → Alice | Priority: HIGH | Message: Server is down!
[SMS]    → Alice | Priority: HIGH | Message: Server is down!
  [OBSERVER] Bob notified about: Server is down!
[PUSH]   → Bob   | Priority: HIGH | Message: Server is down!
  [OBSERVER] Charlie notified about: Server is down!
[EMAIL]  → Charlie | Priority: HIGH | Message: Server is down!
[SMS]    → Charlie | Priority: HIGH | Message: Server is down!
[PUSH]   → Charlie | Priority: HIGH | Message: Server is down!

[PUBLISHER] Event published → Event{ id=102, priority=MEDIUM ... }
  ... (all 3 users notified)

[PUBLISHER] Skipped (priority too low) → Event{ id=103, priority=LOW ... }

========== Direct Notification ==========

[DIRECT] Targeting user id=2
  [OBSERVER] Bob notified about: Bob: your trial expires today
[PUSH]   → Bob | Priority: HIGH | Message: Bob: your trial expires today

[SYSTEM] User not found: id=99

========== Unsubscribing Alice ==========
[PUBLISHER] Unsubscribed: User{ id=1, name='Alice' ... }
[SYSTEM] User removed: User{ id=1, name='Alice' ... }

[PUBLISHER] Event published → Event{ id=106, priority=HIGH, message='Database failover!' }
  [OBSERVER] Bob notified about: Database failover!
[PUSH]   → Bob   | Priority: HIGH | Message: Database failover!
  [OBSERVER] Charlie notified about: Database failover!
[EMAIL]  → Charlie | Priority: HIGH | Message: Database failover!
[SMS]    → Charlie | Priority: HIGH | Message: Database failover!
[PUSH]   → Charlie | Priority: HIGH | Message: Database failover!

========== Registered Users ==========
  Active subscribers: 2
  User{ id=1, name='Alice', channels=2, notified=4 events }   ← Alice still in list (for history)
      ↳ Event{ id=101, ... }
      ...
  User{ id=2, name='Bob', channels=1, notified=4 events }
  User{ id=3, name='Charlie', channels=3, notified=8 events }
======================================
```

---

## 9. Design Decisions Explained

### Decision 1: Why does each User own their own channels?

**Old approach:** System holds channels globally → everyone gets Email+SMS+Push  
**New approach:** Each User has `List<Notification> channels` → Alice gets Email+SMS, Bob gets Push only

```
Real world equivalent:
  WhatsApp → Settings → Notifications → choose Email / SMS / Push
  Each user configures this independently
```

**Benefit:** If you add a new user who only wants Slack notifications, you just:
```java
User dave = new User(4, "Dave");
dave.addChannel(new SlackNotify());   // only Slack, nothing else
system.addUser(dave);
```
No changes anywhere else in the system.

---

### Decision 2: Why is Event immutable?

Event is passed to all channels. If mutable, any channel could call `event.setMessage(...)` and corrupt it for the next channel. Making all fields `final` with no setters prevents this entirely.

---

### Decision 3: Why copy the list in `notifyObservers()`?

```java
new ArrayList<>(observers).forEach(o -> o.update(event));
//  ↑ copy first
```

If `user.update(event)` triggers `system.removeUser()` (e.g., "unsubscribe me after one message"), the original `observers` list would be modified while being iterated → `ConcurrentModificationException`. The copy prevents this.

---

### Decision 4: Why does NotificationSystem keep its own `users` list separately from `publisher.observers`?

`publisher.observers` is for **broadcasting** — who gets called when an event fires.  
`NotificationSystem.users` is for **lookup and display** — `findUser(id)` and `displayUsers()`.

When `removeUser(1)` is called: Alice is removed from BOTH lists.  
But Alice's `eventHistory` is preserved because the `User` object itself is not deleted.

---

## 10. Class Diagram

```
«interface»                    «interface»
  Observer                       Subject
─────────────                ─────────────────
+ update(Event)              + subscribe(Observer)
      ▲                      + unsubscribe(Observer)
      │ implements            + notifyObservers(Event)
      │                              ▲
  ┌───────────────────┐              │ implements
  │      User         │       ┌──────────────────────┐
  │───────────────────│       │    EventPublisher     │
  │ - uId: int        │       │──────────────────────│
  │ - name: String    │       │ - observers: List<>   │
  │ - channels: List  │◄──────│ + publish(Event)      │
  │ - eventHistory:[] │calls  │ + publish(Event,Prior)│
  │───────────────────│update └──────────────────────┘
  │ + addChannel()    │                 ▲
  │ + update(Event)   │                 │ owns
  │ + recordEvent()   │       ┌──────────────────────┐
  └───────────────────┘       │  NotificationSystem  │
           │                  │  (Singleton)         │
           │ owns             │──────────────────────│
           ▼                  │ - publisher          │
  «interface»                 │ - users: List<User>  │
  Notification                │──────────────────────│
─────────────────             │ + addUser(User)      │
+ sendNotification            │ + removeUser(int)    │
  (User, Event)               │ + publish(Event)     │
      ▲                       │ + notifyUser(id,Evt) │
      │ implements            └──────────────────────┘
      ├── EmailNotify
      ├── SmsNotify          «enum»
      └── PushNotify         Priority
                          ────────────
  «class» Event           LOW
─────────────────         MEDIUM
- eId: int (final)        HIGH
- priority (final)
- message (final)
- timeStamp (final)
```

---

## Summary: The 3 Patterns Working Together

```
┌──────────────────────────────────────────────────────────────────┐
│                    NOTIFICATION SYSTEM                           │
│                                                                  │
│  SINGLETON                                                       │
│  NotificationSystem ─── one instance, entry point for all       │
│         │                                                        │
│         │ owns                                                   │
│         ▼                                                        │
│  OBSERVER PATTERN                                                │
│  EventPublisher (Subject) ──fires──► User.update(event)         │
│         ▲                              (Observer callback)       │
│  subscribe/unsubscribe                       │                   │
│  called by NotificationSystem                │ calls            │
│                                              ▼                   │
│  STRATEGY PATTERN                                                │
│  User loops own channels:                                        │
│    EmailNotify.sendNotification(user, event)                     │
│    SmsNotify.sendNotification(user, event)                       │
│    PushNotify.sendNotification(user, event)                      │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

*Implementation guide for `NotificationSystem` — part of the `system-design-lld` project.*

