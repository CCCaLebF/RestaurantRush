package restaurantrush.entity;

/**
 * Running totals for the game: revenue (cents), paid customers and the
 * satisfaction each one had when paying, dishes served and unhappy departures.
 * Only paid bills are ever added to revenue.
 */
public class Ledger {

    private int revenueCents;
    private int paidCount;
    private int paidSatisfactionSum;
    private int servedCount;
    private int departureCount;

    void recordPayment(int amountCents, int satisfactionAtPayment) {
        revenueCents += amountCents;
        paidCount++;
        paidSatisfactionSum += satisfactionAtPayment;
    }

    void recordServed() {
        servedCount++;
    }

    void recordDeparture() {
        departureCount++;
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public int getPaidSatisfactionSum() {
        return paidSatisfactionSum;
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getDepartureCount() {
        return departureCount;
    }

    public boolean hasPaidCustomers() {
        return paidCount > 0;
    }

    /** Average satisfaction of paid customers. Only meaningful if {@link #hasPaidCustomers()}. */
    public double getAverageSatisfaction() {
        return paidCount == 0 ? 0.0 : (double) paidSatisfactionSum / paidCount;
    }

    /** Exact integer comparison: average >= target  <=>  sum >= target * count. */
    public boolean averageSatisfactionAtLeast(int target) {
        return paidCount > 0 && paidSatisfactionSum >= target * paidCount;
    }
}
