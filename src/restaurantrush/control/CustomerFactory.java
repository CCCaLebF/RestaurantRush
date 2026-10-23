package restaurantrush.control;

import restaurantrush.entity.CriticCustomer;
import restaurantrush.entity.Customer;
import restaurantrush.entity.RegularCustomer;
import restaurantrush.entity.VIPCustomer;

/**
 * Creates the customer for each scheduled arrival. This is the only class that
 * names the concrete customer types; everything else works with {@link Customer}.
 */
public class CustomerFactory {

    public Customer create(CustomerType type, int number, int arrivalTurn) {
        return switch (type) {
            case REGULAR -> new RegularCustomer(number, arrivalTurn);
            case VIP -> new VIPCustomer(number, arrivalTurn);
            case CRITIC -> new CriticCustomer(number, arrivalTurn);
        };
    }
}
