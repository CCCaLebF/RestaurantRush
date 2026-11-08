# SC2002 Restaurant Rush: Project Summary

## Question

> could you tell me what is the project about

## Answer

A group assignment where your team builds a **turn-based restaurant management game in Java**, played through a command-line interface. You play the manager of a small restaurant with limited staff, and each turn you decide who gets served first.

## The core game

- Runs for **18 turns** with **2 tables** and three staff: 1 Waiter, 1 Chef, 1 Cashier.
- **Six customers** arrive over the game (turns 1, 4, 7, 10, 13, 16).
- Each turn you choose what the Waiter does (seat a customer, take an order, or serve a ready dish) and what the Chef cooks. Each can do only one thing per turn.
- Customers pick their own food (Salad $9, Burger $15, Pasta $18). Their satisfaction drops each turn they wait, and at 0 they leave without paying.
- **To win:** at least 4 paid customers, at least $45 revenue, and average satisfaction of paid customers at least 60.

## Three stages (each a tagged, runnable version in GitHub)

| Stage | Tag | Adds |
| --- | --- | --- |
| 1 | `stage-1` | Regular customers, menu, Waiter, Chef, Cashier, full turn sequence, victory and defeat endings |
| 2 | `stage-2` | VIP customers (lose 5 satisfaction per turn, 10% off) and Critic customers (lose 12 per turn, plus a special behaviour you define) |
| 3 | `stage-3` | Host (seats customers), ComboMeal (two items bundled at 10% off), Happy Hour pricing policy |

## What the assignment is really testing

Object-oriented design more than the game itself. You need to show how encapsulation, inheritance, polymorphism, composition and SOLID principles handle each new stage with minimal rewriting. For example:

- Customer-specific behaviour is dispatched through customer objects, not type checks in the turn controller.
- Game rules stay out of the UI.

## What you submit (one zip per group)

- Java source code with build and run instructions
- README with repo URL, stage-to-stage comparisons and video timestamps
- PDF report with class diagram(s), sequence diagram, design discussion and reflections
- Recorded video of about 15 minutes, with each member explaining a part
- Signed declarations (original work, and GAI usage per member)

## Marking (100 total)

| Criterion | Marks |
| --- | --- |
| UML class diagram | 25 |
| UML sequence diagram | 20 |
| Design consideration | 15 |
| Implementation code | 20 |
| Thinking and development process | 20 |

No live demo: everything is graded from your submitted files, GitHub repository and video.

## From the briefing slides

- **Due:** Sunday, 15 November 2026, 11:59 PM (SGT)
- Submit **3 class diagrams** (one per stage) and **1 sequence diagram** (a complex use case from Stage 2 or Stage 3).
- Upload your AI chat history to your own branch (e.g. `ai-history/stage-1/chat-01.md`).
- The report includes a separate individual reflection section per member.
