package restaurantrush.entity;

/**
 * A restaurant customer. The common visit flow (seat, order, eat, pay, leave)
 * lives here and every transition checks the current state. Behaviour that
 * varies by customer type is exposed as hooks so that subclasses can change it
 * without the controller checking types.
 */
public abstract class Customer {

    public static final int MAX_SATISFACTION = 100;

    private final int number;
    private final int arrivalTurn;
    private int satisfaction;
    private CustomerState state;
    private Order order;
    private int servedTurn;
    private int readyToPayTurn;
    private int paidSatisfaction;

    protected Customer(int number, int arrivalTurn) {
        this.number = number;
        this.arrivalTurn = arrivalTurn;
        this.satisfaction = MAX_SATISFACTION;
        this.state = CustomerState.WAITING;
    }

    // ---- Type-specific behaviour -------------------------------------------------------

    /**
     * The customer picks their own dish; the manager never chooses it.
     * Default rule (deterministic): customer n orders menu item (n - 1) mod menuSize,
     * so with the base menu C1 Salad, C2 Burger, C3 Pasta, C4 Salad, C5 Burger, C6 Pasta.
     * Customer types with their own preference override this.
     */
    public MenuItem chooseDish(Menu menu) {
        return menu.get((number - 1) % menu.size());
    }

    /** Satisfaction lost for each turn spent waiting for a table or food. */
    public abstract int getWaitingPenalty();

    /** The bill for a dish, in cents. Computed once, when the order is taken. */
    public abstract int computeBillCents(MenuItem item);

    /** Short label for the customer type, used in displays. */
    public abstract String getTypeName();

    /** One-line summary of how this customer type behaves, used in displays. */
    public abstract String describeBehaviour();

    // ---- Visit flow --------------------------------------------------------------------

    public void seat() throws InvalidActionException {
        if (state != CustomerState.WAITING) {
            throw new InvalidActionException(getId() + " is " + state + " and cannot be seated.");
        }
        state = CustomerState.SEATED;
    }

    /** Takes this customer's order; the dish and price are decided by the customer. */
    public Order placeOrder(Menu menu) throws InvalidActionException {
        if (state != CustomerState.SEATED) {
            throw new InvalidActionException(getId() + " is " + state
                    + "; an order can only be taken from a seated customer who has not ordered yet.");
        }
        MenuItem item = chooseDish(menu);
        order = new Order(this, item, computeBillCents(item));
        state = CustomerState.AWAITING_FOOD;
        return order;
    }

    public void receiveFood(int turn) throws InvalidActionException {
        if (state != CustomerState.AWAITING_FOOD || order == null) {
            throw new InvalidActionException(getId() + " is " + state + " and is not waiting for food.");
        }
        order.markServed();
        state = CustomerState.EATING;
        servedTurn = turn;
    }

    /** A customer served on an earlier turn finishes eating and becomes ready to pay. */
    public boolean finishEatingIfDone(int turn) {
        if (state == CustomerState.EATING && servedTurn < turn) {
            state = CustomerState.READY_TO_PAY;
            readyToPayTurn = turn;
            return true;
        }
        return false;
    }

    /** True if the customer became ready to pay on an earlier turn. */
    public boolean canPay(int turn) {
        return state == CustomerState.READY_TO_PAY && readyToPayTurn < turn;
    }

    /** Pays the bill and records satisfaction at this moment. Returns the amount paid in cents. */
    public int pay(int turn) {
        if (!canPay(turn)) {
            throw new IllegalStateException(getId() + " cannot pay on turn " + turn + " (state " + state + ")");
        }
        order.markPaid();
        state = CustomerState.PAID;
        paidSatisfaction = satisfaction;
        return order.getPriceCents();
    }

    /** Only customers still waiting for a table or for food lose satisfaction. */
    public boolean isLosingSatisfaction() {
        return state == CustomerState.WAITING
                || state == CustomerState.SEATED
                || state == CustomerState.AWAITING_FOOD;
    }

    public void applyWaitingPenalty() {
        satisfaction = Math.max(0, satisfaction - getWaitingPenalty());
    }

    public boolean shouldLeave() {
        return satisfaction == 0 && isLosingSatisfaction();
    }

    /** Leaves without paying; any unfinished order is cancelled. */
    public void leave() {
        if (order != null) {
            order.cancel();
        }
        state = CustomerState.LEFT;
    }

    // ---- Queries -----------------------------------------------------------------------

    public String getId() {
        return "C" + number;
    }

    public int getNumber() {
        return number;
    }

    public int getArrivalTurn() {
        return arrivalTurn;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerState getState() {
        return state;
    }

    /** The customer's order, or null if none has been taken yet. */
    public Order getOrder() {
        return order;
    }

    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    public int getPaidSatisfaction() {
        return paidSatisfaction;
    }

    public boolean isActive() {
        return state != CustomerState.PAID && state != CustomerState.LEFT;
    }
}
