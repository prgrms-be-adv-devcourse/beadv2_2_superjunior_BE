package store._0982.point.point.application.dto;

import store._0982.point.point.domain.PaymentPoint;

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
//    public static PaymentPointInfo from(PaymentPoint paymentPoint){
//
//    }
}
