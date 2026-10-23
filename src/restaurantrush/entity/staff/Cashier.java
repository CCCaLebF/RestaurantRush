package restaurantrush.entity.staff;

import java.util.List;
import java.util.Optional;

import restaurantrush.entity.Customer;
import restaurantrush.entity.Restaurant;

/** Automatically takes payment from at most one READY_TO_PAY customer per turn, oldest first. */
public class Cashier extends Staff {

    public Cashier(String name) {
        super(name);
    }

    @Override
    public String describeDuty() {
        return "automatic: one payment per turn, oldest first";
    }

    /** Collects one payment if anyone can pay this turn. Returns the customer who paid, if any. */
    public Optional<Customer> processPayment(Restaurant restaurant, int turn) {
        if (hasActedThisTurn()) {
            return Optional.empty();
        }
        List<Customer> payable = restaurant.getPayableCustomers(turn);
        if (payable.isEmpty()) {
            return Optional.empty();
        }
        Customer customer = payable.get(0);
        restaurant.collectPayment(customer, turn);
        markActed();
        return Optional.of(customer);
    }
}
