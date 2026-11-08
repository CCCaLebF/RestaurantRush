 ▐▛███▛█   Claude Code v2.1.317
▝▜██████▀  Opus 5.5 · Claude Pro
 ▝▝   ▝▝   C:\NTU\NTU Y2S1\SC2002 OBJECT ORIENTED DES & PROG\SC2002 Assignm…


❯ <browser_instruction># Claude in Chrome browser automation                    
                                                                                
  You have access to browser automation tools (mcp__claude-in-chrome__*) for    
  interacting with web pages in Chrome. Follow these guidelines for effective   
  browser automation.                                                           
                                                                                
  ## Loading deferred tools                                                     
                                                                                
  If the mcp__claude-in-chrome__* tools are deferred (must be loaded via        
  ToolSearch before use), load every tool you expect to need in ONE ToolSearch  
  call — the select query accepts a comma-separated list — never one call per   
  tool. Start with the core set:                                                
                                                                                
  ToolSearch with query                                                         
  "select:mcp__claude-in-chrome__tabs_context_mcp,mcp__claude-in-chrome__naviga 
  te,mcp__claude-in-chrome__computer,mcp__claude-in-chrome__read_page,mcp__clau 
  de-in-chrome__tabs_create_mcp,mcp__claude-in-chrome__tabs_close_mcp"          
                                                                                
  Add task-specific tools to the same call when the task obviously needs them:  
  read_console_messages / read_network_requests for debugging, form_input for   
  forms, gif_creator for recordings, javascript_tool for page scripting.        
                                                                                
  ## GIF recording                                                              
                                                                                
  When performing multi-step browser interactions that the user may want to     
  review or share, use mcp__claude-in-chrome__gif_creator to record them.       
                                                                                
  You must ALWAYS:                                                              
  * Capture extra frames before and after taking actions to ensure smooth       
  playback                                                                      
  * Name the file meaningfully to help the user identify it later (e.g.,        
  "login_process.gif")                                                          
                                                                                
  ## Console log debugging                                                      
                                                                                
  You can use mcp__claude-in-chrome__read_console_messages to read console      
  output. Console output may be verbose. If you are looking for specific log    
  entries, use the 'pattern' parameter with a regex-compatible pattern. This    
  filters results efficiently and avoids overwhelming output. For example, use  
  pattern: "[MyApp]" to filter for application-specific logs rather than        
  reading all console output.                                                   
                                                                                
  ## Alerts and dialogs                                                         
                                                                                
  IMPORTANT: Do not trigger JavaScript alerts, confirms, prompts, or browser    
  modal dialogs through your actions. These browser dialogs block all further   
  browser events and will prevent the extension from receiving any subsequent   
  commands. Instead, when possible, use console.log for debugging and then use  
  the mcp__claude-in-chrome__read_console_messages tool to read those log       
  messages. If a page has dialog-triggering elements:                           
  1. Avoid clicking buttons or links that may trigger alerts (e.g., "Delete"    
  buttons with confirmation dialogs)                                            
  2. If you must interact with such elements, warn the user first that this may 
  interrupt the session                                                         
  3. Use mcp__claude-in-chrome__javascript_tool to check for and dismiss any    
  existing dialogs before proceeding                                            
                                                                                
  If you accidentally trigger a dialog and lose responsiveness, inform the user 
  they need to manually dismiss it in the browser.                              
                                                                                
  ## Avoid rabbit holes and loops                                               
                                                                                
  When using browser automation tools, stay focused on the specific task. If    
  you encounter any of the following, stop and ask the user for guidance:       
  - Unexpected complexity or tangential browser exploration                     
  - Browser tool calls failing or returning errors after 2-3 attempts           
  - No response from the browser extension                                      
  - Page elements not responding to clicks or input                             
  - Pages not loading or timing out                                             
  - Unable to complete the browser task despite multiple approaches             
                                                                                
  Explain what you attempted, what went wrong, and ask how the user would like  
  to proceed. Do not keep retrying the same failing browser action or explore   
  unrelated pages without checking in first.                                    
                                                                                
  ## Tab context and session startup                                            
                                                                                
  IMPORTANT: At the start of each browser automation session, call              
  mcp__claude-in-chrome__tabs_context_mcp first to get information about the    
  user's current browser tabs. Use this context to understand what the user     
  might want to work with before creating new tabs.                             
                                                                                
  Never reuse tab IDs from a previous/other session. Follow these guidelines:   
  1. Only reuse an existing tab if the user explicitly asks to work with it     
  2. Otherwise, create a new tab with mcp__claude-in-chrome__tabs_create_mcp    
  3. If a tool returns an error indicating the tab doesn't exist or is invalid, 
  call tabs_context_mcp to get fresh tab IDs                                    
  4. When a tab is closed by the user or a navigation error occurs, call        
  tabs_context_mcp to see what tabs are available</browser_instruction>         

