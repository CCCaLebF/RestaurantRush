# Test Notes — Stage 2

Every run below uses the required base setting (brief Section 2.1) with the Stage 2 arrival schedule, and replays a saved input file. The results are deterministic, so the same file always gives the same game.

| Customer | Arrives | Type | Dish (rule) | Bill |
|---|---|---|---|---|
| C1 | Turn 1 | Regular | Salad (rotation) | $9.00 |
| C2 | Turn 4 | **VIP** | Burger (rotation) | **$13.50** (list $15.00, 10% off) |
| C3 | Turn 7 | Regular | Pasta (rotation) | $18.00 |
| C4 | Turn 10 | **Critic** | **Pasta** (most elaborate dish; the rotation would give Salad) | $18.00 |
| C5 | Turn 13 | Regular | Burger (rotation) | $15.00 |
| C6 | Turn 16 | Regular | Pasta (rotation) | $18.00 |

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

Replace `victory` with `defeat` or `rule-checks` for the other runs. `--echo` prints each input line so the transcript reads like a live game. If an input file runs out, every remaining choice counts as `0` (Wait).

---

## 1. Victory — `runs/victory.txt`

**Strategy:** seat a customer, then take their order and cook it in the same turn when possible. Serve a READY dish before taking a new order. The Critic is the hard decision. Their Pasta needs two Chef turns while they lose 12 satisfaction per turn, so on turn 14 the Waiter serves the Critic **before** taking C5's order. Doing it the other way round would leave the Critic at 40 when paying instead of 52.

| Turn | Waiter | Chef | Step 4 (eating and payment) | End-of-turn revenue / average |
|---|---|---|---|---|
| 1 | Seat C1 → T1 | Wait | | $0.00 / N/A |
| 2 | Take C1's order (Salad $9.00) | Cook C1 → READY | | $0.00 / N/A |
| 3 | Serve C1 | Wait | | $0.00 / N/A |
| 4 | Seat **C2 (VIP)** → T2 | Wait | C1 → READY_TO_PAY | $0.00 / N/A |
| 5 | Take C2's order (Burger **$13.50 (list $15.00)**) | Cook C2 → READY | C1 pays $9.00 (satisfaction 84) | $9.00 / 84.0 |
| 6 | Serve C2 | Wait | | $9.00 / 84.0 |
| 7 | Seat C3 → T1 | Wait | C2 → READY_TO_PAY | $9.00 / 84.0 |
| 8 | Take C3's order (Pasta $18.00) | Cook C3 1/2 | C2 pays **$13.50** (90) | $22.50 / 87.0 |
| 9 | Wait | Cook C3 2/2 → READY | | $22.50 / 87.0 |
| 10 | Serve C3 (**C4 Critic** waits in the queue) | Wait | | $22.50 / 87.0 |
| 11 | Seat C4 → T2 | Wait | C3 → READY_TO_PAY | $22.50 / 87.0 |
| 12 | Take C4's order (**Pasta** $18.00) | Cook C4 1/2 | C3 pays $18.00 (76) | $40.50 / 83.3 |
| 13 | Seat C5 → T1 | Cook C4 2/2 → READY | | $40.50 / 83.3 |
| 14 | **Serve C4** | Wait | | $40.50 / 83.3 |
| 15 | Take C5's order (Burger $15.00) | Cook C5 → READY | C4 → READY_TO_PAY | $40.50 / 83.3 |
| 16 | Serve C5 | Wait | C4 pays $18.00 (52) | $58.50 / 75.5 |
| 17 | Seat C6 → T2 | Wait | C5 → READY_TO_PAY | $58.50 / 75.5 |
| 18 | Take C6's order (Pasta $18.00) | Cook C6 1/2 | C5 pays $15.00 (76) | $73.50 / 75.6 |

C6 arrives on turn 16 and cannot finish paying within 18 turns. The brief intends this.

**Actual final output:**
```
============================== GAME OVER ==============================
  [MET] Paid customers                  5  (target >= 4)
  [MET] Revenue                    $73.50  (target >= $45.00)
  [MET] Avg satisfaction (paid)      75.6  (target >= 60)
Served: 5 | Unhappy departures: 0
RESULT: VICTORY
```

