package restaurantrush.control;

/** One scheduled arrival: a customer of the given type joins the queue at the start of the given turn. */
public record Arrival(int turn, CustomerType type) {
}
