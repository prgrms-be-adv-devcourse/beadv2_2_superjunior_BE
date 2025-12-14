package store._0982.order.client.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record PointDeductRequest(
       UUID idempotencyKey,
       UUID orderId,
       Long amount
) {
}
