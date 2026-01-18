package store._0982.point.application.dto.point;

import java.util.UUID;

public record PointChargeCommand(
        long amount,
        UUID idempotencyKey
) {
}
