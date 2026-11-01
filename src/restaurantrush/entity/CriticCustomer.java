package restaurantrush.entity;

import java.util.Comparator;

/**
 * A food critic: loses 12 satisfaction per waiting turn and pays full price.
 *
 * Order preference (our Stage 2 rule): the Critic always orders the most elaborate
 * dish, meaning the item with the most preparation units, with ties broken by the
 * higher price. With the Stage 3 menu this is the Burger + Pasta combo (3 units, $29.70),
 * whatever the Critic's arrival number.
 */
public class CriticCustomer extends Customer {

    private static final int WAITING_PENALTY = 12;

    private static final Comparator<MenuItem> MOST_ELABORATE =
            Comparator.comparingInt(MenuItem::getPrepUnits).thenComparingInt(MenuItem::getPriceCents);

    public CriticCustomer(int number, int arrivalTurn) {
        super(number, arrivalTurn);
    }

    @Override
    public MenuItem chooseDish(Menu menu) {
        return menu.getItems().stream().max(MOST_ELABORATE).orElseThrow();
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
        return "Critic";
    }

    @Override
    public String describeBehaviour() {
        return "loses " + WAITING_PENALTY + "/turn while waiting, pays full price, always orders the most elaborate dish";
    }
}
