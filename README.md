# Restaurant Rush — SC2002 Group Assignment

A turn-based restaurant management game played from the command line. You are the manager: each turn you choose what the Waiter and the Chef do, and the Cashier takes payments automatically.

- **Repository:** https://github.com/CCCaLebF/RestaurantRush.git
- **Group:** SCEA, Group 2
- **Members:** CALEB FOOK WEI LI, GOH ZHE YU, LI YIJIE JAY, LUCAS LIM JIAN QI 
- **Current stage:** Stage 2, customer behaviour extension (tag `stage-2`)

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

Each run replays a saved input file. `--echo` prints each input line, so the transcript reads like a live game. [`TEST_NOTES.md`](TEST_NOTES.md) has the turn-by-turn strategy, the actual output of each run, and the Stage 2 behaviour checks.

| Stage | Run | Input file | Expected result |
|---|---|---|---|
| 2 | Victory | `runs/victory.txt` | VICTORY: 5 paid, $73.50, average satisfaction 75.6 |
| 2 | Defeat | `runs/defeat.txt` | DEFEAT: 0 paid, 2 unhappy departures (C1 Regular on turn 13, C4 Critic on turn 18) |
| 2 | Rule checks | `runs/rule-checks.txt` | 5 invalid choices rejected with reasons, without using up the staff task |

```bash
# Git Bash / macOS / Linux
java -cp out restaurantrush.Main --echo < runs/victory.txt
```

```powershell
# Windows PowerShell (no < operator)
Get-Content runs/victory.txt | java -cp out restaurantrush.Main --echo
```

## How to play

Each turn the game shows the turn number, revenue, average satisfaction of paid customers, the waiting queue, the tables (each customer's satisfaction and order status) and the tasks each staff member can do. Then:

1. **Waiter** (acts first): seat a queued customer, take a seated customer's order, serve a READY dish, or wait.
2. **Chef**: advance one order by one preparation unit, or wait. The Chef can cook an order the Waiter took this turn.
3. The **Cashier** then takes at most one payment, and the turn ends.

Enter the number shown next to a choice. `0` means wait, or go back from a sub-menu. An invalid choice is explained and you choose again; the staff member's task is not used up.

**Win** after turn 18 with at least 4 paid customers, revenue of at least $45.00, and an average satisfaction of at least 60 among paid customers. Otherwise you lose.

## Game rules and interpretations

### Base rules (Stage 1)

| Topic | Our rule |
|---|---|
| Food choice | Deterministic: customer *n* orders menu item `(n-1) mod 3` from [Salad, Burger, Pasta]. C1 Salad, C2 Burger, C3 Pasta, C4 Salad, C5 Burger, C6 Pasta. The dish and its locked price are shown when the order is taken. From Stage 2, this is the default rule; the Critic overrides it (see below). |
| Start-of-turn display | Shown after the turn's arrival is announced, so the manager can see the new customer before choosing. |
| Waiter before Chef | The Waiter's choice is carried out before the Chef is asked. A dish the Chef finishes this turn can therefore only be served next turn. |
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
| **Critic special behaviour** | **Order preference:** the Critic always orders the most elaborate dish on the menu, meaning the item with the most preparation units, with ties broken by the higher price. On the base menu this is Pasta (2 units, $18.00), whatever the Critic's arrival number. This makes the Critic costly to serve: the dish takes two Chef turns while the Critic loses 12 satisfaction per waiting turn. |
| VIP discount rounding | Bill = 90% of the menu price, rounded half-up to the nearest cent in integer arithmetic. The base menu gives exact amounts: Salad $8.10, Burger $13.50, Pasta $16.20. The bill is locked when the order is taken, and the display shows the menu price alongside it, e.g. `$13.50 (list $15.00)`. |
| Stage 2 arrival schedule | Still six arrivals: turn 1 Regular C1, **turn 4 VIP C2**, turn 7 Regular C3, **turn 10 Critic C4**, turn 13 Regular C5, turn 16 Regular C6. The VIP replaces the turn-4 regular (a Burger, so the discount shows) and the Critic replaces the turn-10 regular (a Salad slot, so the Pasta preference shows). |
| Same flow for every type | Every customer type uses the same visit flow, state checks and win/lose rules. Only the hooks in the table above differ. |
| Abandonment by type | The rules are unchanged (leave at 0). An unserved Critic leaves after 9 waiting turns, a Regular after 13. A VIP would need 20, so a VIP can never abandon within 18 turns. This follows from the brief's penalty table and is not a bug. |

### Host, ComboMeal and Happy Hour (Stage 3)

_To be added in `stage-3`: Host seating, the combo choice rule, and Happy Hour activation and pricing._

## Design overview

Entity–Control–Boundary layering under `src/restaurantrush/`:

| Package | Classes | Role |
|---|---|---|
| `entity` | `Customer` (abstract), `RegularCustomer`, `VIPCustomer`, `CriticCustomer`, `CustomerState`, `Order`, `OrderStatus`, `Menu`, `MenuItem`, `Table`, `WaitingQueue`, `Ledger`, `Restaurant`, `Money`, `InvalidActionException` | Game state and rules. Each object guards its own state transitions. |
| `entity.staff` | `Staff` (abstract), `Waiter`, `Chef`, `Cashier` | One task per turn; a task is used up only when it succeeds. |
| `control` | `GameController`, `GameConfig`, `Arrival`, `CustomerType`, `ArrivalSchedule`, `CustomerFactory`, `ObjectiveEvaluator`, `GameResult`, `TurnReport` | Runs the 6-step turn in order (enforced by phases) and decides win or loss. No I/O. |
| `boundary` | `ConsoleUI`, `StateFormatter` | Reads choices and prints state. No game rules. |
| — | `Main` | Wires the base setting together. |

Behaviour that depends on the customer type is a polymorphic hook on `Customer`: `chooseDish` (a default rule the Critic overrides), `getWaitingPenalty`, `computeBillCents`, `getTypeName` and `describeBehaviour`. `Customer` fixes the visit flow and its subclasses fill in these steps (Template Method). The controller and the UI never check customer types. `CustomerFactory` is the only class that names the concrete customer classes.

## Stage history

Each stage is a tagged, runnable commit: `git checkout stage-1` (or `stage-2`, `stage-3`), then build and run as above.

| Tag | Adds | Commit / compare |
|---|---|---|
| `stage-1` | Regular customers, menu, Waiter/Chef/Cashier, full turn sequence, victory and defeat | [stage-1](https://github.com/CCCaLebF/RestaurantRush/tree/stage-1) |
| `stage-2` | VIP and Critic customers | [stage-2](https://github.com/CCCaLebF/RestaurantRush/tree/stage-2) · [compare 1 → 2](https://github.com/CCCaLebF/RestaurantRush/compare/stage-1...stage-2) |
| `stage-3` | Host, ComboMeal, Happy Hour | _TBD_ |

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

_To be completed at `stage-3`._

- **Compare:** _TBD (`…/compare/stage-2...stage-3`)_
- **New files:** _TBD_
- **Changed files (with reasons):** _TBD_
- **Deleted files (with reasons):** _TBD_

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
