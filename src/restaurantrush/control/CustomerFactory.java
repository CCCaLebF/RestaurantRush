package restaurantrush.control;

import restaurantrush.entity.Customer;
import restaurantrush.entity.RegularCustomer;

/** Creates the customer for each scheduled arrival. Stage 1 has only regular customers. */
public class CustomerFactory {

    public Customer create(int number, int arrivalTurn) {
        return new RegularCustomer(number, arrivalTurn);
    }
}
