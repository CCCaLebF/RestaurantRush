package restaurantrush.entity;

/**
 * An immutable dish on the menu: name, price in cents, and how many
 * preparation units the Chef must spend on it.
 */
public class MenuItem {

    private final String name;
    private final int priceCents;
    private final int prepUnits;

    public MenuItem(String name, int priceCents, int prepUnits) {
        if (priceCents < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (prepUnits < 1) {
            throw new IllegalArgumentException("A dish needs at least one preparation unit");
        }
        this.name = name;
        this.priceCents = priceCents;
        this.prepUnits = prepUnits;
    }

    public String getName() {
        return name;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public int getPrepUnits() {
        return prepUnits;
    }

    @Override
    public String toString() {
        return name + " (" + Money.format(priceCents) + ", " + prepUnits + " unit" + (prepUnits == 1 ? "" : "s") + ")";
    }
}
