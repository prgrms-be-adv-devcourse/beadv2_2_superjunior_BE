package store._0982.commerce.application.grouppurchase.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.common.domain.product.ProductCategory;

public record GroupPurchaseSearchRow(
        UUID groupPurchaseId,
        int minQuantity,
        int maxQuantity,
        String title,
        String description,
        String imageUrl,
        Long discountedPrice,
        GroupPurchaseStatus status,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        int currentQuantity,
        UUID productId,
        ProductCategory category,
        Long price,
        String originalUrl,
        UUID sellerId
) {
}
