package store._0982.commerce.application.order.dto;

import store._0982.commerce.domain.order.Order;
import store._0982.commerce.domain.order.OrderStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderInfo(
        UUID orderId,
        OrderStatus status,
        int quantity,
        Long price,
        Long totalAmount,
        OffsetDateTime createdAt
) {
    public static OrderInfo from(Order order){
        return new OrderInfo(
                order.getOrderId(),
                order.getStatus(),
                order.getQuantity(),
                order.getPrice(),
                order.getQuantity() * order.getPrice(),
                order.getCreatedAt()
        );
    }
}
