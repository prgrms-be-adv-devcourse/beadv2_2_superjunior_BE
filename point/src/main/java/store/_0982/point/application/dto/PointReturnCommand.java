package store._0982.point.application.dto;

import java.util.UUID;

public record PointReturnCommand(
        UUID idempotencyKey,
        int amount
) {
}
