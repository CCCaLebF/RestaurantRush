package restaurantrush.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import restaurantrush.entity.Customer;
import restaurantrush.entity.InvalidActionException;
import restaurantrush.entity.Order;
import restaurantrush.entity.Restaurant;
import restaurantrush.entity.Table;

/**
 * Runs the turn sequence. It holds no I/O: a boundary object calls the methods
 * below in order, and the controller rejects calls made out of order.
 *
 * <pre>
 * startTurn()                  step 1  arrivals
 * waiter...() / waiterWait()   step 2-3 Waiter acts first
 * chefPrepare() / chefWait()   step 2-3 then the Chef
 * endTurn()                    steps 4-6 eating and payment, waiting and abandonment, result
 * </pre>
 */
public class GameController {

    private enum Phase { BETWEEN_TURNS, WAITER, CHEF, RESOLVE, GAME_OVER }

    private final GameConfig config;
    private final Restaurant restaurant;
    private final ArrivalSchedule schedule;
    private final ObjectiveEvaluator evaluator;
    private int turn;
    private Phase phase = Phase.BETWEEN_TURNS;
    private GameResult result;

    public GameController(GameConfig config, Restaurant restaurant, ArrivalSchedule schedule,
                          ObjectiveEvaluator evaluator) {
        this.config = config;
        this.restaurant = restaurant;
        this.schedule = schedule;
        this.evaluator = evaluator;
    }

    // ---- Step 1: arrivals --------------------------------------------------------------

    /** Begins the next turn and returns the customers who arrived. */
    public List<Customer> startTurn() {
        requirePhase(Phase.BETWEEN_TURNS);
        turn++;
        restaurant.getWaiter().resetForTurn();
        restaurant.getChef().resetForTurn();
        restaurant.getCashier().resetForTurn();
        List<Customer> arrivals = schedule.arrivalsFor(turn);
        arrivals.forEach(restaurant::admit);
        phase = Phase.WAITER;
        return arrivals;
    }

    // ---- Steps 2-3: Waiter then Chef ---------------------------------------------------

    public void waiterSeat(Customer customer, Table table) throws InvalidActionException {
        requirePhase(Phase.WAITER);
        restaurant.getWaiter().seat(restaurant, customer, table);
        phase = Phase.CHEF;
    }

    public Order waiterTakeOrder(Customer customer) throws InvalidActionException {
        requirePhase(Phase.WAITER);
        Order order = restaurant.getWaiter().takeOrder(restaurant, customer);
        phase = Phase.CHEF;
        return order;
    }

    public void waiterServe(Customer customer) throws InvalidActionException {
        requirePhase(Phase.WAITER);
        restaurant.getWaiter().serve(restaurant, customer, turn);
        phase = Phase.CHEF;
    }

    public void waiterWait() throws InvalidActionException {
        requirePhase(Phase.WAITER);
        restaurant.getWaiter().waitTurn();
        phase = Phase.CHEF;
    }

    public void chefPrepare(Order order) throws InvalidActionException {
        requirePhase(Phase.CHEF);
        restaurant.getChef().prepare(order);
        phase = Phase.RESOLVE;
    }

    public void chefWait() throws InvalidActionException {
        requirePhase(Phase.CHEF);
        restaurant.getChef().waitTurn();
        phase = Phase.RESOLVE;
    }

    // ---- Steps 4-6 ---------------------------------------------------------------------

    public TurnReport endTurn() {
        requirePhase(Phase.RESOLVE);

        // Step 4: the Cashier pays first, so that anyone who becomes READY_TO_PAY
        // below waits until next turn. Then customers served earlier finish eating.
        Optional<Customer> paid = restaurant.getCashier().processPayment(restaurant, turn);
        int paidAmount = paid.map(c -> c.getOrder().getPriceCents()).orElse(0);
        List<Customer> finishedEating = new ArrayList<>();
        for (Customer customer : restaurant.getActiveCustomers()) {
            if (customer.finishEatingIfDone(turn)) {
                finishedEating.add(customer);
            }
        }

        // Step 5: anyone still waiting for a table or food loses satisfaction; at 0 they leave.
        List<Customer> departures = new ArrayList<>();
        for (Customer customer : restaurant.getActiveCustomers()) {
            if (customer.isLosingSatisfaction()) {
                customer.applyWaitingPenalty();
                if (customer.shouldLeave()) {
                    restaurant.depart(customer);
                    departures.add(customer);
                }
            }
        }

        // Step 6: the game always runs every turn; the result is decided after the last one.
        if (turn >= config.getMaxTurns()) {
            result = evaluator.evaluate(restaurant.getLedger());
            phase = Phase.GAME_OVER;
        } else {
            phase = Phase.BETWEEN_TURNS;
        }
        return new TurnReport(turn, paid.orElse(null), paidAmount, finishedEating, departures, result);
    }

    // ---- Queries -----------------------------------------------------------------------

    public boolean isGameOver() {
        return phase == Phase.GAME_OVER;
    }

    public Optional<GameResult> getResult() {
        return Optional.ofNullable(result);
    }

    public int getTurn() {
        return turn;
    }

    public int getMaxTurns() {
        return config.getMaxTurns();
    }

    public GameConfig getConfig() {
        return config;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    private void requirePhase(Phase expected) {
        if (phase != expected) {
            throw new IllegalStateException("Expected phase " + expected + " but game is in " + phase);
        }
    }
}
