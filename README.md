# Restaurant Rush — SC2002 Group Assignment

A turn-based restaurant management game played from the command line. You are the manager: each turn you decide whether to start Happy Hour and choose what the Host, the Waiter and the Chef do, and the Cashier takes payments automatically.

- **Repository:** https://github.com/CCCaLebF/RestaurantRush.git
- **Group:** SCEA, Group 2
- **Members:** CALEB FOOK WEI LI, GOH ZHE YU, LI YIJIE JAY, LUCAS LIM JIAN QI 
- **Current stage:** Stage 3, new role and menu policy (tag `stage-3`)

## Build and run

Requires JDK 17 or newer. There are no external libraries and no network access is needed.

```bash
# Git Bash / macOS / Linux
javac -d out $(find src -name "*.java")
java -cp out restaurantrush.Main
```

```powershell
# Windows PowerShell
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out restaurantrush.Main
```

### Reproducible runs

Each run replays a saved input file. `--echo` prints each input line, so the transcript reads like a live game. [`TEST_NOTES.md`](TEST_NOTES.md) has the turn-by-turn strategy, the actual output of each run, and the Stage 3 behaviour checks.

| Stage | Run | Input file | Expected result |
|---|---|---|---|
| 3 | Victory | `runs/victory.txt` | VICTORY: 5 paid, $89.64, average satisfaction 87.4 (Happy Hour on turn 13) |
| 3 | Defeat | `runs/defeat.txt` | DEFEAT: 0 paid, 2 unhappy departures (C1 Regular on turn 13, C4 Critic on turn 18) |
| 3 | Rule checks | `runs/rule-checks.txt` | 8 invalid choices rejected with reasons, without using up the task (including the Host and a second Happy Hour) |
| 3 | Stage 3 checks | `runs/stage3-checks.txt` | Host and Waiter both seat on turn 4; Happy Hour recovery and countdown; VIP + Happy Hour bill $10.80 |

**About `runs/stage3-checks.txt`:** it is a short scripted game that shows the Stage 3 rules in the brief's own terms. The objective is not to win, but to show, over turns 1–7, the Host and the Waiter each seating a customer in the same turn, Happy Hour's recovery and its countdown (`2 turns left` → `last turn` → `used`), the brief's VIP + Happy Hour example ($15.00 × 0.9 × 0.8 = $10.80), and full price again once Happy Hour ends. The input then runs out, every remaining choice counts as Wait, and the game ends in DEFEAT. This is expected.

**Where each Stage 3 rule is shown:** no single run covers every rule, so the rules are spread across three runs. `TEST_NOTES.md` Section 5 lists them rule by rule with the output that shows each one.

| Stage 3 rule | Shown in |
|---|---|
| Host seats one queued customer per turn, before the Waiter | victory, stage3-checks |
| Waiter can still seat (two customers seated in one turn) | stage3-checks |
| Host follows the same seating rules | rule-checks |
| Combo price (90% of both parts) and units (sum of both) | the menu at game start, victory |
| Combo choice rule and cooking a 3-unit combo | victory (C4 and C5) |
| Happy Hour once per game | rule-checks |
| Happy Hour lasts two turns and shows how long is left | stage3-checks |
| 20% off orders taken while active, after the VIP discount | stage3-checks, rule-checks, victory (C5) |
| Recovery of 15 at the start of each active turn, max 100 | victory, stage3-checks |
| Price locked when the order is taken | victory (C4 ordered before Happy Hour), stage3-checks (C2 after it ends) |

```bash
# Git Bash / macOS / Linux
java -cp out restaurantrush.Main --echo < runs/victory.txt
```

```powershell
# Windows PowerShell (no < operator)
Get-Content runs/victory.txt | java -cp out restaurantrush.Main --echo
```

## How to play

Each turn the game shows the turn number, revenue, average satisfaction of paid customers, Happy Hour's state, the waiting queue, the tables (each customer's satisfaction and order status) and the tasks each staff member can do. Then:

