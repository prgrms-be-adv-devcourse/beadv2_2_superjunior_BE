package store._0982.commerce.application.product.dto;

import store._0982.commerce.domain.order.OrderStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderVectorInfo(
        UUID orderId,
        UUID memberId,
        UUID productId,
        int quantity,
        OffsetDateTime createdAt,
        OrderStatus orderStatus,
        float[] productVector
) {
}
