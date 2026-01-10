package store._0982.point.application.dto;

import java.util.UUID;

public record PointChargeCommand(
        long amount,
        UUID idempotencyKey
) {
}
