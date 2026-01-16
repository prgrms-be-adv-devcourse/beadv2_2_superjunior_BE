package store._0982.ai.feign.commerce.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CartInfo(
        UUID cartId,
        UUID memberId,
        UUID groupPurchaseId,
        int quantity,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
