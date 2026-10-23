package restaurantrush.entity;

import java.util.ArrayList;
import java.util.List;

/** First-in, first-out queue of customers waiting for a table. */
public class WaitingQueue {

    private final List<Customer> customers = new ArrayList<>();

    void enqueue(Customer customer) {
        customers.add(customer);
    }

    boolean remove(Customer customer) {
        return customers.remove(customer);
    }

    public boolean contains(Customer customer) {
        return customers.contains(customer);
    }

    public boolean isEmpty() {
        return customers.isEmpty();
    }

    /** Snapshot of the queue, front first. */
    public List<Customer> asList() {
        return List.copyOf(customers);
    }
}
