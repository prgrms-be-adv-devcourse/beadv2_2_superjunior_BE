package store._0982.point.application.dto.pg;

import java.util.UUID;

public record PgCancelCommand(
        UUID orderId,
        String cancelReason,
        Long amount         // null일 경우 전액 환불
) {
}