1. **Happy Hour** (manager): switch it on, or not now. Once per game.
2. **Host** (acts first): seat a queued customer, or wait.
3. **Waiter**: seat a queued customer, take a seated customer's order, serve a READY dish, or wait.
4. **Chef**: advance one order by one preparation unit, or wait. The Chef can cook an order the Waiter took this turn.
5. The **Cashier** then takes at most one payment, and the turn ends.

Enter the number shown next to a choice. `0` means wait (or not now), or go back from a sub-menu. An invalid choice is explained and you choose again; the staff member's task is not used up.

**Win** after turn 18 with at least 4 paid customers, revenue of at least $45.00, and an average satisfaction of at least 60 among paid customers. Otherwise you lose.

## Game rules and interpretations

### Base rules (Stage 1)

| Topic | Our rule |
|---|---|
| Food choice | Deterministic: customer *n* orders menu item `(n-1) mod` (menu size). On the Stage 1 menu [Salad, Burger, Pasta]: C1 Salad, C2 Burger, C3 Pasta, C4 Salad, C5 Burger, C6 Pasta. The dish and its locked price are shown when the order is taken. From Stage 2, this is the default rule; the Critic overrides it. From Stage 3 the menu also has three combos, so the rotation covers six items (see Stage 3). |
| Start-of-turn display | Shown after the turn's arrival is announced, so the manager can see the new customer before choosing. |
| Waiter before Chef | The Waiter's choice is carried out before the Chef is asked. A dish the Chef finishes this turn can therefore only be served next turn. From Stage 3 the Host acts before both. |
| Eating and payment (step 4) | The Cashier pays first, and only customers who were already READY_TO_PAY before this turn. Then customers served on an earlier turn become READY_TO_PAY. Served on turn *t* → READY_TO_PAY on *t+1* → paid on *t+2* at the earliest. |
| Cashier "oldest first" | Earliest turn of becoming READY_TO_PAY, then earliest arrival. |
| Satisfaction loss | At turn end, for customers who are WAITING (queue), SEATED, or AWAITING_FOOD (this includes a READY dish not yet served). Minimum 0. Customers who are eating or ready to pay lose nothing. The amount depends on the customer type: −8 for a Regular (see Stage 2 for the other types). |
| Abandonment | At 0 the customer leaves without paying: any unfinished order is CANCELLED and their table (or queue place) is freed. |
| Average satisfaction | Uses the satisfaction recorded at payment and is shown to 1 decimal place, or N/A if nobody has paid. The target check uses exact integers (`sum >= 60 × count`). |
| Money | Stored as integer cents and displayed as `$x.xx`. |
| Turn 16 customer | Cannot finish paying within 18 turns, so at most 5 customers can pay. This is intended by the brief. |

### Customer types (Stage 2)

| Customer | Satisfaction loss per waiting turn | Bill | Dish choice |
|---|---|---|---|
| Regular | 8 | Full price | Default rotation rule |
| VIP | 5 | 10% off | Default rotation rule |
| Critic | 12 | Full price | **Always orders the most elaborate dish** (see below) |

