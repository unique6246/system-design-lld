# 🔐 Thread Safety Guide — NotificationSystem

> Where concurrency risks were found, which tool was applied to fix each one, and WHY that specific tool was chosen.

---

## 📌 Table of Contents

1. [The 3 Concurrency Tools Used](#1-the-3-concurrency-tools-used)
2. [Risk Map — Every Unsafe Spot Found](#2-risk-map)
3. [Fix Applied to Each File](#3-fix-applied-to-each-file)
4. [Decision Guide — When to Use What](#4-decision-guide)
5. [The Concurrent Test Explained](#5-the-concurrent-test-explained)

---

## 1. The 3 Concurrency Tools Used

### Tool 1 — `volatile`
```java
private static volatile NotificationSystem INSTANCE = null;
```
**What it does:** Guarantees that changes to a variable are **visible to all threads immediately**.  
Without `volatile`, one thread might cache `INSTANCE = null` even after another thread set it — causing two Singletons to be created.  
**Use when:** A variable is read/written from multiple threads and **ordering of visibility matters**.

---

### Tool 2 — `synchronized`
```java
public synchronized void addUser(User user) { ... }
```
**What it does:** Only ONE thread can execute this method at a time. All others wait.  
**Use when:** You have **multiple steps that must happen atomically** — if a second thread can interrupt between step 1 and step 2 and cause incorrect state.

```
Thread A: findUser(1) → user found ✓
                ↕ Thread B sneaks in here
Thread B: findUser(1) → user found ✓, removes user
Thread A: users.remove(user) → user already removed → undefined
```
`synchronized` prevents Thread B from sneaking between Thread A's steps.

---

### Tool 3 — `CopyOnWriteArrayList` (COWAL)
```java
private final List<Observer> observers = new CopyOnWriteArrayList<>();
```
**What it does:**
- Every **write** (add/remove) creates a **fresh internal copy** of the array → other threads reading the old copy are unaffected
- Every **read/iteration** operates on a **stable snapshot** → no `ConcurrentModificationException` ever

**Use when:** You have a list that is:
- **Read/iterated frequently** (every event fires → iterate all observers)
- **Written to infrequently** (subscribe/unsubscribe happen rarely compared to broadcasts)

```
Thread A: notifyObservers() — iterating [Alice, Bob, Charlie]
Thread B: unsubscribe(Alice) — creates NEW internal array [Bob, Charlie]

Thread A continues iterating the OLD snapshot [Alice, Bob, Charlie] — safe ✅
Thread B's removal affects the NEXT iteration — correct ✅
```

---

## 2. Risk Map

Every unsafe spot identified before any fixes:

```
┌────────────────────────────────────────────────────────────────────────┐
│  CLASS                 FIELD/METHOD         RISK                       │
├────────────────────────────────────────────────────────────────────────┤
│                                                                        │
│  EventPublisher        observers: ArrayList  ❌ Not thread-safe        │
│                        subscribe()           ❌ contains()+add() race  │
│                        notifyObservers()     ❌ ConcurrentModEx risk   │
│                                                                        │
│  NotificationSystem    users: ArrayList      ❌ Not thread-safe        │
│                        addUser()             ❌ add()+subscribe() race │
│                        removeUser()          ❌ find+unsub+remove race │
│                        Singleton INSTANCE    ⚠️  needs volatile        │
│                                                                        │
│  User                  channels: ArrayList   ❌ addChannel vs update() │
│                        eventHistory:[]       ❌ concurrent recordEvent │
│                        getEventHistory()     ❌ read during write      │
│                                                                        │
│  Event                 all fields final      ✅ Already immutable      │
│  Priority              enum                  ✅ Inherently safe        │
│  EmailNotify/SMS/Push  stateless             ✅ No shared state        │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Fix Applied to Each File

---

### `EventPublisher.java` — 3 risks fixed

#### Risk 1: `ArrayList observers` — not thread-safe
```java
// ❌ Before
private final List<Observer> observers = new ArrayList<>();

// ✅ After
private final List<Observer> observers = new CopyOnWriteArrayList<>();
```
**Why COWAL?** `notifyObservers()` iterates this list on EVERY event. If a thread subscribes/unsubscribes during iteration on an `ArrayList` → `ConcurrentModificationException`. COWAL's snapshot iteration makes this impossible.

---

#### Risk 2: `subscribe()` — `contains()` + `add()` not atomic
```java
// ❌ Before (not atomic — race window between contains() and add())
public void subscribe(Observer observer) {
    if (!observers.contains(observer)) {  // ← Thread A checks: not present ✓
                                          // ← Thread B also checks: not present ✓
        observers.add(observer);          // ← BOTH threads add → duplicate!
    }
}

// ✅ After (synchronized makes check+add a single atomic unit)
public synchronized void subscribe(Observer observer) {
    if (!observers.contains(observer)) {
        observers.add(observer);          // ← only one thread can be here at a time
    }
}
```

---

#### Risk 3: `notifyObservers()` — manual copy no longer needed
```java
// ❌ Before (manual copy was a workaround for ArrayList's unsafety)
new ArrayList<>(observers).forEach(o -> o.update(event));

// ✅ After (COWAL handles the snapshot internally — no manual copy needed)
observers.forEach(o -> o.update(event));
```

---

### `NotificationSystem.java` — 3 risks fixed

#### Risk 1: `ArrayList users` — not thread-safe
```java
// ❌ Before
private final List<User> users = new ArrayList<>();

// ✅ After
private final List<User> users = new CopyOnWriteArrayList<>();
```
**Why COWAL?** `findUser()`, `displayUsers()`, and `notifyUser()` all read/iterate `users`. COWAL ensures reads are safe without locking.

---

#### Risk 2: `addUser()` — `add()` + `subscribe()` not atomic
```java
// ❌ Before — two threads could add same user simultaneously
public void addUser(User user) {
    users.add(user);            // Thread A adds
                                // Thread B adds same user
    publisher.subscribe(user);  // both subscribe → duplicate
}

// ✅ After — synchronized makes the whole operation atomic
public synchronized void addUser(User user) {
    if (findUser(user.getuId()).isPresent()) return; // duplicate guard
    users.add(user);
    publisher.subscribe(user);
}
```

---

#### Risk 3: `removeUser()` — find + unsubscribe + remove not atomic
```java
// ❌ Before — two threads removing same user simultaneously:
// Thread A: finds user → Thread B: finds same user
// Thread A: unsubscribes → Thread B: unsubscribes again
// Thread A: removes → Thread B: tries to remove again → no-op but still a bug
public void removeUser(int userId) { ... }

// ✅ After — synchronized makes the 3 steps atomic
public synchronized void removeUser(int userId) {
    findUser(userId).ifPresentOrElse(user -> {
        publisher.unsubscribe(user);
        users.remove(user);
    }, ...);
}
```

---

### `User.java` — 3 risks fixed

#### Risk 1: `channels: ArrayList` — unsafe during concurrent `update()`
```java
// ❌ Before
private final List<Notification> channels = new ArrayList<>();

// ✅ After
private final List<Notification> channels = new CopyOnWriteArrayList<>();
```
**Scenario fixed:** Two events fire concurrently. Both call `user.update()` which iterates `channels`. At the same time, `addChannel()` is called from another thread. With `ArrayList` → `ConcurrentModificationException`. With COWAL → safe snapshot iteration.

---

#### Risk 2: `eventHistory: ArrayList` — concurrent `recordEvent()` calls
```java
// ❌ Before
private final List<Event> eventHistory = new ArrayList<>();

public void recordEvent(Event event) {
    eventHistory.add(event); // ← two threads call this simultaneously → corruption
}

// ✅ After
private final List<Event> eventHistory = new CopyOnWriteArrayList<>();

public void recordEvent(Event event) {
    eventHistory.add(event); // ← COWAL.add() is atomic → safe
}
```
**Scenario fixed:** Event A and Event B are published simultaneously. Both go through `notifyObservers()` → both call `alice.update()` concurrently → both call `alice.recordEvent()` at the same time. `ArrayList.add()` is NOT thread-safe — can corrupt internal array. COWAL.add() is always atomic.

---

#### Risk 3: `getEventHistory()` — reading while writing
```java
// ❌ Before — returns live unmodifiable view — reads during concurrent writes → inconsistent
public List<Event> getEventHistory() {
    return Collections.unmodifiableList(eventHistory);
}

// ✅ After — returns snapshot copy at this moment — consistent regardless of concurrent adds
public List<Event> getEventHistory() {
    return new ArrayList<>(eventHistory); // snapshot — safe
}
```

---

## 4. Decision Guide — When to Use What

```
You have a shared variable/field:
            │
            ▼
Is it a single primitive or reference?
(boolean, int, object reference — NOT a collection)
        YES ──────────────────────────────► volatile
        │                                   (makes updates visible across threads)
        NO
        ▼
Is it a collection (List, Map, Set)?
        YES ──────────────────────────────► CopyOnWriteArrayList / ConcurrentHashMap
        │   (choose based on read:write ratio)         │
        NO                                              ▼
        ▼                             Reads >> Writes? → CopyOnWriteArrayList
Is it a multi-step operation            (subscribe/notify pattern)
that must be atomic?                 Writes ≈ Reads?  → ConcurrentHashMap
(find + modify + save)               or high-write?   → use synchronized with ArrayList
        YES ──────────────────────────────► synchronized method or block
        │
        NO
        ▼
Is it a single-step read on an
already-safe collection?
        YES ──────────────────────────────► No extra sync needed
                                            (COWAL/ConcurrentHashMap handle it)
```

---

### Quick Reference Table

| Situation | Tool | Example in this project |
|---|---|---|
| Singleton instance variable | `volatile` | `NotificationSystem.INSTANCE` |
| Observer list (many reads, few writes) | `CopyOnWriteArrayList` | `EventPublisher.observers` |
| User list (read + rare mutation) | `CopyOnWriteArrayList` | `NotificationSystem.users` |
| Multi-step mutation (find+modify+remove) | `synchronized` method | `addUser()`, `removeUser()` |
| Duplicate-check + add atomically | `synchronized` method | `subscribe()`, `addUser()` |
| Event history (concurrent appends) | `CopyOnWriteArrayList` | `User.eventHistory` |
| Channel list (iteration + rare add) | `CopyOnWriteArrayList` | `User.channels` |
| Stateless objects | Nothing needed | `EmailNotify`, `SmsNotify`, `PushNotify` |
| Immutable objects | Nothing needed | `Event` (all fields `final`) |
| Enum values | Nothing needed | `Priority` |

---

### COWAL vs `synchronized` — When to Pick Each

```
CopyOnWriteArrayList                    synchronized
────────────────────────                ────────────────────────
✅ Many concurrent readers               ✅ Multi-step operations must be atomic
✅ Infrequent writes                     ✅ Check-then-act patterns
✅ Iteration is the primary use          ✅ High write frequency
❌ Expensive for frequent writes         ❌ Blocks all threads — lower throughput
   (creates new array on every write)
```

In this system:
- `publish()` → `notifyObservers()` iterates observers **constantly** — readers dominate → COWAL ✅
- `addUser()` does find+add+subscribe in **3 steps that must be atomic** → `synchronized` ✅

---

## 5. The Concurrent Test Explained

```java
CountDownLatch startGate = new CountDownLatch(1);  // single gate
CountDownLatch endGate   = new CountDownLatch(7);  // 5 publishers + 2 sub/unsub
```

### What is `CountDownLatch`?
A countdown timer for threads. `await()` blocks until count reaches 0. `countDown()` decrements the count.

```
startGate = CountDownLatch(1)
  All 7 threads call startGate.await()  ← ALL BLOCKED, waiting
  Main thread calls startGate.countDown() ← count = 0, ALL RELEASED SIMULTANEOUSLY

endGate = CountDownLatch(7)
  Each thread calls endGate.countDown() when done
  Main thread calls endGate.await() ← BLOCKED until all 7 finish
```

### What the test proves

| Thread | Action | Safety tested |
|---|---|---|
| Publisher-1..5 | Fire 5 events simultaneously | `CopyOnWriteArrayList` iteration in `notifyObservers()` |
| Subscribe-thread | `addUser(Grace)` during broadcast | `synchronized addUser()` + COWAL `users` list |
| Unsubscribe-thread | `removeUser(Eve)` during broadcast | `synchronized removeUser()` + COWAL `observers` list |

If any thread-safety fix is missing → `ConcurrentModificationException` or data corruption during the test.  
If all pass with no exceptions → **all fixes are working correctly** ✅

---

## Summary: All Changes at a Glance

```
File                    Change                              Why
──────────────────────────────────────────────────────────────────────────
EventPublisher          ArrayList → CopyOnWriteArrayList    Safe concurrent iteration
EventPublisher          subscribe() → synchronized          Atomic check+add
EventPublisher          Remove manual copy in notify()      COWAL handles it

NotificationSystem      ArrayList → CopyOnWriteArrayList    Safe concurrent reads
NotificationSystem      addUser() → synchronized            Atomic find+add+subscribe
NotificationSystem      removeUser() → synchronized         Atomic find+unsub+remove
NotificationSystem      Duplicate guard in addUser()        Prevent double registration

User                    channels → CopyOnWriteArrayList     Safe during concurrent update()
User                    eventHistory → CopyOnWriteArrayList  Safe concurrent recordEvent()
User                    getEventHistory() → snapshot copy   Consistent read during writes
User                    Thread name logged in update()      Visualize concurrent delivery

NotifyExample           Added Part 2: concurrent stress test  Prove all fixes work live
```

