package restaurantrush.entity.staff;

import restaurantrush.entity.InvalidActionException;

/**
 * A staff member who can complete at most one task per turn. A task only
 * counts as used once it succeeds, so a rejected choice can be retried.
 */
public abstract class Staff {

    private final String name;
    private boolean actedThisTurn;

    protected Staff(String name) {
        this.name = name;
    }

    /** One-line description of what this role does each turn. */
    public abstract String describeDuty();

    public void resetForTurn() {
        actedThisTurn = false;
    }

    /** The manager chooses to leave this staff member idle for the turn. */
    public void waitTurn() throws InvalidActionException {
        ensureAvailable();
        markActed();
    }

    protected void ensureAvailable() throws InvalidActionException {
        if (actedThisTurn) {
            throw new InvalidActionException(name + " has already completed a task this turn.");
        }
    }

    protected void markActed() {
        actedThisTurn = true;
    }

    public boolean hasActedThisTurn() {
        return actedThisTurn;
    }

    public String getName() {
        return name;
    }
}
