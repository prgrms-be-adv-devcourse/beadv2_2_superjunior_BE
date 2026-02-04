package store._0982.commerce.application.order.dto;

import store._0982.commerce.domain.order.CancelReason;

import java.util.UUID;

public record OrderCancelCommand(
        UUID memberId,
        UUID orderId,
        CancelReason reason,
        String detailReason,
        String idempotencyKey
) {
}
