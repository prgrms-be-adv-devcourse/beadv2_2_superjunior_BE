package store._0982.point.application.dto;

import store._0982.point.domain.entity.PaymentPoint;
import store._0982.point.domain.constant.PaymentPointStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentPointHistoryInfo(
    UUID paymentPointId,
    UUID memberId,
    UUID orderId,
    String paymentMethod,
    String paymentKey,
    int amount,
    PaymentPointStatus status,
    OffsetDateTime createdAt,
    OffsetDateTime approvedAt,
    OffsetDateTime updatedAt
) {
    public static PaymentPointHistoryInfo from(PaymentPoint paymentPoint){
        return new PaymentPointHistoryInfo(
                paymentPoint.getId(),
                paymentPoint.getMemberId(),
                paymentPoint.getPgOrderId(),
                paymentPoint.getPaymentMethod(),
                paymentPoint.getPaymentKey(),
                paymentPoint.getAmount(),
                paymentPoint.getStatus(),
                paymentPoint.getCreatedAt(),
                paymentPoint.getApprovedAt(),
                paymentPoint.getUpdatedAt()
        );
    }
}
