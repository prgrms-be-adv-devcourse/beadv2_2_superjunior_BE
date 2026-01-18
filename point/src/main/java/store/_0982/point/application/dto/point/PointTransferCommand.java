package store._0982.point.application.dto.point;

import java.util.UUID;

public record PointTransferCommand(
        long amount,
        UUID idempotencyKey
) {
}
