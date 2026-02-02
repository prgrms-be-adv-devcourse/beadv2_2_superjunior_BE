package store._0982.commerce.application.order.dto;

import store._0982.commerce.domain.order.Order;
import store._0982.commerce.domain.order.OrderStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderCancelInfo(
        UUID orderId,
        OrderStatus status,
        int quantity,
        Long price,
        Long totalAmount,
        String reason,
        OffsetDateTime createdAt
) {
    public static OrderCancelInfo toOrderCancelInfo(Order order) {
        long price = order.getPrice() == null ? 0L : order.getPrice();
        long totalAmount = price * order.getQuantity();
        return new OrderCancelInfo(
                order.getOrderId(),
                order.getStatus(),
                order.getQuantity(),
                order.getPrice(),
                totalAmount,
                null,
                order.getCreatedAt()
        );
    }
}
