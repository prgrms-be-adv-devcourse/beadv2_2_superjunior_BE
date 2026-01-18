package store._0982.point.application.dto.pg;

import java.util.UUID;

public record PgCancelCommand(
        UUID orderId,
        String cancelReason,
        long amount
) {
}
