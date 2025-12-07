package store._0982.point.application.dto;

import store._0982.point.domain.PaymentPointFailure;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PointChargeFailInfo(
    UUID id,
    UUID orderId,
    String paymentKey,
    String errorCode,
    String errorMessage,
    int amount,
    OffsetDateTime createdAt
) {
    public static PointChargeFailInfo from(PaymentPointFailure failure) {
        return new PointChargeFailInfo(
                failure.getId(),
                failure.getPaymentPoint().getOrderId(),
                failure.getPaymentKey(),
                failure.getErrorCode(),
                failure.getErrorMessage(),
                failure.getAmount(),
                failure.getCreatedAt()
        );
    }
}
