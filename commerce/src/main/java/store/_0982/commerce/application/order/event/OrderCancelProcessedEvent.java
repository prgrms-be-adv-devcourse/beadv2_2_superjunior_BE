package store._0982.commerce.application.order.event;

import store._0982.commerce.domain.order.Order;

public record OrderCancelProcessedEvent(
    Order order,
    String reason,
    Long amount
) {
}
