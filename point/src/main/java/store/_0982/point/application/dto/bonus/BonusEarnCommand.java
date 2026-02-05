package store._0982.point.application.dto.bonus;

import java.util.UUID;

public record BonusEarnCommand(
        UUID idempotencyKey,
        UUID orderId,
        UUID groupPurchaseId,
        String productCategory
) {
    public BonusEarnCommand(UUID idempotencyKey, UUID orderId) {
        this(idempotencyKey, orderId, null, null);
    }
}
