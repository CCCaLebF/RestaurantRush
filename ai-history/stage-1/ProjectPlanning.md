---
title: "SC2002 Restaurant Rush: Conversation with Claude"
---

# 1. How to use you?

**You:** how to use you?

**Claude:**

I'm Claude Code, an AI assistant that can read, write and run code, and work with files. You give me tasks in plain language, and I do them.

## What you can ask me to do

**Coding**

- "Build a to-do app in React"
- "Fix the bug in `app.py` where login fails"
- "Write tests for this function"
- "Explain how this codebase works"
- "Review my changes for bugs" (or type `/code-review`)

**Files and documents**

- Create or edit spreadsheets, Word documents, PDFs and slide decks
- "Turn this CSV into a chart"
- "Summarize this PDF"

**Pages and visuals**

- Interactive web pages, dashboards, diagrams or small games that you can open and share via a link

**Research and automation**

- "Research X and write me a report"
- Run a task on a schedule, like "check this every morning"

## How this session works

- **You're in a cloud session.** I'm running in a temporary container, not on your computer. Your working folder (`/home/user`) is currently empty and isn't a git repository.
- **To work on your own code,** tell me the GitHub repo, for example "work on `myname/myproject`". I can clone it, make changes, and push them to a branch. Save your work by committing and pushing, because the container is eventually deleted.
- **Slash commands:** typing `/` shows shortcuts such as `/code-review`, `/init` and `/loop`.

## Tips

- **Be specific:** "Add a dark mode toggle to the settings page" works better than "improve the UI."
- **Go step by step:** you can review what I did and ask for changes.
- **Ask questions:** "What does this function do?" is a fine request too.

# 2. The project specifics and code expectations

**You:** *(uploaded `SC2002_Restaurant_Rush_Group_Assignment1_1.docx`)* can u explain to me the specifics of this project and what are the expectations for the code

**Claude:**

## The project in one sentence

Your group builds a **turn-based restaurant manager game in Java** that runs in the command line. The player is the manager and decides what the Waiter and Chef do each turn. You build it in **three stages**, and each stage gets its own git tag. Everything is due **15 Nov 2026, 11:59pm**, as one zip named like `FCS1_Grp1.zip`.

## How the game works (base setting, required)

| Thing | Value |
|---|---|
| Turns | Exactly 18. There is no early ending. |
| Tables | 2 |
| Staff | 1 Waiter (1 task per turn), 1 Chef (1 prep unit per turn), 1 Cashier (1 payment per turn, automatic) |
| Arrivals | One customer at the start of turns 1, 4, 7, 10, 13 and 16 |
| To win | After turn 18: **≥4 paid customers**, **revenue ≥ $45** and **average satisfaction of paid customers ≥ 60** |

**Menu:** Salad $9 (1 unit), Burger $15 (1 unit), Pasta $18 (2 units).

### Each turn, in this exact order

1. **Arrival:** the scheduled customer joins the queue with satisfaction 100.
2. **Manager choices:**
    - Waiter does one of: seat a customer, take an order, serve a READY dish, or wait.
    - Chef either advances one order by 1 unit or waits.
3. **Kitchen:** the Waiter acts *before* the Chef. A dish the Chef finishes this turn can only be served **next** turn. The Chef *can* cook an order the Waiter took this same turn.
4. **Eating and payment:** a customer served on an earlier turn finishes eating and becomes READY_TO_PAY. The Cashier charges the oldest READY_TO_PAY customer, but only someone who became READY_TO_PAY on an earlier turn.
5. **Satisfaction decay:** anyone waiting or seated but not yet served loses 8 points. At 0 they leave without paying, their order is cancelled and their table is freed.
6. **Result:** after turn 18, the game shows Victory or Defeat.

The example in section 3.3 of the brief (Pasta customer: seated turn 1, paid turn 6 with satisfaction 76) is a good first test case for your code.

### Lifecycle states you'll need

- **Customer:** WAITING → SEATED → ORDERED → SERVED → (eating) → READY_TO_PAY → PAID, or LEFT if they abandon.
- **Order:** IN_PROGRESS → READY → SERVED → PAID, or CANCELLED.

### Display requirements

- **Start of every turn:** turn number, revenue, average satisfaction (N/A if nobody has paid), the queue, the tables, each customer's satisfaction, order statuses and the available staff tasks.
- **End of the game:** served count, unhappy departures, revenue, average satisfaction and turns used.

## The three stages

**Stage 1 (tag `stage-1`): the core game.** Regular customers, the menu, Waiter, Chef, Cashier, Table, Order, the queue, and the full turn loop with victory and defeat. It must contain **no Stage 2 or 3 code**. Your test notes need one reproducible victory run and one reproducible defeat run.

**Stage 2 (tag `stage-2`): customer types.**

| Type | Loss per turn | Bill | Special |
|---|---|---|---|
| Regular | 8 | Full price | — |
| VIP | 5 | 10% off | — |
| Critic | 12 | Full price | A behaviour **you invent and document** (e.g. always orders Pasta, or a satisfaction penalty if served late) |

- The base run must include at least one VIP and one Critic, and still have 6 arrivals.
- ⚠️ **This is the key OOP test:** the differences must come from **polymorphism**, such as `customer.getSatisfactionLoss()` and `customer.applyDiscount(price)`. Writing `if (c instanceof VIPCustomer) ... else if ...` in the controller will lose marks.

**Stage 3 (tag `stage-3`): Host, ComboMeal and Happy Hour.**

- **Host:** seats at most one extra queued customer per turn, on top of the Waiter's task.
- **ComboMeal:** made of two menu items. Prep units are the sum of both. Price is 90% of the combined price, rounded to the nearest cent (Salad + Pasta = $24.30, 3 units). This is a natural fit for **composition** or the Composite pattern, so a `ComboMeal` can be treated like any other `MenuItem`.
- **Happy Hour:** the manager can switch it on once per game, at the start of a turn. It lasts for that turn and the next, and the game shows how much time is left. Orders taken during it get 20% off, with the price **locked at order time**. Customers still waiting recover 15 satisfaction at turn start, up to a maximum of 100. Discounts stack VIP first, then Happy Hour: VIP Burger = 15 × 0.9 × 0.8 = **$10.80**.

## Expectations for the code itself

1. **Store money as integer cents** (e.g. `int priceCents = 1800`) and display it as `$18.00`. This avoids floating-point errors like $10.799999.
2. **Keep the UI separate from the rules.** The CLI class only reads input and prints state. The rules live in domain and controller classes. A sensible split:
    - **Boundary:** `GameCLI`
    - **Control:** `GameController` / `TurnManager`
    - **Entity:** `Customer` (with VIP and Critic subclasses), `Order`, `MenuItem`, `ComboMeal`, `Table`, the staff classes, `Restaurant`
    - **Policy:** `PricingPolicy` / `HappyHour`
3. **Handle invalid input robustly.** For a bad choice (like seating someone when no table is free), explain why it failed and ask again. The failed attempt must not use up that staff member's task.
4. **Make customer food choices deterministic** (a fixed rule or seeded `Random`) so a TA can reproduce your runs. The *customer* picks the food, never the manager.
5. **Work offline**, with clear build and run instructions in the README.
6. **Have real stage history in git.** Each tag must point to a commit that actually compiles and runs at that stage. The README must list, for transitions 1→2 and 2→3, which files were added or changed, why, and the compare links.
7. **Don't over-engineer.** The brief says not to add interfaces just to tick a box. Patterns should solve a real problem. Ideally Stage 2 and 3 should mostly *add* classes rather than rewrite Stage 1 code (the Open/Closed Principle). Any changes to earlier code must be explained.

## How it's marked

| Part | Marks |
|---|---|
| UML class diagram (must match the Stage 3 code) | 25 |
| UML sequence diagram (one full customer flow plus a Stage 2 or 3 feature) | 20 |
| Design discussion (responsibilities, SOLID where real, trade-offs, one alternative you considered) | 15 |
| **Code** | **20** |
| Process (tags, change analysis, reflection, ~15-minute video where every member speaks) | 20 |

