package store._0982.commerce.infrastructure.client.payment.dto;

import java.util.UUID;

public record PointReturnRequest(
        UUID idempotencyKey,
        UUID orderId,
        Long amount
) {
}
