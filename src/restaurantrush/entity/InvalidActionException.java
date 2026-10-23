package restaurantrush.entity;

/**
 * Thrown when a manager's choice breaks a game rule. The message is a
 * human-readable reason that the UI shows before asking again.
 */
public class InvalidActionException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidActionException(String reason) {
        super(reason);
    }
}
