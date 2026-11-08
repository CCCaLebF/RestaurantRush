# Claude Code session 24d9a3d0-a388-4b3f-8002-25f91a7babb6

- Folder: `c:\NTU\NTU Y2S1\SC2002 OBJECT ORIENTED DES & PROG\SC2002 Assignment\RestaurantRush`
- Started: 2026-11-06 14:41 SGT
- Last activity: 2026-11-06 15:14 SGT
- User prompts: 9
- Files Claude edited or created: none

---

## User (2026-11-06 14:41)

list down the use cases in the code that has only been introduced in stage 3.

**Claude:**

I'll start with what the Stage 3 commit changed compared with Stage 2.

> _tool: Bash: Show Stage 3 commit stats and list tracked files_

> _tool: Bash: Show Stage 3 source, README and check diffs_

> _tool: Read: C:\Users\skyri\.claude\projects\c--NTU-NTU-Y2S1-SC2002-OBJECT-ORIENTED-DES---PROG-SC2002-Assignment-RestaurantRush\24d9a3d0-a388-4b3f-8002-25f91a7babb6\tool-res_

**Claude:**

The diff covers the code. I'll also check whether the plan docs already define use cases, so my names match yours.

> _tool: Bash: Search plan docs for use case definitions_

**Claude:**

No document in the repo defines formal use cases, so I worked these out from the Stage 3 commit (`15ec7a5`). I read the code but didn't run it. There are **7 new use cases**, plus 3 existing ones that gained new behaviour.

## New in Stage 3

| # | Use case | Actor | What happens | Main code |
|---|---|---|---|---|
| 1 | **Activate Happy Hour** | Manager | The manager is asked at the start of every turn and can switch it on or choose "Not now". It can only be used once per game, and a second attempt is refused with a reason. It can't be activated once the Host has acted. | [ConsoleUI.java](src/restaurantrush/boundary/ConsoleUI.java) `chooseHappyHour`, [HappyHour.java](src/restaurantrush/entity/HappyHour.java) `activate` |
| 2 | **Recover customer satisfaction** | System | Customers who are WAITING, SEATED or AWAITING_FOOD gain 15 satisfaction, up to 100. This happens on activation and again at the start of the second active turn. | [Restaurant.java](src/restaurantrush/entity/Restaurant.java) `applyHappyHourRecovery`, [Customer.java](src/restaurantrush/entity/Customer.java) `recover` |
| 3 | **Count down and end Happy Hour** | System | Each turn end uses up one active turn, moving from `2 turns left` to `last turn` to `used`. If activated on turn 18, it lasts only that turn. | [HappyHour.java](src/restaurantrush/entity/HappyHour.java) `endTurn`, [GameController.java](src/restaurantrush/control/GameController.java) `endTurn` |
| 4 | **View Happy Hour status** | Manager | The start-of-turn display shows whether it is available, active (with turns left) or used. | [StateFormatter.java](src/restaurantrush/boundary/StateFormatter.java) `happyHourStatus` |
| 5 | **Host seats a queued customer** | Manager, through the Host | The Host seats at most one customer per turn, before the Waiter acts. The usual seating rules apply: the customer must be queued and the table free. An invalid choice doesn't use up the Host's task. | [Host.java](src/restaurantrush/entity/staff/Host.java) `seat`, [GameController.java](src/restaurantrush/control/GameController.java) `hostSeat` |
| 6 | **Host waits** | Manager, through the Host | The Host skips the turn. | [GameController.java](src/restaurantrush/control/GameController.java) `hostWait` |
| 7 | **Order a combo meal** | Customer | A customer gets one of three combos through the six-item rotation, or the Critic's "most elaborate dish" rule. A combo costs 90% of its two parts combined, and its preparation units are the sum of both. | [ComboMeal.java](src/restaurantrush/entity/ComboMeal.java), [Menu.java](src/restaurantrush/entity/Menu.java) `standard` |

## Existing use cases with new behaviour

These aren't new, so you may want to show them as `<<extend>>` relationships rather than separate use cases:

- **Take Order:** orders taken during Happy Hour get 20% off, applied after the VIP discount and locked into the order. For example, a VIP Burger goes from $15.00 to $13.50 to $10.80 (`Customer.placeOrder`).
- **Seat Customer (Waiter):** the Waiter's seating is unchanged, but with the Host, two customers can now be seated in one turn. The Waiter can also take the order of a customer the Host has just seated, in the same turn.
- **Prepare Order (Chef):** the Chef's code is unchanged, but combos mean an order can now take up to 3 units. The Critic's order changes too, from Pasta to Burger + Pasta.

