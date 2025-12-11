package store._0982.point.application.dto;

import store._0982.point.domain.PaymentPoint;
import store._0982.point.domain.constant.PaymentPointStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentPointInfo(
        UUID paymentPointId,
        UUID memberId,
        UUID orderId,
        String paymentMethod,
        String paymentKey,
        int amount,
        PaymentPointStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime requestedAt
) {
    public static PaymentPointInfo from(PaymentPoint paymentPoint){
        return new PaymentPointInfo(
                paymentPoint.getId(),
                paymentPoint.getMemberId(),
                paymentPoint.getPgOrderId(),
                paymentPoint.getPaymentMethod(),
                paymentPoint.getPaymentKey(),
                paymentPoint.getAmount(),
                paymentPoint.getStatus(),
                paymentPoint.getCreatedAt(),
                paymentPoint.getRequestedAt()
        );
    }
}
