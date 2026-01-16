package store._0982.point.application.dto.pg;

import java.util.UUID;

public record PgCreateCommand(
        UUID orderId,
        long amount
) {
}
