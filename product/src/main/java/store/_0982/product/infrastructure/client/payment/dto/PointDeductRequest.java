package store._0982.product.infrastructure.client.payment.dto;

import java.util.UUID;

public record PointDeductRequest(
       UUID idempotencyKey,
       UUID orderId,
       Long amount
) {
}
