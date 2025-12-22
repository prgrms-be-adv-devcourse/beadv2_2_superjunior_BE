package store._0982.commerce.infrastructure.client.product.dto;

import java.util.UUID;

public record GroupPurchaseFeignInternalInfo(
        UUID groupPurchaseId,
        UUID sellerId,
        Long totalAmount
) {
}
