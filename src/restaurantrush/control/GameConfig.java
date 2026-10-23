package restaurantrush.control;

import java.util.List;

/**
 * Game parameters. {@link #base()} returns the required base setting from the
 * brief (Section 2.1) so that every run is reproducible.
 */
public class GameConfig {

    private final int maxTurns;
    private final int tableCount;
    private final List<Arrival> arrivals;
    private final int targetPaidCustomers;
    private final int targetRevenueCents;
    private final int targetAverageSatisfaction;

    public GameConfig(int maxTurns, int tableCount, List<Arrival> arrivals,
                      int targetPaidCustomers, int targetRevenueCents, int targetAverageSatisfaction) {
        this.maxTurns = maxTurns;
        this.tableCount = tableCount;
        this.arrivals = List.copyOf(arrivals);
        this.targetPaidCustomers = targetPaidCustomers;
        this.targetRevenueCents = targetRevenueCents;
        this.targetAverageSatisfaction = targetAverageSatisfaction;
    }

    /**
     * Base setting with the Stage 2 schedule: still six arrivals on turns 1, 4, 7, 10, 13 and 16,
     * with a VIP replacing the turn-4 regular and a Critic replacing the turn-10 regular.
     */
    public static GameConfig base() {
        List<Arrival> arrivals = List.of(
                new Arrival(1, CustomerType.REGULAR),
                new Arrival(4, CustomerType.VIP),
                new Arrival(7, CustomerType.REGULAR),
                new Arrival(10, CustomerType.CRITIC),
                new Arrival(13, CustomerType.REGULAR),
                new Arrival(16, CustomerType.REGULAR));
        return new GameConfig(18, 2, arrivals, 4, 4500, 60);
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public int getTableCount() {
        return tableCount;
    }

    public List<Arrival> getArrivals() {
        return arrivals;
    }

    public int getTargetPaidCustomers() {
        return targetPaidCustomers;
    }

    public int getTargetRevenueCents() {
        return targetRevenueCents;
    }

    public int getTargetAverageSatisfaction() {
        return targetAverageSatisfaction;
    }
}