❯ go through the code and identify the specific use cases for me to create      
  sequence diagram                                                              

● Failed to refresh OAuth token: another Claude Code process is 
  refreshing it or exited mid-refresh. This is usually transient; retry 
  in a minute, and if it persists close other Claude Code processes or 
  sign in again

❯ go through the code and identify the specific use cases for me to create      
  sequence diagram                                                              

  Ran 1 shell command (ctrl+o to expand)

● Small codebase; I'll read the core files.

  Ran 2 shell commands (ctrl+o to expand)

● I read all 27 classes. The game has a three-layer design (boundary, control,
  entity), and almost every action follows the same path:

  Manager → ConsoleUI → GameController → Staff (Waiter/Chef/Cashier) → 
  Restaurant → Customer/Order/Table/WaitingQueue/Ledger

  Below are the use cases worth drawing, with the actual method calls in order.
  Each line reads Caller → Callee: method().

  ---

  UC1: Start game (setup and game loop)

  Source: Main.java (src/restaurantrush/Main.java), ConsoleUI.java
  (src/restaurantrush/boundary/ConsoleUI.java)

  1. Main → GameConfig: base()
  2. Main → Menu: standard() → creates 3 × MenuItem
  3. Main → Restaurant: new(menu, tableCount) → creates Table × 2, WaitingQueue,
     Ledger, Waiter, Chef, Cashier
  4. Main → ArrivalSchedule: new(arrivalTurns, new CustomerFactory())
  5. Main → GameController: new(config, restaurant, schedule, new 
     ObjectiveEvaluator(config))
  6. Main → ConsoleUI: run() → printIntro()
  7. loop [!controller.isGameOver()]: ConsoleUI → ConsoleUI: playOneTurn()

  Use this as a high-level overview. It's mostly object creation.

  UC2: Start turn and customer arrival

  Source: GameController.java (src/restaurantrush/control/GameController.java)
  startTurn()

  1. ConsoleUI → GameController: startTurn()
  2. GameController → GameController: requirePhase(BETWEEN_TURNS), turn++
  3. GameController → Waiter / Chef / Cashier: resetForTurn()
  4. GameController → ArrivalSchedule: arrivalsFor(turn)
     - loop each arrivalTurn, opt [arrivalTurn == turn]: ArrivalSchedule → 
       CustomerFactory: create(n, turn) → «create» RegularCustomer
  5. loop each arrival: GameController → Restaurant: admit(c) →
     customers.add(c), Restaurant → WaitingQueue: enqueue(c)
  6. phase = WAITER, return arrivals
  7. ConsoleUI → StateFormatter: turnHeader() / arrival(c) / startOfTurn()

  UC3: Waiter seats a customer

  Source: ConsoleUI.seatCustomer() → Waiter.seat() → Restaurant.seat()

  1. Manager → ConsoleUI: choice 1
  2. ConsoleUI → ConsoleUI: pickCustomer() → Restaurant: getActiveCustomers() →
     readChoice()
  3. ConsoleUI → ConsoleUI: pickTable() → Restaurant: getTables() → readChoice()
     - opt [choice == 0]: return "back" and re-prompt
  4. ConsoleUI → GameController: waiterSeat(c, t) → requirePhase(WAITER)
  5. GameController → Waiter: seat(restaurant, c, t) → ensureAvailable()
  6. Waiter → Restaurant: seat(c, t)
     - Restaurant → WaitingQueue: contains(c). alt [false] throws
       InvalidActionException
     - Restaurant → Table: isFree(). alt [false] throws
     - Restaurant → Customer: seat(). alt [state != WAITING] throws
     - Restaurant → Table: seat(c)
     - Restaurant → WaitingQueue: remove(c)
  7. Waiter → Waiter: markActed(), GameController: phase = CHEF
  8. alt on exception: ConsoleUI prints "Invalid: …" and loops back to the
     Waiter menu

  UC4: Waiter takes an order (shows polymorphism)

  Source: Customer.placeOrder(), RegularCustomer

  1. ConsoleUI → GameController: waiterTakeOrder(c)
  2. GameController → Waiter: takeOrder(restaurant, c) → ensureAvailable()
  3. Waiter → Restaurant: takeOrder(c)
  4. Restaurant → Customer: placeOrder(menu). alt [state != SEATED] throws
     - Customer → RegularCustomer: chooseDish(menu) (abstract hook) → Menu: 
       get((n-1) % size())
     - Customer → RegularCustomer: computeBillCents(item) (abstract hook) →
       MenuItem: getPriceCents()
     - Customer → Order: «create» new Order(this, item, price) (the bill is
       fixed here)
     - state = AWAITING_FOOD
  5. Return the Order up the chain, then Waiter: markActed()
  6. ConsoleUI → Order / MenuItem: getItem(), getName(), getPrepUnits(),
     Money.format(getPriceCents())

  UC5: Chef prepares an order

  Source: ConsoleUI.chooseChefTask(), Chef.prepare(), Order.prepareOneUnit()

  1. ConsoleUI → Restaurant: getOpenOrders()
  2. loop: ConsoleUI → StateFormatter: orderLine(o), then readChoice()
  3. alt [choice == 0]: GameController: chefWait() → Chef: waitTurn()
  4. else: ConsoleUI → GameController: chefPrepare(order) → requirePhase(CHEF)
     - GameController → Chef: prepare(order) → ensureAvailable()
     - Chef → Order: prepareOneUnit(). alt [!isCookable()] throws (e.g. the
       order is already READY)
     - unitsDone++. alt [unitsDone >= prepUnits] → READY, else IN_PREPARATION
     - Chef: markActed(), phase = RESOLVE
  5. ConsoleUI → Order: describeStatus()

  UC6: Waiter serves a READY order (good for showing rule rejection)

  Source: Restaurant.serve(), Customer.receiveFood(), Order.markServed()

  1. ConsoleUI → GameController: waiterServe(c)
  2. GameController → Waiter: serve(restaurant, c, turn) → ensureAvailable()
  3. Waiter → Restaurant: serve(c, turn)
  4. Restaurant → Customer: receiveFood(turn). alt [state != AWAITING_FOOD]
     throws
     - Customer → Order: markServed(). alt [status != READY] throws
       InvalidActionException
     - state = EATING, servedTurn = turn
  5. Restaurant → Ledger: recordServed()
  6. alt on exception: ConsoleUI shows the reason and the Waiter's task isn't
     used up, so the player picks again

  UC7: End of turn: payment, eating, waiting penalty, departure

  Source: GameController.endTurn(). This is the richest diagram and may need
  splitting into 7a–7c.

  7a. Cashier collects a payment (automatic)
  1. ConsoleUI → GameController: endTurn() → requirePhase(RESOLVE)
  2. GameController → Cashier: processPayment(restaurant, turn)
     - opt [hasActedThisTurn()] returns empty
     - Cashier → Restaurant: getPayableCustomers(turn) → loop Customer: 
       canPay(turn), sorted by readyToPayTurn and then arrivalTurn
     - opt [list not empty]: Cashier → Restaurant: collectPayment(first, turn)
       - Restaurant → Customer: pay(turn) → Order: markPaid(), state = PAID
       - Restaurant → Ledger: recordPayment(amount, paidSatisfaction)
       - Restaurant → Restaurant: freeTableOf(c) → findTableOf(c) → Table: 
         free()
       - Cashier: markActed()

  7b. Customers finish eating
  3. loop Restaurant.getActiveCustomers(): GameController → Customer: 
  finishEatingIfDone(turn). opt [EATING && servedTurn < turn] → READY_TO_PAY

  7c. Waiting penalty and abandonment
  4. loop active customers, opt [isLosingSatisfaction()]:
  - GameController → Customer: applyWaitingPenalty() → getWaitingPenalty()
    (polymorphic, 8 for Regular)
  - opt [shouldLeave()]: GameController → Restaurant: depart(c)
    - Restaurant → Customer: leave() → opt [order != null] Order: cancel()
    - Restaurant → WaitingQueue: remove(c), freeTableOf(c), Ledger: 
      recordDeparture()

  7d. Turn report and game result
  5. alt [turn >= maxTurns]: GameController → ObjectiveEvaluator: 
  evaluate(ledger) → Ledger: getPaidCount(), getRevenueCents(), 
  averageSatisfactionAtLeast(60) → «create» GameResult, phase = GAME_OVER. else
  phase = BETWEEN_TURNS
  6. GameController → TurnReport: «create»
  7. ConsoleUI → StateFormatter: endOfTurn(report), then opt [finalResult 
  present] finalResult(result)

  ---

  Which ones to draw

  If you can only include 3–4 diagrams, pick these:

  ┌─────────────┬───────────────────────────────────────────────────────────┐
  │   Diagram   │                    Why it scores well                     │
  ├─────────────┼───────────────────────────────────────────────────────────┤
  │ UC4 Take    │ It shows polymorphism: Customer calls its abstract hooks  │
  │ order       │ chooseDish and computeBillCents, and RegularCustomer      │
  │             │ supplies them.                                            │
  ├─────────────┼───────────────────────────────────────────────────────────┤
  │ UC6 Serve   │ It shows validation inside the entities and the alt path  │
  │ order       │ where an invalid move is rejected and retried.            │
  ├─────────────┼───────────────────────────────────────────────────────────┤
  │ UC7 End of  │ It shows automatic behaviour, loop/opt fragments,         │
  │ turn        │ departures and the win check.                             │
  ├─────────────┼───────────────────────────────────────────────────────────┤
  │ UC3 Seat    │ It's the clearest example of the full layer chain, with   │
  │ customer    │ three separate rule checks.                               │
  └─────────────┴───────────────────────────────────────────────────────────┘

  Tips:
  - Put the Manager (the user) as the actor on the far left.
  - Show ConsoleUI as «boundary», GameController as «control», and the rest as
    «entity».
  - Skip the plain "Wait" actions. They're trivial.
  - requirePhase() and ensureAvailable() can be shown as self-calls once and
    then left out to keep the diagrams clean.

  I can turn any of these into PlantUML or Mermaid code for you to paste into a
  diagram tool.

