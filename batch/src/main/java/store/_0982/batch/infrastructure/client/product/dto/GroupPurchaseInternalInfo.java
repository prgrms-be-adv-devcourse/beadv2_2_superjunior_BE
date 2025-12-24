package store._0982.batch.infrastructure.client.product.dto;

import java.util.UUID;

public record GroupPurchaseInternalInfo(
        UUID groupPurchaseId,
        UUID sellerId,
        Long totalAmount
) {
}
