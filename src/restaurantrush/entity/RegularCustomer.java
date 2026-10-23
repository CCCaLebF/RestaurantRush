package restaurantrush.entity;

/**
 * A regular customer: loses 8 satisfaction per waiting turn and pays full price.
 *
 * Dish choice rule (deterministic): customer n orders menu item (n - 1) mod menuSize,
 * so with the base menu C1 Salad, C2 Burger, C3 Pasta, C4 Salad, C5 Burger, C6 Pasta.
 */
public class RegularCustomer extends Customer {

    private static final int WAITING_PENALTY = 8;

    public RegularCustomer(int number, int arrivalTurn) {
        super(number, arrivalTurn);
    }

    @Override
    public MenuItem chooseDish(Menu menu) {
        return menu.get((getNumber() - 1) % menu.size());
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
}
