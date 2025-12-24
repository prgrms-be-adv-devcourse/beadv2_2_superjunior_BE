package store._0982.batch.infrastructure.client.payment.dto;

import java.util.UUID;

public record PointReturnRequest(
        UUID idempotencyKey,
        UUID orderId,
        Long amount
) {
}
