package store._0982.point.point.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentPointResponse(
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
}
