package restaurantrush;

import java.util.Arrays;

import restaurantrush.boundary.ConsoleUI;
import restaurantrush.boundary.StateFormatter;
import restaurantrush.control.ArrivalSchedule;
import restaurantrush.control.CustomerFactory;
import restaurantrush.control.GameConfig;
import restaurantrush.control.GameController;
import restaurantrush.control.ObjectiveEvaluator;
import restaurantrush.entity.Menu;
import restaurantrush.entity.Restaurant;


public class Main {

    public static void main(String[] args) {
        boolean echo = Arrays.asList(args).contains("--echo");

        GameConfig config = GameConfig.base();
        Restaurant restaurant = new Restaurant(Menu.standard(), config.getTableCount());
        ArrivalSchedule schedule = new ArrivalSchedule(config.getArrivalTurns(), new CustomerFactory());
        GameController controller = new GameController(config, restaurant, schedule, new ObjectiveEvaluator(config));

        new ConsoleUI(controller, new StateFormatter(), System.in, System.out, echo).run();
    }
}
