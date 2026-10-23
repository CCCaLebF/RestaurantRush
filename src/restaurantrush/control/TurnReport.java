package restaurantrush.control;

import java.util.List;
import java.util.Optional;

import restaurantrush.entity.Customer;

/** What happened during steps 4-6 of a turn, for the UI to display. */
public class TurnReport {

    private final int turn;
    private final Customer paidCustomer;
    private final int paidAmountCents;
    private final List<Customer> finishedEating;
    private final List<Customer> departures;
    private final GameResult finalResult;

    public TurnReport(int turn, Customer paidCustomer, int paidAmountCents, List<Customer> finishedEating,
                      List<Customer> departures, GameResult finalResult) {
        this.turn = turn;
        this.paidCustomer = paidCustomer;
        this.paidAmountCents = paidAmountCents;
        this.finishedEating = List.copyOf(finishedEating);
        this.departures = List.copyOf(departures);
        this.finalResult = finalResult;
    }

    public int getTurn() {
        return turn;
    }

    public Optional<Customer> getPaidCustomer() {
        return Optional.ofNullable(paidCustomer);
    }

    public int getPaidAmountCents() {
        return paidAmountCents;
    }

    public List<Customer> getFinishedEating() {
        return finishedEating;
    }

    public List<Customer> getDepartures() {
        return departures;
    }

    /** Present only after the last turn. */
    public Optional<GameResult> getFinalResult() {
        return Optional.ofNullable(finalResult);
    }
}
