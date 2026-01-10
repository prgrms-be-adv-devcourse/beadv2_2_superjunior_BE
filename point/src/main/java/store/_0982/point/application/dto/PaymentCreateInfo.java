package store._0982.point.application.dto;

import store._0982.point.domain.entity.Payment;
import store._0982.point.domain.constant.PaymentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentCreateInfo(
        UUID paymentPointId,
        UUID memberId,
        UUID orderId,
        long amount,
        PaymentStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime requestedAt
) {
    public static PaymentCreateInfo from(Payment requested) {
        return new PaymentCreateInfo(
                requested.getId(),
                requested.getMemberId(),
                requested.getOrderId(),
                requested.getAmount(),
                requested.getStatus(),
                requested.getCreatedAt(),
                requested.getRequestedAt()
        );
    }
}
