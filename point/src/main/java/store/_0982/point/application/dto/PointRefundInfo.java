package store._0982.point.application.dto;

import store._0982.point.domain.entity.PaymentPoint;
import store._0982.point.domain.constant.PaymentPointStatus;

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
                paymentPoint.getPgOrderId(),
                paymentPoint.getPaymentMethod(),
                paymentPoint.getPaymentKey(),
                paymentPoint.getAmount(),
                paymentPoint.getStatus(),
                paymentPoint.getCreatedAt(),
                paymentPoint.getRefundedAt()
        );
    }
}
