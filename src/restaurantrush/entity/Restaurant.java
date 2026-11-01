package restaurantrush.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import restaurantrush.entity.staff.Cashier;
import restaurantrush.entity.staff.Chef;
import restaurantrush.entity.staff.Host;
import restaurantrush.entity.staff.Staff;
import restaurantrush.entity.staff.Waiter;

/**
 * The restaurant's state: tables, waiting queue, menu, staff, ledger, Happy Hour
 * and every customer who has arrived. Operations that touch several of these
 * objects at once (seating, ordering, serving, payment, departure) are kept here
 * so they stay consistent, and staff classes call them rather than editing state directly.
 */
public class Restaurant {

    private final Menu menu;
    private final List<Table> tables = new ArrayList<>();
    private final WaitingQueue queue = new WaitingQueue();
    private final List<Customer> customers = new ArrayList<>();
    private final Ledger ledger = new Ledger();
    private final HappyHour happyHour = new HappyHour();
    private final Host host = new Host("Host");
    private final Waiter waiter = new Waiter("Waiter");
    private final Chef chef = new Chef("Chef");
    private final Cashier cashier = new Cashier("Cashier");

    public Restaurant(Menu menu, int tableCount) {
        this.menu = menu;
        for (int i = 1; i <= tableCount; i++) {
            tables.add(new Table(i));
        }
    }

    // ---- Operations --------------------------------------------------------------------

    /** A new customer joins the back of the waiting queue. */
    public void admit(Customer customer) {
        customers.add(customer);
        queue.enqueue(customer);
    }

    public void seat(Customer customer, Table table) throws InvalidActionException {
        if (!queue.contains(customer)) {
            throw new InvalidActionException(customer.getId() + " is not in the waiting queue.");
        }
        if (!table.isFree()) {
            throw new InvalidActionException(table.getId() + " is occupied by " + table.getOccupant().getId() + ".");
        }
        customer.seat();
        table.seat(customer);
        queue.remove(customer);
    }

    /** The bill is locked when the order is taken, with Happy Hour applied if it is active now. */
    public Order takeOrder(Customer customer) throws InvalidActionException {
        return customer.placeOrder(menu, happyHour::applyTo);
    }

    public void serve(Customer customer, int turn) throws InvalidActionException {
        customer.receiveFood(turn);
        ledger.recordServed();
    }

    /** The customer pays: revenue and satisfaction are recorded and the table is freed. */
    public int collectPayment(Customer customer, int turn) {
        int amount = customer.pay(turn);
        ledger.recordPayment(amount, customer.getPaidSatisfaction());
        freeTableOf(customer);
        return amount;
    }

    /** The customer leaves unhappy: their order is cancelled and their place freed. */
    public void depart(Customer customer) {
        customer.leave();
        queue.remove(customer);
        freeTableOf(customer);
        ledger.recordDeparture();
    }

    private void freeTableOf(Customer customer) {
        findTableOf(customer).ifPresent(Table::free);
    }

    /**
     * Switches Happy Hour on and applies this turn's recovery at once.
     * Returns the customers who recovered.
     */
    public List<Customer> activateHappyHour() throws InvalidActionException {
        happyHour.activate();
        return applyHappyHourRecovery();
    }

    /** At the start of an active Happy Hour turn, customers waiting for a table or food recover. */
    public List<Customer> applyHappyHourRecovery() {
        if (!happyHour.isActive()) {
            return List.of();
        }
        List<Customer> waiting = getActiveCustomers().stream().filter(Customer::isLosingSatisfaction).toList();
        waiting.forEach(c -> c.recover(HappyHour.RECOVERY));
        return waiting;
    }

    /** End-of-turn upkeep: one Happy Hour turn is used up. */
    public void endTurnEffects() {
        happyHour.endTurn();
    }

    // ---- Queries -----------------------------------------------------------------------

    public Optional<Table> findTableOf(Customer customer) {
        return tables.stream().filter(t -> t.getOccupant() == customer).findFirst();
    }

    public List<Table> getFreeTables() {
        return tables.stream().filter(Table::isFree).toList();
    }

    public List<Customer> getQueuedCustomers() {
        return queue.asList();
    }

    public List<Customer> getSeatedWithoutOrder() {
        return activeCustomersWithState(CustomerState.SEATED);
    }

    /** Customers whose dish is READY right now and can be served. */
    public List<Customer> getCustomersWithReadyOrders() {
        return activeCustomersWithState(CustomerState.AWAITING_FOOD).stream()
                .filter(c -> c.getOrder().isReady())
                .toList();
    }

    /** Orders the Chef can still work on, oldest customer first. */
    public List<Order> getCookableOrders() {
        return activeCustomersWithState(CustomerState.AWAITING_FOOD).stream()
                .map(Customer::getOrder)
                .filter(Order::isCookable)
                .toList();
    }

    /** Orders whose customer is still waiting for food (being prepared or READY). */
    public List<Order> getOpenOrders() {
        return activeCustomersWithState(CustomerState.AWAITING_FOOD).stream()
                .map(Customer::getOrder)
                .toList();
    }

    /** Customers ready to pay this turn, oldest (earliest READY_TO_PAY, then earliest arrival) first. */
    public List<Customer> getPayableCustomers(int turn) {
        return customers.stream()
                .filter(c -> c.canPay(turn))
                .sorted(Comparator.comparingInt(Customer::getReadyToPayTurn)
                        .thenComparingInt(Customer::getArrivalTurn))
                .toList();
    }

    public List<Customer> getActiveCustomers() {
        return customers.stream().filter(Customer::isActive).toList();
    }

    public List<Customer> getAllCustomers() {
        return List.copyOf(customers);
    }

    private List<Customer> activeCustomersWithState(CustomerState state) {
        return customers.stream().filter(c -> c.getState() == state).toList();
    }

    public List<Table> getTables() {
        return List.copyOf(tables);
    }

    public Menu getMenu() {
        return menu;
    }

    public Ledger getLedger() {
        return ledger;
    }

    public HappyHour getHappyHour() {
        return happyHour;
    }

    /** Every staff member, in the order they act each turn. */
    public List<Staff> getStaff() {
        return List.of(host, waiter, chef, cashier);
    }

    public Host getHost() {
        return host;
    }

    public Waiter getWaiter() {
        return waiter;
    }

    public Chef getChef() {
        return chef;
    }

    public Cashier getCashier() {
        return cashier;
    }
}