**Not included:** serving, payment, abandonment, customer arrivals and win/lose evaluation have no new behaviour. Stage 3 only added a Happy Hour countdown step to `endTurn`.

## User (2026-11-06 14:43)

out of all these use cases, which is the most complicated one

**Claude:**

**Activate Happy Hour (#1)** is the most complicated, because it's the only use case that changes how other use cases behave after it finishes.

- **It passes through every layer:** the call goes from `ConsoleUI` to `GameController`, then `Restaurant`, then `HappyHour`, and each one has its own job. The UI loops until the choice is valid, the controller checks the phase, `Restaurant` coordinates the activation and recovery, and `HappyHour` enforces the once-per-game rule.
- **It has several rules and alternate paths:** the manager can decline, a second activation is refused and the manager is asked again, and it's only allowed before the Host acts. Activating on turn 18 is also a special case: it lasts one turn, so the display shows `last turn` instead of `2 turns left`.
- **Its effects continue across turns:** activation triggers recovery straight away, then `startTurn` triggers it again on the next turn. It also changes the price of any order taken while active, and `endTurn` counts it down. As a result, use cases 2, 3 and 4 and Take Order all depend on it.
- **Timing matters:** the discount applies when an order is taken, not when it's paid. An order placed before Happy Hour stays at full price, while one placed during it keeps the discount after it ends. The discount also has to come after the VIP discount, with rounding at each step.

**Runner-up:** Order a combo meal (#7) looks like a big feature, but it's quite simple. `ComboMeal` is a `MenuItem`, so `Order`, the Chef and the customer dish rules all work with it unchanged. The Host use cases are simpler still, because `Host.seat` just reuses the existing `Restaurant.seat`.

## User (2026-11-06 14:44)

if i were to create a uml sequence diagram for this, would i have to show the entire run to demonstrate this use case?

**Claude:**

No, a sequence diagram shows the messages for **one use case**, not a whole game. For Activate Happy Hour, start when the manager is asked the Happy Hour question and stop when the result is printed. All of that happens inside a single turn, so you don't need `startTurn`, the staff tasks or `endTurn`.

**Lifelines:** `Manager` (actor), `ConsoleUI`, `StateFormatter`, `GameController`, `Restaurant`, `HappyHour`, and `Customer`

**Message flow:**

```text
loop [until a valid choice is made]
  ConsoleUI -> StateFormatter: happyHourStatus(happyHour, finalTurn)
  ConsoleUI -> Manager: show status and options
  Manager -> ConsoleUI: enter choice

  alt choice = 0 (Not now)
    ConsoleUI exits with no change

  else choice = 1 (Activate)
    ConsoleUI -> GameController: activateHappyHour()
    GameController -> GameController: requirePhase(HOST)
    GameController -> Restaurant: activateHappyHour()
    Restaurant -> HappyHour: activate()

    alt Happy Hour not used yet
      HappyHour sets used = true, turnsLeft = 2
      Restaurant -> Restaurant: applyHappyHourRecovery()
      loop [each customer waiting for a table or food]
        Restaurant -> Customer: recover(15)
      end
      Restaurant --> GameController --> ConsoleUI: recovered customers
      ConsoleUI -> StateFormatter: recovery(recovered)
      ConsoleUI -> Manager: print "OK" message

    else Happy Hour already used
      HappyHour throws InvalidActionException
      ConsoleUI -> Manager: print "Invalid", then ask again
    end
  end
end
```

**Later effects:** leave these out of this diagram, because they happen in other use cases:
- Recovery on the second active turn, which runs from `startTurn`
- The 20% discount, which applies during Take Order
- The countdown, which runs from `endTurn`

If you want to show how they connect, add a short note or a `ref` fragment pointing to a separate diagram. For example, a Take Order diagram could show the discount.

**Simplifying it:** you can leave out `StateFormatter` and show only the display steps if the diagram gets crowded. Keep the two `alt` fragments, as the "Not now" and "already used" paths are what make this use case complex.

Check your assignment brief too: if it asks for a specific scenario or level of detail, follow that instead.

## User (2026-11-06 15:03)

explain what does applyHappyHourRecovery() do

**Claude:**

`applyHappyHourRecovery()` gives 15 satisfaction to every customer still waiting for a table or food, as long as Happy Hour is active. It then returns the list of those customers.

```java
public List<Customer> applyHappyHourRecovery() {
    if (!happyHour.isActive()) {
        return List.of();
    }
    List<Customer> waiting = getActiveCustomers().stream().filter(Customer::isLosingSatisfaction).toList();
    waiting.forEach(c -> c.recover(HappyHour.RECOVERY));
    return waiting;
}
```

1. **Checks Happy Hour is active:** if it isn't, the method returns an empty list and changes nothing. This is why `startTurn` can safely call it every turn.
2. **Finds the waiting customers:** it takes the active customers and keeps those where `isLosingSatisfaction()` is true. According to the README, those are customers in the WAITING, SEATED or AWAITING_FOOD states, so anyone eating or ready to pay is left out.
3. **Applies the recovery:** it calls `recover(15)` on each one. `Customer.recover` adds the points but caps satisfaction at 100.
4. **Returns the list:** `ConsoleUI` passes this to `StateFormatter.recovery()` to print a message like `Recovered +15 (max 100): C1 -> 72`.

**When it runs:** there are two call sites, one for each active turn:

| Active turn | Called from | Return value |
|---|---|---|
| First (the turn it's switched on) | `Restaurant.activateHappyHour()`, straight after `happyHour.activate()` | Passed back to `ConsoleUI` and printed |
| Second | `GameController.startTurn()`, after arrivals are admitted | Ignored; the status line just says customers recovered 15 |

On the first turn, `startTurn` also calls it, but Happy Hour isn't active yet, so nothing happens and customers don't recover twice.

**Two details:**
- The returned list includes everyone who was eligible, even someone already at 100 who gained nothing.
- On the second active turn, a customer who arrived that turn is included too, but they gain nothing if they start at the 100 cap.

## User (2026-11-06 15:04)

when does activate_happy_hour get called?

**Claude:**

It's called when the manager enters **`1`** at the Happy Hour prompt, near the start of a turn. The code spells it `activateHappyHour()`, and it isn't called automatically: entering `0` ("Not now") skips it for that turn.

**Within a turn, the order is:**
1. `controller.startTurn()` runs, then arrivals and the start-of-turn state are printed.
2. `chooseHappyHour()` asks the manager. **`activateHappyHour()` is called here if they enter `1`.**
3. `chooseHostTask()`, `chooseWaiterTask()` and `chooseChefTask()` run.
4. The turn ends.

The prompt appears every turn, even after Happy Hour has been used. Entering `1` still makes the call on those later turns, but `HappyHour.activate()` throws `InvalidActionException`. The UI prints the reason and asks again.

**There are two methods with this name, and one calls the other:**

```text
ConsoleUI.chooseHappyHour()
  -> GameController.activateHappyHour()   checks the phase is HOST
    -> Restaurant.activateHappyHour()     activates, then applies recovery
      -> HappyHour.activate()             enforces once per game
```

The `requirePhase(Phase.HOST)` check enforces the timing, because the phase moves to `WAITER` as soon as the Host finishes their task. So the controller would reject any call made after that point, although the UI only ever calls it before the Host acts.

## User (2026-11-06 15:06)

if happy hour is not active but applyHappyHourRecovery gets called, what is the program's bvehaviour

**Claude:**

Nothing changes: the method returns an empty list straight away, and no customer gains satisfaction.

```java
if (!happyHour.isActive()) {
    return List.of();
}
```

`isActive()` is `turnsLeft > 0`, so this covers all three cases where Happy Hour isn't running: before it's been used, after it's finished, and on the turn it's about to be switched on (when `startTurn` runs before the manager has chosen).

No exception is thrown and nothing is printed, because this is a normal case rather than an error. `GameController.startTurn()` calls the method at the start of every turn and relies on this check to do nothing when Happy Hour is off. It also ignores the return value, so the empty list isn't used there.

## User (2026-11-06 15:09)

which function calls the recover(15) to restore 15 points to the customer's satisfaction level?

**Claude:**

**`Restaurant.applyHappyHourRecovery()`** calls it, on this line:

```java
waiting.forEach(c -> c.recover(HappyHour.RECOVERY));
```

The code doesn't literally say `recover(15)`: it passes the constant `HappyHour.RECOVERY`, which is set to 15 in [HappyHour.java](src/restaurantrush/entity/HappyHour.java).

From the Stage 3 diff, this is the only place that calls `recover`. It's reached in two ways:
- **First active turn:** `Restaurant.activateHappyHour()` calls `applyHappyHourRecovery()`.
- **Second active turn:** `GameController.startTurn()` calls `applyHappyHourRecovery()`.

## User (2026-11-06 15:11)

help me check the first page of this document, and see if the sequence diagram accurately matches what the code is doing

**Claude:**

I'll compare page 1 with the current source, including the parts the Stage 3 diff didn't show, like `admit`, `arrivalsFor` and `getActiveCustomers`.

> _tool: Bash: Print code involved in start of turn and Happy Hour_

**Claude:**

Page 1 is **mostly accurate**: the calls exist, they're in the right order, and the return values match. However, one fragment doesn't match the code, and two pieces of logic are missing. I only compared the diagram with the code by reading them, and I haven't changed anything.

## What matches
- **`startTurn` order:** it runs `turn++`, resets staff, gets arrivals, admits them, applies recovery, then returns `arrivals`.
- **Admitting customers:** `admit` does `customers.add(c)`, then `enqueue(c)`.
- **Recovery:** the inactive path returns `List.of()`, and `GameController` ignores the returned list.
- **Activation:** `activateHappyHour()` goes from the UI through the controller and `Restaurant` to `HappyHour.activate()`. It sets `used = true` and `turnsLeft = 2`, then recovery runs and the list returns to the UI.
- **Footer notes:** these are correct, apart from the turn 18 exception below.

## Should fix
1. **The recovery loop doesn't match the code ([Restaurant.java:104-105](src/restaurantrush/entity/Restaurant.java#L104-L105)).** The diagram has one loop over customers already losing satisfaction, which then calls `isLosingSatisfaction()` and always gets `true`. The code makes two passes: it checks every active customer, so the answer can be false, and then it recovers the ones that passed. Use this instead:
   ```text
   loop [each active customer]
     r -> c: isLosingSatisfaction()
     c --> r: true / false
   end
   loop [each waiting customer]
     r -> c: recover(15)
   end
   ```
2. **The phase logic is missing ([GameController.java:51-66](src/restaurantrush/control/GameController.java#L51-L66)).** Add `requirePhase(BETWEEN_TURNS)` at the start of `startTurn()` and `phase = HOST` just before it returns. Also add `requirePhase(HOST)` at the start of `activateHappyHour()`. This check is what enforces activating before the Host acts.
3. **The retry after a refusal is missing ([ConsoleUI.java:71-85](src/restaurantrush/boundary/ConsoleUI.java#L71-L85)).** After `InvalidActionException`, the UI prints the reason and asks again, but the `opt` makes it look like the flow just ends. Wrap the `opt` in `loop [until Not now or activation succeeds]`.

## Worth improving
4. **Exception path:** the arrow goes straight from `HappyHour` to `ConsoleUI`, but the exception actually passes through `Restaurant` and `GameController`. Either draw those steps or label it "via r and gc", as page 2 does.
5. **Customer creation:** `arrivalsFor(turn)` creates the customers through `CustomerFactory.create(...)`, which isn't shown ([ArrivalSchedule.java:24](src/restaurantrush/control/ArrivalSchedule.java#L24)). Add a `CustomerFactory` lifeline, or a note if you want to keep the diagram smaller.
6. **`alt` guards:** `alt [inactive] / [active]` puts both guards in the frame's title. In UML, each guard goes at the top of its own section, so put `[inactive]` and `[active]` there, and do the same for `[unused]` and `[already used]`.
7. **Second recovery call:** the self-call after activation hides the same logic shown earlier. Add a note or `ref` saying it runs the same steps, and that Happy Hour is now active.
8. **Footer wording:** "lasts two turns" isn't true on turn 18, when it lasts only that turn. Recovery is also capped at 100, which the footer doesn't mention.

Leaving out the display calls (`StateFormatter`, `getTurn()` and similar) is fine, since they aren't part of the game logic.

## User (2026-11-06 15:14)

if i exclude fixes 1,2 and 3 would the diagram be completely wrong

**Claude:**

No, it wouldn't be completely wrong. The main flow would still be correct, but the diagram would have one small inaccuracy and two gaps.

| Fix | If you skip it | Severity |
|---|---|---|
| **1. Recovery loop** | The right customers still recover, but the diagram shows one loop where the code has two passes. Its guard also makes the `isLosingSatisfaction()` check look pointless, as it can only return `true`. | Small inaccuracy, the only one that's actually wrong |
| **2. Phase logic** | Everything shown is still true, but the diagram doesn't show how the code enforces activating before the Host acts. | Omission, and sequence diagrams often leave out internal checks like this |
| **3. Retry after refusal** | The refused path looks like it ends after the exception, when the UI actually asks again. | Omission that slightly misleads |

If you only make one change, I'd do **fix 1**, because it's the only one a marker could compare with the code and call incorrect. It's also quick: split the loop in two, or change the guard to `[each active customer]` and put `recover(15)` inside an `opt [isLosingSatisfaction() is true]`. That second version still isn't exactly how the code runs, but it's close and shows the check can fail.

For fixes 2 and 3, a one-line note can do the job instead of redrawing, such as: "Activation is only allowed before the Host acts; if refused, the UI asks again."

