package store._0982.point.application.dto;

import store._0982.point.domain.entity.Payment;
import store._0982.point.domain.constant.PaymentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentInfo(
        UUID paymentPointId,
        UUID memberId,
        UUID orderId,
        String paymentMethod,
        String paymentKey,
        String failMessage,
        long amount,
        PaymentStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime requestedAt
) {
    public static PaymentInfo from(Payment payment) {
        return new PaymentInfo(
                payment.getId(),
                payment.getMemberId(),
                payment.getOrderId(),
                payment.getPaymentMethod(),
                payment.getPaymentKey(),
                payment.getFailMessage(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getRequestedAt()
        );
    }
}
