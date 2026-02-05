package store._0982.point.application.dto.pg;

import store._0982.point.domain.constant.PaymentMethod;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.constant.PgPaymentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PgPaymentInfo(
        UUID paymentPointId,
        UUID memberId,
        UUID orderId,
        PaymentMethod paymentMethod,
        String paymentKey,
        long amount,
        PgPaymentStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime requestedAt,
        String groupPurchaseName
) {
    public static PgPaymentInfo from(PgPayment pgPayment) {
        return new PgPaymentInfo(
                pgPayment.getId(),
                pgPayment.getMemberId(),
                pgPayment.getOrderId(),
                pgPayment.getPaymentMethod(),
                pgPayment.getPaymentKey(),
                pgPayment.getAmount(),
                pgPayment.getStatus(),
                pgPayment.getCreatedAt(),
                pgPayment.getRequestedAt(),
                pgPayment.getGroupPurchaseName()
        );
    }
}
