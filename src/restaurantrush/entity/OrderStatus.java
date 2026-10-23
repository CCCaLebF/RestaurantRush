package restaurantrush.entity;

/** Lifecycle of an order from being taken to being paid (or cancelled). */
public enum OrderStatus {
    PLACED,
    IN_PREPARATION,
    READY,
    SERVED,
    PAID,
    CANCELLED
}
