package store._0982.batch.batch.grouppurchase.event;

import store._0982.batch.domain.order.Order;

public record OrderCancelProcessedEvent(
        Order order,
        String reason,
        Long amount
) {
}
