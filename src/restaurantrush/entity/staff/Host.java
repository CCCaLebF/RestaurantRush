package restaurantrush.entity.staff;

import restaurantrush.entity.Customer;
import restaurantrush.entity.InvalidActionException;
import restaurantrush.entity.Restaurant;
import restaurantrush.entity.Table;

/**
 * Seats at most one queued customer per turn, in addition to the Waiter's task.
 * The Host acts before the Waiter, so a customer seated by the Host can order
 * in the same turn.
 */
public class Host extends Staff {

    public Host(String name) {
        super(name);
    }

    @Override
    public String describeDuty() {
        return "seats one queued customer per turn";
    }

    public void seat(Restaurant restaurant, Customer customer, Table table) throws InvalidActionException {
        ensureAvailable();
        restaurant.seat(customer, table);
        markActed();
    }
}
