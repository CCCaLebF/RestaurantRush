package restaurantrush.entity;

/**
 * Money helper. All amounts in the game are stored as integer cents to avoid
 * floating-point rounding; this class formats them for display and applies
 * percentage discounts without leaving integer arithmetic.
 */
public final class Money {

    private Money() {
    }

    /**
     * Returns {@code percent}% of an amount, rounded half-up to the nearest cent,
     * e.g. percentOf(1500, 90) -> 1350 and percentOf(1005, 90) -> 905.
     */
    public static int percentOf(int cents, int percent) {
        if (cents < 0 || percent < 0) {
            throw new IllegalArgumentException("Amount and percentage cannot be negative");
        }
        return (cents * percent + 50) / 100;
    }

    /** Formats an amount in cents as dollars with two decimals, e.g. 1500 -> "$15.00". */
    public static String format(int cents) {
        String sign = cents < 0 ? "-" : "";
        int abs = Math.abs(cents);
        return String.format("%s$%d.%02d", sign, abs / 100, abs % 100);
    }
}