❯ save these use cases in a md file.                                            

● Write(C:\NTU\NTU Y2S1\SC2002 OBJECT ORIENTED DES & PROG\SC2002
       Assignment\RestaurantGame\docs\sequence-diagram-use-cases.md)
  ⎿  Wrote 151 lines to C:\NTU\NTU Y2S1\SC2002 OBJECT ORIENTED DES & 
     PROG\SC2002 
     Assignment\RestaurantGame\docs\sequence-diagram-use-cases.md
      1 # Sequence Diagram Use Cases — Restaurant Rush (Stage 1)
      2 
      3 The code follows a Boundary–Control–Entity design. Almost every a
        ction flows:
      4 
      5 **Manager → `ConsoleUI` → `GameController` → Staff (`Waiter`/`Che
        f`/`Cashier`) → `Restaurant` → `Customer`/`Order`/`Table`/`Waitin
        gQueue`/`Ledger`**
      6 
      7 Each step below is written as `Caller → Callee: method()`, in cal
     … +145 lines (~8 KB) (ctrl+o to expand)

● I saved all seven use cases, plus the recommendations table and the tips, to
  docs/sequence-diagram-use-cases.md.

  I put it in a new docs/ folder at the project root so it sits apart from the
  source code. It isn't committed yet.

❯ Create an agent to explore all classes in detail and write the details into   
  an md file. Then create another agent to draw out a rough class diagram in a  
  separate environment to show how all classes are related to one another.      
                                                                                
  You will act as the orchestrator agent                                        

