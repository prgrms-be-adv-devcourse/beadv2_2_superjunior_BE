package store._0982.order.application.order.dto;

import store._0982.order.domain.order.Order;
import store._0982.order.domain.order.OrderStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderDetailInfo(
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
    public static OrderDetailInfo from(Order order){
        return new OrderDetailInfo(
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
