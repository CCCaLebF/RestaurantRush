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

    /** The Stage 1 base menu: Salad, Burger, Pasta (in this order). */
    public static Menu standard() {
        return new Menu(List.of(
                new MenuItem("Salad", 900, 1),
                new MenuItem("Burger", 1500, 1),
                new MenuItem("Pasta", 1800, 2)));
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
