package restaurantrush.entity;

/**
 * The Happy Hour pricing policy. The manager may switch it on once per game, at
 * the start of a turn, and it lasts for that turn and the next. While it is
 * active, orders taken get 20% off their bill (after any customer-type discount)
 * and, at the start of each active turn, customers still waiting for a table or
 * food recover 15 satisfaction.
 */
public class HappyHour {

    public static final int DURATION_TURNS = 2;
    public static final int RECOVERY = 15;
    private static final int BILL_PERCENT = 80;

    private boolean used;
    private int turnsLeft;

    /** Switches Happy Hour on for this turn and the next. Allowed once per game. */
    void activate() throws InvalidActionException {
        if (used) {
            throw new InvalidActionException("Happy Hour has already been used this game.");
        }
        used = true;
        turnsLeft = DURATION_TURNS;
    }

    /** Called at the end of every turn: one active turn is used up. */
    void endTurn() {
        if (turnsLeft > 0) {
            turnsLeft--;
        }
    }

    /** The bill after the Happy Hour discount, rounded half-up; unchanged when not active. */
    public int applyTo(int billCents) {
        return isActive() ? Money.percentOf(billCents, BILL_PERCENT) : billCents;
    }

    public boolean isActive() {
        return turnsLeft > 0;
    }

    public boolean isAvailable() {
        return !used;
    }

    /** Active turns left, counting the current one. */
    public int getTurnsLeft() {
        return turnsLeft;
    }

    public int getDiscountPercent() {
        return 100 - BILL_PERCENT;
    }
}
