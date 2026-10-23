package restaurantrush.entity;

/**
 * Money helper. All amounts in the game are stored as integer cents to avoid
 * floating-point rounding; this class only formats them for display.
 */
public final class Money {

    private Money() {
    }

    /** Formats an amount in cents as dollars with two decimals, e.g. 1500 -> "$15.00". */
    public static String format(int cents) {
        String sign = cents < 0 ? "-" : "";
        int abs = Math.abs(cents);
        return String.format("%s$%d.%02d", sign, abs / 100, abs % 100);
    }
}
