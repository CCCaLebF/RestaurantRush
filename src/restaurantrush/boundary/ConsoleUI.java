package restaurantrush.boundary;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import restaurantrush.control.GameController;
import restaurantrush.control.TurnReport;
import restaurantrush.entity.Customer;
import restaurantrush.entity.InvalidActionException;
import restaurantrush.entity.MenuItem;
import restaurantrush.entity.Money;
import restaurantrush.entity.Order;
import restaurantrush.entity.Restaurant;
import restaurantrush.entity.Table;

/**
 * Command-line boundary. It only collects the manager's decisions and shows
 * state. Every rule is checked by the domain, and a rejected choice is shown
 * with its reason before the manager is asked again.
 */
public class ConsoleUI {

    private final GameController controller;
    private final StateFormatter formatter;
    private final Scanner in;
    private final PrintStream out;
    private final boolean echoInput;
    private boolean inputEnded;

    public ConsoleUI(GameController controller, StateFormatter formatter, InputStream input, PrintStream output,
                     boolean echoInput) {
        this.controller = controller;
        this.formatter = formatter;
        this.in = new Scanner(input);
        this.out = output;
        this.echoInput = echoInput;
    }

    public void run() {
        printIntro();
        while (!controller.isGameOver()) {
            playOneTurn();
        }
    }

    private void playOneTurn() {
        List<Customer> arrivals = controller.startTurn();
        out.println(formatter.turnHeader(controller.getTurn(), controller.getMaxTurns()));
        arrivals.forEach(c -> out.println(formatter.arrival(c)));
        out.println(formatter.startOfTurn(controller));

        chooseWaiterTask();
        chooseChefTask();

        TurnReport report = controller.endTurn();
        out.println(formatter.endOfTurn(report, controller));
        report.getFinalResult().ifPresent(result -> out.println(formatter.finalResult(result, controller)));
    }

    // ---- Waiter ------------------------------------------------------------------------

    private void chooseWaiterTask() {
        while (true) {
            out.println("\nWaiter - choose a task:");
            out.println("  1) Seat a queued customer   2) Take an order   3) Serve a READY order   0) Wait");
            int choice = readChoice(0, 3);
            try {
                boolean done = switch (choice) {
                    case 1 -> seatCustomer();
                    case 2 -> takeOrder();
                    case 3 -> serveOrder();
                    default -> {
                        controller.waiterWait();
                        out.println("OK: Waiter waits this turn.");
                        yield true;
                    }
                };
                if (done) {
                    return;
                }
            } catch (InvalidActionException e) {
                out.println("Invalid: " + e.getMessage() + " Choose again (the Waiter's task is not used up).");
            }
        }
    }

    private boolean seatCustomer() throws InvalidActionException {
        Customer customer = pickCustomer("Seat which customer?");
        if (customer == null) {
            return false;
        }
        Table table = pickTable();
        if (table == null) {
            return false;
        }
        controller.waiterSeat(customer, table);
        out.println("OK: " + customer.getId() + " seated at " + table.getId() + ".");
        return true;
    }

    private boolean takeOrder() throws InvalidActionException {
        Customer customer = pickCustomer("Take whose order?");
        if (customer == null) {
            return false;
        }
        Order order = controller.waiterTakeOrder(customer);
        MenuItem item = order.getItem();
        out.println("OK: " + customer.getId() + " orders " + item.getName() + " (" + item.getPrepUnits()
                + " prep unit" + (item.getPrepUnits() == 1 ? "" : "s") + "). Bill locked at "
                + formatter.bill(order) + ".");
        return true;
    }

    private boolean serveOrder() throws InvalidActionException {
        Customer customer = pickCustomer("Serve which customer?");
        if (customer == null) {
            return false;
        }
        controller.waiterServe(customer);
        out.println("OK: " + customer.getId() + "'s " + customer.getOrder().getItem().getName() + " is SERVED.");
        return true;
    }

    // ---- Chef --------------------------------------------------------------------------

