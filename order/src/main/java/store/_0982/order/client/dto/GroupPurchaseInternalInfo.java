package store._0982.order.client.dto;

import java.util.UUID;

public record GroupPurchaseInternalInfo(
        UUID groupPurchaseId,
        UUID sellerId,
        Long totalAmount
) {
}
