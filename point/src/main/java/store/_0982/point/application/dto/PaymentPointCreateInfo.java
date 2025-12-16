package store._0982.point.application.dto;

import store._0982.point.domain.entity.PaymentPoint;
import store._0982.point.domain.constant.PaymentPointStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentPointCreateInfo(
        UUID paymentPointId,
        UUID memberId,
        UUID orderId,
        long amount,
        PaymentPointStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime requestedAt
) {
    public static PaymentPointCreateInfo from(PaymentPoint requested) {
        return new PaymentPointCreateInfo(
                requested.getId(),
                requested.getMemberId(),
                requested.getPgOrderId(),
                requested.getAmount(),
                requested.getStatus(),
                requested.getCreatedAt(),
                requested.getRequestedAt()
        );
    }
}
