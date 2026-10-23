package restaurantrush.entity;

/**
 * A VIP customer: loses 5 satisfaction per waiting turn and gets 10% off the bill,
 * rounded half-up to the nearest cent. Orders by the default rotation rule.
 */
public class VIPCustomer extends Customer {

    private static final int WAITING_PENALTY = 5;
    private static final int BILL_PERCENT = 90;

    public VIPCustomer(int number, int arrivalTurn) {
        super(number, arrivalTurn);
    }

    @Override
    public int getWaitingPenalty() {
        return WAITING_PENALTY;
    }

    @Override
    public int computeBillCents(MenuItem item) {
        return Money.percentOf(item.getPriceCents(), BILL_PERCENT);
    }

    @Override
    public String getTypeName() {
        return "VIP";
    }

    @Override
    public String describeBehaviour() {
        return "loses " + WAITING_PENALTY + "/turn while waiting, gets " + (100 - BILL_PERCENT) + "% off the bill";
    }
}
