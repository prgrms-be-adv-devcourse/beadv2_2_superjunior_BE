package store._0982.product.infrastructure.client.product.dto;

import java.util.UUID;

public record GroupPurchaseFeignInternalInfo(
        UUID groupPurchaseId,
        UUID sellerId,
        Long totalAmount
) {
}