The code is only 20% of the grade. **Diagrams, design reasoning and git history are worth 80%**, so plan your class design before you start coding.

Your group must also submit a **Declaration on Use of GAI Assistance**, so record any help you get from me. I can help you design the class structure, draft the UML, explain concepts, or review and debug your code.

# 3. How to start Stage 1

**You:** how do i start stage one? outline it for me

**Claude:**

Below is how I'd build Stage 1, in order. Each step leaves you with something you can compile and run. Stage 1 must contain **no** VIP, Critic, Host, Combo or Happy Hour code, but you can still structure it so those slot in later without rewriting it.

## Step 0: Set up the repo (about 30 minutes)

1. One person creates a GitHub repo and adds everyone as a collaborator.
2. Use plain `javac` or a simple Maven/Gradle project. Whichever you choose, the README must say exactly how to build and run it.
3. Suggested layout:

```
src/restaurant/
├── Main.java                 // entry point: wires objects together, starts the game
├── boundary/
│   └── GameCLI.java          // ONLY input and printing
├── control/
│   ├── GameController.java   // runs the 18 turns, the 6 steps, the win check
│   └── ArrivalSchedule.java  // turns 1,4,7,10,13,16 → new customer
├── entity/
│   ├── Customer.java
│   ├── CustomerStatus.java   // enum
│   ├── Order.java
│   ├── OrderStatus.java      // enum
│   ├── MenuItem.java
│   ├── Menu.java
│   ├── Table.java
│   ├── Restaurant.java       // holds the tables, queue, customers, revenue
│   └── staff/  Waiter.java, Chef.java, Cashier.java
└── util/
    └── Money.java            // cents ↔ "$18.00" formatting
```

## Step 1: Entities (the data and their own rules)

| Class | Key fields | Key methods | Notes |
|---|---|---|---|
| `MenuItem` | `name`, `priceCents`, `prepUnits` | getters | Make it an extensible base: in Stage 3, `ComboMeal` can extend it or implement a shared interface. |
| `Menu` | list of items | `getItems()`, `findByName()` | Salad 900/1, Burger 1500/1, Pasta 1800/2 |
| `Customer` | `id`, `satisfaction`, `status`, `order`, `table`, `servedTurn`, `readyToPayTurn`, `recordedSatisfaction` | `chooseItem(Menu)`, `getSatisfactionLoss()` → 8, `computeBill(int base)` → base, `loseSatisfaction()` | **Important:** put loss and billing *on the customer* now. In Stage 2, VIP and Critic just override these methods. That is your polymorphism story. |
| `CustomerStatus` | `WAITING, SEATED, ORDERED, SERVED, READY_TO_PAY, PAID, LEFT` | | |
| `Order` | `item`, `unitsDone`, `status`, `billCents`, `readyTurn` | `advance(turn)`, `isReady()` | `billCents` is set when the order is taken (price is locked, which Stage 3 needs). |
| `OrderStatus` | `IN_PROGRESS, READY, SERVED, PAID, CANCELLED` | | |
| `Table` | `id`, `occupant` | `isFree()`, `seat(c)`, `free()` | |
| `Restaurant` | `tables`, `waitingQueue`, `allCustomers`, `revenueCents` | `freeTable()`, `addRevenue()`, `averagePaidSatisfaction()` | |

**Customer food choice:** pick one documented deterministic rule. For example, "customer *n* orders `menu[(n-1) % 3]`" gives Salad, Burger, Pasta, Salad, ... A fixed seeded `Random` also works. Check that your rule still allows a victory: you need 4 paid customers, ≥ $45 and an average satisfaction ≥ 60.

## Step 2: Staff (the actions and their validity checks)

Each action **returns a result** (success, or failure with a reason) and **never prints**. The CLI does the printing.

- **`Waiter`**
    - `seat(customer, table)`: the customer must be WAITING and the table must be free.
    - `takeOrder(customer, turn)`: the customer must be SEATED. Creates the `Order`.
    - `serve(order, turn)`: the order must be READY with `readyTurn < turn`. Sets it to SERVED and records `servedTurn`.
- **`Chef`**
    - `advance(order, turn)`: the order must be IN_PROGRESS. It becomes READY when `unitsDone == prepUnits`, recording `readyTurn = turn`.
- **`Cashier`**
    - `processPayment(restaurant, turn)`: automatic. Takes the oldest READY_TO_PAY customer with `readyToPayTurn < turn`, adds the bill to revenue, records satisfaction, marks PAID and frees the table.

A small `ActionResult` class (`boolean ok`, `String reason`) makes the rule "invalid choice → explain → choose again without losing the task" easy to implement.

## Step 3: GameController (the turn loop)

This is the core of the game, and it should contain no `System.out` or `Scanner` calls.

```java
for (turn = 1; turn <= 18; turn++) {
    // 1 Arrivals
    schedule.arrivalFor(turn).ifPresent(restaurant::enqueue);   // satisfaction 100

    cli.showTurnState(turn, restaurant);                        // start-of-turn display

    // 2–3 Manager choices: the Waiter acts BEFORE the Chef
    doUntilValid(() -> cli.askWaiterAction(...));               // retry on invalid choice
    doUntilValid(() -> cli.askChefAction(...));

    // 4 Eating, then payment
    for (Customer c : served customers with servedTurn < turn) → READY_TO_PAY, readyToPayTurn = turn
    cashier.processPayment(restaurant, turn);                   // only readyToPayTurn < turn

    // 5 Satisfaction decay and abandonment
    for (Customer c : WAITING, SEATED or ORDERED customers) {
        c.loseSatisfaction();                                   // uses c.getSatisfactionLoss()
        if (c.getSatisfaction() == 0) leave(c);                 // cancel order, free table, LEFT
    }
}
// 6 Result
boolean win = paid >= 4 && revenue >= 4500 && avg >= 60;
cli.showResult(...);
```

**Timing rules people commonly get wrong:**

- The **turn stamps** (`readyTurn`, `servedTurn`, `readyToPayTurn`) are the simplest way to enforce the "not until next turn" rules.
- Customers who are SERVED, eating or READY_TO_PAY do **not** lose satisfaction.
- The Chef may cook an order the Waiter took earlier in the same turn.
- The game always runs all 18 turns, even if everyone has left.

## Step 4: GameCLI (input and output)

- `showTurnState(...)`: turn number, revenue, average satisfaction (or N/A), queue, tables, each customer's satisfaction and order status, and the available tasks.
- `askWaiterAction(...)` / `askChefAction(...)`: show a numbered menu like `1) Seat C2 at T1  2) Take order C1  3) Wait`.
- Handle non-numbers and out-of-range numbers without crashing.
- `showResult(...)`: served count, unhappy departures, revenue, average satisfaction, turns used, and VICTORY or DEFEAT.

## Step 5: Test with the brief's own example

Play a single Pasta customer and check against the table in section 3.3 of the brief:

| Turn | Expected satisfaction at turn end |
|---|---|
| 1 | 92 |
| 2 | 84 |
| 3 | 76 |
| 4 | 76 |
| 5 | 76 |
| 6 | Pays $18.00, 76 recorded |

Then **write down two input sequences**, one that wins and one that loses, and save them in a file such as `test-notes/stage1-runs.md`. Being able to replay them (`java Main < victory.txt`) makes your runs reproducible for the TA and handy for the video.

## Step 6: Tag it

When Stage 1 works and the code has nothing from Stage 2 or 3:

```bash
git tag stage-1
git push origin stage-1
```

## Suggested split for a team of 4–5 (deadline Nov 15)

