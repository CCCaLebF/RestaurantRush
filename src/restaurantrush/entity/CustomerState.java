package restaurantrush.entity;


/** Where a customer is in their visit. */
public enum CustomerState {
    /** In the waiting queue, not yet seated. */
    WAITING,
    /** Seated at a table, order not yet taken. */
    SEATED,
    /** Order taken, food not yet served. */
    AWAITING_FOOD,
    /** Served; eating for one full turn. */
    EATING,
    READY_TO_PAY,
    PAID,
    /** Left unhappy without paying. */
    LEFT
}
