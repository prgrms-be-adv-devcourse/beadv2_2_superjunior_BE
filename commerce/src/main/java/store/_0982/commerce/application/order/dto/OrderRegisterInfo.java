package store._0982.commerce.application.order.dto;

import store._0982.commerce.domain.order.OrderStatus;
import store._0982.commerce.domain.order.Order;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderRegisterInfo(
        UUID orderId,
        int quantity,
        Long price,
        OrderStatus status,
        UUID memberId,
        String address,
        String addressDetail,
        String postalCode,
        String receiverName,
        UUID sellerId,
        UUID groupPurchaseId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime deletedAt
) {
    public static OrderRegisterInfo from(Order order){
        return new OrderRegisterInfo(
                order.getOrderId(),
                order.getQuantity(),
                order.getPrice(),
                order.getStatus(),
                order.getMemberId(),
                order.getAddress(),
                order.getAddressDetail(),
                order.getPostalCode(),
                order.getReceiverName(),
                order.getSellerId(),
                order.getGroupPurchaseId(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getDeletedAt()
        );
    }
}