| Who | Task | Target |
|---|---|---|
| A | Entities and enums, plus `Money` | Day 1–2 |
| B | Staff classes and `ActionResult` | Day 2–3 |
| C | `GameController` turn loop | Day 2–4 |
| D | `GameCLI` and input handling | Day 2–4 |
| Everyone | Integration, the 3.3 test, victory and defeat runs, tag `stage-1` | ~Day 5 |

Agree on the class names and method signatures (a rough class diagram) **before** splitting up. That sketch also becomes the starting point for your 25-mark UML diagram.

# 4. A customer reaching 0 while their order is READY

**You:** What happens when a customer reaches 0 satisfaction while their order is READY but not yet served? The brief says to cancel unfinished orders. Is a READY order counted as unfinished?

**Claude:**

Yes, treat a READY order as unfinished and cancel it. The brief doesn't say this word for word, but it's the only reading that holds together.

## Why

**1. The customer still loses satisfaction.** The brief says that every "waiting or seated customer who has not been served loses satisfaction". A customer whose dish is READY but not yet served falls into that group, so they can reach 0. Only "served, eating or ready to pay" customers are protected.

**2. In this game, "finished" means delivered to the customer, not done cooking.** The order lifecycle runs IN_PROGRESS → READY → SERVED → PAID. An order isn't finished until it reaches the customer. A READY dish sitting at the pass is still an open order.

**3. Leaving it READY breaks things.** The customer is gone and their table is freed, so the order has nobody to serve it to. If it stayed READY, the Waiter's "serve a READY order" list could still offer it, and you'd need extra code to block that.

**4. Revenue is unaffected either way.** "Revenue never includes unpaid or abandoned orders," and revenue is only added at payment. Cancelling just removes the order from play. The Chef's work on it is wasted, and that waste is part of the game's challenge.

## When this can happen

The Waiter acts in step 2 and satisfaction drops in step 5, so the case comes up in two situations:

- The dish became READY **this turn**, so the Waiter wasn't allowed to serve it yet, and the drop to 0 happens at the end of the same turn.
- The dish was READY earlier, but the manager used the Waiter for something else.

Your video needs "a decision where staff are constrained". A dish wasted this way, alongside a run where the manager prioritised serving and saved the customer, would show that well.

## In code

```java
// Order
public void cancel() {
    if (status == OrderStatus.IN_PROGRESS || status == OrderStatus.READY) {
        status = OrderStatus.CANCELLED;
    }
}

// GameController, step 5
private void leave(Customer c) {
    if (c.getOrder() != null) c.getOrder().cancel();
    if (c.getTable() != null) c.getTable().free();
    restaurant.removeFromQueue(c);   // in case they were still waiting
    c.setStatus(CustomerStatus.LEFT);
    unhappyDepartures++;
}
```

The guard is effectively always true, because SERVED and PAID customers never lose satisfaction and so can never leave. It still keeps `cancel()` safe if it's called from somewhere else later.

## Write it down

Because the brief leaves this open, record it as a stated assumption in your README and report. For example:

> *Assumption: an order is "unfinished" until it is SERVED. If a customer leaves while their order is IN_PROGRESS or READY, the order becomes CANCELLED, earns no revenue and is never served to another customer.*

Markers usually reward a clearly documented interpretation. If you want to be certain, ask your TA or post on the course forum, but this reading is the safe default.

# 5. SOLID principles to be careful of

**You:** what are some solid principles i should be careful of when implementing? i think actually all of them? i think OCP and ISP are pretty important

**Claude:**

SOLID principles matter in this project because the stage changes are **built to test them**. Stage 2 tests whether you can add customer types without editing the controller (OCP plus polymorphism). Stage 3 tests whether you can add a new role and a new menu type cleanly (OCP and ISP). The brief also has a warning to keep in mind:

> *"SRP, OCP, LSP, ISP and DIP, **only where genuinely reflected** in your code."*
> *"Do not create interfaces just to satisfy a checklist."*

Markers reward principles that solve a real problem in your code, not ones bolted on to tick a box.

## OCP: open for extension, closed for modification

**Where it matters:** your git diffs for stage 1→2 and 2→3 are direct evidence. Ideally they show mostly **new files**, plus a few small "registration" edits, which the brief explicitly allows.

**✅ Do: put behaviour in methods subclasses can override**

```java
public class Customer {
    public int getSatisfactionLoss()        { return 8; }
    public int applyCustomerDiscount(int c) { return c; }       // cents in, cents out
    public MenuItem chooseItem(Menu menu)   { ... }             // default rule
    public void onServed(int turn)          { }                 // hook, does nothing by default
}

public class VIPCustomer extends Customer {
    @Override public int getSatisfactionLoss()        { return 5; }
    @Override public int applyCustomerDiscount(int c) { return Math.round(c * 0.9f); }
}
```

The controller then just calls `c.loseSatisfaction()`. It never needs to know which type `c` is.

**❌ Don't: check types in the controller**

```java
if (c instanceof VIPCustomer) loss = 5;
else if (c instanceof CriticCustomer) loss = 12;     // the brief names this as wrong

switch (c.getType()) { case VIP: ... }               // same problem, hidden behind an enum
```

**Plan these three extension points now, while building Stage 1**

| Stage 2/3 feature | What Stage 1 should already have |
|---|---|
| VIP and Critic customers | Loss, billing, food choice and reaction hooks as overridable methods on `Customer` |
| ComboMeal | The controller and `Order` only use `getPriceCents()`, `getPrepUnits()` and `getName()`, so a combo can be dropped in |
| Happy Hour | One place where the bill is calculated at order time, so a pricing step can be inserted there |

For the pricing step, a sketch:

```java
// in Waiter.takeOrder or an OrderFactory: the price is locked here, once
int price = item.getPriceCents();
price = customer.applyCustomerDiscount(price);   // VIP first (Stage 2)
price = pricingPolicy.apply(price, turn);        // then Happy Hour (Stage 3); Stage 1 uses a "no discount" policy
order.setBillCents(price);
```

Check the brief's example: VIP Burger during Happy Hour = 1500 → 1350 → **1080** ✔

**Be honest about it:** Stage 3 will still need some edits to existing code, such as a Happy Hour toggle in the CLI and a Host step in the turn loop. That's fine. Explain each change in the README, as the brief asks. "We had to modify X because..." is good evidence of design thinking.

## ISP: no class should depend on methods it doesn't use

**The trap:** one large `Staff` interface.

```java
interface Staff {                     // ❌
    void seat(...); void takeOrder(...); void serve(...);
    void cook(...); void collectPayment(...);
}
```

The Chef would then need `seat()` implemented as `throw new UnsupportedOperationException()`, which breaks both ISP and LSP.

**Places where ISP fits this project naturally**

**1. Seating in Stage 3.** The Waiter and the new Host both seat customers, but only the Waiter takes orders and serves. That gives you a real reason for a small interface:

```java
public interface CanSeat { ActionResult seat(Customer c, Table t); }

public class Waiter implements CanSeat { /* + takeOrder, serve */ }
public class Host   implements CanSeat { /* seating only */ }
```

The controller's seating logic can work with any `CanSeat`. This is the strongest ISP example you'll have, and it appears exactly when the brief introduces the Host.

**2. Menu items in Stage 3.** `Order` only needs a price, prep units and a name:

```java
public interface Orderable { String getName(); int getPriceCents(); int getPrepUnits(); }
// implemented by MenuItem and ComboMeal
```

**3. Input for the controller.** See the DIP section below.

**Don't:** create `IChef`, `ICashier` and `ITable` interfaces that each have exactly one implementation and no reason to exist. The brief warns against exactly that.

## LSP: subclasses must be usable anywhere the parent is

Every customer type "follows the same order and service flow". A `CriticCustomer` must work anywhere a `Customer` does.

**Rules for your subclasses:**

- Satisfaction always stays between 0 and 100, for every type. Clamp it in the **base** class, and don't let subclasses set the field directly. Make it `private` and change it only through methods.
- Bills are never negative, and the price is still locked when the order is taken.
- A subclass may **not** skip states. A Critic that refuses to pay, or skips eating, breaks LSP.
- `ComboMeal` must behave like a menu item. Its prep units and price follow the same rules the Chef and Cashier already rely on.