Revenue: $9.00 + $13.50 + $18.00 + $18.00 + $15.00 = $73.50. Average: (84 + 90 + 76 + 52 + 76) / 5 = 75.6.

---

## 2. Defeat — `runs/defeat.txt`

**Strategy:** the Waiter and the Chef wait on every turn (36 × `0`). Nobody is ever seated, so every customer keeps losing satisfaction at their own type's rate.

| Customer | Type (loss per turn) | Outcome |
|---|---|---|
| C1 | Regular (8) | Reaches 0 and **leaves at the end of turn 13** |
| C2 | VIP (5) | Still waiting at **25** when the game ends. The slower loss keeps the VIP in the restaurant. |
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

---

## 3. Rule checks — `runs/rule-checks.txt`

This run makes illegal choices on purpose. Each one must be refused with a reason and must not use up the staff member's task (brief Section 3.4). After the last input, the remaining choices count as Wait, so the game ends in DEFEAT.

| Turn | Choice | Actual response |
|---|---|---|
| 1 | `abc`, then `9` at the Waiter menu | `Please enter a number from 0 to 3.` (twice), then asks again |
| 1 | Take an order from C1, who is still in the queue | `Invalid: C1 is WAITING; an order can only be taken from a seated customer who has not ordered yet. Choose again (the Waiter's task is not used up).` |
| 2 | Serve C1 before any order exists | `Invalid: C1 is SEATED and is not waiting for food. Choose again (the Waiter's task is not used up).` |
| 3 | Chef cooks C1's order, which is already READY | `Invalid: Order for C1 is READY; only PLACED or IN_PREPARATION orders can be prepared. Choose again (the Chef's task is not used up).` |
| 4 | Seat C2 at T1, which C1 occupies | `Invalid: T1 is occupied by C1. Choose again (the Waiter's task is not used up).` |
| 5 | Seat C2 again after C2 was already seated | `Invalid: C2 is not in the waiting queue. Choose again (the Waiter's task is not used up).` |

In every case, the same staff member is asked again in the same turn and makes a valid choice: a task on turns 1, 2, 4 and 5, and Wait on turn 3. This shows the rejected choice did not use up the task. The input file ends during turn 6.

---

## 4. Stage 2 behaviour checks

All of these are visible in the runs above.

| Check | Where it shows | Result |
|---|---|---|
| The VIP loses 5 per waiting turn | Victory: C2 is at 95 after turn 4 (seated, not served) and 90 after turn 5. Defeat: C2 ends at 25 after 15 waiting turns. | ✅ |
| The Critic loses 12 per waiting turn | Victory: C4 is at 88 after turn 10, then 76, 64, 52. Defeat: C4 leaves after 9 turns. | ✅ |
| The Regular still loses 8 per waiting turn | Victory: C1 is at 92 after turn 1 and 84 after turn 2 | ✅ |
| The VIP bill is 10% off and locked when the order is taken | `OK: C2 orders Burger (1 prep unit). Bill locked at $13.50 (list $15.00).` Revenue goes up by $13.50 on turn 8, not $15.00. | ✅ |
| The Critic orders the most elaborate dish | `OK: C4 orders Pasta (2 prep units).` The rotation rule would have given C4 a Salad. | ✅ |
| An unserved Critic abandons | Defeat: `C4 reached 0 satisfaction and LEFT without paying.` on turn 18 | ✅ |
| Each type's behaviour is shown when it arrives | e.g. `>> C2 (VIP) arrives and joins the queue (satisfaction 100). VIP: loses 5/turn while waiting, gets 10% off the bill.` | ✅ |
| Every type follows the same flow and rules | All three types go queue → seat → order → cook → READY → SERVED → eat → READY_TO_PAY → pay in the victory run. The rule-check rejections are the same as in Stage 1. | ✅ |
| There are still six arrivals | Turns 1, 4, 7, 10, 13 and 16 | ✅ |
