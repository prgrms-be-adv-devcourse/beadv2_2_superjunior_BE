package store._0982.order.infrastructure.client.product.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupPurchaseInfo(
        UUID groupPurchaseId,
        int minQuantity,
        int maxQuantity,
        Long discountedPrice,
        String title,
        String description,
        GroupPurchaseStatus status,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        UUID sellerId,
        UUID productId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