**Watch the Critic's special behaviour.** You design it yourself, so keep it inside the existing hooks. "Always orders the most expensive item", or "loses an extra 10 if served more than 2 turns after ordering", are both fine. "Leaves the restaurant without paying" is not, because it changes the flow everyone else follows.

## SRP: one reason to change per class

| Class | Its single job | Common mistake |
|---|---|---|
| `GameCLI` | Read input and print state | Checking whether a table is free (that's a rule, so it belongs in the domain) |
| `GameController` | Run the turn steps in order | Becoming a 600-line class that does everything |
| `Customer` | Track its own satisfaction, status and choices | Printing itself to the console |
| `Cashier` | Process payments | Calculating discounts (that happens at order time) |
| `Money` | Convert cents to `$x.xx` | `String.format("%.2f", cents/100.0)` copied across 10 files |

**Tip:** if `GameController` gets large, move each step into its own method (`processArrivals()`, `processEatingAndPayment()`, `processDecay()`) or into small helper classes. That's SRP at the method level, and it's easier to draw in your sequence diagram.

## DIP: depend on abstractions, not concrete classes

This one helps you in practice, beyond the report. Make the controller depend on an input abstraction instead of `Scanner`:

```java
public interface ManagerInput {
    int chooseWaiterAction(List<String> options);
    int chooseChefAction(List<String> options);
}
// ConsoleInput implements ManagerInput      → real play
// ScriptedInput implements ManagerInput     → replays a saved list of choices
```

That gives you **reproducible victory and defeat runs** (the brief requires them for every stage) and automated tests, with a real justification for the interface. `Main` decides which implementation to create and passes it in.

Customer food choice can follow the same approach. A `FoodChoiceRule` that you pass in, or a seeded `Random`, means a TA gets the same game every run.

## Red-flag checklist before each tag

- ☐ No `instanceof` or type `switch` on customers in the controller
- ☐ No `System.out` or `Scanner` outside the CLI or boundary package
- ☐ No staff method that throws `UnsupportedOperationException`
- ☐ Every interface has a reason to exist that you can explain in one sentence
- ☐ Satisfaction and money fields are `private` and only changed through methods
- ☐ Money is stored as `int` cents everywhere, and formatted in one place
- ☐ The git diff for this stage is mostly new files, and each edit to an old file has a reason written in the README

# 6. Is Stage 1 the core and Stages 2 and 3 extensions?

**You:** is stage 1 the core implementation and stage 2 and 3 just extensions?

**Claude:**

Yes, mostly. Stage 1 is the complete core game, and Stages 2 and 3 extend it. There are a few details to get right, though.

## Stage 1 is a complete game, not a prototype

At tag `stage-1` the game has to be fully playable on its own: all 18 turns, all six turn steps, satisfaction loss, customers leaving, payment, and both Victory and Defeat. It must also have reproducible win and lose runs, and it's marked on its own ("Stage 1 core game" in the code criterion). Most of your game logic lives here.

## Stages 2 and 3 extend it, but are still full versions

Each later stage is also a separately tagged version that must be "independently playable" and use the same win and lose rules. You're not submitting add-on modules. You're submitting three snapshots of one growing game:

```
stage-1:  core game
stage-2:  core game + VIP/Critic
stage-3:  core game + VIP/Critic + Host, ComboMeal, Happy Hour
```

## How much each extension touches existing code

| Stage | Mostly new classes | Changes to existing code you should expect |
|---|---|---|
| **2** | `VIPCustomer`, `CriticCustomer` | The arrival schedule now creates at least one VIP and one Critic. If you planned the hooks in Stage 1, little else should change. |
| **3** | `Host`, `ComboMeal`, Happy Hour policy | Bigger changes: a Host seating step in the turn loop, a Happy Hour on/off option at turn start, the +15 recovery at turn start, a remaining-duration display, and combo choices on the menu. |

Stage 2 should be close to a pure extension. Stage 3 changes the turn sequence itself, so some edits to the controller and CLI can't be avoided. That's expected. The brief only asks you to explain why each existing file changed, in the README for each transition.

## Why this split matters for marks

The **20-mark "thinking and development process"** criterion is about exactly this: how your design changed from stage to stage. Markers will look at the `stage-1 → stage-2` and `stage-2 → stage-3` diffs.

- **A good diff** is mostly new files, plus a few small, explained edits. This shows your Stage 1 design anticipated extension (OCP).
- **A bad diff** rewrites `GameController` with type checks, or reworks billing and satisfaction logic from scratch. This suggests the Stage 1 design didn't scale. If it does happen, the brief says to explain why and show that the earlier behaviour still works.

## The balancing act

- **Do** build Stage 1 with extension points: overridable methods on `Customer`, a single place where the bill is calculated, and the `Orderable` and `CanSeat` interfaces.
- **Don't** put any actual Stage 2 or 3 features into Stage 1, such as an unused `VIPCustomer` class, a disabled Happy Hour flag, or a `Host` stub. The brief requires Stage 1 to contain no Stage 2 or 3 features.

So: build the core well in Stage 1, leave room to extend it, and treat Stages 2 and 3 as tests of how well you planned.

# 7. How the stages link together

**You:** how is stage 1 link to stage 2 and how is stage 2 link to stage 3

**Claude:**

The stages are linked in two ways: **in code**, each stage reuses and extends the classes from the one before, and **in git**, each stage is a later commit on the same repo.

## Stage 1 → Stage 2: new customer types

Stage 2 hooks into one place, the **`Customer` class**.

```
                Customer                      ← Stage 1 (unchanged)
          getSatisfactionLoss() → 8
          applyCustomerDiscount() → full price
          chooseItem(), onServed() ...
                   ▲
         ┌─────────┴─────────┐
    VIPCustomer         CriticCustomer        ← Stage 2 (new files)
    loss → 5            loss → 12
    discount → 10% off  + your special behaviour
```

| Stage 1 piece | How Stage 2 uses it |
|---|---|
| `Customer` methods for loss, billing and food choice | VIP and Critic **override** them |
| Turn loop step 5, `c.loseSatisfaction()` | **Unchanged.** Calls the right version automatically (polymorphism) |
| Bill locked when the order is taken | **Unchanged.** The VIP discount runs inside `applyCustomerDiscount()` |
| Order flow, Chef, Cashier, tables | **Unchanged.** All customer types follow the same flow |
| `ArrivalSchedule` | **Small edit.** Turns 4 and 10, for example, now create a VIP and a Critic |

The link is **inheritance and polymorphism**. If Stage 1 is designed well, Stage 2 is two new classes plus one edit to the schedule.

## Stage 2 → Stage 3: new role, new menu type, new pricing rule

Stage 3 hooks into **three** places, one per feature.

```
 Host       ──implements──▶  CanSeat  ◀──implements── Waiter       (staff)
 ComboMeal  ──implements──▶  Orderable ◀──implements── MenuItem    (menu)
 HappyHour  ──applies after──▶ customer.applyCustomerDiscount()    (pricing)
```

| Feature | Connects to | What changes |
|---|---|---|
| **Host** | The seating logic the Waiter already has | New `Host` class. Turn loop gets an extra "Host seats one" step |
| **ComboMeal** | `Order`, `Chef` and `Menu`, which already work with prices and prep units | New `ComboMeal` holding two `MenuItem`s (**composition**). Menu lists the combos. Chef and Cashier are **unchanged** |
| **Happy Hour** | The single bill calculation point, which now includes Stage 2's VIP discount | New policy class runs **after** the VIP discount (VIP first, then Happy Hour, as the brief requires). Turn start gains the toggle, the +15 recovery and the duration display |

Stage 3 also depends on **Stage 2 specifically**, not just Stage 1. The brief's example of a VIP Burger during Happy Hour ($15 × 0.9 × 0.8 = **$10.80**) only works because VIP pricing already exists, so Stage 3 must keep the VIP and Critic behaviour.

## Git: one repo with a straight line of commits

```
main:  ──●──●──●──[stage-1]──●──●──[stage-2]──●──●──●──[stage-3]
          core game           + VIP/Critic     + Host, Combo, Happy Hour
```

- You **don't** create three separate projects. Keep committing on the same code, and put a tag on the commit where each stage is finished and runnable.
- Someone checking out `stage-1` sees only Stage 1 code. Checking out `stage-3` gives the full game.
- The README describes each link: for 1→2 and 2→3, list new files, changed files with the reason for each change, and a compare link: `https://github.com/<you>/<repo>/compare/stage-1...stage-2`

## Summary

| Link | Mechanism | Expected size of change |
|---|---|---|
| **1 → 2** | Inheritance: new `Customer` subclasses override existing methods | Small. Two new classes and a schedule edit |
| **2 → 3** | Interfaces (`CanSeat`, `Orderable`), composition (combo) and adding a pricing step after the VIP discount | Medium. Three new classes plus explained edits to the turn loop and CLI |

Each stage only fits neatly onto the previous one if the earlier stage left the right extension points. That's why your Stage 1 design matters most.

# 8. What changes from Stage 1 to Stage 2

**You:** what changes from stage 1 to 2? what file i need to change

**Claude:**

This assumes you built Stage 1 as outlined earlier, with the overridable methods on `Customer` and a single place where the bill is calculated. If so, Stage 1 → Stage 2 is a small change.

## Files to add (new)

| File | What goes in it |
|---|---|
| `entity/VIPCustomer.java` | `extends Customer`. Overrides loss → **5** and discount → **10% off** |
| `entity/CriticCustomer.java` | `extends Customer`. Overrides loss → **12**, full price, plus **your special behaviour** |

```java
public class VIPCustomer extends Customer {
    public VIPCustomer(int id) { super(id); }

    @Override public int getSatisfactionLoss()          { return 5; }
    @Override public int applyCustomerDiscount(int c)   { return Math.round(c * 0.9f); } // 1500 → 1350
    @Override public String getTypeLabel()              { return "VIP"; }
}

public class CriticCustomer extends Customer {
    public CriticCustomer(int id) { super(id); }

    @Override public int getSatisfactionLoss()          { return 12; }
    @Override public String getTypeLabel()              { return "Critic"; }

    // Special behaviour: you choose ONE and document it. Examples:
    @Override public MenuItem chooseItem(Menu menu)     { return menu.mostExpensive(); } // always Pasta
    // or: @Override public void onServed(int turn) { if (turn - orderedTurn > 2) reduceSatisfaction(10); }
}
```

The brief says the Critic needs "a distinct order preference **or** reaction to service". Either option above works. Pick one, keep it inside the normal service flow (so the Critic still eats and still pays), and write the rule in your README.

## Files to change (existing)

| File | Change | Reason to write in the README |
|---|---|---|
| `control/ArrivalSchedule.java` | Swap two Regular arrivals for a VIP and a Critic (e.g. turn 4 → VIP, turn 10 → Critic). Still 6 arrivals in total. | The brief requires at least one VIP and one Critic in the base run. This is a setup/registration change, which the brief explicitly allows. |
| `boundary/GameCLI.java` *(maybe)* | Show the customer type, e.g. `C2 [VIP] sat 95`. | Only needed if your display doesn't already call `getTypeLabel()`/`toString()`. If it does, nothing changes here. |
| `README.md` | Add a "Stage 1 → 2" section: new files, changed files with reasons, and the compare link. | Required by the brief. |
| `test-notes/` | New Stage 2 victory and defeat input sequences. | Satisfaction and prices differ now, so the Stage 1 runs won't replay to the same results. |

## Files that should **not** change

`GameController`, `Waiter`, `Chef`, `Cashier`, `Order`, `Table`, `Menu`, `MenuItem`, `Restaurant` and `Money` should all stay the same.

The reason: step 5 already calls `c.loseSatisfaction()`, and `takeOrder` already calls `customer.applyCustomerDiscount(price)`. Java calls the VIP or Critic version automatically. If you find yourself editing `GameController` to add `if (c instanceof VIPCustomer)`, stop: that's the type-check chain the brief warns against.

## If Stage 1 didn't have the hooks

If your Stage 1 `Customer` hardcoded the loss (e.g. `satisfaction -= 8`) or `Waiter` set the bill straight from `item.getPriceCents()`, you'll need to edit them as well:

- **`Customer`**: move the `8` into an overridable `getSatisfactionLoss()` method.
- **`Waiter`/order creation**: route the price through `customer.applyCustomerDiscount()`.

That's acceptable. Explain it honestly in the README, for example: "We refactored `Customer` to expose `getSatisfactionLoss()` so subtypes could override it; Regular behaviour is unchanged (verified by replaying the stage-1 victory run)." Better still, add these hooks before you tag `stage-1`, so the 1→2 diff stays clean.

## Checklist before tagging `stage-2`

- ☐ VIP loses 5 per turn, Critic loses 12, Regular still loses 8
- ☐ VIP bills: Salad $8.10, Burger $13.50, Pasta $16.20
- ☐ The Critic's special behaviour is visible in play and documented
- ☐ The base run has 6 arrivals, including at least one VIP and one Critic
- ☐ Victory and defeat are both reproducible with saved inputs
- ☐ No `instanceof` or type `switch` outside the customer classes
- ☐ README has the 1→2 section with the compare link

Then:

```bash
git tag stage-2
git push origin stage-2
```

So the expected 1→2 diff is **2 new classes, 1–2 small edits, and README/test updates**.

# 9. Inheritance vs interfaces and composition

**You:** so stage 1-2 is more of inheritance and stage 2-3 is more of interfaces and composition?

**Claude:**

Yes, that's a good summary, with one refinement: **polymorphism runs through both transitions**. What changes is the mechanism that provides it.

| Transition | Main mechanism | Where it shows up |
|---|---|---|
| **1 → 2** | **Inheritance** + method overriding | `VIPCustomer` and `CriticCustomer` extend `Customer` and override loss, discount and choice |
| **2 → 3** | **Interfaces** + **composition** | `Host` and `Waiter` share `CanSeat`. `ComboMeal` *has* two `MenuItem`s. Happy Hour is a pricing object that order creation *uses* |

## Why each fits

**1 → 2 uses inheritance** because a VIP really *is a* customer. It has the same states, the same flow and the same data, and only a few values differ. That's the textbook case for subclassing, and it keeps LSP easy to satisfy.

**2 → 3 uses interfaces and composition** because:

- **Host:** a Host *is not* a Waiter. It can only seat people. Making Host extend Waiter would give it `takeOrder()` and `serve()` methods it must not use, which breaks LSP and ISP. A small shared `CanSeat` interface fits better.
- **ComboMeal:** a combo is *made of* two items (has-a, which is composition). Its price and prep units are calculated from its parts. Implementing `Orderable` lets `Order`, `Chef` and `Cashier` treat it like any other item. This is the **Composite pattern**.
- **Happy Hour:** it's a **policy** that can be switched on and off, not a kind of customer or item. Putting it in its own object that the pricing step delegates to (composition, similar to the Strategy pattern) keeps the rule in one place and out of `Customer` and `Order`.

## Alternatives for your report

The brief asks for "one alternative design you considered", and each of these choices has one:

| Choice you made | Alternative | Trade-off |
|---|---|---|
| Customer subclasses | A single `Customer` holding a `CustomerProfile` object (composition instead of inheritance) | More flexible (a customer's type could change at runtime), but more classes for a simple fixed set of types |
| `ComboMeal implements Orderable` | `ComboMeal extends MenuItem` | Simpler, but a combo has no fixed price or prep units of its own, so inheriting those fields is misleading |
| Happy Hour as a pricing policy | Discount `if` checks inside `Order` or `Waiter` | Fewer classes, but pricing rules get scattered, and the next promotion means editing core classes (breaks OCP) |

## For your class diagram

The brief asks you to **annotate examples of encapsulation, polymorphism and composition**. Your stages give you one of each:

- **Polymorphism:** `Customer` ← `VIPCustomer`/`CriticCustomer` (Stage 2)
- **Composition:** `ComboMeal` ◆— `MenuItem` ×2 (Stage 3)
- **Encapsulation:** private `satisfaction` and `billCents`, changed only through methods (all stages)

The brief leaves design choices to you, so this split isn't mandatory. It is a clean, defensible structure, and it gives your report a clear story for how the design evolved.

# 10. Linking boundary, control and entity classes in Stage 1

**You:** how do i link the boundary, control and entity class in stage 1?

**Claude:**

Use the standard Boundary–Control–Entity (BCE) rule: **dependencies point inward**. The boundary knows about the control, the control knows about the entities, and the entities know about neither.

```
┌───────────────┐        ┌──────────────────┐        ┌──────────────────────┐
│   BOUNDARY    │        │     CONTROL      │        │       ENTITY         │
│   GameCLI     │◀──────▶│  GameController  │───────▶│ Restaurant, Customer │
│ (Scanner,     │  via   │  ArrivalSchedule │        │ Order, Table, Menu   │
│  System.out)  │ interf.│                  │        │ Waiter, Chef, Cashier│
└───────────────┘        └──────────────────┘        └──────────────────────┘
        ▲                         ▲
        └──────── Main.java ──────┘   (creates everything and connects them)
```

## The one tricky part: the controller needs input

The controller runs the turn loop, so it must **ask** the manager for choices. If it called `GameCLI` directly, your control layer would depend on the console. Avoid this with **two small interfaces in the control package**, which `GameCLI` implements. This is DIP and the "ManagerInput" idea from earlier.

```java
// control/ManagerInput.java: what the controller needs to ASK
public interface ManagerInput {
    int chooseWaiterAction(List<String> options);   // returns the chosen index
    int chooseChefAction(List<String> options);
}

// control/GameView.java: what the controller needs to SHOW
public interface GameView {
    void showTurnStart(int turn, Restaurant r);
    void showMessage(String msg);              // e.g. "C2 paid $18.00"
    void showError(String reason);            // invalid choice explanation
    void showResult(GameResult result);
}

// boundary/GameCLI.java: the only class that uses Scanner or System.out
public class GameCLI implements ManagerInput, GameView {
    private final Scanner in = new Scanner(System.in);

    public int chooseWaiterAction(List<String> options) {
        System.out.println("Waiter tasks:");
        for (int i = 0; i < options.size(); i++)
            System.out.printf("  %d) %s%n", i + 1, options.get(i));
        return readIntInRange(1, options.size()) - 1;   // re-prompts on bad input
    }
    // showTurnStart only reads entities via getters, e.g. r.getRevenueCents(), c.getSatisfaction()
    ...
}
```

## The controller: runs the steps and uses entities, never the console

```java
public class GameController {
    private final Restaurant restaurant;
    private final Waiter waiter; private final Chef chef; private final Cashier cashier;
    private final ArrivalSchedule schedule;
    private final ManagerInput input;    // an interface, not GameCLI
    private final GameView view;         // an interface, not GameCLI

    public GameResult run() {
        for (int turn = 1; turn <= 18; turn++) {
            schedule.arrivalFor(turn).ifPresent(restaurant::enqueue);
            view.showTurnStart(turn, restaurant);
            handleWaiter(turn);
            handleChef(turn);
            processEatingAndPayment(turn);
            processDecay();
        }
        GameResult result = restaurant.evaluate();   // win check lives in the domain
        view.showResult(result);
        return result;
    }

    private void handleWaiter(int turn) {
        while (true) {
            List<WaiterAction> actions = waiter.possibleActions(restaurant);  // rules: entity/control
            int pick = input.chooseWaiterAction(labels(actions));            // asking: boundary
            ActionResult r = actions.get(pick).execute(waiter, turn);        // doing: entity
            if (r.ok()) { view.showMessage(r.message()); return; }
            view.showError(r.reason());                                      // explain, task not used up
        }
    }
}
```

## Main connects everything

```java
public class Main {
    public static void main(String[] args) {
        Restaurant restaurant = new Restaurant(2, Menu.standard());
        GameCLI cli = new GameCLI();
        GameController game = new GameController(
            restaurant, new Waiter(), new Chef(), new Cashier(),
            ArrivalSchedule.base(), cli, cli);     // the CLI fills both roles
        game.run();
    }
}
```

For reproducible runs, pass a `ScriptedInput implements ManagerInput` that replays saved choices instead of the CLI. No other code changes.

## Who is allowed to call whom

| Layer | May use | Must **not** |
|---|---|---|
| **Boundary** (`GameCLI`) | Read entity **getters** for display. Implements control's interfaces | Change entities, or decide whether an action is valid |
| **Control** (`GameController`) | Entities, and `ManagerInput`/`GameView` interfaces | `System.out`, `Scanner`, or `GameCLI` by name |
| **Entity** (`Customer`, `Order`, `Waiter`...) | Other entities | Print anything, or know about the controller or CLI |

**About invalid choices:** the brief requires that invalid choices explain why they failed. If you only list options that are always valid, a manager can never make an invalid choice. Two common fixes:

- List every task, including ones that fail, such as "Seat C3" when no table is free. Validation in `Waiter` then rejects it with a reason.
- Or accept typed commands like `seat 3 1`.

Either way, the reason text comes from the entity or control layer. The CLI only displays it.

## In your sequence diagram

The layering maps directly onto your sequence diagram:

```
Manager → GameCLI → GameController → Waiter → Customer/Table/Order
                  ◀── ActionResult ──┘
GameCLI ◀── showMessage() ── GameController
```

That gives the "messages among the CLI, controller, staff and domain objects" the brief asks for, with the retry loop as a `loop [invalid choice]` frame.

# 11. Rough class diagram for each stage

**You:** i want to roughly see what each class diagram in each stage looks like so i get a rough idea what to draw and expect. make a simple drawing, dont draw every single class etc thoroughly, just rough idea.

**Claude:**

Here are rough sketches of the class diagram at each stage. Each stage repeats the previous one, so **[NEW]** marks what that stage adds. The final UML you submit only needs to be the Stage 3 one, but it helps to see how it grows.

**Legend:** `──▷` is inheritance (extends); `- -▷` is implements an interface; `◆──` is composition (owns); `──>` is association (uses); numbers are multiplicities.

## Stage 1: core game

```
  BOUNDARY                 CONTROL                              ENTITY
 ┌──────────┐      ┌───────────────────┐
 │ GameCLI  │- - -▷│ «interface»       │
 └──────────┘      │ ManagerInput      │
       ¦           └───────────────────┘
       └ - - - - -▷┌───────────────────┐
                   │ «interface»       │
                   │ GameView          │
                   └───────────────────┘
                            ▲ uses
                   ┌───────────────────┐  1     1  ┌──────────────┐ 1   2 ┌───────┐
                   │  GameController   │──────────>│  Restaurant  │◆──────│ Table │
                   │ - turn: int       │           │ - revenue    │       └───────┘
                   │ + run()           │           │ + evaluate() │            │ 0..1
                   └───────────────────┘           └──────────────┘            ▼
                     │     │     │    │               ◆ queue 0..*     ┌──────────────┐
                     ▼     ▼     ▼    ▼               └───────────────>│   Customer   │
                ┌──────┐┌────┐┌───────┐┌─────────────────┐            │ - satisfaction│
                │Waiter││Chef││Cashier││ ArrivalSchedule │──creates──>│ + getSatLoss()│
                └──────┘└────┘└───────┘└─────────────────┘            └──────────────┘
                                                                        1 │
                                                                     0..1 ▼
                 «enum» CustomerStatus                             ┌────────────┐ * 1 ┌──────────┐
                 «enum» OrderStatus                                │   Order    │────>│ MenuItem │
                                                                   │ - billCents│     └──────────┘
                                                                   └────────────┘          ▲ 3
                                                                                   Menu ◆──┘
```

**Key idea:** BCE layering. `GameCLI` implements the control-layer interfaces, the controller drives the staff, and the entities hold the state.

## Stage 2: customer types (inheritance)

Everything from Stage 1 stays. Only the `Customer` area grows:

```
                         ┌───────────────────────────┐
  ArrivalSchedule ──────>│         Customer          │
                         │ + getSatisfactionLoss()   │  → 8
                         │ + applyCustomerDiscount() │  → full price
                         │ + chooseItem()            │
                         └───────────────────────────┘
                               △               △
                               │               │
                 ┌─────────────┴──┐     ┌──────┴──────────────┐
                 │ VIPCustomer    │     │ CriticCustomer      │   [NEW]
                 │ loss → 5       │     │ loss → 12           │
                 │ discount → 10% │     │ + special behaviour │
                 └────────────────┘     └─────────────────────┘
```

**Key idea:** polymorphism. The controller still talks only to `Customer`. Annotate this on the diagram.

## Stage 3: Host, ComboMeal, Happy Hour (interfaces and composition)

Everything from Stage 2 stays, plus three additions:

```
 (a) Staff seating: ISP

            ┌──────────────────────┐
            │ «interface» CanSeat  │
            │ + seat(c, t)         │
            └──────────────────────┘
               △ ¦             ¦ △
        ┌──────┴───────┐  ┌───┴───────┐
        │    Waiter    │  │   Host    │  [NEW]
        │ + takeOrder()│  │ (seats    │
        │ + serve()    │  │  only)    │
        └──────────────┘  └───────────┘


 (b) Menu items: Composite

            ┌────────────────────────┐
  Order ───>│ «interface» Orderable  │
            │ + getPriceCents()      │
            │ + getPrepUnits()       │
            └────────────────────────┘
               △ ¦             ¦ △
        ┌──────┴─────┐   ┌─────┴────────────┐
        │  MenuItem  │ 2 │   ComboMeal      │  [NEW]
        │            │──◆│ price = 90% sum  │
        └────────────┘   │ units = sum      │
                         └──────────────────┘


 (c) Pricing: Strategy-like

  Waiter.takeOrder() ──> customer.applyCustomerDiscount()   (VIP first)
                     ──> ┌──────────────────────┐
                         │      HappyHour       │  [NEW]
                         │ - turnsLeft: int     │
                         │ - used: boolean      │
                         │ + activate()         │
                         │ + apply(price)       │  → 20% off if active
                         └──────────────────────┘
                                   ▲
              GameController ──────┘  (toggle at turn start, +15 recovery)
```

**Key ideas:** ISP (`CanSeat`), composition (`ComboMeal ◆ MenuItem ×2`) and an extensible pricing step.

## Big picture

| Stage | Diagram grows by | OOP concept to annotate |
|---|---|---|
| 1 | Base layout: boundary, control, entities | Encapsulation, BCE layering |
| 2 | 2 subclasses under `Customer` | Inheritance and polymorphism |
| 3 | 2 interfaces, `Host`, `ComboMeal`, `HappyHour` | ISP, composition |

## When you draw the real one (Stage 3 only, worth 25 marks)

- Add the **key attributes and methods with visibility** (`-` private, `+` public).
- Put **multiplicities** on associations, e.g. Restaurant 1 → 2 Table, Customer 1 → 0..1 Order.
- Include the **enums**.
- Add **annotations** (UML notes) pointing at your encapsulation, polymorphism and composition examples.
- Make sure it **matches your code**: same class names, same methods.

Tools like draw.io, PlantUML or StarUML work well.

# 12. Questions asked so far

**You:** what are the questions i have asked so far in our conversation

**Claude:**

Here are the questions you'd asked up to that point, in order:

1. **"How to use you?"**: how to work with me in this session.
2. **What the project involves and what the code is expected to do**, based on your uploaded brief.
3. **How to start Stage 1**, with an outline.
4. **What happens when a customer hits 0 satisfaction while their order is READY but not served**, and whether a READY order counts as "unfinished".
5. **Which SOLID principles to be careful of**, with a focus on OCP and ISP.
6. **Whether Stage 1 is the core and Stages 2 and 3 are just extensions.**
7. **How Stage 1 links to Stage 2, and Stage 2 to Stage 3.**
8. **What changes from Stage 1 to 2, and which files to change.**
9. **Whether 1→2 is mainly inheritance and 2→3 mainly interfaces and composition.**
10. **How to link the boundary, control and entity classes in Stage 1.**
11. **A rough class diagram for each stage.**
12. **That question**: a list of everything you'd asked.

# 13. Stage 1 code skeleton

**You:** can you give me the rough outline of the stage 1 code skeleton so i can build upon it?

*(This request was interrupted before the skeleton was written, so no code was produced.)*

# 14. Test cases likely to fail

**You:** what are some test cases that have a high chance of failing and i should be careful of

**Claude:**

The bugs that slip through are usually in the **timing rules** (what can happen in the same turn and what has to wait), followed by edge cases and rounding. Each test below gives the scenario, the correct result, and the bug that usually breaks it.

## 1. Timing rules (most likely to fail)

| # | Scenario | Expected result | Common bug |
|---|---|---|---|
| 1 | Chef finishes a dish on turn *t*. Waiter tries to serve it on turn *t* | Rejected: "not ready yet". Can be served on turn *t*+1 | Chef acts before Waiter, or the serve check ignores `readyTurn` |
| 2 | Waiter takes a Burger order, then the Chef cooks it in the same turn | Allowed. Burger becomes READY this turn and can be served next turn | Chef's option list is built before the Waiter acts, so the new order is missing |
| 3 | Customer served on turn *t* | Not READY_TO_PAY until turn *t*+1. Pays on turn *t*+2 at the earliest | Eating is processed in the same turn as serving |
| 4 | Customer becomes READY_TO_PAY this turn | Cashier skips them until next turn | Eating and payment run in the wrong order, or the turn check is missing |
| 5 | Two customers are READY_TO_PAY | Only one pays per turn: the one who became READY_TO_PAY first | Both pay, or the wrong one pays first |
| 6 | **The brief's example in 3.3** (one Pasta customer, full attention) | Satisfaction 92 → 84 → 76 → 76 → 76, pays **$18.00 on turn 6** with 76 recorded | Any of the timing bugs above. **This is your most important regression test.** |

## 2. Satisfaction and abandonment

| # | Scenario | Expected result | Common bug |
|---|---|---|---|
| 7 | Customer served this turn | Loses **nothing** at the end of this turn (stays 76 on turn 4 in the example) | Loss applied before the status check |
| 8 | Customer waiting in the queue, never seated | Also loses 8 per turn | Only seated customers lose satisfaction |
| 9 | Regular never served | 100 → … → 4 → **0** (not −4). Leaves at the end of turn *arrival* + 12 | No clamp at 0, or the leave check uses `< 0` |
| 10 | Customer leaves while their order is IN_PROGRESS or READY | Order CANCELLED, table freed, counted as an unhappy departure, no revenue | Table still occupied, or the order is still listed for the Chef or Waiter |
| 11 | Customer leaves from the queue | Removed from the queue | They stay in the queue and can still be seated |
| 12 | After a customer leaves, Chef or Waiter tries to act on their order | Rejected or not listed | `NullPointerException`, or the cancelled order gets cooked or served |

## 3. Invalid input

| # | Scenario | Expected result | Common bug |
|---|---|---|---|
| 13 | Seat a customer when both tables are full | Shows the reason, asks again, **task not used up** | Task counted as used, or no explanation given |
| 14 | Serve a non-READY order. Take an order twice. Chef advances an already-READY order | Each is rejected with a reason | State is silently corrupted |
| 15 | Type `abc`, an empty line, `-1` or `99` | Re-prompts without crashing | `InputMismatchException`, or an infinite loop because the `Scanner` doesn't consume the bad token |
| 16 | Scripted input file runs out (EOF) | Clean exit or a clear error | Infinite loop on `hasNextInt()` |

## 4. End of game and scoring

| # | Scenario | Expected result | Common bug |
|---|---|---|---|
| 17 | Everyone leaves early | Game still runs all **18** turns | Game ends early |
| 18 | No one has paid yet | Average shows **N/A** | Division by zero, or `NaN` |
| 19 | Exactly 4 paid, exactly $45.00, average exactly 60 | **Victory** (all three targets use ≥) | `>` used instead of `>=` |
| 20 | Customer served but not paid by turn 18 | Not counted in revenue or the average | Revenue added when the order is taken or served |
| 21 | Turn-16 customer | Can never pay. At most 5 customers pay | Treated as a bug and "fixed" by changing the rules |
| 22 | Same inputs run twice | Identical output | Unseeded `Random` |

## 5. Stage 2 and 3 money and rules

| # | Scenario | Expected result | Common bug |
|---|---|---|---|
| 23 | VIP bills | Salad **$8.10**, Burger **$13.50**, Pasta **$16.20** | Discount applied at payment instead of when the order is taken |
| 24 | Satisfaction loss per type | Regular −8, VIP −5, Critic −12 (Critic hits 0 after 9 turns) | Loss hardcoded in the controller |
| 25 | Combo prices | Salad + Pasta **$24.30** (3 units). Salad + Burger **$21.60** (2 units). Burger + Pasta **$29.70** (3 units) | Using `double` gives `$24.299999`. Use integer cents: `(cents * 9 + 5) / 10` |
| 26 | VIP orders a Burger during Happy Hour | **$10.80** (VIP 10% first, then 20%) | Discounts in the wrong order, or added together as 30% off ($10.50) |
| 27 | Order taken just before Happy Hour starts | Full price stays locked | Price recalculated at payment time |
| 28 | Happy Hour activated on turn *t* | Active on *t* and *t*+1, off on *t*+2. Can't be activated a second time | Lasts 1 or 3 turns, or can be activated again |
| 29 | Happy Hour activated on turn 18 | Active for 1 turn only. No error | Index past the end, or a crash |
| 30 | Happy Hour recovery | Waiting customers get +15 at turn start, capped at 100. Served, eating and departed customers get nothing | No cap, or applied to the wrong customers |
| 31 | Host and Waiter both seat in one turn | Two different customers at two different tables | Same customer seated twice, or Host seats more than one |
| 32 | Host has no free table or no one waiting | Does nothing, with no error | Crash |

## How to test efficiently

- **Turn #6 into an automated test.** It covers most of the timing rules in one run.
- **Save input scripts** (`victory.txt`, `defeat.txt`, `abandonment.txt`) and replay them after every change. The `ScriptedInput` idea from earlier makes this easy, and you need reproducible runs for marking anyway.
- **Write down your interpretations** (for example "oldest first means earliest to become READY_TO_PAY, ties broken by arrival order") in your test notes, so the TA can see that the behaviour is intended.

# 15. Continuing this conversation later

**You:** i would like to return to this conversation, if i open claude ai next time, would i still be able to continue this conversation?

**Claude:**

Yes, you can come back to this conversation. One thing won't carry over, though.

**What stays:** the conversation itself. It's saved in your session list, at claude.ai/code or under Code in the Claude app. Open it and send a new message, and I'll still have the earlier messages, so you can say something like "let's do the Stage 1 skeleton now" without explaining again.

**What may be lost:** files I created. This session runs on a temporary cloud machine, and that machine is deleted after a period of inactivity. These files live only there:

- `SC2002_Restaurant_Rush_Conversation.pdf`
- `conversation.md`

**Download the PDF now** from the file card I sent. You'd also need to re-upload your assignment brief if a new machine starts. I can rebuild the PDF if needed, since the conversation itself is kept.

**For your actual project code:** once you start coding, put it in a GitHub repo. Then in any session you can say "work on `yourname/restaurant-rush`" and I'll clone it, so nothing is lost when the machine resets.

# 16. Start: the Stage 1 code skeleton

**You:** start

**Claude:**

I took "start" to mean the Stage 1 code skeleton, since that's where we stopped last time. It's written, it compiles, and a full 18-turn game runs from start to the result screen. Choosing an action that isn't built yet shows a message and lets you choose again, and typing letters or out-of-range numbers just re-prompts. It's sent as a zip (`restaurant-rush-stage1-skeleton.zip`) and also saved in `/home/user/restaurant-rush/`.

The game rules themselves are left for your team, so you can't win yet: with the rules empty, a full game always ends in Defeat.

## What's in it

```
restaurant-rush/
├── README.md                 build/run steps, TODO list, assumptions, stage table
└── src/restaurant/
    ├── Main.java             creates the objects and starts the game
    ├── boundary/GameCLI.java the only class that reads input or prints
    ├── control/              GameController (turn loop), ArrivalSchedule,
    │                         ManagerInput, GameView, StaffOption
    ├── entity/               Customer, Order, MenuItem, Menu, Table, Restaurant,
    │   │                     the two status enums, ActionResult, GameResult
    │   └── staff/            Waiter, Chef, Cashier
    └── util/Money.java       integer cents and "$18.00" formatting
```

## Already working

- **Turn loop:** the six steps in the brief's order, with the Waiter acting before the Chef. The Chef's options are listed after the Waiter acts, so an order taken this turn can be cooked this turn.
- **Choosing again after a bad pick:** the controller keeps asking until an action succeeds, so a failed choice doesn't use up that staff member's task.
- **Option lists:** every Waiter task is listed, including ones that will fail (like seating when no table is free), so the game can explain why.
- **Customer hooks:** satisfaction loss, discount and food choice are methods Stage 2 can override.
- **Satisfaction:** always kept between 0 and 100.
- **Money:** stored as integer cents.
- **Arrivals:** turns 1, 4, 7, 10, 13 and 16.

## Your team implements (search for `TODO`)

| File | What to write |
|---|---|
| `Order` | `advance`, `cancel` |
| `Waiter` | `seat`, `takeOrder`, `serve` (checks plus state changes) |
| `Chef` | `advance` |
| `Cashier` | `processPayment` |
| `GameController` | `processEating`, `processDecay`, `leave` |
| `Restaurant` | `averagePaidSatisfaction`, `evaluate` |
| `GameCLI` | also show customers who are eating or ready to pay |

Each `TODO` has a comment saying exactly what to do. These are about 13 small methods, which splits well across your team.

## Build and run

```bash
javac -d out $(find src -name "*.java")
java -cp out restaurant.Main
```

## Next steps

1. Put this in a GitHub repo now, so it isn't lost when this cloud machine resets.
2. Fill in the TODOs. The brief's 3.3 Pasta example should then pay $18.00 on turn 6 with satisfaction 76.
3. Record this help in your GAI declarations.

# 17. Test

**You:** test

**Claude:**

Yes, I'm here and I remember the whole conversation: your SC2002 Restaurant Rush brief, the Stage 1 outline, the class diagrams, the PDF and the skeleton code.

Your files are still on this machine:

- `restaurant-rush/`, the Stage 1 skeleton project
- `restaurant-rush-stage1-skeleton.zip`
- `conversation-pdf/`, with the conversation PDF

These files can still be deleted when the machine is reset, so download the zip if you haven't already, or push the code to GitHub.

If you meant a test of the game itself, I can write automated checks for the Stage 1 rules, starting with the brief's Pasta example (pays $18.00 on turn 6 with satisfaction 76). They'll fail until the TODOs are filled in, so you can use them to check your team's work as it comes in.
