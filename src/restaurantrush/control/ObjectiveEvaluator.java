package restaurantrush.control;

import restaurantrush.entity.Ledger;

/** Victory requires all three targets in the configuration to be met. */
public class ObjectiveEvaluator {

    private final GameConfig config;

    public ObjectiveEvaluator(GameConfig config) {
        this.config = config;
    }

    public GameResult evaluate(Ledger ledger) {
        return new GameResult(
                ledger.getPaidCount() >= config.getTargetPaidCustomers(),
                ledger.getRevenueCents() >= config.getTargetRevenueCents(),
                ledger.averageSatisfactionAtLeast(config.getTargetAverageSatisfaction()));
    }
}
