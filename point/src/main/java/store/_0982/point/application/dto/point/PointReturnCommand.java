package store._0982.point.application.dto.point;

import java.util.UUID;

public record PointReturnCommand(
        UUID idempotencyKey,
        UUID orderId,
        String cancelReason,
        long amount
) {
}