| Topic | Our rule |
|---|---|
| **Critic special behaviour** | **Order preference:** the Critic always orders the most elaborate dish on the menu, meaning the item with the most preparation units, with ties broken by the higher price. On the Stage 2 menu this is Pasta (2 units, $18.00), whatever the Critic's arrival number. This makes the Critic costly to serve: the dish takes two Chef turns while the Critic loses 12 satisfaction per waiting turn. From Stage 3 the same rule picks the Burger + Pasta combo (3 units, $29.70); the Critic's code is unchanged. |
| VIP discount rounding | Bill = 90% of the menu price, rounded half-up to the nearest cent in integer arithmetic. The base menu gives exact amounts: Salad $8.10, Burger $13.50, Pasta $16.20. The bill is locked when the order is taken, and the display shows the menu price alongside it, e.g. `$13.50 (list $15.00)`. |
| Stage 2 arrival schedule | Still six arrivals: turn 1 Regular C1, **turn 4 VIP C2**, turn 7 Regular C3, **turn 10 Critic C4**, turn 13 Regular C5, turn 16 Regular C6. The VIP replaces the turn-4 regular (a Burger, so the discount shows) and the Critic replaces the turn-10 regular (a Salad slot, so the Pasta preference shows). |
| Same flow for every type | Every customer type uses the same visit flow, state checks and win/lose rules. Only the hooks in the table above differ. |
| Abandonment by type | The rules are unchanged (leave at 0). An unserved Critic leaves after 9 waiting turns, a Regular after 13. A VIP would need 20, so a VIP can never abandon within 18 turns. This follows from the brief's penalty table and is not a bug. |

### Host, ComboMeal and Happy Hour (Stage 3)

| Topic | Our rule |
|---|---|
| **Host** | Seats at most one queued customer per turn, in addition to the Waiter's task. The manager chooses the Host's task (seat or wait), like the other staff. The same seating rules apply (customer must be queued, table must be free). |
| Turn order | Happy Hour choice → **Host** → Waiter → Chef → Cashier. The Host acts first, so the Waiter can take the order of a customer the Host has just seated, and the Chef can start cooking it, in the same turn. The Waiter can still seat customers, so two customers can be seated in one turn. |
| **Combos** | Each pair of single dishes is a combo: **Salad + Burger** $21.60 (2 units), **Salad + Pasta** $24.30 (3 units), **Burger + Pasta** $29.70 (3 units). Price = 90% of the two prices combined, rounded half-up to the nearest cent; preparation units = the sum of the two. A combo is cooked one unit per Chef turn like any dish. |
| **Combo choice rule** | The menu lists the three dishes, then the three combos, and the default rotation runs over all six: customer *n* orders item `(n-1) mod 6`. The Critic's "most elaborate dish" rule now picks the Burger + Pasta combo. With the base schedule: C1 Salad, C2 Burger, C3 Pasta, **C4 (Critic) Burger + Pasta**, **C5 Salad + Pasta**, C6 Burger + Pasta. C1–C3 order the same as in Stage 2. |
| **Happy Hour: activation** | The manager is asked at the start of every turn, before the Host acts. It can be switched on once per game; choosing it again is refused with a reason. It lasts for the turn it is switched on and the next one (on turn 18 it only lasts that turn, because the game ends). |
| Happy Hour: display | The start-of-turn display shows `available (once per game)`, `ACTIVE: last turn` or `used`; switching it on shows `ACTIVE: 2 turns left (this turn and next)`, or `ACTIVE: last turn` if it is switched on in the final turn. |
| Happy Hour: discount | Orders **taken** while it is active get 20% off, applied **after** the VIP discount, each step rounded half-up to the nearest cent. VIP Burger: $15.00 → $13.50 → **$10.80**. The bill is locked when the order is taken, so orders placed before Happy Hour keep their price and orders placed during it keep the discount afterwards. |
| Happy Hour: recovery | At the start of each active turn, customers still waiting for a table or food (WAITING, SEATED or AWAITING_FOOD, the same customers who lose satisfaction) recover 15 satisfaction, up to 100. On the first turn this happens when the manager switches it on. Customers who are eating, ready to pay, paid or gone do not recover. |
| Base setting | Unchanged arrivals, tables and targets. The staff are now Host × 1, Waiter × 1, Chef × 1 and Cashier × 1. |

## Design overview

Entity–Control–Boundary layering under `src/restaurantrush/`:

