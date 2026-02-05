package store._0982.commerce.application.order.event;

import store._0982.commerce.domain.order.CanceledOrder;

public record OrderCancelProcessedEvent(
        CanceledOrder canceledOrder,
        String productName
) {
}
