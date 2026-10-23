package restaurantrush.entity;

/** A table holds at most one customer until they pay or leave. */
public class Table {

    private final int number;
    private Customer occupant;

    public Table(int number) {
        this.number = number;
    }

    public boolean isFree() {
        return occupant == null;
    }

    void seat(Customer customer) throws InvalidActionException {
        if (!isFree()) {
            throw new InvalidActionException(getId() + " is occupied by " + occupant.getId() + ".");
        }
        occupant = customer;
    }

    void free() {
        occupant = null;
    }

    public String getId() {
        return "T" + number;
    }

    /** The seated customer, or null if the table is free. */
    public Customer getOccupant() {
        return occupant;
    }
}
