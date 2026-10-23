package restaurantrush.control;

import java.util.List;

/**
 * Game parameters. {@link #base()} returns the required base setting from the
 * brief (Section 2.1) so that every run is reproducible.
 */
public class GameConfig {

    private final int maxTurns;
    private final int tableCount;
    private final List<Integer> arrivalTurns;
    private final int targetPaidCustomers;
    private final int targetRevenueCents;
    private final int targetAverageSatisfaction;

    public GameConfig(int maxTurns, int tableCount, List<Integer> arrivalTurns,
                      int targetPaidCustomers, int targetRevenueCents, int targetAverageSatisfaction) {
        this.maxTurns = maxTurns;
        this.tableCount = tableCount;
        this.arrivalTurns = List.copyOf(arrivalTurns);
        this.targetPaidCustomers = targetPaidCustomers;
        this.targetRevenueCents = targetRevenueCents;
        this.targetAverageSatisfaction = targetAverageSatisfaction;
    }

    public static GameConfig base() {
        return new GameConfig(18, 2, List.of(1, 4, 7, 10, 13, 16), 4, 4500, 60);
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public int getTableCount() {
        return tableCount;
    }

    public List<Integer> getArrivalTurns() {
        return arrivalTurns;
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
