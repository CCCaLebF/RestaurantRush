package restaurantrush.entity;

/**
 * A customer's order. The bill is locked in when the order is created, and the
 * order guards its own status transitions so illegal moves are rejected here
 * rather than in the controller or UI.
 */
public class Order {

    private final Customer customer;
    private final MenuItem item;
    private final int priceCents;
    private int unitsDone;
    private OrderStatus status;

    public Order(Customer customer, MenuItem item, int priceCents) {
        this.customer = customer;
        this.item = item;
        this.priceCents = priceCents;
        this.unitsDone = 0;
        this.status = OrderStatus.PLACED;
    }

    /** Chef spends one preparation unit. The order becomes READY after its last unit. */
    public void prepareOneUnit() throws InvalidActionException {
        if (!isCookable()) {
            throw new InvalidActionException("Order for " + customer.getId() + " is " + status
                    + "; only PLACED or IN_PREPARATION orders can be prepared.");
        }
        unitsDone++;
        status = unitsDone >= item.getPrepUnits() ? OrderStatus.READY : OrderStatus.IN_PREPARATION;
    }

    public void markServed() throws InvalidActionException {
        if (status != OrderStatus.READY) {
            throw new InvalidActionException("Order for " + customer.getId() + " is " + describeStatus()
                    + ", not READY. A dish can only be served once it is READY.");
        }
        status = OrderStatus.SERVED;
    }

    void markPaid() {
        if (status != OrderStatus.SERVED) {
            throw new IllegalStateException("Cannot pay for an order that is " + status);
        }
        status = OrderStatus.PAID;
    }

    /** Cancels an unfinished order when its customer leaves. Served orders are left as they are. */
    void cancel() {
        if (status == OrderStatus.PLACED || status == OrderStatus.IN_PREPARATION || status == OrderStatus.READY) {
            status = OrderStatus.CANCELLED;
        }
    }

    public boolean isCookable() {
        return status == OrderStatus.PLACED || status == OrderStatus.IN_PREPARATION;
    }

    public boolean isReady() {
        return status == OrderStatus.READY;
    }

    /** Status with preparation progress, e.g. "IN_PREPARATION (1/2)". */
    public String describeStatus() {
        if (status == OrderStatus.PLACED || status == OrderStatus.IN_PREPARATION) {
            return status + " (" + unitsDone + "/" + item.getPrepUnits() + ")";
        }
        return status.toString();
    }

    public Customer getCustomer() {
        return customer;
    }

    public MenuItem getItem() {
        return item;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public int getUnitsDone() {
        return unitsDone;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
