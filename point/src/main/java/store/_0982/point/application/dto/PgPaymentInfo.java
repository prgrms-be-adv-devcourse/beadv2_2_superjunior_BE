package store._0982.point.application.dto;

import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.constant.PgPaymentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PgPaymentInfo(
        UUID paymentPointId,
        UUID memberId,
        UUID orderId,
        String paymentMethod,
        String paymentKey,
        String failMessage,
        long amount,
        PgPaymentStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime requestedAt
) {
    public static PgPaymentInfo from(PgPayment pgPayment) {
        return new PgPaymentInfo(
                pgPayment.getId(),
                pgPayment.getMemberId(),
                pgPayment.getOrderId(),
                pgPayment.getPaymentMethod(),
                pgPayment.getPaymentKey(),
                pgPayment.getFailMessage(),
                pgPayment.getAmount(),
                pgPayment.getStatus(),
                pgPayment.getCreatedAt(),
                pgPayment.getRequestedAt()
        );
    }
}
