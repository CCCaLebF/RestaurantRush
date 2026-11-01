package restaurantrush.entity;

import java.util.List;

/**
 * A combo of two existing menu items. It needs the preparation units of both
 * parts and costs 90% of their combined price, rounded half-up to the nearest
 * cent, e.g. Salad + Pasta = ($9.00 + $18.00) x 0.9 = $24.30, 3 units.
 *
 * A combo is a MenuItem, so orders, the Chef and the customers' dish rules
 * handle it without knowing it is a combo.
 */
public class ComboMeal extends MenuItem {

    private static final int COMBO_PERCENT = 90;

    private final MenuItem first;
    private final MenuItem second;

    public ComboMeal(MenuItem first, MenuItem second) {
        super(first.getName() + " + " + second.getName(),
                Money.percentOf(first.getPriceCents() + second.getPriceCents(), COMBO_PERCENT),
                first.getPrepUnits() + second.getPrepUnits());
        this.first = first;
        this.second = second;
    }

    public List<MenuItem> getComponents() {
        return List.of(first, second);
    }

    @Override
    public String toString() {
        return getName() + " combo (" + Money.format(getPriceCents()) + ", " + getPrepUnits() + " units, "
                + (100 - COMBO_PERCENT) + "% off " + Money.format(first.getPriceCents() + second.getPriceCents()) + ")";
    }
}