| Package | Classes | Role |
|---|---|---|
| `entity` | `Customer` (abstract), `RegularCustomer`, `VIPCustomer`, `CriticCustomer`, `CustomerState`, `Order`, `OrderStatus`, `Menu`, `MenuItem`, `ComboMeal`, `Table`, `WaitingQueue`, `Ledger`, `HappyHour`, `Restaurant`, `Money`, `InvalidActionException` | Game state and rules. Each object guards its own state transitions. |
| `entity.staff` | `Staff` (abstract), `Host`, `Waiter`, `Chef`, `Cashier` | One task per turn; a task is used up only when it succeeds. |
| `control` | `GameController`, `GameConfig`, `Arrival`, `CustomerType`, `ArrivalSchedule`, `CustomerFactory`, `ObjectiveEvaluator`, `GameResult`, `TurnReport` | Runs the 6-step turn in order (enforced by phases) and decides win or loss. No I/O. |
| `boundary` | `ConsoleUI`, `StateFormatter` | Reads choices and prints state. No game rules. |
| — | `Main` | Wires the base setting together. |

Behaviour that depends on the customer type is a polymorphic hook on `Customer`: `chooseDish` (a default rule the Critic overrides), `getWaitingPenalty`, `computeBillCents`, `getTypeName` and `describeBehaviour`. `Customer` fixes the visit flow and its subclasses fill in these steps (Template Method). The controller and the UI never check customer types. `CustomerFactory` is the only class that names the concrete customer classes.

Stage 3 builds on the same seams:
- `ComboMeal` is a `MenuItem`, so `Order`, the Chef and the customers' dish rules use combos without knowing they are combos.
- `Host` is another `Staff`, and `GameController` resets every staff member through `Restaurant.getStaff()`.
- `HappyHour` is owned by `Restaurant`, which passes its discount into `Customer.placeOrder` as a price adjustment applied after the customer's own discount.

## Stage history

Each stage is a tagged, runnable commit: `git checkout stage-1` (or `stage-2`, `stage-3`), then build and run as above.

