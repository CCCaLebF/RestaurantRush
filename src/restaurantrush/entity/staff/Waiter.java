package restaurantrush.entity.staff;

import restaurantrush.entity.Customer;
import restaurantrush.entity.InvalidActionException;
import restaurantrush.entity.Order;
import restaurantrush.entity.Restaurant;
import restaurantrush.entity.Table;

/** Seats a queued customer, takes an order, or serves a READY dish: one per turn. */
public class Waiter extends Staff {

    public Waiter(String name) {
        super(name);
    }

    @Override
    public String describeDuty() {
        return "one task per turn: seat, take order, or serve";
    }

    public void seat(Restaurant restaurant, Customer customer, Table table) throws InvalidActionException {
        ensureAvailable();
        restaurant.seat(customer, table);
        markActed();
    }

    public Order takeOrder(Restaurant restaurant, Customer customer) throws InvalidActionException {
        ensureAvailable();
        Order order = restaurant.takeOrder(customer);
        markActed();
        return order;
    }

    public void serve(Restaurant restaurant, Customer customer, int turn) throws InvalidActionException {
        ensureAvailable();
        restaurant.serve(customer, turn);
        markActed();
    }
}
