package store._0982.point.point.application.dto;

import store._0982.point.point.domain.PaymentPoint;
import store._0982.point.point.domain.PaymentPointStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentPointCreateInfo(
        UUID paymentPointId,
        UUID memberId,
        UUID orderId,
        int amount,
        PaymentPointStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime requestedAt
) {
    public static PaymentPointCreateInfo from(PaymentPoint requested) {
        return new PaymentPointCreateInfo(
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
