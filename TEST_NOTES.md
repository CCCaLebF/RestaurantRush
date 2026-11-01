# Test Notes — Stage 3

Every run below uses the required base setting (brief Section 2.1) plus the Stage 3 Host, with the same arrival schedule as Stage 2, and replays a saved input file. The results are deterministic, so the same file always gives the same game.

| Customer | Arrives | Type | Dish (rule) | Bill |
|---|---|---|---|---|
| C1 | Turn 1 | Regular | Salad (rotation) | $9.00 |
| C2 | Turn 4 | **VIP** | Burger (rotation) | **$13.50** (list $15.00, 10% off) |
| C3 | Turn 7 | Regular | Pasta (rotation) | $18.00 |
| C4 | Turn 10 | **Critic** | **Burger + Pasta combo** (most elaborate dish: 3 units, highest price) | $29.70 |
| C5 | Turn 13 | Regular | **Salad + Pasta combo** (rotation) | $24.30 |
| C6 | Turn 16 | Regular | Burger + Pasta combo (rotation) | $29.70 |

Bills taken while Happy Hour is active get a further 20% off, applied after the VIP discount.

## How to replay

Build first (see README), then:

```bash
# Git Bash / macOS / Linux
java -cp out restaurantrush.Main --echo < runs/victory.txt
```

```powershell
# Windows PowerShell
Get-Content runs/victory.txt | java -cp out restaurantrush.Main --echo
```

Replace `victory` with `defeat`, `rule-checks` or `stage3-checks` for the other runs. `--echo` prints each input line so the transcript reads like a live game. Each turn asks, in order: Happy Hour, Host, Waiter, Chef. If an input file runs out, every remaining choice counts as `0` (Not now / Wait).

---

## 1. Victory — `runs/victory.txt`

**Strategy:** the Host seats each new customer, so the Waiter can take their order and the Chef can start cooking in the same turn. Serve a READY dish before taking a new order. **Happy Hour goes on at the start of turn 13**, while the Critic's READY combo is waiting and C5 is about to order.

| Turn | Happy Hour | Host | Waiter | Chef | Step 4 (eating and payment) | End-of-turn revenue / average |
|---|---|---|---|---|---|---|
| 1 | | Seat C1 → T1 | Take C1's order (Salad $9.00) | Cook C1 → READY | | $0.00 / N/A |
| 2 | | | Serve C1 | | | $0.00 / N/A |
| 3 | | | | | C1 → READY_TO_PAY | $0.00 / N/A |
| 4 | | Seat **C2 (VIP)** → T2 | Take C2's order (Burger **$13.50 (list $15.00)**) | Cook C2 → READY | C1 pays $9.00 (satisfaction 92) | $9.00 / 92.0 |
| 5 | | | Serve C2 | | | $9.00 / 92.0 |
| 6 | | | | | C2 → READY_TO_PAY | $9.00 / 92.0 |
| 7 | | Seat C3 → T1 | Take C3's order (Pasta $18.00) | Cook C3 1/2 | C2 pays **$13.50** (95) | $22.50 / 93.5 |
| 8 | | | Wait (not READY yet) | Cook C3 2/2 → READY | | $22.50 / 93.5 |
| 9 | | | Serve C3 | | | $22.50 / 93.5 |
| 10 | | Seat **C4 (Critic)** → T2 | Take C4's order (**Burger + Pasta $29.70**, 3 units) | Cook C4 1/3 | C3 → READY_TO_PAY | $22.50 / 93.5 |
| 11 | | | | Cook C4 2/3 | C3 pays $18.00 (84) | $40.50 / 90.3 |
| 12 | | | | Cook C4 3/3 → READY | | $40.50 / 90.3 |
| 13 | **Activate**: C4 64 → 79 | Seat C5 → T1 | Take C5's order (**Salad + Pasta $19.44 (list $24.30)**) | Cook C5 1/3 | | $40.50 / 90.3 |
| 14 | Active, last turn: C4 67 → 82, C5 92 → 100 | | **Serve C4** | Cook C5 2/3 | | $40.50 / 90.3 |
| 15 | | | | Cook C5 3/3 → READY | C4 → READY_TO_PAY | $40.50 / 90.3 |
| 16 | | No free table (C6 waits) | Serve C5 | | C4 pays $29.70 (82) | $70.20 / 88.3 |
| 17 | | Seat C6 → T2 | Take C6's order (Burger + Pasta $29.70) | Cook C6 1/3 | C5 → READY_TO_PAY | $70.20 / 88.3 |
| 18 | | | | Cook C6 2/3 | C5 pays **$19.44** (84) | $89.64 / 87.4 |

