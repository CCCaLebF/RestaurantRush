package restaurantrush.control;

/** Outcome of the game after the final turn, with the result of each target. */
public class GameResult {

    private final boolean paidTargetMet;
    private final boolean revenueTargetMet;
    private final boolean satisfactionTargetMet;

    public GameResult(boolean paidTargetMet, boolean revenueTargetMet, boolean satisfactionTargetMet) {
        this.paidTargetMet = paidTargetMet;
        this.revenueTargetMet = revenueTargetMet;
        this.satisfactionTargetMet = satisfactionTargetMet;
    }

    public boolean isVictory() {
        return paidTargetMet && revenueTargetMet && satisfactionTargetMet;
    }

    public boolean isPaidTargetMet() {
        return paidTargetMet;
    }

    public boolean isRevenueTargetMet() {
        return revenueTargetMet;
    }

    public boolean isSatisfactionTargetMet() {
        return satisfactionTargetMet;
    }
}
