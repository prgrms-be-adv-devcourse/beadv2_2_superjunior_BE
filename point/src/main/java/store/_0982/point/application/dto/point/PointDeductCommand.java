package store._0982.point.application.dto.point;

import java.util.UUID;

public record PointDeductCommand(
        UUID idempotencyKey,
        UUID orderId,
        long amount
) {
}
