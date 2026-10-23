package restaurantrush.control;

import java.util.ArrayList;
import java.util.List;

import restaurantrush.entity.Customer;

/** Decides who arrives on each turn, numbering customers C1, C2, ... in arrival order. */
public class ArrivalSchedule {

    private final List<Integer> arrivalTurns;
    private final CustomerFactory factory;
    private int nextNumber = 1;

    public ArrivalSchedule(List<Integer> arrivalTurns, CustomerFactory factory) {
        this.arrivalTurns = List.copyOf(arrivalTurns);
        this.factory = factory;
    }

    public List<Customer> arrivalsFor(int turn) {
        List<Customer> arrivals = new ArrayList<>();
        for (int arrivalTurn : arrivalTurns) {
            if (arrivalTurn == turn) {
                arrivals.add(factory.create(nextNumber++, turn));
            }
        }
        return arrivals;
    }
}
