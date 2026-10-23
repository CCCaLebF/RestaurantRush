package restaurantrush.control;

import java.util.ArrayList;
import java.util.List;

import restaurantrush.entity.Customer;

/** Decides who arrives on each turn, numbering customers C1, C2, ... in arrival order. */
public class ArrivalSchedule {

    private final List<Arrival> arrivals;
    private final CustomerFactory factory;
    private int nextNumber = 1;

    public ArrivalSchedule(List<Arrival> arrivals, CustomerFactory factory) {
        this.arrivals = List.copyOf(arrivals);
        this.factory = factory;
    }

    public List<Customer> arrivalsFor(int turn) {
        List<Customer> customers = new ArrayList<>();
        for (Arrival arrival : arrivals) {
            if (arrival.turn() == turn) {
                customers.add(factory.create(arrival.type(), nextNumber++, turn));
            }
        }
        return customers;
    }
}
