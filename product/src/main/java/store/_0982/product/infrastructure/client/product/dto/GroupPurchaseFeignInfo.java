package store._0982.product.infrastructure.client.product.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupPurchaseFeignInfo(
        UUID groupPurchaseId,
        int minQuantity,
        int maxQuantity,
        Long discountedPrice,
        String title,
        String description,
        GroupPurchaseFeignStatus status,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        UUID sellerId,
        UUID productId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
