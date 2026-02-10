package store._0982.commerce.application.order.dto;

import store._0982.common.domain.order.Order;
import store._0982.common.domain.order.OrderStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderInfo(
        UUID orderId,
        String orderNumber,
        OrderStatus status,
        int quantity,
        Long price,
        Long totalAmount,
        UUID groupPurchaseId,
        String groupPurchaseName,
        OffsetDateTime createdAt
) {
    public static OrderInfo from(Order order, UUID groupPurchaseId, String groupPurchaseName){
        return new OrderInfo(
                order.getOrderId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getQuantity(),
                order.getPrice(),
                order.getQuantity() * order.getPrice(),
                groupPurchaseId,
                groupPurchaseName,
                order.getCreatedAt()
        );
    }
}
