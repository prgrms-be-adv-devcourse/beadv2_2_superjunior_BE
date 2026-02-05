package store._0982.commerce.application.order.event;

import store._0982.commerce.domain.order.Order;

public record OrderCreateProcessedEvent(
        Order order,
        String productName
) {
}
