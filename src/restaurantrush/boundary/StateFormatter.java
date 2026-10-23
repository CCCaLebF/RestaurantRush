package restaurantrush.boundary;

import java.util.List;
import java.util.stream.Collectors;

import restaurantrush.control.GameConfig;
import restaurantrush.control.GameController;
import restaurantrush.control.GameResult;
import restaurantrush.control.TurnReport;
import restaurantrush.entity.Customer;
import restaurantrush.entity.Ledger;
import restaurantrush.entity.Money;
import restaurantrush.entity.Order;
import restaurantrush.entity.Restaurant;
import restaurantrush.entity.Table;

/** Turns game state into display text. Reads state only and never changes it. */
public class StateFormatter {

    private static final String RULE = "------------------------------------------------------------";

    public String turnHeader(int turn, int maxTurns) {
        return "\n============================ TURN " + turn + " / " + maxTurns + " ============================";
    }

    public String arrival(Customer customer) {
        return ">> " + customer.getId() + " (" + customer.getTypeName() + ") arrives and joins the queue (satisfaction "
                + customer.getSatisfaction() + ").";
    }

    /** Start-of-turn display (brief Section 3.1). */
    public String startOfTurn(GameController controller) {
        Restaurant r = controller.getRestaurant();
        Ledger ledger = r.getLedger();
        StringBuilder sb = new StringBuilder();
        sb.append("Turn ").append(controller.getTurn()).append(" | Revenue: ").append(Money.format(ledger.getRevenueCents()))
          .append(" | Avg satisfaction (paid): ").append(average(ledger))
          .append(" | Paid: ").append(ledger.getPaidCount())
          .append(" | Left: ").append(ledger.getDepartureCount()).append('\n');

        List<Customer> queued = r.getQueuedCustomers();
        sb.append("Waiting queue: ");
        sb.append(queued.isEmpty() ? "(empty)"
                : queued.stream().map(c -> c.getId() + " (sat " + c.getSatisfaction() + ")")
                        .collect(Collectors.joining(", ")));
        sb.append('\n');

        sb.append("Tables:\n");
        for (Table table : r.getTables()) {
            sb.append("  ").append(table.getId()).append(": ");
            sb.append(table.isFree() ? "(free)" : customerLine(table.getOccupant()));
            sb.append('\n');
        }

        sb.append("Staff tasks available:\n");
        sb.append("  Waiter  - ").append(waiterOptions(r)).append('\n');
        sb.append("  Chef    - ").append(chefOptions(r)).append('\n');
        sb.append("  Cashier - ").append(r.getCashier().describeDuty());
        return sb.toString();
    }

    public String customerLine(Customer c) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-3s sat %3d  %-13s", c.getId(), c.getSatisfaction(), c.getState()));
        Order order = c.getOrder();
        sb.append(order == null ? "  order: -" : "  order: " + orderLine(order));
        return sb.toString();
    }

    public String orderLine(Order order) {
        return order.getItem().getName() + " " + Money.format(order.getPriceCents()) + " [" + order.describeStatus() + "]";
    }

    private String waiterOptions(Restaurant r) {
        String seat = r.getQueuedCustomers().isEmpty() || r.getFreeTables().isEmpty()
                ? "seat: none" : "seat: " + ids(r.getQueuedCustomers()) + " -> " + tableIds(r.getFreeTables());
        String order = r.getSeatedWithoutOrder().isEmpty() ? "take order: none" : "take order: " + ids(r.getSeatedWithoutOrder());
        String serve = r.getCustomersWithReadyOrders().isEmpty() ? "serve: none" : "serve: " + ids(r.getCustomersWithReadyOrders());
        return seat + " | " + order + " | " + serve;
    }

    private String chefOptions(Restaurant r) {
        List<Order> cookable = r.getCookableOrders();
        if (cookable.isEmpty()) {
            return "prepare: none (an order taken by the Waiter this turn can still be cooked)";
        }
        return "prepare: " + cookable.stream()
                .map(o -> o.getCustomer().getId() + " " + o.getItem().getName() + " " + o.describeStatus())
                .collect(Collectors.joining(", "));
    }

    /** Steps 4-6 events and the end-of-turn results (brief Section 3.5). */
    public String endOfTurn(TurnReport report, GameController controller) {
        StringBuilder sb = new StringBuilder();
        sb.append(RULE).append('\n');
        report.getPaidCustomer().ifPresent(c -> sb.append(">> Cashier collects ").append(Money.format(report.getPaidAmountCents()))
                .append(" from ").append(c.getId()).append(" (satisfaction ").append(c.getPaidSatisfaction())
                .append(" recorded). Table freed.\n"));
        for (Customer c : report.getFinishedEating()) {
            sb.append(">> ").append(c.getId()).append(" finishes eating and is READY_TO_PAY (pays next turn at the earliest).\n");
        }
        for (Customer c : report.getDepartures()) {
            sb.append(">> ").append(c.getId()).append(" reached 0 satisfaction and LEFT without paying.\n");
        }
        sb.append(endOfTurnSummary(controller));
        return sb.toString();
    }

    public String endOfTurnSummary(GameController controller) {
        Ledger ledger = controller.getRestaurant().getLedger();
        return "End of turn " + controller.getTurn()
                + " | Served: " + ledger.getServedCount()
                + " | Unhappy departures: " + ledger.getDepartureCount()
                + " | Revenue: " + Money.format(ledger.getRevenueCents())
                + " | Avg satisfaction (paid): " + average(ledger)
                + " | Turns used: " + controller.getTurn() + "/" + controller.getMaxTurns();
    }

    public String finalResult(GameResult result, GameController controller) {
        Ledger ledger = controller.getRestaurant().getLedger();
        GameConfig cfg = controller.getConfig();
        return "\n============================== GAME OVER ==============================\n"
                + target(result.isPaidTargetMet(), "Paid customers", ledger.getPaidCount() + "", ">= " + cfg.getTargetPaidCustomers())
                + target(result.isRevenueTargetMet(), "Revenue", Money.format(ledger.getRevenueCents()),
                        ">= " + Money.format(cfg.getTargetRevenueCents()))
                + target(result.isSatisfactionTargetMet(), "Avg satisfaction (paid)", average(ledger),
                        ">= " + cfg.getTargetAverageSatisfaction())
                + "Served: " + ledger.getServedCount() + " | Unhappy departures: " + ledger.getDepartureCount() + "\n"
                + "RESULT: " + (result.isVictory() ? "VICTORY" : "DEFEAT");
    }

    private String target(boolean met, String label, String actual, String required) {
        return String.format("  [%s] %-24s %8s  (target %s)%n", met ? "MET" : "NOT MET", label, actual, required);
    }

    public String average(Ledger ledger) {
        return ledger.hasPaidCustomers() ? String.format("%.1f", ledger.getAverageSatisfaction()) : "N/A";
    }

    private String ids(List<Customer> customers) {
        return customers.stream().map(Customer::getId).collect(Collectors.joining(", "));
    }

    private String tableIds(List<Table> tables) {
        return tables.stream().map(Table::getId).collect(Collectors.joining(", "));
    }
}
