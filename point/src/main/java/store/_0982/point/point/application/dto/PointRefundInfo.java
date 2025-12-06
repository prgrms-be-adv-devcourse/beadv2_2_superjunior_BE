package store._0982.point.point.application.dto;

import store._0982.point.point.domain.PaymentPoint;
import store._0982.point.point.domain.PaymentPointStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PointRefundInfo(
        UUID paymentPointId,
        UUID memberId,
        UUID orderId,
        String paymentMethod,
        String paymentKey,
        int amount,
        PaymentPointStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime refundedAt
) {
    public static PointRefundInfo from(PaymentPoint paymentPoint){
        return new PointRefundInfo(
                paymentPoint.getId(),
                paymentPoint.getMemberId(),
                paymentPoint.getOrderId(),
                paymentPoint.getPaymentMethod(),
                paymentPoint.getPaymentKey(),
                paymentPoint.getAmount(),
                paymentPoint.getStatus(),
                paymentPoint.getCreatedAt(),
                paymentPoint.getRefundedAt()
        );
    }
}
