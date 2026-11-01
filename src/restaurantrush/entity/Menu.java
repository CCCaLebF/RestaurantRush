package restaurantrush.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The ordered list of dishes customers can choose from. */
public class Menu {

    private final List<MenuItem> items = new ArrayList<>();

    public Menu(List<MenuItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Menu must contain at least one item");
        }
        this.items.addAll(items);
    }

    /**
     * The base menu: Salad, Burger, Pasta, then a combo of each pair of them
     * (Salad + Burger, Salad + Pasta, Burger + Pasta), in this order.
     */
    public static Menu standard() {
        MenuItem salad = new MenuItem("Salad", 900, 1);
        MenuItem burger = new MenuItem("Burger", 1500, 1);
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        return new Menu(List.of(
                salad, burger, pasta,
                new ComboMeal(salad, burger),
                new ComboMeal(salad, pasta),
                new ComboMeal(burger, pasta)));
    }

    public List<MenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public MenuItem get(int index) {
        return items.get(index);
    }

    public int size() {
        return items.size();
    }
}