| Tag | Adds | Commit / compare |
|---|---|---|
| `stage-1` | Regular customers, menu, Waiter/Chef/Cashier, full turn sequence, victory and defeat | [stage-1](https://github.com/CCCaLebF/RestaurantRush/tree/stage-1) |
| `stage-2` | VIP and Critic customers | [stage-2](https://github.com/CCCaLebF/RestaurantRush/tree/stage-2) · [compare 1 → 2](https://github.com/CCCaLebF/RestaurantRush/compare/stage-1...stage-2) |
| `stage-3` | Host, ComboMeal, Happy Hour | [stage-3](https://github.com/CCCaLebF/RestaurantRush/tree/stage-3) · [compare 2 → 3](https://github.com/CCCaLebF/RestaurantRush/compare/stage-2...stage-3) |

### Stage 1 → Stage 2

- **Compare:** https://github.com/CCCaLebF/RestaurantRush/compare/stage-1...stage-2
- **Summary:** the two new customer types are new `Customer` subclasses, created by the existing `CustomerFactory`. The turn logic is untouched: `GameController`, `Restaurant`, `Order`, `Ledger`, `Table`, `WaitingQueue`, `Menu`, `MenuItem` and all staff classes are unchanged since `stage-1`.

**New files**

| File | Purpose |
|---|---|
| `entity/VIPCustomer.java` | VIP: loses 5 per waiting turn; bill is 90% of the menu price |
| `entity/CriticCustomer.java` | Critic: loses 12 per waiting turn; full price; overrides `chooseDish` to order the most elaborate dish |
| `control/CustomerType.java` | Enum `REGULAR`, `VIP`, `CRITIC`, so the schedule can say which type arrives |
| `control/Arrival.java` | Record `(turn, type)`: one scheduled arrival |
| `TEST_NOTES.md` | Stage 2 reproducible runs with actual outputs, and behaviour checks |
| `diagrams/RestaurantRushStage2.drawio` | Stage 2 class diagram: adds `VIPCustomer`, `CriticCustomer`, `CustomerType` and `Arrival`, the factory's «create» links to each customer type, and the polymorphism note. It also draws six getters that already existed in Stage 1 but were left off its diagram (`GameController.getRestaurant/getResult`, `TurnReport.getPaidCustomer/getFinalResult`, `Restaurant.getTables/getAllCustomers`); their code is unchanged. |

**Changed files (with reasons)**

| File | Change | Reason |
|---|---|---|
| `entity/Customer.java` | `chooseDish` is no longer abstract: it holds the rotation rule as a default. New abstract `describeBehaviour()`. | Regular and VIP share the rotation and only the Critic overrides it, so a default avoids duplicating it. The UI can describe each type without type checks. |
| `entity/RegularCustomer.java` | Its `chooseDish` override is removed (inherited now); `describeBehaviour()` added | Follows from the `Customer` change |
| `entity/Money.java` | New `percentOf(cents, percent)`, rounding half-up in integer cents | VIP discount without floating point. Stage 3's Happy Hour discount can reuse it. |
| `control/CustomerFactory.java` | `create(number, turn)` → `create(type, number, turn)` | The extension point planned in Stage 1. It's the only place that names concrete customer classes, and its switch must cover every `CustomerType`, so the compiler catches a missing type. |
| `control/ArrivalSchedule.java` | Iterates over `Arrival`s and passes each type to the factory | The schedule now includes the customer type |
| `control/GameConfig.java` | `arrivalTurns: List<Integer>` → `arrivals: List<Arrival>`; `base()` holds the Stage 2 schedule | The brief requires a VIP and a Critic in the base run, with six arrivals still |
| `Main.java` | `getArrivalTurns()` → `getArrivals()` (one line) | Setup change that follows from `GameConfig` |
| `boundary/StateFormatter.java` | Shows the customer type in queue and table lines, the type's behaviour on arrival, and `bill (list price)` when a bill is discounted | Makes the new behaviour visible to the manager, using only polymorphic getters |
| `boundary/ConsoleUI.java` | Banner says Stage 2; the take-order message shows the discounted bill | Display only |
| `runs/victory.txt` | New keystrokes | The Stage 1 strategy no longer wins with a VIP and a Critic in the schedule (it ends with 3 paid, $40.50) |
| `README.md` | Stage 2 rules, expected results, design overview and this section | Documentation |
| `diagrams/RestaurantRush.drawio`, `.pdf` | Renamed to `RestaurantRushStage1.drawio` and `.pdf`; content unchanged | Keeps the Stage 1 diagram as evidence next to the new Stage 2 diagram |
| `.gitignore` | Ignores the local `docs/` folder and draw.io backup files (`*.bkp`) | Housekeeping |

**Unchanged run files:** `runs/defeat.txt` and `runs/rule-checks.txt` still give DEFEAT and the same rule rejections under Stage 2.

**Deleted files:** none. The Stage 1 diagram was renamed, not deleted.

### Stage 2 → Stage 3

- **Compare:** https://github.com/CCCaLebF/RestaurantRush/compare/stage-2...stage-3
- **Summary:** the three features are three new classes plus wiring. All customer subclasses, `Order`, `MenuItem`, `Money`, `Table`, `WaitingQueue`, `Ledger`, the existing staff classes and the arrival/creation classes are unchanged. `GameController` changes for the first time since `stage-1`, because the Host needs its own slot in the turn and Happy Hour has start-of-turn and end-of-turn effects; its existing steps are unchanged.

**New files**

| File | Purpose |
|---|---|
| `entity/ComboMeal.java` | A `MenuItem` made of two menu items: price 90% of both (rounded), units = sum |
| `entity/HappyHour.java` | Happy Hour state and rules: once per game, two turns, 20% off, recovery of 15 |
| `entity/staff/Host.java` | A `Staff` member who seats one queued customer per turn |
| `runs/stage3-checks.txt` | Scripted run showing the Host, Happy Hour and VIP + Happy Hour pricing |
| `diagrams/RestaurantRushStage3.drawio` | Final class diagram: adds `Host`, `ComboMeal` and `HappyHour` and the new operations. Matches the Stage 3 code. |

**Changed files (with reasons)**

| File | Change | Reason |
|---|---|---|
| `entity/Menu.java` | `standard()` adds the three combos after the single dishes | ComboMeal; the combo choice rule rotates over the whole menu |
| `entity/Customer.java` | `placeOrder(menu)` → `placeOrder(menu, priceAdjustment)`; new `recover(points)`; the dish-rule comment lists the six-item rotation | Happy Hour is applied after the customer's own discount and locked into the order; Happy Hour recovery |
| `entity/CriticCustomer.java` | Comment only: the most elaborate dish is now the Burger + Pasta combo | The code is unchanged; the menu changed |
| `entity/Restaurant.java` | Owns a `Host` and a `HappyHour`. New `getHost()`, `getHappyHour()`, `getStaff()`, `activateHappyHour()`, `applyHappyHourRecovery()`, `endTurnEffects()`. `takeOrder` passes the Happy Hour discount. | The restaurant owns its staff and policies and keeps multi-object operations consistent |
| `control/GameController.java` | New `HOST` phase before `WAITER`; new `activateHappyHour()`, `hostSeat()`, `hostWait()`. `startTurn` resets every staff member via `getStaff()` and applies Happy Hour recovery; `endTurn` counts Happy Hour down. | The Host needs a slot in the turn, and Happy Hour has start-of-turn and end-of-turn effects. Resetting through `getStaff()` means a future role needs no change here. |
| `boundary/ConsoleUI.java` | Asks for Happy Hour and the Host's task before the Waiter; seating is shared by the Host and the Waiter; banner and intro updated for Stage 3 | Collects the new decisions; no rules |
| `boundary/StateFormatter.java` | Happy Hour line with remaining duration; Host in the staff tasks; recovery message | Brief: show the staff tasks available and Happy Hour's remaining duration |
| `runs/victory.txt`, `runs/defeat.txt`, `runs/rule-checks.txt` | New keystrokes | Each turn now asks for a Happy Hour choice and a Host task, so the Stage 2 keystrokes no longer line up. The new victory uses the Host and Happy Hour; the rule checks add three Stage 3 rejections. |
| `TEST_NOTES.md` | Rewritten for Stage 3 | Reproducible runs with actual outputs and Stage 3 behaviour checks |
| `README.md` | Stage 3 rules, expected results, design overview and this section | Documentation |

**Unchanged since `stage-2`:** `RegularCustomer`, `VIPCustomer`, `Order`, `OrderStatus`, `MenuItem`, `Money`, `Table`, `WaitingQueue`, `Ledger`, `CustomerState`, `InvalidActionException`, `Staff`, `Waiter`, `Chef`, `Cashier`, `CustomerFactory`, `CustomerType`, `Arrival`, `ArrivalSchedule`, `GameConfig`, `ObjectiveEvaluator`, `GameResult`, `TurnReport`, `Main`.

**Deleted files:** none. The Stage 1 and Stage 2 diagrams are kept next to the Stage 3 one.

## Video timestamps

| # | Segment | Timestamp | Presenter |
|---|---|---|---|
| 1 | The game launching from the submitted project | _TBD_ | _TBD_ |
| 2 | Meaningful manager choices, including a decision where staff are constrained | _TBD_ | _TBD_ |
| 3 | A dish going READY → SERVED → payment | _TBD_ | _TBD_ |
| 4 | A VIP or Critic behaviour | _TBD_ | _TBD_ |
| 5 | A Stage 3 feature | _TBD_ | _TBD_ |
| 6 | One victory and one defeat | _TBD_ | _TBD_ |
| 7 | The stage tag comparisons and the diagrams | _TBD_ | _TBD_ |