    private void chooseChefTask() {
        while (true) {
            // READY orders are listed too, so choosing one is refused by the domain with a reason.
            List<Order> orders = controller.getRestaurant().getOpenOrders();
            out.println("\nChef - choose an order to prepare one unit (0 = Wait):");
            if (orders.isEmpty()) {
                out.println("  (no open orders)");
            }
            for (int i = 0; i < orders.size(); i++) {
                Order o = orders.get(i);
                out.println("  " + (i + 1) + ") " + o.getCustomer().getId() + " " + formatter.orderLine(o));
            }
            int choice = readChoice(0, orders.size());
            try {
                if (choice == 0) {
                    controller.chefWait();
                    out.println("OK: Chef waits this turn.");
                } else {
                    Order order = orders.get(choice - 1);
                    controller.chefPrepare(order);
                    out.println("OK: Chef prepares " + order.getCustomer().getId() + "'s " + order.getItem().getName()
                            + " -> " + order.describeStatus() + ".");
                }
                return;
            } catch (InvalidActionException e) {
                out.println("Invalid: " + e.getMessage() + " Choose again (the Chef's task is not used up).");
            }
        }
    }

    // ---- Target selection --------------------------------------------------------------

    /** Lists every active customer; the domain decides whether the chosen one is valid. Null means "back". */
    private Customer pickCustomer(String prompt) {
        List<Customer> customers = controller.getRestaurant().getActiveCustomers();
        out.println("  " + prompt + " (0 = back)");
        if (customers.isEmpty()) {
            out.println("    (no customers in the restaurant)");
        }
        for (int i = 0; i < customers.size(); i++) {
            out.println("    " + (i + 1) + ") " + formatter.customerLine(customers.get(i)));
        }
        int choice = readChoice(0, customers.size());
        return choice == 0 ? null : customers.get(choice - 1);
    }

    private Table pickTable() {
        Restaurant r = controller.getRestaurant();
        List<Table> tables = r.getTables();
        out.println("  Which table? (0 = back)");
        for (int i = 0; i < tables.size(); i++) {
            Table t = tables.get(i);
            out.println("    " + (i + 1) + ") " + t.getId() + (t.isFree() ? " (free)" : " (occupied by " + t.getOccupant().getId() + ")"));
        }
        int choice = readChoice(0, tables.size());
        return choice == 0 ? null : tables.get(choice - 1);
    }

    // ---- Input -------------------------------------------------------------------------

    /** Reads a number in [min, max], asking again on bad input. End of input counts as 0 (Wait / back). */
    private int readChoice(int min, int max) {
        while (true) {
            out.print("> ");
            if (inputEnded || !in.hasNextLine()) {
                if (!inputEnded) {
                    out.println("\n(no more input: treating remaining choices as 0)");
                    inputEnded = true;
                }
                return 0;
            }
            // Strip a byte-order mark: Windows PowerShell adds one when piping a file in.
            String line = in.nextLine().replace("﻿", "").trim();
            if (echoInput) {
                out.println(line);
            }
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the message below
            }
            out.println("Please enter a number from " + min + " to " + max + ".");
        }
    }

    private void printIntro() {
        out.println("==================== RESTAURANT RUSH (Stage 2) ====================");
        out.println("You are the manager. " + controller.getMaxTurns() + " turns, "
                + controller.getRestaurant().getTables().size() + " tables, 1 Waiter, 1 Chef, 1 Cashier.");
        out.println("Menu:");
        for (MenuItem item : controller.getRestaurant().getMenu().getItems()) {
            out.println("  " + item);
        }
        out.println("Win at the end of turn " + controller.getMaxTurns() + ": at least "
                + controller.getConfig().getTargetPaidCustomers() + " paid customers, revenue at least "
                + Money.format(controller.getConfig().getTargetRevenueCents())
                + ", and average satisfaction of paid customers at least "
                + controller.getConfig().getTargetAverageSatisfaction() + ".");
    }
}
