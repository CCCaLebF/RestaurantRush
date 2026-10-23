# Restaurant Rush — SC2002 Group Assignment

A turn-based restaurant management game played from the command line. You are the manager: each turn you choose what the Waiter and the Chef do, and the Cashier takes payments automatically.

- **Repository:** https://github.com/CCCaLebF/RestaurantRush.git
- **Group:** SCEA, Group 2
- **Members:** CALEB FOOK WEI LI, GOH ZHE YU, LI YIJIE JAY, LUCAS LIM JIAN QI 
- **Current stage:** Stage 1, basic restaurant operation (tag `stage-1`)

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

Each run replays a saved input file. `--echo` prints each input line, so the transcript reads like a live game.

| Stage | Run | Input file | Expected result |
|---|---|---|---|
| 1 | Victory | `runs/victory.txt` | VICTORY: 5 paid, $66.00, average satisfaction 79.2 |
| 1 | Defeat | `runs/defeat.txt` | DEFEAT: 0 paid, 2 unhappy departures |
| 1 | Rule checks | `runs/rule-checks.txt` | Invalid choices rejected with reasons |

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
| Food choice | Deterministic: customer *n* orders menu item `(n-1) mod 3` from [Salad, Burger, Pasta]. C1 Salad, C2 Burger, C3 Pasta, C4 Salad, C5 Burger, C6 Pasta. The dish and its locked price are shown when the order is taken. |
| Start-of-turn display | Shown after the turn's arrival is announced, so the manager can see the new customer before choosing. |
| Waiter before Chef | The Waiter's choice is carried out before the Chef is asked. A dish the Chef finishes this turn can therefore only be served next turn. |
| Eating and payment (step 4) | The Cashier pays first, and only customers who were already READY_TO_PAY before this turn. Then customers served on an earlier turn become READY_TO_PAY. Served on turn *t* → READY_TO_PAY on *t+1* → paid on *t+2* at the earliest. |
| Cashier "oldest first" | Earliest turn of becoming READY_TO_PAY, then earliest arrival. |
| Satisfaction loss | −8 at turn end for customers who are WAITING (queue), SEATED, or AWAITING_FOOD (this includes a READY dish not yet served). Minimum 0. Customers who are eating or ready to pay lose nothing. |
| Abandonment | At 0 the customer leaves without paying: any unfinished order is CANCELLED and their table (or queue place) is freed. |
| Average satisfaction | Uses the satisfaction recorded at payment and is shown to 1 decimal place, or N/A if nobody has paid. The target check uses exact integers (`sum >= 60 × count`). |
| Money | Stored as integer cents and displayed as `$x.xx`. |
| Turn 16 customer | Cannot finish paying within 18 turns, so at most 5 customers can pay. This is intended by the brief. |

### Customer types (Stage 2)

_To be added in `stage-2`: VIP and Critic rules, the Critic's special behaviour, VIP discount rounding, and the Stage 2 arrival schedule._

### Host, ComboMeal and Happy Hour (Stage 3)

_To be added in `stage-3`: Host seating, the combo choice rule, and Happy Hour activation and pricing._

## Design overview

Entity–Control–Boundary layering under `src/restaurantrush/`:

| Package | Classes | Role |
|---|---|---|
| `entity` | `Customer` (abstract), `RegularCustomer`, `CustomerState`, `Order`, `OrderStatus`, `Menu`, `MenuItem`, `Table`, `WaitingQueue`, `Ledger`, `Restaurant`, `Money`, `InvalidActionException` | Game state and rules. Each object guards its own state transitions. |
| `entity.staff` | `Staff` (abstract), `Waiter`, `Chef`, `Cashier` | One task per turn; a task is used up only when it succeeds. |
| `control` | `GameController`, `GameConfig`, `ArrivalSchedule`, `CustomerFactory`, `ObjectiveEvaluator`, `GameResult`, `TurnReport` | Runs the 6-step turn in order (enforced by phases) and decides win or loss. No I/O. |
| `boundary` | `ConsoleUI`, `StateFormatter` | Reads choices and prints state. No game rules. |
| — | `Main` | Wires the base setting together. |

Behaviour that depends on the customer type (`chooseDish`, `getWaitingPenalty`, `computeBillCents`) is a polymorphic hook on `Customer`. The controller never checks customer types.

## Stage history

Each stage is a tagged, runnable commit: `git checkout stage-1` (or `stage-2`, `stage-3`), then build and run as above.

| Tag | Adds | Commit / compare |
|---|---|---|
| `stage-1` | Regular customers, menu, Waiter/Chef/Cashier, full turn sequence, victory and defeat | _TODO: commit link_ |
| `stage-2` | VIP and Critic customers | _TBD_ |
| `stage-3` | Host, ComboMeal, Happy Hour | _TBD_ |

### Stage 1 → Stage 2

_To be completed at `stage-2`._

- **Compare:** _TBD (`…/compare/stage-1...stage-2`)_
- **New files:** _TBD_
- **Changed files (with reasons):** _TBD_
- **Deleted files (with reasons):** _TBD_

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
