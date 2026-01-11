package store._0982.point.application.dto;

import store._0982.point.domain.entity.Payment;
import store._0982.point.domain.constant.PaymentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PointRefundInfo(
        UUID paymentPointId,
        UUID memberId,
        UUID orderId,
        String paymentMethod,
        String paymentKey,
        long amount,
        PaymentStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime refundedAt
) {
    public static PointRefundInfo from(Payment payment){
        return new PointRefundInfo(
                payment.getId(),
                payment.getMemberId(),
                payment.getOrderId(),
                payment.getPaymentMethod(),
                payment.getPaymentKey(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getRefundedAt()
        );
    }
}
