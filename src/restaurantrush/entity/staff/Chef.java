package restaurantrush.entity.staff;

import restaurantrush.entity.InvalidActionException;
import restaurantrush.entity.Order;

/** Advances one order by one preparation unit per turn. */
public class Chef extends Staff {

    public Chef(String name) {
        super(name);
    }

    @Override
    public String describeDuty() {
        return "one preparation unit per turn";
    }

    public void prepare(Order order) throws InvalidActionException {
        ensureAvailable();
        order.prepareOneUnit();
        markActed();
    }
}