C6 arrives on turn 16 and cannot finish paying within 18 turns. The brief intends this.

**Actual final output:**
```
============================== GAME OVER ==============================
  [MET] Paid customers                  5  (target >= 4)
  [MET] Revenue                    $89.64  (target >= $45.00)
  [MET] Avg satisfaction (paid)      87.4  (target >= 60)
Served: 5 | Unhappy departures: 0
RESULT: VICTORY
```

Revenue: $9.00 + $13.50 + $18.00 + $29.70 + $19.44 = $89.64. Average: (92 + 95 + 84 + 82 + 84) / 5 = 87.4.

**How the Happy Hour choice changes the result:** the same keystrokes with `0` instead of `1` at the turn-13 Happy Hour prompt give **$94.50 and an average of 79.8**. Without the recovery, the Critic pays at 52 instead of 82, and C5 pays at 76 instead of 84. Happy Hour trades $4.86 of revenue (C5's bill) for 7.6 points of average satisfaction.

**A constrained decision on turn 13:** the Waiter can serve the Critic's READY combo or take C5's order, not both. Taking C5's order first is what lets C5's 3-unit combo be cooked on turns 13–15 and paid on turn 18. The Host is what makes it possible to seat C5 and take the order in the same turn.

---

## 2. Defeat — `runs/defeat.txt`

**Strategy:** every choice is `0` (72 lines): Happy Hour is never used, and the Host, Waiter and Chef wait every turn. Nobody is ever seated, so every customer keeps losing satisfaction at their own type's rate.

| Customer | Type (loss per turn) | Outcome |
|---|---|---|
| C1 | Regular (8) | Reaches 0 and **leaves at the end of turn 13** |
| C2 | VIP (5) | Still waiting at **25** when the game ends |
| C3 | Regular (8) | Still waiting at 4 |
| C4 | Critic (12) | Arrives on turn 10, reaches 0 after 9 turns and **leaves at the end of turn 18** |
| C5, C6 | Regular (8) | Still waiting at 52 and 76 |

**Actual final output:**
```
============================== GAME OVER ==============================
  [NOT MET] Paid customers                  0  (target >= 4)
  [NOT MET] Revenue                     $0.00  (target >= $45.00)
  [NOT MET] Avg satisfaction (paid)       N/A  (target >= 60)
Served: 0 | Unhappy departures: 2
RESULT: DEFEAT
```

The outcome is the same as Stage 2, as expected: the Host and Happy Hour are never used.

---

## 3. Rule checks — `runs/rule-checks.txt`

This run makes illegal choices on purpose. Each one must be refused with a reason and must not use up the staff member's task (brief Section 3.4). After the last input, the remaining choices count as Wait, so the game ends in DEFEAT (C1 leaves at the end of turn 17, C4 at the end of turn 18).

| Turn | Who | Choice | Actual response |
|---|---|---|---|
| 1 | Waiter | `abc`, then `9` | `Please enter a number from 0 to 3.` (twice), then asks again |
| 1 | Waiter | Take an order from C1, who is still in the queue | `Invalid: C1 is WAITING; an order can only be taken from a seated customer who has not ordered yet. Choose again (the Waiter's task is not used up).` |
| 2 | Waiter | Serve C1 before any order exists | `Invalid: C1 is SEATED and is not waiting for food. Choose again (the Waiter's task is not used up).` |
| 3 | Chef | Cook C1's order, which is already READY | `Invalid: Order for C1 is READY; only PLACED or IN_PREPARATION orders can be prepared. Choose again (the Chef's task is not used up).` |
| 4 | **Host** | Seat C2 at T1, which C1 occupies | `Invalid: T1 is occupied by C1. Choose again (the Host's task is not used up).` |
| 4 | Waiter | Seat C2 at T1, which C1 occupies | `Invalid: T1 is occupied by C1. Choose again (the Waiter's task is not used up).` |
| 5 | **Manager** | Activate Happy Hour again (it was switched on at turn 4) | `Invalid: Happy Hour has already been used this game. Choose again.` |
| 5 | **Host** | Seat C2, who was already seated on turn 4 | `Invalid: C2 is not in the waiting queue. Choose again (the Host's task is not used up).` |
| 5 | Waiter | Seat C2 again | `Invalid: C2 is not in the waiting queue. Choose again (the Waiter's task is not used up).` |

In every case, the same staff member (or the manager) is asked again in the same turn and makes a valid choice. On turn 5 the Waiter then takes C2's order during Happy Hour: `Bill locked at $10.80 (list $15.00)`. The input file ends during turn 6.

---

## 4. Stage 3 feature checks — `runs/stage3-checks.txt`

A short scripted game that shows the Stage 3 rules in the brief's own terms. Nobody is seated on turns 1–3, so C1 is waiting at 76 when Happy Hour starts. After turn 7 the input runs out and the game ends in DEFEAT (C2 pays $10.80 on turn 9).

| Turn | Choices | Actual output |
|---|---|---|
| 4 | Activate Happy Hour | `OK: Happy Hour is ACTIVE: 2 turns left (this turn and next), 20% off orders taken. Recovered +15 (max 100): C1 -> 91, C2 -> 100.` |
| 4 | Host seats C1 → T1, **Waiter seats C2 → T2** | `OK: Host seats C1 at T1.` then `OK: Waiter seats C2 at T2.` Two customers seated in one turn. |
| 5 | (start of turn) | `Happy Hour: ACTIVE: last turn, 20% off orders taken; customers waiting for a table or food recovered 15 at the start of this turn`. C1 83 → 98, C2 95 → 100. |
| 5 | Waiter takes C2's order | `OK: C2 orders Burger (1 prep unit). Bill locked at $10.80 (list $15.00).` ($15.00 × 0.9 × 0.8, the brief's example) |
| 6 | (start of turn) | `Happy Hour: used` |
| 6 | Waiter takes C1's order | `Bill locked at $9.00.` Happy Hour is over, so full price. |
| 7–9 | Serve C2; C2 eats and pays | C2's bill stays `$10.80 (list $15.00)` after Happy Hour ends; `Cashier collects $10.80 from C2`. |