● I'll run them one after the other. The explorer goes first, and the diagram
  agent then works from its notes in an isolated git worktree.

● Agent(Document all classes in detail)
  ⎿  Done (7 tool uses · 106.9k tokens · 4m 18s)
  (ctrl+o to expand)

› Message from general-purpose
  I've written the class notes to `C:\NTU\NTU Y2S1\SC2002 OBJECT ORIENTED DES &
  PROG\SC2002 Assignment\RestaurantGame\docs\class-details.md`. I read all 27
  .java files and README.md in full. No source files were changed and nothing
  was committed.

  **Types documented: 28.** That is 27 top-level classes, abstract classes,
  enums and exceptions, plus the private nested enum `GameController.Phase`
  (`BETWEEN_TURNS`, `WAITER`, `CHEF`, `RESOLVE`, `GAME_OVER`). The file has all
  five sections you asked for: overview, one section per class, the consolidated
  relationship table, enums with their transitions, and OOP principles with
  references to specific classes and methods.

  **Ambiguous points** (the file notes each one):
  1. **`Menu` to `MenuItem`:** I recorded this as aggregation (1 to 1..*). The
  public constructor takes items made elsewhere, and `Order` shares the same
  `MenuItem` objects. Composition is also defensible, because `Menu.standard()`
  creates the items itself.
  2. **`Restaurant` to `Table`:** `Restaurant` creates the tables, so it is
  composition. The number comes from `tableCount`, which can be any int, so the
  general multiplicity is `*`. The base config gives 2. Your example `2..*`
  holds only for the base config.
  3. **`Restaurant` to `Menu` and `Customer`:** I marked both as aggregation.
  The menu is passed into the constructor, and customers are created by
  `CustomerFactory` and then added with `admit()`.
  4. **`Customer` and `Order`:** `Customer.placeOrder` creates the `Order`, so
  that side is composition 1 to 0..1. `Order` also keeps a link back to its
  `Customer`, so draw one line that can be followed both ways.
  5. **Control classes:** `GameController`, `ArrivalSchedule`,
  `ObjectiveEvaluator` and `ConsoleUI` get their collaborators through
  constructors. That is association or aggregation, not composition. The one
  exception is `ConsoleUI`, which creates its own `Scanner`.
  6. **Package dependency loop:** `entity.Restaurant` owns the `entity.staff`
  objects, and the staff classes take `Restaurant` as a method parameter. So
  those two packages depend on each other.
  7. **`Cashier` breaks the staff pattern:** the other staff call
  `ensureAvailable()`, which throws an exception if they have already acted.
  `Cashier` checks `hasActedThisTurn()` instead and returns an empty `Optional`,
  so it never throws.
  8. **Exceptions:** `Customer.pay`, `Order.markPaid` and
  `GameController.requirePhase` throw `IllegalStateException` rather than
  `InvalidActionException`. These cover programming errors, not manager
  mistakes.
  9. **"FIFO" queue:** `WaitingQueue` is described as first-in, first-out, but
  the manager can seat any queued customer, because `remove` takes any element.
  10. **Unused members:** `GameController.getResult()`, `TurnReport.getTurn()`
  and `Restaurant.getAllCustomers()` are never called. Also,
  `Restaurant.activeCustomersWithState` filters by state only and never calls
  `isActive()`, despite its name. This is harmless because it is only called
  with active states.
  11. **Stereotypes:** `GameResult` and `TurnReport` sit in `control` but are
  really result data objects. `Money` is a utility class inside `entity`. I
  labelled them by package and said what they actually are.

● The class notes are done. Next I'm starting the diagram agent in an isolated
  worktree, working from those notes.

● Agent(Draw rough class diagram)
  ⎿  Done (11 tool uses · 87.7k tokens · 2m 52s)
  (ctrl+o to expand)

› Message from general-purpose
  I wrote the class diagram to `C:\NTU\NTU Y2S1\SC2002 OBJECT ORIENTED DES &
  PROG\SC2002 Assignment\RestaurantGame\.claude\worktrees\agent-a77729e71eab973d
  3\docs\class-diagram.md`. Mermaid CLI isn't installed or cached on this
  machine, so I couldn't render it to check the syntax. Instead, a script
  checked that each of the 27 Mermaid classes is defined and appears in at least
  one relationship, and both checks passed. No source files were changed and
  nothing was committed.

  - **Diagrams:** the file has a Mermaid `classDiagram` grouped into namespaces
  `app` (for `Main`), `boundary`, `control`, `entity` and `entity_staff`. It has
  stereotypes, about 3-6 key members per class, and multiplicities on every
  structural relationship. The same diagram is also there as a PlantUML block
  using the real package names, plus a note that the base config gives 2 tables.
  - **Your decisions are all followed:** `Restaurant` has its six compositions
  (`Table` is 1 to *) and aggregates `Menu` and `Customer`. `Customer` *--
  `Order` is one line, 1 to 0..1, labelled `order / customer`. `Menu` o--
  `MenuItem` is 1 to 1..*, and both inheritance trees are drawn. The control
  classes' injected collaborators are drawn as associations.
  `InvalidActionException` has dashed lines labelled throws, propagates or
  catches, and the two enums are linked to their owners by association.
  - **Spot-check:** the notes match the source for the fields of `Restaurant`,
  `Customer`, `Order` and `GameController`. I found nothing off.
  - **The file also includes:** a legend of the arrow types, a "how to read it"
  paragraph, and a list of simplifications.
  - **Main simplifications:**
    - Mermaid only shows one stereotype per class, so `Customer` and `Staff`
  show `<<abstract>>`, not `<<entity>>`.
    - The private nested enum `Phase` appears only as an attribute of
  `GameController`, not as its own box.
    - The three `TurnReport` → `Customer` fields are merged into one line.
    - Most boundary → entity read-only dependencies are left out.
    - JDK types are left out.
    - Mermaid can't put a dot in a namespace name, so `entity.staff` is written
  `entity_staff`.

● The diagram is done. I'll check it and copy it from the worktree into your
  project's docs/ folder.

● Bash(cd "C:/NTU/NTU Y2S1/SC2002 OBJECT ORIENTED DES & PROG/SC2002
      Assignment/RestaurantGame" && cp
      ".claude/worktrees/agent-a77729e71eab973d3/docs/class-diagram.md"…)
  ⎿   1102 docs/class-details.md
       595 docs/class-diagram.md
       151 docs/sequence-diagram-use-cases.md
     … +143 lines (ctrl+o to expand)
  ⎿  1 file changed (binary, mode only or too large to show)

● I had two agents work on this, and both files are now in your project's docs/
  folder:

  - docs/class-details.md (about 1,100 lines): the first agent read all 27
    source files and documented 28 types. That count includes the private Phase
    enum nested inside GameController. Each class gets its attributes and
    methods with UML visibility, its relationships, and notes on its design. The
    file ends with one combined table of every relationship and a section on
    which OOP principles the code shows.
  - docs/class-diagram.md: the second agent drew a rough class diagram from
    those notes in a separate copy of the project (a git worktree), and I copied
    the result back. It contains the diagram in Mermaid syntax, grouped by
    package, with stereotypes and multiplicities. It also has the same diagram
    in PlantUML, a key to the arrow types, and a list of what was simplified.

  Relationship choices you can defend either way. The first agent flagged these,
  and I had the diagram follow them:
  - Menu to MenuItem is drawn as aggregation. Composition is also arguable,
    because Menu.standard() creates the items itself.
  - Restaurant to Table is drawn as composition with multiplicity 1 to *,
    because the table count comes from the config. The base config gives 2.
  - Customer to Order is composition, 1 to 0..1, drawn as one line you can
    follow both ways, since Order keeps a link back to its Customer.
  - Restaurant holds the three staff objects, and the staff methods take
    Restaurant as a parameter. So entity and entity.staff depend on each other
    in a loop, which a marker might point out.

  Before you hand this in:
  - The diagram hasn't been rendered. The Mermaid tools aren't installed here,
    so its syntax wasn't checked. A script did confirm that every class is
    defined and appears in at least one relationship. You can paste it into
    mermaid.live or a PlantUML viewer to check it.
  - Mermaid limits: a class can show only one stereotype, so Customer and Staff
    are marked <<abstract>> rather than <<entity>>. Mermaid also can't put a dot
    in a package name, so entity.staff appears as entity_staff. The PlantUML
    version uses the real package names.
  - Cleanup: the worktree is still at
    .claude/worktrees/agent-a77729e71eab973d3/. It and docs/ both show as
    untracked in git, and nothing has been committed. I can delete the worktree
    and add .claude/ to .gitignore if you'd like.

❯ how do i visualise this mermaid diagram                                       

● You're in VS Code and you already use Obsidian, so the easiest options are:

  1. VS Code (where you are now)
  - VS Code's built-in Markdown preview doesn't draw Mermaid diagrams, so
    install the Markdown Preview Mermaid Support extension
    (bierner.markdown-mermaid).
  - Open docs/class-diagram.md and press Ctrl+Shift+V, or Ctrl+K V to open the
    preview side by side.

  2. Obsidian (nothing to install)
  - Obsidian draws ```mermaid blocks on its own. Copy the file into your SC2002
    vault folder and switch to Reading view.

  3. mermaid.live (in a browser, easiest for exporting)
  - Go to https://mermaid.live and paste in everything inside the ```mermaid
    block, without the fences.
  - The diagram appears straight away, and from the Actions menu you can export
    it as PNG or SVG for your report.
  - If the syntax has errors, this is where you'll see them.

  4. GitHub
  - If you push the repo to GitHub, .md files show Mermaid diagrams
    automatically.

  For the PlantUML version, paste the @startuml … @enduml block into
  https://www.plantuml.com/plantuml. In VS Code, the PlantUML extension
  (jebbs.plantuml) previews it with Alt+D, but it needs Java.

  The diagram has 27 classes, so it'll be wide. Use direction LR (already set)
  and zoom out, or try changing it to direction TB if the layout looks cramped.

  I can also turn it into a web page here that draws the diagram and lets you
  zoom, if you'd like.