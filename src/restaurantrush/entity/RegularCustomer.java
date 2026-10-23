package restaurantrush.entity;

/**
 * A regular customer: loses 8 satisfaction per waiting turn, pays full price,
 * and orders by the default rotation rule in {@link Customer#chooseDish(Menu)}.
 */
public class RegularCustomer extends Customer {

    private static final int WAITING_PENALTY = 8;

    public RegularCustomer(int number, int arrivalTurn) {
        super(number, arrivalTurn);
    }

    @Override
    public int getWaitingPenalty() {
        return WAITING_PENALTY;
    }

    @Override
    public int computeBillCents(MenuItem item) {
        return item.getPriceCents();
    }

    @Override
    public String getTypeName() {
        return "Regular";
    }

    @Override
    public String describeBehaviour() {
        return "loses " + WAITING_PENALTY + "/turn while waiting, pays full price";
    }
}