---

## 5. Behaviour checks

All of these are visible in the runs above.

| Check | Where it shows | Result |
|---|---|---|
| **Host** seats at most one queued customer per turn, before the Waiter | Victory: the Host seats C1–C6; each time, the Waiter takes the order in the same turn | ✅ |
| **Waiter can still seat** | Stage 3 checks turn 4: Host seats C1 and Waiter seats C2 | ✅ |
| Host follows the same seating rules | Rule checks turns 4–5: occupied table and already-seated customer refused, Host's task not used up | ✅ |
| **Combo price** = 90% of the parts, units = sum | Menu: Salad + Burger $21.60 (2 units), Salad + Pasta $24.30 (3 units), Burger + Pasta $29.70 (3 units) | ✅ |
| **Combo choice rule** | Victory: C5 (rotation) orders Salad + Pasta; C4 (Critic, most elaborate) orders Burger + Pasta | ✅ |
| A combo is cooked like any dish | Victory: C4's and C5's combos take 3 Chef turns each (1/3, 2/3, READY) | ✅ |
| **Happy Hour** once per game | Rule checks turn 5: second activation refused | ✅ |
| Lasts that turn and the next; remaining duration shown | Stage 3 checks: `2 turns left` → `last turn` → `used` | ✅ |
| 20% off orders taken while active, after the VIP discount | Stage 3 checks and rule checks: VIP Burger `$10.80 (list $15.00)`. Victory: C5's combo `$19.44 (list $24.30)` | ✅ |
| Recovery of 15 at the start of each active turn, max 100 | Victory turns 13–14: C4 64 → 79 and 67 → 82; C5 92 → 100 (capped) | ✅ |
| Price locked when the order is taken | Victory: C4 ordered on turn 10, before Happy Hour, and still pays $29.70. Stage 3 checks: C2's $10.80 stays after Happy Hour ends | ✅ |
| Stage 2 behaviour unchanged | VIP loses 5 and gets 10% off; Critic loses 12 and pays full price; defeat outcome identical to Stage 2 | ✅ |
| There are still six arrivals | Turns 1, 4, 7, 10, 13 and 16 | ✅ |
