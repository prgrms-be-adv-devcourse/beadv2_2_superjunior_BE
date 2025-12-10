package store._0982.order.infrastructure.client.dto;

import java.util.UUID;

public record GroupPurchaseInfo(
        UUID groupPurchaseId,
        UUID sellerId,
        int discountedPrice,
        int currentQuantity
) {
}
