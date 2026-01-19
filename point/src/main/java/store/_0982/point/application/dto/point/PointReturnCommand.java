package store._0982.point.application.dto.point;

import java.util.UUID;

public record PointReturnCommand(
        UUID idempotencyKey,
        UUID orderId,
        String cancelReason,
        Long amount             // null일 경우 사용 기록에 나와 있는 대로 처리 (전액 환불)
) {
}
